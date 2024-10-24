package com.aischool.goodswap.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aischool.goodswap.domain.DeliveryAddress;

@Repository
public interface DeliveryAddressRepository extends  JpaRepository<DeliveryAddress, Long> {

    DeliveryAddress findFirstByUserEmail(String email);

}
