package com.aischool.goodswap.exception.auth;

import com.aischool.goodswap.exception.order.PaymentException;

public class UserException extends PaymentException {
  public UserException(String userMessage, String systemMessage, Throwable cause) {
    super(userMessage, systemMessage, cause);
  }
}