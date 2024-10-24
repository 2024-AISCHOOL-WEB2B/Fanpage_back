package com.aischool.goodswap.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aischool.goodswap.domain.CreditCard;

public interface CardRepository extends JpaRepository<CreditCard, Long>  {
    
    CreditCard findFirstByUserEmail(String user);
}
