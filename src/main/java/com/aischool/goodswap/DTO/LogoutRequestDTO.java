package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogoutRequestDTO {
    private String refreshToken;

    @Builder
    public LogoutRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
