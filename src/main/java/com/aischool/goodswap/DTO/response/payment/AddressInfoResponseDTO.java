package com.aischool.goodswap.DTO.response.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressInfoResponseDTO {
    private Long id;
    private String address;
}
