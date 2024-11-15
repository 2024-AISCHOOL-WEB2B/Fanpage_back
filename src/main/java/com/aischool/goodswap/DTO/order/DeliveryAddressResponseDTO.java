package com.aischool.goodswap.DTO.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryAddressResponseDTO {
  private Long id;
  private String address;
  private String deliveryDetailAddr;
  private String postCode;
  private String userEmail;
  private String userName;
  private String userPhone;

}
