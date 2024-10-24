package com.aischool.goodswap.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import com.aischool.goodswap.domain.DeliveryAddress;

@Repository
public interface DeliveryAddressRepository extends  JpaRepository<DeliveryAddress, Long> {


    DeliveryAddress findFirstByUserEmail(String user);

    List<DeliveryAddress> findAllByUserEmail(String email);

    Optional<DeliveryAddress> findByIdAndUserEmail(Long id, String userEmail);

}
