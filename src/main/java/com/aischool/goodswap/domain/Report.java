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
@ToString(exclude = {"user"})
@Table(name = "tb_report")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_idx")
    private Long id;

    @Column(name = "report_category", nullable = false)
    private String reportCategory;

    @Column(name = "report_src", nullable = false)
    private String reportSrc;

    @Column(name = "src_idx", nullable = false)
    private Long srcIdx;

    @Column(name = "report_content", nullable = false)
    private String reportContent;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "is_reported", nullable = false)
    private Boolean isReported = false;

    @Builder
    public Report(String reportCategory, String reportSrc, Long srcIdx, String reportContent, User user) {
        this.reportCategory = reportCategory;
        this.reportSrc = reportSrc;
        this.srcIdx = srcIdx;
        this.reportContent = reportContent;
        this.user = user;
    }
}
