package com.aischool.goodswap.service;

import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.CardRepository;
import com.aischool.goodswap.security.AESUtil;
import com.aischool.goodswap.DTO.CreditCardResponseDTO;

import java.util.ArrayList;
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
    List<CreditCardResponseDTO> cardInfoList = new ArrayList<>();

    for (CreditCard card : cards) {
      CreditCardResponseDTO cardInfo = CreditCardResponseDTO.builder()
        .cardId(card.getId())
        .cardNumber(aesUtil.decrypt(card.getCardNumber()))
        .cardCvc(aesUtil.decrypt(card.getCardCvc()))
        .expiredAt(aesUtil.decrypt(card.getExpiredAt()))
        .userEmail(card.getUser().getUserEmail())
        .build();
      cardInfoList.add(cardInfo);
    }
    return cardInfoList;
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
