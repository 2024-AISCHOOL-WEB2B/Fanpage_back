package com.aischool.goodswap.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoodsDTO {
  private String goodsName;
  private int goodsPrice;
  private int shippingFee;
}
