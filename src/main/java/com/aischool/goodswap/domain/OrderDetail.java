package com.aischool.goodswap.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Builder;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString(exclude = {"order", "goods"})
@Table(name = "tb_orderdetail")
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "od_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_idx", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "goods_idx", nullable = false)
    private Goods goods;

    @Column(name = "order_cnt", nullable = false)
    private int orderCnt;

    @Builder
    public OrderDetail(Order order, Goods goods, int orderCnt) {
        this.order = order;
        this.goods = goods;
        this.orderCnt = orderCnt;
    }
}
