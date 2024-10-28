package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

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
@ToString(exclude = {"user"})
@Table(name = "tb_order")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_idx")
    private Long id;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "ordered_at", updatable = false)
    private LocalDateTime orderedAt;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "pay_amount", nullable = false)
    private int payAmount;

    @Column(name = "pay_method", nullable = false)
    private String payMethod;

    @Column(name = "paid_amount", nullable = false)
    private int paidAmount;

    @Column(name = "delivery_addr", nullable = false)
    private String deliveryAddress;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "request", nullable = false)
    private String request;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Builder
    public Order(String orderNo, User user, int totalAmount, int discountAmount, int payAmount, String payMethod,
      int paidAmount, String deliveryAddress, String receiverName, String receiverPhone,
      String request, String orderStatus) {
        this.orderNo = orderNo;
        this.user = user;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.payAmount = payAmount;
        this.payMethod = payMethod;
        this.paidAmount = paidAmount;
        this.deliveryAddress = deliveryAddress;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.request = request;
        this.orderStatus = orderStatus;
    }
}
