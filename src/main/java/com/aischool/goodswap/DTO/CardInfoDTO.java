package com.aischool.goodswap.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardInfoDTO {
  private String cardNumber;
  private String cardCvc;
  private String expiredAt;
}
