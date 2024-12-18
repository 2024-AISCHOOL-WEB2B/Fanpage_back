package com.aischool.goodswap.exception.order;

// 주문 관련 예외 클래스
public class OrderException extends PaymentException {

  public static final String ORDER_PROCESS_ERROR = "주문 처리중 오류가 발생했습니다."; // 상수 추가
  public static final String CANCEL_PROCESS_ERROR = "주문 취소중 오류가 발생했습니다."; // 상수 추가

  public static final String ORDER_AMOUNT_MISMATCH = "주문 금액이 불일치합니다.";
  public static final String ORDER_ALREADY_CANCELLED = "이미 취소된 주문입니다.";
  public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다.";
  public static final String ORDER_NOT_FOUND = "주문을 찾을 수 없습니다.";

  public OrderException(String userMessage, String systemMessage, Throwable cause) {
    super(userMessage, systemMessage, cause);
  }

  public OrderException(String userMessage) {
    super(userMessage);
  }

}
