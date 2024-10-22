package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_pg")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pg {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pg_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_idx", nullable = false)
    private Order order;

    @Column(name = "pg_company", nullable = false)
    private String company;

    @Column(name = "pg_tno", nullable = false)
    private String tno;

    @Column(name = "pg_gw", nullable = false)
    private String gateway;

    @Column(name = "pg_bank", nullable = false)
    private String bank;

    @CreationTimestamp
    @Column(name = "pg_time", updatable = false)
    private LocalDateTime pgTime;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

}
