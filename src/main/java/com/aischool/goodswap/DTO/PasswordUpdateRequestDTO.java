package com.aischool.goodswap.DTO;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordUpdateRequestDTO {

    private String email;
    private String newPassword;
    private String confirmPassword;

    @Builder
    public PasswordUpdateRequestDTO(String email, String newPassword, String confirmPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
