package com.aischool.goodswap.service.order;

import com.aischool.goodswap.DTO.order.AddressDTO;
import com.aischool.goodswap.DTO.order.CardInfoDTO;
import com.aischool.goodswap.DTO.order.GoodsDTO;
import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.DeliveryAddress;
import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.repository.DeliveryAddressRepository;
import com.aischool.goodswap.repository.GoodsRepository;
import com.aischool.goodswap.repository.PointRepository;

import com.aischool.goodswap.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncPaymentServiceImpl implements AsyncPaymentService {

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

  public AsyncPaymentServiceImpl(PointRepository pointRepository,
    DeliveryAddressRepository deliveryAddressRepository,
    CardRepository cardRepository,
    GoodsRepository goodsRepository) {
    this.pointRepository = pointRepository;
    this.deliveryAddressRepository = deliveryAddressRepository;
    this.cardRepository = cardRepository;
    this.goodsRepository = goodsRepository;
  }

  @Async // 비동기 통신을 하기위한 어노테이션
  @Override
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
  @Override
  public CompletableFuture<AddressDTO> getDeliveryAddress(String userEmail) {
    return CompletableFuture.supplyAsync(() -> {
      // 사용자 이메일을 통해 첫 번째 배송 주소를 조회
      DeliveryAddress deliveryAddress = deliveryAddressRepository.findFirstByUser_UserEmail(userEmail);

      // 조회된 배송 주소를 DTO로 변환
      AddressDTO addressDTO = AddressDTO.builder()
        .address(deliveryAddress.getDeliveryAddr())
        .deliveryDetailAddr(deliveryAddress.getDeliveryDetailAddr())
        .postCode(deliveryAddress.getPostCode())
        .build();

      return addressDTO;
    });
  }

  @Async
  @Override
  public CompletableFuture<CardInfoDTO> getCardInfo(String userEmail) {
    return CompletableFuture.supplyAsync(() -> {
      // 사용자 이메일을 통해 카드 정보 조회
      CreditCard card = cardRepository.findFirstByUser_UserEmail(userEmail);

      // cardInfo 대신 CardInfoDTO를 반환
      CardInfoDTO cardInfoDTO = CardInfoDTO.builder()
        .cardNumber((card != null) ? aesUtil.decrypt(card.getCardNumber()) : null)
        .cardCvc((card != null) ? aesUtil.decrypt(card.getCardCvc()) : null)
        .expiredAt((card != null) ? aesUtil.decrypt(card.getExpiredAt()) : null)
        .build();

      return cardInfoDTO;
    });
  }

  @Async
  @Override
  public CompletableFuture<GoodsDTO> getGoodsInfo(Long goodsId) {
    return CompletableFuture.supplyAsync(() -> {
      // 상품 ID를 통해 상품 정보 조회
      Optional<Goods> goods = Optional.ofNullable(goodsRepository.findById(goodsId)
        .orElseThrow(() -> new IllegalArgumentException("해당 상품 정보를 찾을 수 없습니다.")));

      // 조회된 상품 정보를 DTO로 변환
      GoodsDTO dto = GoodsDTO.builder()
        .goodsName(goods.get().getGoodsName())
        .goodsPrice(goods.get().getGoodsPrice())
        .shippingFee(goods.get().getShippingFee())
        .build();

      return dto;
    });
  }
}