package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.CreditCardResponseDTO;
import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.service.order.CreditCardService;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Card", description = "카드 관련 API 정보")
@RestController
@RequestMapping("/api/order/card")
@RequiredArgsConstructor
public class CardController {

  private final CreditCardService creditCardService;

  @PostMapping("/")
  @Operation(summary = "카드 등록", description = "회원의 카드 정보를 등록하고 다시 전체 카드 정보를 전달하는 API")
  public ResponseEntity<Object> addCreditCard(@RequestBody CreditCard creditCard) {
    try {
      List<CreditCardResponseDTO> cardInfo = creditCardService.addCreditCard(creditCard);
      return ResponseEntity.ok(cardInfo);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카드 추가 처리 중 오류가 발생했습니다.");  // 500 Internal Server Error
    }
  }

  @DeleteMapping("/{cardId}")
  @Operation(summary = "카드 삭제", description = "회원의 특정 카드 정보를 제거하고 다시 전체 카드 정보를 전달하는 API")
  public ResponseEntity<Object> removeCreditCard(
    @PathVariable Long cardId, @RequestBody String userEmail) {
    try {
      List<CreditCardResponseDTO> cardInfo = creditCardService.removeCreditCard(userEmail, cardId);
      return ResponseEntity.ok(cardInfo);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카드 삭제 처리 중 서버 오류가 발생했습니다.");  // 500 Internal Server Error
    }
  }
}