package com.aischool.goodswap.DTO.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetRequestDTO {

    private String email;

    @Builder
    public PasswordResetRequestDTO(String email) {
        this.email = email;
    }
}
