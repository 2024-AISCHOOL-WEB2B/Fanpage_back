package com.aischool.goodswap.exception.order;

import com.aischool.goodswap.exception.PaymentException;

public class TransactionException extends PaymentException {
  public static final String TRANSACTION_FAILURE = "트랜잭션 실패";
  public static final String UNEXPECTED_ERROR = "결제 처리 중 예상치 못한 오류 발생";  // 예상치 못한 오류 메시지 추가

  public TransactionException(String userMessage) {
    super(userMessage);
  }


}