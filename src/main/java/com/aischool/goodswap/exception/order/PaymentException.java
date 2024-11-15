package com.aischool.goodswap.exception.order;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class PaymentException extends RuntimeException {

  // Getter 메서드
  private final String userMessage;  // 사용자에게 보여줄 메시지
  private final String systemMessage; // 시스템에서 로깅할 메시지

  // 기본 생성자
  public PaymentException(String message) {
    super(message);
    this.userMessage = message;
    this.systemMessage = message;
  }

  // 사용자 메시지, 시스템 메시지, 예외 객체를 받는 생성자
  public PaymentException(String userMessage, String systemMessage, Throwable cause) {
    super(systemMessage, cause);
    this.userMessage = userMessage;
    this.systemMessage = systemMessage;
  }

  // 로그 남기고 예외 던지기
  public static void logErrorAndThrow(String systemMessage, RuntimeException exception) {
    log.error(systemMessage + ": " + exception.getMessage());
  }

}
