package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_qna")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Qna {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goods_idx", nullable = false)
    private Goods goods;

    @Column(name = "qna_content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "seller_email", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "asker_email", nullable = false)
    private User asker;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
