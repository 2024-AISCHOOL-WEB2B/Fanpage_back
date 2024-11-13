package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordUpdateResponseDTO {

    private String status;
    private String message;

    @Builder
    public PasswordUpdateResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static PasswordUpdateResponseDTO success(String message) {
        return PasswordUpdateResponseDTO.builder()
                .status("SUCCESS")
                .message(message)
                .build();
    }

    public static PasswordUpdateResponseDTO fail(String message) {
        return PasswordUpdateResponseDTO.builder()
                .status("FAIL")
                .message(message)
                .build();
    }
}
