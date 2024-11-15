package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.CreditCardResponseDTO;
import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.service.order.CreditCardService;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Card", description = "카드 관련 API 정보")
@RestController
@RequestMapping("/api/order/card")
@RequiredArgsConstructor
public class CardController {

  private final CreditCardService creditCardService;

  @PostMapping
  @Operation(summary = "카드 등록", description = "회원의 카드 정보를 등록하고 다시 전체 카드 정보를 전달하는 API")
  public ResponseEntity<List<CreditCardResponseDTO>> addCreditCard(@RequestBody CreditCard creditCard) {
    try {
      List<CreditCardResponseDTO> cardInfo = creditCardService.addCreditCard(creditCard);
      return ResponseEntity.ok(cardInfo);
    } catch (Exception e) {
      log.error("Error adding credit card", e);
      return ResponseEntity.badRequest().body(null); // 잘못된 요청 응답
    }
  }

  @DeleteMapping("/{cardId}")
  @Operation(summary = "카드 삭제", description = "회원의 특정 카드 정보를 제거하고 다시 전체 카드 정보를 전달하는 API")
  public ResponseEntity<List<CreditCardResponseDTO>> removeCreditCard(
    @PathVariable Long cardId, @RequestBody String userEmail) {
    try {
      List<CreditCardResponseDTO> cardInfo = creditCardService.removeCreditCard(userEmail, cardId);
      return ResponseEntity.ok(cardInfo);
    } catch (Exception e) {
      log.error("Error removing credit card", e);
      return ResponseEntity.badRequest().body(null); // 잘못된 요청 응답
    }
  }
}
