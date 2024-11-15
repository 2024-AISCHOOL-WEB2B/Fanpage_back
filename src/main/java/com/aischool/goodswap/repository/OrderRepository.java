package com.aischool.goodswap.repository;

import com.aischool.goodswap.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByMerchantUidAndUser_UserEmail(String MerchantUid, String userEmail);
  List<Order> findByUser_UserEmail(String userEmail);
}
