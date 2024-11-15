package com.aischool.goodswap.DTO.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressDTO {
  private String address;
  private String deliveryDetailAddr;
  private String postCode;
}
