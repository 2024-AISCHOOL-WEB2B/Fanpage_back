package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponseDTO {
    private String message;
    private String accessToken;
    private int exprTime;
    private String refreshToken;

    @Builder
    public LoginResponseDTO(String message, String accessToken, int exprTime, String refreshToken) {
        this.message = message;
        this.accessToken = accessToken;
        this.exprTime = exprTime;
        this.refreshToken = refreshToken;
    }
}
