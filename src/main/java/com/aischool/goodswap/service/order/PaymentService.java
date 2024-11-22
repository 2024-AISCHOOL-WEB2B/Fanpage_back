package com.aischool.goodswap.service.order;

import com.aischool.goodswap.DTO.order.AddressDTO;
import com.aischool.goodswap.DTO.order.CardInfoDTO;
import com.aischool.goodswap.DTO.order.GoodsDTO;
import com.aischool.goodswap.DTO.order.OrderRequestDTO;
import com.aischool.goodswap.DTO.order.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.domain.Order;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.exception.order.OrderException;
import com.aischool.goodswap.exception.order.TransactionException;
import com.aischool.goodswap.exception.order.PaymentException;

import com.aischool.goodswap.repository.OrderRepository;
import com.aischool.goodswap.service.auth.UserService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AsyncPaymentService asyncPaymentService;
    @Autowired
    private UserService userService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PointService pointService;

    // 결제 사전정보 전송
    @Transactional(readOnly = true)
    @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000))
    public CompletableFuture<PaymentInfoResponseDTO> getPaymentInfo(String user, Long goodsId) {
        // 비동기 메서드 호출
        CompletableFuture<Integer> pointsFuture = asyncPaymentService.getTotalPoints(user);
        CompletableFuture<AddressDTO> deliveryAddrFuture = asyncPaymentService.getDeliveryAddress(user);
        CompletableFuture<CardInfoDTO> cardInfoFuture = asyncPaymentService.getCardInfo(user);
        CompletableFuture<GoodsDTO> goodsFuture = asyncPaymentService.getGoodsInfo(goodsId);

        long timeoutInMillis = 1000; // 타임아웃 설정 (1초 이내에 완료되지 않으면 예외 발생)

        // 모든 비동기 작업이 완료될 때까지 기다리고 결과 처리
        return CompletableFuture.allOf(pointsFuture, deliveryAddrFuture, cardInfoFuture, goodsFuture)
          .handleAsync((result, ex) -> {
              if (ex != null) {
                  // 예외 발생 시 예외를 CompletableFuture로 반환
                  return CompletableFuture.<PaymentInfoResponseDTO>failedFuture(ex);  // 예외를 전달
              }

              try {
                  // 1초 이내에 모든 결과를 받아옴 (타임아웃 처리)
                  Integer points = pointsFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  AddressDTO addressDTO = deliveryAddrFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  CardInfoDTO cardInfoDTO = cardInfoFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                  GoodsDTO goodsDTO = goodsFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);

                  // 정상적으로 처리된 경우
                  return CompletableFuture.completedFuture(PaymentInfoResponseDTO.builder()
                    .user(user)
                    .point(points)
                    .address(addressDTO)
                    .cardInfo(cardInfoDTO)
                    .goods(goodsDTO)
                    .build());

              } catch (Exception timeoutException) {
                  // 타임아웃 예외 처리
                  return CompletableFuture.<PaymentInfoResponseDTO>failedFuture(timeoutException);
              }
          }).thenCompose(Function.identity());
    }

    // 결제 정보 검증 메서드.
    @Transactional
    public void validateOrderPayment(IamportResponse<Payment> payment, String userEmail) {
        try {
            String merchantUid = payment.getResponse().getMerchantUid();
            Order order = findOrder(merchantUid, userEmail);

            // DB에서 가져온 Order와 결제 정보(IamportResponse<Payment>) 비교
            isOrderValid(order, payment);

            changeOrderStatus(order, "결제 완료");
        } catch (DataAccessException e) {
            PaymentException.logErrorAndThrow(TransactionException.TRANSACTION_FAILURE, e);
            throw new TransactionException(TransactionException.UNEXPECTED_ERROR);
        } catch (RuntimeException e) {
            // 예외 처리 시, 메시지를 문자열로 변환하여 전달
            PaymentException.logErrorAndThrow(OrderException.UNEXPECTED_ERROR, e);
            throw new RuntimeException(TransactionException.UNEXPECTED_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(String userEmail) {
        try{
            return orderRepository.findByUser_UserEmail(userEmail);
        } catch (RuntimeException e) {
            PaymentException.logErrorAndThrow(OrderException.ORDER_PROCESS_ERROR, e);
            throw new TransactionException(OrderException.ORDER_PROCESS_ERROR);  // 트랜잭션 예외 처리
        }
    }

    @Transactional
    public String registerOrder(OrderRequestDTO orderRequestDTO) {
        try {
            User user = userService.findByEmail(orderRequestDTO.getUser())
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Goods goods = goodsService.findGoods(orderRequestDTO.getGoods());

            int expectedAmount = goods.getGoodsPrice() * orderRequestDTO.getQuantity();
            if (orderRequestDTO.getTotalAmount() != expectedAmount) {
                log.error("결제 금액 : " + orderRequestDTO.getTotalAmount() + ", 예상 금액 : " + expectedAmount);
                throw new OrderException(OrderException.ORDER_AMOUNT_MISMATCH);
            }
            goodsService.decreaseGoodsStockWithLock(goods, orderRequestDTO.getQuantity());
            pointService.updatePoints(user, "use", "결제 완료", orderRequestDTO.getDiscountAmount());

            String merchantUid = generateMerchantUid();
            Order order = createOrder(orderRequestDTO, user, goods, merchantUid);
            orderRepository.save(order);
            return merchantUid;
        } catch (RuntimeException e) {
            PaymentException.logErrorAndThrow(OrderException.ORDER_PROCESS_ERROR, e);
            throw new TransactionException(OrderException.ORDER_PROCESS_ERROR);  // 트랜잭션 예외 처리
        }
    }



    @Transactional
    public void cancelOrder(String merchantUid, String userEmail) {
        try {
            Order order = findOrder(merchantUid, userEmail);
            if ("주문 취소".equals(order.getOrderStatus())) {
                throw new IllegalStateException(OrderException.ORDER_ALREADY_CANCELLED);
            }

            Goods goods = goodsService.findGoods(order.getGoods().getId());
            goodsService.restoreGoodsStockWithLock(goods, order.getQuantity());
            pointService.updatePoints(order.getUser(), "restore", "결제 취소", order.getDiscountAmount());
            changeOrderStatus(order, "주문 취소");
        } catch (DataAccessException e) {
            // PaymentException 처리
            PaymentException.logErrorAndThrow(OrderException.CANCEL_PROCESS_ERROR, e);
            throw new TransactionException(OrderException.CANCEL_PROCESS_ERROR);
        } catch (RuntimeException e) {
            // 예상치 못한 오류 처리
            PaymentException.logErrorAndThrow(OrderException.CANCEL_PROCESS_ERROR, e);
            throw new RuntimeException(OrderException.CANCEL_PROCESS_ERROR);  // 트랜잭션 예외 처리
        }
    }

    public Order findOrder(String merchantUid, String userEmail) {
        return orderRepository.findByMerchantUidAndUser_UserEmail(merchantUid, userEmail)
          .orElseThrow(() -> new IllegalArgumentException(OrderException.ORDER_NOT_FOUND));
    }

    public Order createOrder(OrderRequestDTO dto, User user, Goods goods, String merchantUid) {
        return Order.builder()
          .merchantUid(merchantUid)
          .user(user)
          .goods(goods)
          .quantity(dto.getQuantity())
          .totalAmount(dto.getTotalAmount())
          .discountAmount(dto.getDiscountAmount())
          .payMethod(dto.getPayMethod())
          .deliveryAddr(dto.getDeliveryAddr())
          .deliveryDetailAddr(dto.getDeliveryDetailAddr())
          .postCode(dto.getPostCode())
          .receiverName(dto.getReceiverName())
          .receiverPhone(dto.getReceiverPhone())
          .request(dto.getRequest())
          .orderStatus(dto.getOrderStatus())
          .build();
    }

    private void isOrderValid(Order order, IamportResponse<Payment> payment) {
        // 주문 총액과 결제 총액이 일치하는지 확인
        int totalAmount = order.getTotalAmount() - order.getDiscountAmount();
        if (totalAmount != payment.getResponse().getAmount().intValue()) {
            log.error("결제 금액이 주문 총액과 일치하지 않습니다. 주문 금액: {}, 결제 금액: {}", order.getTotalAmount(), payment.getResponse().getAmount());
            throw new IllegalArgumentException("결제 금액이 주문 총액과 일치하지 않습니다.");
        }

        // 주문 상태가 "결제 준비"인지 확인 (중복 결제 방지)
        if (!"결제 준비".equals(order.getPayMethod())) {
            log.error("이미 결제가 완료된 주문입니다. 주문 상태: {}", order.getPayMethod());
            throw new PaymentException("결제 진행 중이 아닙니다.");
        }
    }


    public void changeOrderStatus(Order order, String status) {
        order.updateStatus(status);
        orderRepository.save(order);
    }

    public String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDay = LocalDateTime.now().format(formatter);

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay + '-' + uniqueString;
    }
}