package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class PaymentInfoRequestDTO {
    
    private String user;
    private String deliveryAddr;
    private String deliveryDetailAddr;
    private String postCode;
    private String userName;
    private String userPhone;

    @Builder
    public PaymentInfoRequestDTO(String user, String deliveryAddr, String deliveryDetailAddr, String postCode, String userName, String userPhone) {
        this.user = user;
        this.deliveryAddr = deliveryAddr;
        this.deliveryDetailAddr = deliveryDetailAddr;
        this.postCode = postCode;
        this.userName = userName;
        this.userPhone = userPhone;
    }
}
