package com.aischool.goodswap.domain;

import jakarta.persistence.Entity;
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

    @Builder
    public PaymentInfoRequestDTO(String user, String deliveryAddr) {
        this.user = user;
        this.deliveryAddr = deliveryAddr;
    }
}
