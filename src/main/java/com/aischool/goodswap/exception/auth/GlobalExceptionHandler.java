package com.aischool.goodswap.exception.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "유효하지 않은 토큰입니다.");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }
}
