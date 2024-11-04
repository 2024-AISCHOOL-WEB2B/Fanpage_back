package com.aischool.goodswap.repository.payment;

import com.aischool.goodswap.domain.OrderTemporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderTemporal, Long> {

}
