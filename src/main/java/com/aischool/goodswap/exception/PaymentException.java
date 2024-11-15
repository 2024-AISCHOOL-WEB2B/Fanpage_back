package com.aischool.goodswap.exception;

public class PaymentException extends RuntimeException {

  private String userMessage;  // 사용자에게 보여줄 메시지
  private String systemMessage; // 시스템에서 로깅할 메시지

  // 기본 생성자
  public PaymentException(String message) {
    super(message);
    this.userMessage = message;
    this.systemMessage = message;
  }

  // 사용자 메시지와 시스템 메시지를 분리하여 처리하는 생성자
  public PaymentException(String userMessage, String systemMessage) {
    super(systemMessage);
    this.userMessage = userMessage;
    this.systemMessage = systemMessage;
  }

  // 사용자 메시지, 시스템 메시지, 예외 객체를 받는 생성자
  public PaymentException(String userMessage, String systemMessage, Throwable cause) {
    super(systemMessage, cause);
    this.userMessage = userMessage;
    this.systemMessage = systemMessage;
  }

  // Getter 메서드
  public String getUserMessage() {
    return userMessage;
  }

  public String getSystemMessage() {
    return systemMessage;
  }
}
