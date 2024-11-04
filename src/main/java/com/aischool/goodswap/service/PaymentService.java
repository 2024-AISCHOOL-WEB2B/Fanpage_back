package com.aischool.goodswap.service;

import com.aischool.goodswap.DTO.AddressInfoResponseDTO;
import com.aischool.goodswap.DTO.PaymentInfoRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.aischool.goodswap.repository.payment.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private AsyncPaymentService asyncPaymentService;

    @Transactional(readOnly = true)
    public CompletableFuture<PaymentInfoResponseDTO> getPaymentInfo(String user, Long goodsId) {
        // 비동기 메서드 호출
        CompletableFuture<Integer> pointsFuture = asyncPaymentService.getTotalPoints(user);
        CompletableFuture<String> deliveryAddrFuture = asyncPaymentService.getDeliveryAddress(user);
        CompletableFuture<Map<String, String>> cardInfoFuture = asyncPaymentService.getCardInfo(user);
        CompletableFuture<Goods> goodsFuture = asyncPaymentService.getGoodsInfo(goodsId);

        // 모든 CompletableFuture 작업이 완료될 때까지 기다린 후 결과를 처리
        return CompletableFuture.allOf(pointsFuture, deliveryAddrFuture, cardInfoFuture, goodsFuture)
          .thenApplyAsync(voided -> {
              Integer points = pointsFuture.join();
              String deliveryAddr = deliveryAddrFuture.join();
              Map<String, String> cardInfo = cardInfoFuture.join();
              Goods goods = goodsFuture.join();

              return PaymentInfoResponseDTO.builder()
                .user(user)
                .point(points)
                .deliveryAddr(deliveryAddr)
                .cardNumber(cardInfo.get("cardNumber"))
                .cardCvc(cardInfo.get("cardCvc"))
                .expiredAt(cardInfo.get("expiredAt"))
                .goodName(goods.getGoodsName())
                .goodsPrice(goods.getGoodsPrice())
                .shippingFee(goods.getShippingFee())
                .build();
          });
    }

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
        return addressInfo;
    }

    @Transactional
    public List<AddressInfoResponseDTO> removeDeliveryAddress(String user, Long addrId) {
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, user)
          .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));

        deliveryAddressRepository.delete(address);

        return getAddressInfo(user);
    }

    @Transactional
    public List<AddressInfoResponseDTO> updateDeliveryAddress(Long addrId, PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser();
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

    @Transactional
    public List<AddressInfoResponseDTO> addDeliveryAddress(PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser(); // userEmail 추출
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
          .user(new User(userEmail)) // User 객체 생성
          .userName(newUserName)    // 배송지 주소 설정
          .userPhone(newUserPhone)    // 배송지 주소 설정
          .build();

        // 배송지 저장
        deliveryAddressRepository.save(newDeliveryAddress);

        // 모든 주소 정보를 반환
        return getAddressInfo(userEmail);
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> getCardInfo(String userEmail) {
        List<CreditCard> cards = cardRepository.findAllByUser_UserEmail(userEmail);
        List<Map<String, String>> cardInfoList = new ArrayList<>();
        for (CreditCard card : cards) {
            Map<String, String> cardInfo = Map.of(
              "cardId", card.getId().toString(),
              "cardNumber", card.getCardNumber(),
              "cardCvc", card.getCardCvc(),
              "expiredAt", card.getExpiredAt(),
              "userEmail", card.getUser().getUserEmail()
            );
            cardInfoList.add(cardInfo);
        }
        return cardInfoList;
    }

    @Transactional
    public List<Map<String, String>> addCreditCard(CreditCard creditCard) {
        String userEmail = creditCard.getUser().getUserEmail(); // User 객체에서 이메일 가져오기
        String cardNumber = creditCard.getCardNumber();
        String cardCvc = creditCard.getCardCvc();
        String expiredAt = creditCard.getExpiredAt();

        // 카드 번호가 이미 존재하는지 확인
        if (cardRepository.existsByCardNumberAndUser_UserEmail(cardNumber, userEmail)) {
            throw new IllegalArgumentException("해당 카드 번호는 이미 등록되어 있습니다.");
        }

        // 빌더 패턴을 사용하여 새로운 CreditCard 객체 생성
        CreditCard newCard = CreditCard.builder()
          .cardNumber(cardNumber)
          .cardCvc(cardCvc)
          .expiredAt(expiredAt)
          .user(new User(userEmail)) // User 객체 생성
          .build();

        // 카드 저장
        cardRepository.save(newCard);

        // 모든 카드 정보를 반환
        return getCardInfo(userEmail);
    }

    @Transactional
    public List<Map<String, String>> removeCreditCard(String userEmail, Long cardId) {
        CreditCard card = cardRepository.findByIdAndUser_UserEmail(cardId, userEmail)
          .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

        cardRepository.delete(card);

        // 모든 카드 정보를 반환
        return getCardInfo(userEmail);
    }

    @Transactional
    public String saveOrderInfo(OrderTemporal orderTemporal) {
        // 상품 조회
        Goods goods = goodsRepository.findById(orderTemporal.getGoods())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 주문 금액 검증 및 재고 업데이트
        int expectedAmount = goods.getGoodsPrice() * orderTemporal.getQuantity();
        if (orderTemporal.getAmount() != expectedAmount) {
            throw new IllegalArgumentException("주문 금액이 상품 가격과 일치하지 않습니다.");
        }

        if (goods.getGoodsStock() < orderTemporal.getQuantity()) {
            throw new IllegalArgumentException("재고가 부족하여 주문을 처리할 수 없습니다.");
        }

        // 재고 감소 처리
        goods.decreaseQuantity(orderTemporal.getQuantity());

        // merchant_uid 생성
        String merchantUid = generateMerchantUid();

        // OrderTemporal 객체 생성 및 저장
        OrderTemporal newOrder = OrderTemporal.builder()
                .merchantUid(merchantUid)
                .amount(orderTemporal.getAmount())
                .discountAmount(orderTemporal.getDiscountAmount())
                .goods(orderTemporal.getGoods())
                .quantity(orderTemporal.getQuantity())
                .build();

        orderRepository.save(newOrder); // 주문 정보만 저장

        // 생성한 merchant_uid 반환
        return merchantUid;
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
}