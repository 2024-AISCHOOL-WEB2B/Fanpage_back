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
@ToString(exclude = {"user"})
@Table(name = "tb_delivery_addr")
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_addr_idx")
    private Long id;

    @Column(name = "delivery_addr", nullable = false, length = 100)
    private String deliveryAddr;

    @Column(name = "delivery_detail_addr", nullable = false, length = 500)
    private String deliveryDetailAddr;

    @Column(name = "post_code", nullable = false, length = 100)
    private String postCode;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "user_name", nullable = false, length = 10)
    private String userName;

    @Column(name = "user_phone", nullable = false, length = 50)
    private String userPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public DeliveryAddress(String deliveryAddr, String deliveryDetailAddr, String postCode, User user, String userName, String userPhone) {
        this.deliveryAddr = deliveryAddr;
        this.deliveryDetailAddr = deliveryDetailAddr;
        this.postCode = postCode;
        this.user = user;
        this.userName = userName;
        this.userPhone = userPhone;
    }

}
