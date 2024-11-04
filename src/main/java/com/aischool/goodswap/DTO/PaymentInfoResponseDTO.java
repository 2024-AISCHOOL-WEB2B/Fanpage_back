package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class PaymentInfoResponseDTO {
    
    private String user;

    private int point;

    private String deliveryAddr;

    private String cardNumber;
    private String expiredAt;
    private String cardCvc;

    private String goodName;
    private int goodsPrice;
    private int shippingFee;

    @Builder
    public PaymentInfoResponseDTO(String user, int point, String deliveryAddr, String cardNumber, String expiredAt, String cardCvc, String goodName, int goodsPrice, int shippingFee) {
        this.user = user;
        this.point = point;
        this.deliveryAddr = deliveryAddr;
        this.cardNumber = cardNumber;
        this.expiredAt = expiredAt;
        this.cardCvc = cardCvc;
        this.goodName = goodName;
        this.goodsPrice = goodsPrice;
        this.shippingFee = shippingFee;
    }
}
