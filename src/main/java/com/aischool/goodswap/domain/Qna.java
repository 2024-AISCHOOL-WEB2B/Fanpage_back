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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString(exclude = {"goods", "seller", "asker"})
@Table(name = "tb_qna")
public class Qna {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goods_idx", nullable = false)
    private Goods goods;

    @Column(name = "qna_content", nullable = false)
    private String qnaContent;

    @ManyToOne
    @JoinColumn(name = "seller_email", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "asker_email", nullable = false)
    private User asker;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Qna(Goods goods, String qnaContent, User seller, User asker) {
        this.goods = goods;
        this.qnaContent = qnaContent;
        this.seller = seller;
        this.asker = asker;
    }
}
