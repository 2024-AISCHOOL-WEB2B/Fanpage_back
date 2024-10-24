package com.aischool.goodswap.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_goods")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Goods {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_idx")
    private Long id;

    @Column(name = "goods_name", nullable = false)
    private String name;

    @Column(name = "goods_price", nullable = false)
    private int price;

    @Column(name = "goods_stock", nullable = false)
    private int stock;

    @Column(name = "shipping_fee", nullable = false)
    private int fee;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "goods_desc", nullable = false)
    private String description;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "goods_status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
