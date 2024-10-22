package com.aischool.goodswap.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_orderdetail")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
    private int orderCount;
    
}
