package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetCodeValidationResponseDTO {

    private String status;
    private String message;

    @Builder
    public PasswordResetCodeValidationResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static PasswordResetCodeValidationResponseDTO success(String message) {
        return new PasswordResetCodeValidationResponseDTO("SUCCESS", message);
    }

    public static PasswordResetCodeValidationResponseDTO fail(String message) {
        return new PasswordResetCodeValidationResponseDTO("FAIL", message);
    }
}
