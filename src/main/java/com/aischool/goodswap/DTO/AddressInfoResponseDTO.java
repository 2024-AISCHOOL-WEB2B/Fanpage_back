package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressInfoResponseDTO {
    private Long id;
    private String address;
    private String deliveryDetailAddr;
    private String postCode;
    private String userName;
    private String userPhone;

    @Builder
    public AddressInfoResponseDTO(Long id, String address, String deliveryDetailAddr, String postCode, String userName, String userPhone) {
        this.id = id;
        this.address = address;
        this.deliveryDetailAddr = deliveryDetailAddr;
        this.postCode = postCode;
        this.userName = userName;
        this.userPhone = userPhone;
    }
}
