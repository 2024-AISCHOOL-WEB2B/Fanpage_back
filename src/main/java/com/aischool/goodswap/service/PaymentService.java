package com.aischool.goodswap.service;

import com.aischool.goodswap.domain.AddressInfoResponseDTO;
import com.aischool.goodswap.domain.PaymentInfoRequestDTO;
import com.aischool.goodswap.domain.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.User;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.DeliveryAddress;
import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.repository.payment.CardRepository;
import com.aischool.goodswap.repository.payment.DeliveryAddressRepository;
import com.aischool.goodswap.repository.payment.GoodsRepository;
import com.aischool.goodswap.repository.payment.PointRepository;

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

        // 기존 주소 엔티티 가져오기
        DeliveryAddress existingAddress = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, userEmail)
          .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));

        // 업데이트 메서드를 사용해 기존 객체의 주소 수정
        existingAddress.updateDeliveryAddr(newAddress);

        // 수정된 객체 저장
        deliveryAddressRepository.save(existingAddress);

        return getAddressInfo(userEmail);
    }

    @Transactional
    public List<AddressInfoResponseDTO> addDeliveryAddress(PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser(); // userEmail 추출
        String newAddress = paymentInfoRequest.getDeliveryAddr(); // 배송지 주소 추출

        // 빌더 패턴을 사용하여 새로운 DeliveryAddress 객체 생성
        DeliveryAddress newDeliveryAddress = DeliveryAddress.builder()
          .user(new User(userEmail)) // User 객체 생성
          .deliveryAddr(newAddress)    // 배송지 주소 설정
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
}