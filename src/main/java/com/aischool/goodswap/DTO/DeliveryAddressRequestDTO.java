package com.aischool.goodswap.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryAddressRequestDTO {

    private Long id;
    private AddressDTO address;
    private String userEmail;
    private String userName;
    private String userPhone;


  }
