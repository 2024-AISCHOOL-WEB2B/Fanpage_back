package com.aischool.goodswap.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_credit_card")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
    
}
