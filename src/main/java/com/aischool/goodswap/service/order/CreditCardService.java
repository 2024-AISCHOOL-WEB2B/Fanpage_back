package com.aischool.goodswap.service.order;

import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.security.AESUtil;
import com.aischool.goodswap.DTO.order.CreditCardResponseDTO;

import java.util.ArrayList;
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

  // 회원 이메일을 기준으로 모든 카드정보 가져옴
  @Transactional(readOnly = true)
  @Retryable(value = Exception.class, backoff = @Backoff(delay = 1000))
  public List<CreditCardResponseDTO> getCardInfo(String userEmail) {
    List<CreditCard> cards = cardRepository.findAllByUser_UserEmail(userEmail);

    // CompletableFuture를 사용하여 병렬로 작업 수행
    List<CompletableFuture<CreditCardResponseDTO>> futures = cards.stream()
      .map(card -> CompletableFuture.supplyAsync(() -> decodeCardInfo(card)))
      .toList();

    // CompletableFuture 결과를 모아 최종 리스트로 반환
    return futures.stream()
      .map(CompletableFuture::join) // 모든 작업이 완료될 때까지 기다림
      .collect(Collectors.toList());
  }

  // 카드 정보를 복호화하는 메서드
  private CreditCardResponseDTO decodeCardInfo(CreditCard card) {
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
  }

  @Transactional
  public List<CreditCardResponseDTO> removeCreditCard(String userEmail, Long cardId) {
    CreditCard card = cardRepository.findByIdAndUser_UserEmail(cardId, userEmail)
      .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

    cardRepository.delete(card);
    return getCardInfo(userEmail);
  }
}
