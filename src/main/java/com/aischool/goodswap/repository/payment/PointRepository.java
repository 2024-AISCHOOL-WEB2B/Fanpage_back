package com.aischool.goodswap.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aischool.goodswap.domain.Point;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    
    @Query(value = "SELECT SUM(p.point) FROM tb_point p WHERE p.user_email = :user_email", nativeQuery = true)
    Integer findTotalPointsByUserEmail(@Param("user_email") String email);

    // Integer findTotalPointsByUser_Email(String email);


}
