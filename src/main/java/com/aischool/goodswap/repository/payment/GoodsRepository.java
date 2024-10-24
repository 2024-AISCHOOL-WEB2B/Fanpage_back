package com.aischool.goodswap.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aischool.goodswap.domain.Goods;

@Repository
public interface GoodsRepository  extends JpaRepository<Goods, Long> {

}
