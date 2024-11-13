package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetResponseDTO {

    private String status;
    private String message;

    @Builder
    public PasswordResetResponseDTO(String status, String message, Long expirationTimestamp) {
        this.status = status;
        this.message = message;
    }

    public static PasswordResetResponseDTO success(String message, long expirationTimestamp) {
        return PasswordResetResponseDTO.builder()
                .status("SUCCESS")
                .message(message)
                .build();
    }

    public static PasswordResetResponseDTO fail(String message) {
        return PasswordResetResponseDTO.builder()
                .status("FAIL")
                .message(message)
                .build();
    }
}
