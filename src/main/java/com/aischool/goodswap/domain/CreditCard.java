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
@ToString(exclude = {"user"})
@Table(name = "tb_credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_idx")
    private Long id;

    @Column(name = "card_num", nullable = false)
    private String cardNumber;

    @Column(name = "expired_at", nullable = false)
    private String expiredAt;

    @Column(name = "card_cvc", nullable = false)
    private String cardCvc;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Builder
    public CreditCard(String cardNumber, String expiredAt, String cardCvc, User user) {
        this.cardNumber = cardNumber;
        this.expiredAt = expiredAt;
        this.cardCvc = cardCvc;
        this.user = user;
    }
}
