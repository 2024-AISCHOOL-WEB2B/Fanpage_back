package com.aischool.goodswap.DTO.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreditCardResponseDTO {
  private Long cardId;
  private String cardNumber;
  private String cardCvc;
  private String expiredAt;
  private String userEmail;
}
