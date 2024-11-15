package com.aischool.goodswap.service.order;

import com.aischool.goodswap.DTO.order.AddressDTO;
import com.aischool.goodswap.DTO.order.CardInfoDTO;
import com.aischool.goodswap.DTO.order.GoodsDTO;

import java.util.concurrent.CompletableFuture;

public interface AsyncPaymentService {

  CompletableFuture<Integer> getTotalPoints(String userEmail);

  CompletableFuture<AddressDTO> getDeliveryAddress(String userEmail);

  CompletableFuture<CardInfoDTO> getCardInfo(String userEmail);

  CompletableFuture<GoodsDTO> getGoodsInfo(Long goodsId);
}
