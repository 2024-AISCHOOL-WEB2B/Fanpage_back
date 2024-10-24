package com.aischool.goodswap.DTO.response.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoResponseDTO {
    
    private String user;

    private int point;

    private String address;

    private String cardNumber;
    private String expiredAt;
    private String cardCvc;

    private String goodName;
    private int goodsPrice;
    private int shippingFee;

}
