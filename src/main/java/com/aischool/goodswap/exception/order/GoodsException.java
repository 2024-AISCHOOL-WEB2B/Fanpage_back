package com.aischool.goodswap.exception.order;

import com.aischool.goodswap.exception.PaymentException;

public class GoodsException extends PaymentException {

  // 상품 재고 처리 오류
  public static final String GOODS_PROCESS_ERROR = "주문 처리 오류"; // 상수 추가
  // 상품 재고 부족 관련 상수
  public static final String GOODS_STOCK_INSUFFICIENT = "상품 재고가 부족합니다";
  // 상품 재고 복구 실패 관련 상수
  public static final String GOODS_RESTORE_FAILED = "상품 재고 복구 실패";
  public static final String GOODS_NOT_FOUND = "상품을 찾을 수 없습니다.";

  public GoodsException(String userMessage) {
    super(userMessage);
  }


}