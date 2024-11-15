package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogoutResponseDTO {
    private String message;

    @Builder
    public LogoutResponseDTO(String message) {
        this.message = message;
    }
}
