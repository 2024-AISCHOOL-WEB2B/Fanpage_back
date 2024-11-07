package com.aischool.goodswap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", e.getMessage());

    // 로그를 남기거나 추가적인 처리를 할 수 있습니다.

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
