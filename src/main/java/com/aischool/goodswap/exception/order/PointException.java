package com.aischool.goodswap.exception.order;

import com.aischool.goodswap.exception.PaymentException;

// 포인트 관련 예외 클래스
public class PointException extends PaymentException {


  public PointException(String userMessage, String systemMessage, Throwable cause) {
    super(userMessage, systemMessage, cause);
  }
}
