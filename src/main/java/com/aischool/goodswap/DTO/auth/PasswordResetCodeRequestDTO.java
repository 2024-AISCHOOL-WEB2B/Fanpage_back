package com.aischool.goodswap.DTO.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetCodeRequestDTO {
    private String email;
    private String resetCode;

    @Builder
    public PasswordResetCodeRequestDTO(String email, String resetCode) {
        this.email = email;
        this.resetCode = resetCode;
    }
}
