package com.aischool.goodswap.domain;

import jakarta.persistence.Entity;
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

    @Builder
    public AddressInfoResponseDTO(Long id, String address) {
        this.id = id;
        this.address = address;
    }
}
