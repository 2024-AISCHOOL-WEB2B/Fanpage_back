package com.aischool.goodswap.service;

import com.aischool.goodswap.DTO.AddressInfoResponseDTO;
import com.aischool.goodswap.DTO.OrderRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.*;
import com.aischool.goodswap.exception.EncryptionException;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.repository.DeliveryAddressRepository;
import com.aischool.goodswap.repository.GoodsRepository;
import com.aischool.goodswap.repository.OrderRepository;
import com.aischool.goodswap.repository.PointRepository;
import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.security.AESUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

    @Slf4j
    @Service
    public class PaymentService {

    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AsyncPaymentService asyncPaymentService;
    @Autowired
    private AESUtil aesUtil;

    // 결제 사전정보 전송
    @Transactional(readOnly = true)
    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000))
    public CompletableFuture<PaymentInfoResponseDTO> getPaymentInfo(String user, Long goodsId) {
        // 비동기 메서드 호출
        // 각각의 정보를 비동기 방식으로 받아옴을 명시
        CompletableFuture<Integer> pointsFuture = asyncPaymentService.getTotalPoints(user);
        CompletableFuture<String> deliveryAddrFuture = asyncPaymentService.getDeliveryAddress(user);
        CompletableFuture<Map<String, String>> cardInfoFuture = asyncPaymentService.getCardInfo(user);
        CompletableFuture<Goods> goodsFuture = asyncPaymentService.getGoodsInfo(goodsId);

        // 타임아웃 설정 (1초 이내에 완료되지 않으면 예외 발생)
        long timeoutInMillis = 1000;

        // 모든 CompletableFuture 작업이 완료될 때까지 기다린 후 결과를 처리
        return CompletableFuture.allOf(pointsFuture, deliveryAddrFuture, cardInfoFuture, goodsFuture)
          .handleAsync((result, ex) -> {
              // 예외 처리
              if (ex != null) {
                  // 예외가 발생하면 failedFuture로 전달
                  return CompletableFuture.<PaymentInfoResponseDTO>failedFuture(ex);  // 예외를 전달
              }

              try {
                  // 1초 이내에 모든 결과를 받아옴 (타임아웃 처리)
                  Integer points = pointsFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  String deliveryAddr = deliveryAddrFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  Map<String, String> cardInfo = cardInfoFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  Goods goods = goodsFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);

                  // 정상적으로 처리된 경우
                  return CompletableFuture.completedFuture(PaymentInfoResponseDTO.builder()
                    .user(user)
                    .point(points)
                    .deliveryAddr(deliveryAddr)
                    .cardNumber(cardInfo.get("cardNumber"))
                    .cardCvc(cardInfo.get("cardCvc"))
                    .expiredAt(cardInfo.get("expiredAt"))
                    .goodName(goods.getGoodsName())
                    .goodsPrice(goods.getGoodsPrice())
                    .shippingFee(goods.getShippingFee())
                    .build());

              } catch (Exception timeoutException) {
                  // 타임아웃 예외 처리
                  return CompletableFuture.<PaymentInfoResponseDTO>failedFuture(timeoutException);
              }
          }).thenCompose(Function.identity());  // CompletableFuture를 이어서 처리
    }

    // 회원 이메일을 기준으로 등록된 주소를 가져옴
    @Transactional(readOnly = true)
    public List<AddressInfoResponseDTO> getAddressInfo(String user) {
        List<DeliveryAddress> deliveryAddresses = deliveryAddressRepository.findAllByUser_UserEmail(user);
        List<AddressInfoResponseDTO> addressInfo = new ArrayList<>();

        for (DeliveryAddress address : deliveryAddresses) {
            // 빌더 패턴을 사용하여 AddressInfoResponseDTO 객체를 생성
            AddressInfoResponseDTO dto = AddressInfoResponseDTO.builder()
              .id(address.getId())
              .address(address.getDeliveryAddr())
              .deliveryDetailAddr(address.getDeliveryDetailAddr())
              .postCode(address.getPostCode())
              .userName(address.getUserName())
              .userPhone(address.getUserPhone())
              .build();
            addressInfo.add(dto);
        }

        log.info("addressInfo Info: {}", addressInfo);

        return addressInfo;
    }

    // 회원 이메일을 기준으로 등록된 주소를 제거
    @Transactional
    public List<AddressInfoResponseDTO> removeDeliveryAddress(String user, Long addrId) {
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, user)
          .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));  // 예외조건 설정
        deliveryAddressRepository.delete(address);
        return getAddressInfo(user);
    }

    // 회원 이메일을 기준으로 등록된 주소 수정
    @Transactional
    public List<AddressInfoResponseDTO> updateDeliveryAddress(Long addrId, PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser().getUserEmail();
        String newAddress = paymentInfoRequest.getDeliveryAddr();
        String newDetailAddress = paymentInfoRequest.getDeliveryDetailAddr();
        String newPostCode = paymentInfoRequest.getPostCode();
        String newUserName = paymentInfoRequest.getUserName();
        String newUserPhone = paymentInfoRequest.getUserPhone();

        // 기존 주소 엔티티 가져오기
        DeliveryAddress existingAddress = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, userEmail)
          .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));

        // 업데이트 메서드를 사용해 기존 객체의 주소 수정
        existingAddress.updateDeliveryAddr(newAddress);
        existingAddress.updateDeliveryDetailAddr(newDetailAddress);
        existingAddress.updatePostCode(newPostCode);
        existingAddress.updateUserName(newUserName);
        existingAddress.updateUserPhone(newUserPhone);

        // 수정된 객체 저장
        deliveryAddressRepository.save(existingAddress);
        return getAddressInfo(userEmail);
    }

    // 회원 이메일을 기준으로 주소 추가 등록
    @Transactional
    public List<AddressInfoResponseDTO> addDeliveryAddress(PaymentInfoRequestDTO paymentInfoRequest) {
        User userEmail = paymentInfoRequest.getUser(); // userEmail 추출
        String newAddress = paymentInfoRequest.getDeliveryAddr(); // 배송지 주소 추출
        String newDetailAddress = paymentInfoRequest.getDeliveryDetailAddr();
        String newPostCode = paymentInfoRequest.getPostCode();
        String newUserName = paymentInfoRequest.getUserName();
        String newUserPhone = paymentInfoRequest.getUserPhone();

        // 빌더 패턴을 사용하여 새로운 DeliveryAddress 객체 생성
        DeliveryAddress newDeliveryAddress = DeliveryAddress.builder()
          .deliveryAddr(newAddress)    // 배송지 주소 설정
          .deliveryDetailAddr(newDetailAddress)    // 배송지 주소 설정
          .postCode(newPostCode)    // 배송지 주소 설정
          .user(userEmail) // User 객체 생성
          .userName(newUserName)    // 배송지 주소 설정
          .userPhone(newUserPhone)    // 배송지 주소 설정
          .build();

        // 배송지 저장
        deliveryAddressRepository.save(newDeliveryAddress);
        return getAddressInfo(userEmail.getUserEmail());
    }

    // 회원 이메일을 기준으로 모든 카드정보 가져옴
    @Transactional(readOnly = true)
    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000))
    public List<Map<String, String>> getCardInfo(String userEmail) {
        List<CreditCard> cards = cardRepository.findAllByUser_UserEmail(userEmail);
        List<Map<String, String>> cardInfoList = new ArrayList<>();

        for (CreditCard card : cards) {
            Map<String, String> cardInfo = new HashMap<>();
            try {
                cardInfo.put("cardId", card.getId().toString());
                cardInfo.put("cardNumber", aesUtil.decrypt(card.getCardNumber()));
                cardInfo.put("cardCvc", aesUtil.decrypt(card.getCardCvc()));
                cardInfo.put("expiredAt", aesUtil.decrypt(card.getExpiredAt()));
                cardInfo.put("userEmail", card.getUser().getUserEmail());
            } catch (EncryptionException e) {
                throw new RuntimeException("카드 정보를 복호화하는 중 오류 발생", e);
            }
            cardInfoList.add(cardInfo);
        }
        return cardInfoList;
    }

    // 회원 이메일을 기준으로 카드정보 등록
    @Transactional
    public List<Map<String, String>> addCreditCard(CreditCard creditCard) {
        User userEmail = creditCard.getUser(); // User 객체에서 이메일 가져오기
        String cardNumber = creditCard.getCardNumber();
        String cardCvc = creditCard.getCardCvc();
        String expiredAt = creditCard.getExpiredAt();

        // 카드 번호가 이미 존재하는지 확인
        if (cardRepository.existsByCardNumberAndUser_UserEmail(cardNumber, userEmail.getUserEmail())) {
            throw new IllegalArgumentException("해당 카드 번호는 이미 등록되어 있습니다.");
        }

        // 카드 정보를 암호화
        CreditCard newCard = CreditCard.builder()
          .cardNumber(aesUtil.encrypt(cardNumber))
          .cardCvc(aesUtil.encrypt(cardCvc))
          .expiredAt(aesUtil.encrypt(expiredAt))
          .user(userEmail)
          .build();

        // 카드 저장
        cardRepository.save(newCard);
        return getCardInfo(userEmail.getUserEmail());
    }

    @Transactional
    public List<Map<String, String>> removeCreditCard(String userEmail, Long cardId) {
        CreditCard card = cardRepository.findByIdAndUser_UserEmail(cardId, userEmail)
          .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

        cardRepository.delete(card);
        return getCardInfo(userEmail);
    }

    @Transactional
    public String saveOrderInfo(OrderRequestDTO orderRequestDTO) {

        User user = findUser(orderRequestDTO.getUser());
        Goods goods = findGoods(orderRequestDTO.getGoods());

        // 주문 금액 검증 및 재고 업데이트
        validateOrderAmount(orderRequestDTO, goods);

        try {
            // 재고 감소 처리
            if (!decreaseGoodsStockWithLock(goods, orderRequestDTO.getQuantity())) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            Point newPoint = Point.builder()
              .user(user)
              .pointType("use")
              .reason("Purchase goods")
              .point(-orderRequestDTO.getDiscountAmount()) // User 객체 생성
              .build();

            // 포인트 감소 처리
            pointRepository.save(newPoint);

            // merchant_uid 생성
            String merchantUid = generateMerchantUid();
            orderRequestDTO.updateMerchantUid(merchantUid);

            // 주문 정보 저장
            Order order = upsertOrderHistory(orderRequestDTO, user, goods);
            orderRepository.save(order); // 주문 정보 저장

            log.info("결제 사전정보 출력");
            log.info("User Info: {}", user);
            log.info("Goods Info: {}", goods);
            log.info("Order Request DTO: {}", orderRequestDTO);
            log.info("New Point Info: {}", newPoint);
            log.info("Merchant UID: {}", merchantUid);
            log.info("Order Info: {}", order);
            log.info("결제 사전정보 입력완료");

            // 생성한 merchant_uid 반환
            return merchantUid;

        } catch (Exception e) {
            log.error("주문 처리 중 오류 발생: " + e.getMessage());
            throw e;  // 예외를 다시 던져서 트랜잭션 롤백 처리
        }
    }

    private boolean decreaseGoodsStockWithLock(Goods goods, int quantity) {
        try {
            // Pessimistic Lock을 사용하여 동시성 문제를 방지
            Goods lockedGoods = goodsRepository.lockGoodsForUpdate(goods.getId());

            // 재고 감소
            if (lockedGoods.getGoodsStock() < quantity) {
                return false;
            }

            lockedGoods.decreaseQuantity(quantity); // lock된 상품에서 재고 차감
            goodsRepository.save(lockedGoods); // 변경된 재고 저장

            return true;

        } catch (Exception e) {
            log.error("재고 차감 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("재고 차감 실패", e);
        }
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        // 주문 정보 조회
        Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if ("cancel".equals(order.getOrderStatus())) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        Goods goods = goodsRepository.findById(order.getGoods().getId())
          .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 재고 복구
        try {
            goodsRepository.restoreGoodsQuantity(goods.getId(), order.getQuantity());
        } catch (Exception e) {
            log.error("재고 복구 실패: " + e.getMessage());
            throw new IllegalStateException("재고 복구에 실패하여 주문 취소를 처리할 수 없습니다.", e);
        }

        try {
            restorePoints(order, reason);
        } catch (Exception e) {
            // 포인트 복구 실패 시 로그 기록 및 알림 처리
            log.error("포인트 복구 실패: " + e.getMessage());
            // 필요 시 관리자에게 알림을 보내거나 특정 재시도 로직을 추가할 수도 있음
        }

        // 주문 삭제
        order.updateStatus("cancel");
        orderRepository.save(order);
    }

    private void restorePoints(Order order, String reason) {
        Point newPoint = Point.builder()
          .user(order.getUser())
          .pointType("get")
          .reason(reason)
          .point(order.getDiscountAmount())
          .build();

        // 포인트 감소 처리
        pointRepository.save(newPoint);
    }


    private String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDay = today.format(formatter);

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay + '-' + uniqueString;
    }

    private void validateOrderAmount(OrderRequestDTO orderRequestDTO, Goods goods) {
        int expectedAmount = goods.getGoodsPrice() * orderRequestDTO.getQuantity();
        if (orderRequestDTO.getTotalAmount() != expectedAmount) {
            throw new IllegalArgumentException("주문 금액이 상품 가격과 일치하지 않습니다.");
        }

        if (goods.getGoodsStock() < orderRequestDTO.getQuantity()) {
            throw new IllegalArgumentException("재고가 부족하여 주문을 처리할 수 없습니다.");
        }
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Goods findGoods(Long goodsId) {
        return goodsRepository.findById(goodsId)
          .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    @Transactional
    public void processPaymentDone(OrderRequestDTO orderRequestDTO) {

        //orders 테이블에서 해당 부분 결제true 처리
        Order currentOrder = orderRepository.findById(orderRequestDTO.getOrderIdx())
          .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다."));

        User user = findUser(orderRequestDTO.getUser());
        Goods goods = findGoods(orderRequestDTO.getGoods());

        log.info("결제 등록");
        log.info("User Info: {}", user);
        log.info("Goods Info: {}", goods);
        log.info("currentOrder Info: {}", currentOrder);
        log.info("Order Request DTO: {}", orderRequestDTO);
        log.info("결제 등록 완료");

        // 결제 처리 후 주문 내역 저장
        upsertOrderHistory(orderRequestDTO, user, goods);
    }

    private Order upsertOrderHistory(OrderRequestDTO orderRequestDTO, User user, Goods goods) {
        return Order.builder()
          .merchantUid(orderRequestDTO.getMerchantUid())
          .user(user)
          .goods(goods)
          .quantity(orderRequestDTO.getQuantity())
          .totalAmount(orderRequestDTO.getTotalAmount())
          .discountAmount(orderRequestDTO.getDiscountAmount())
          .payMethod(orderRequestDTO.getPayMethod())
          .deliveryAddr(orderRequestDTO.getDeliveryAddr())
          .deliveryDetailAddr(orderRequestDTO.getDeliveryDetailAddr())
          .postCode(orderRequestDTO.getPostCode())
          .receiverName(orderRequestDTO.getReceiverName())
          .receiverPhone(orderRequestDTO.getReceiverPhone())
          .request(orderRequestDTO.getRequest())
          .orderStatus(orderRequestDTO.getOrderStatus())
          .build();
    }
}