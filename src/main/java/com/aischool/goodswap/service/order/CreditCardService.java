package com.aischool.goodswap.service.order;

import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.util.AESUtil;
import com.aischool.goodswap.DTO.order.CreditCardResponseDTO;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CreditCardService {

  @Autowired
  private  CardRepository cardRepository;
  @Autowired
  private AESUtil aesUtil;

  // 회원 이메일을 기준으로 모든 카드 정보를 비동기적으로 조회
  @Transactional(readOnly = true) // 조회 전용 트랜잭션
  @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000)) // 실패 시 재시도
  public List<CreditCardResponseDTO> getCardInfo(String userEmail) {
    // 사용자 이메일을 기준으로 카드 목록을 가져옴
    List<CreditCard> cards = cardRepository.findAllByUser_UserEmail(userEmail);

    // 카드 정보를 비동기적으로 복호화하여 처리
    List<CompletableFuture<CreditCardResponseDTO>> futures = cards.stream()
      .map(card -> CompletableFuture.supplyAsync(() -> decodeCardInfo(card)))
      .toList();

    // 모든 비동기 작업이 완료될 때까지 기다리고 결과를 리스트로 반환
    return futures.stream()
      .map(CompletableFuture::join) // 모든 작업이 완료될 때까지 기다림
      .collect(Collectors.toList());
  }

  // 카드 정보를 복호화하는 메서드
  private CreditCardResponseDTO decodeCardInfo(CreditCard card) {
    // 카드 정보를 복호화하여 DTO로 반환
    return CreditCardResponseDTO.builder()
      .cardId(card.getId())
      .cardNumber(aesUtil.decrypt(card.getCardNumber())) // 복호화 작업
      .cardCvc(aesUtil.decrypt(card.getCardCvc()))
      .expiredAt(aesUtil.decrypt(card.getExpiredAt()))
      .userEmail(card.getUser().getUserEmail())
      .build();
  }

  // 회원 이메일을 기준으로 카드정보 등록
  @Transactional
  public List<CreditCardResponseDTO> addCreditCard(CreditCard creditCard) {
    User userEmail = creditCard.getUser(); // User 객체에서 이메일 가져오기
    String cardNumber = creditCard.getCardNumber();
    String cardCvc = creditCard.getCardCvc();
    String expiredAt = creditCard.getExpiredAt();

    try {
      // 카드 번호가 이미 존재하는지 확인
      if (cardRepository.existsByCardNumberAndUser_UserEmail(cardNumber, userEmail.getUserEmail())) {
        throw new IllegalArgumentException("해당 카드 번호는 이미 등록되어 있습니다.");
      }

      // 카드 정보를 암호화
      CreditCard newCard = CreditCard.builder()
        .cardNumber(aesUtil.encrypt(cardNumber))
        .cardCvc(aesUtil.encrypt(cardCvc))
        .expiredAt(aesUtil.encrypt(expiredAt))
        .user(userEmail)
        .build();

      // 카드 저장
      cardRepository.save(newCard);
      return getCardInfo(userEmail.getUserEmail());
    } catch (IllegalArgumentException e) {
      log.error("Failed to add card: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while adding card: {}", e.getMessage(), e);
      throw new RuntimeException("서버 오류가 발생했습니다.");  // 서버 오류 예외 처리
    }
  }

  @Transactional
  public List<CreditCardResponseDTO> removeCreditCard(String userEmail, Long cardId) {
    try {
      // 카드 ID와 사용자 이메일을 기준으로 카드 정보 조회
      CreditCard card = cardRepository.findByIdAndUser_UserEmail(cardId, userEmail)
        .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

      // 카드 정보 삭제
      cardRepository.delete(card);
      return getCardInfo(userEmail);
    } catch (IllegalArgumentException e) {
      log.error("Failed to remove card: {}", e.getMessage());
      throw e;  // 잘못된 요청 예외 처리
    } catch (Exception e) {
      log.error("Unexpected error while removing card: {}", e.getMessage(), e);
      throw new RuntimeException("서버 오류가 발생했습니다.");  // 서버 오류 예외 처리
    }
  }
}
