package com.aischool.goodswap.exception;

public class UserException extends PaymentException {
  public UserException(String userMessage, String systemMessage, Throwable cause) {
    super(userMessage, systemMessage, cause);
  }
}