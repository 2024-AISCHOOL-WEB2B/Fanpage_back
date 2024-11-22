package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.CreditCardResponseDTO;
import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.service.order.CreditCardService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Credit Card", description = "신용카드 관련 API")
@RestController
@RequestMapping("/api/order/card")
@RequiredArgsConstructor
public class CardController {

  private final CreditCardService creditCardService;

  @PostMapping("/")
  @Operation(
    summary = "신용카드 추가",
    description = "사용자의 신용카드를 추가합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "신용카드 추가 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditCardResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "카드 정보가 유효하지 않습니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "카드 추가 처리 중 오류가 발생했습니다."))),
    }
  )
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
  @Operation(
    summary = "신용카드 삭제",
    description = "사용자의 신용카드를 삭제합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "신용카드 삭제 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditCardResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "유효하지 않은 카드 ID 또는 사용자 이메일입니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "카드 삭제 처리 중 서버 오류가 발생했습니다.")))
    }
  )
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