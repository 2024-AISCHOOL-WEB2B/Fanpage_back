package com.aischool.goodswap.DTO;

// src/main/com/aischool/goodswap/DTO/SignUpResponseDTO
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "set")
public class SignUpResponseDTO<D> {
    private boolean result;
    private String message;
    private D data;

    public  static <D> SignUpResponseDTO<D> setSuccess(String message) {
        return SignUpResponseDTO.set(true, message, null);
    }

    public static <D> SignUpResponseDTO<D> setFailed(String message)
    {
        return SignUpResponseDTO.set(false, message, null);
    }

    public static <D> SignUpResponseDTO<D> setSuccessData(String message, D data) {
        return SignUpResponseDTO.set(true, message, data);
    }

    public static <D> SignUpResponseDTO<D> setFailedData(String message, D data) {
        return SignUpResponseDTO.set(false, message, data);
    }
}