package com.aischool.goodswap.service;

import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.DeliveryAddress;
import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.repository.DeliveryAddressRepository;
import com.aischool.goodswap.repository.GoodsRepository;
import com.aischool.goodswap.repository.PointRepository;

import com.aischool.goodswap.security.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncPaymentService {

  @Autowired
  private PointRepository pointRepository;
  @Autowired
  private DeliveryAddressRepository deliveryAddressRepository;
  @Autowired
  private CardRepository cardRepository;
  @Autowired
  private GoodsRepository goodsRepository;
  @Autowired
  private AESUtil aesUtil;

  public AsyncPaymentService(PointRepository pointRepository,
    DeliveryAddressRepository deliveryAddressRepository,
    CardRepository cardRepository,
    GoodsRepository goodsRepository) {
    this.pointRepository = pointRepository;
    this.deliveryAddressRepository = deliveryAddressRepository;
    this.cardRepository = cardRepository;
    this.goodsRepository = goodsRepository;
  }

  @Async // 비동기 통신을 하기위한 어노테이션
  public CompletableFuture<Integer> getTotalPoints(String userEmail) {
    return CompletableFuture.supplyAsync(() -> { // 비동기 작업을 시작해 완료될 시 결과값을 반환하는 메서드
      Integer totalPoints = pointRepository.findTotalPointsByUser_UserEmail(userEmail);
      if (totalPoints == null) {
        throw new IllegalArgumentException("포인트 정보를 확인할 수 없습니다.");
      }
      return totalPoints;
    });
  }

  @Async
  public CompletableFuture<String> getDeliveryAddress(String userEmail) {
    return CompletableFuture.supplyAsync(() -> {
      DeliveryAddress deliveryAddress = deliveryAddressRepository.findFirstByUser_UserEmail(userEmail);
      return (deliveryAddress != null) ? deliveryAddress.getDeliveryAddr() : "배송지 정보가 없습니다.";
    });
  }

  @Async
  public CompletableFuture<Map<String, String>> getCardInfo(String userEmail) {
    return CompletableFuture.supplyAsync(() -> {
      CreditCard card = cardRepository.findFirstByUser_UserEmail(userEmail);
      Map<String, String> cardInfo = new HashMap<>();
      cardInfo.put("cardNumber", (card != null) ? aesUtil.decrypt(card.getCardNumber()) : null);
      cardInfo.put("cardCvc", (card != null) ? aesUtil.decrypt(card.getCardCvc()) : null);
      cardInfo.put("expiredAt", (card != null) ? aesUtil.decrypt(card.getExpiredAt()) : null);
      return cardInfo;
    });
  }

  @Async
  public CompletableFuture<Goods> getGoodsInfo(Long goodsId) {
    return CompletableFuture.supplyAsync(() -> {
      return goodsRepository.findById(goodsId)
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
    });
  }
}