package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Builder;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "goods"})
@Table(name = "tb_order")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_idx")
    private Long id;

    @Column(name = "merchant_uid", nullable = false, length = 100)
    private String merchantUid;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JoinColumn(name = "goods_idx", nullable = false)
    private Goods goods;

    @Column(name = "quantity", nullable = false) // 구매 수량
    private int quantity;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "pay_method", length = 10)
    private String payMethod = "결제 준비";

    @Column(name = "delivery_addr", nullable = false, length = 100)
    private String deliveryAddr;

    @Column(name = "delivery_detail_addr", nullable = false, length = 500)
    private String deliveryDetailAddr;

    @Column(name = "post_code", nullable = false, length = 100)
    private String postCode;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "request", nullable = false, length = 200)
    private String request;

    @Column(name = "order_status", nullable = false, length = 10)
    private String orderStatus = "ready";

    @CreationTimestamp
    @Column(name = "ordered_at", updatable = false)
    private LocalDateTime orderedAt = LocalDateTime.now();

    @Builder
    public Order(String merchantUid, User user, Goods goods, int quantity, int totalAmount, int discountAmount, String payMethod,
      String deliveryAddr, String deliveryDetailAddr, String postCode, String receiverName, String receiverPhone,
      String request, String orderStatus) {
        this.merchantUid = merchantUid;
        this.user = user;
        this.goods = goods;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.payMethod = payMethod;
        this.deliveryAddr = deliveryAddr;
        this.deliveryDetailAddr = deliveryDetailAddr;
        this.postCode = postCode;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.request = request;
        this.orderStatus = orderStatus;
    }

    public void updateStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
