package com.aischool.goodswap.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
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
