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
@ToString(exclude = {"order"})
@Table(name = "tb_pg")
public class Pg {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pg_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_idx", nullable = false)
    private Order order;

    @Column(name = "pg_company", nullable = false)
    private String pgCompany;

    @Column(name = "pg_tno", nullable = false)
    private String pgTno;

    @Column(name = "pg_gw", nullable = false)
    private String pgGw;

    @Column(name = "pg_bank", nullable = false)
    private String pgBank;

    @CreationTimestamp
    @Column(name = "pg_time", updatable = false)
    private LocalDateTime pgTime;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    public Pg(Order order, String pgCompany, String pgTno, String pgGw, String pgBank, Boolean isApproved, LocalDateTime approvedAt) {
        this.order = order;
        this.pgCompany = pgCompany;
        this.pgTno = pgTno;
        this.pgGw = pgGw;
        this.pgBank = pgBank;
        this.isApproved = isApproved;
        this.approvedAt = approvedAt;
    }
}
