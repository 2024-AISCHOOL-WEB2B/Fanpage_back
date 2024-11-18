package com.aischool.goodswap.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aischool.goodswap.domain.CreditCard;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CreditCard, Long>  {

    CreditCard findFirstByUser_UserEmail(String user);
    boolean existsByCardNumberAndUser_UserEmail(String cardNumber, String userEmail);
    List<CreditCard> findAllByUser_UserEmail(String userEmail);
    Optional<CreditCard> findByIdAndUser_UserEmail(Long cardId, String userEmail);

}
