package com.aischool.goodswap.repository.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aischool.goodswap.domain.CreditCard;

public interface CardRepository extends JpaRepository<CreditCard, Long>  {

    CreditCard findFirstByUser_UserEmail(String user);
    List<CreditCard> findAllByUser_UserEmail(String userEmail);

}
