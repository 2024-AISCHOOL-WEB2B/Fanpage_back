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
@ToString(exclude = {"report", "admin"})
@Table(name = "tb_report_management")
public class ReportManagement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "management_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_idx", nullable = false)
    private Report report;

    @Column(name = "management_content", nullable = false)
    private String managementContent;

    @ManyToOne
    @JoinColumn(name = "admin_email", nullable = false)
    private User admin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ReportManagement(Report report, String managementContent, User admin) {
        this.report = report;
        this.managementContent = managementContent;
        this.admin = admin;
    }
}
