package com.aischool.goodswap.service;

import com.aischool.goodswap.DTO.AddressDTO;
import com.aischool.goodswap.DTO.CardInfoDTO;
import com.aischool.goodswap.DTO.GoodsDTO;

import java.util.concurrent.CompletableFuture;

public interface AsyncPaymentService {

  CompletableFuture<Integer> getTotalPoints(String userEmail);

  CompletableFuture<AddressDTO> getDeliveryAddress(String userEmail);

  CompletableFuture<CardInfoDTO> getCardInfo(String userEmail);

  CompletableFuture<GoodsDTO> getGoodsInfo(Long goodsId);
}
