package com.aischool.goodswap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import com.aischool.goodswap.domain.DeliveryAddress;

@Repository
public interface DeliveryAddressRepository extends  JpaRepository<DeliveryAddress, Long> {


    DeliveryAddress findFirstByUser_UserEmail(String user);

    List<DeliveryAddress> findAllByUser_UserEmail(String userEmail);

    Optional<DeliveryAddress> findByIdAndUser_UserEmail(Long id, String userEmail);

    boolean existsByDeliveryAddrAndUser_UserEmail(String deliveryAddr, String userEmail);
}
