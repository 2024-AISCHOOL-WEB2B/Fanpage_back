package com.aischool.goodswap.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInfoResponseDTO {
    
    private String user;

    private int point;

    private AddressDTO address;
    private CardInfoDTO cardInfo;
    private GoodsDTO goods;

}
