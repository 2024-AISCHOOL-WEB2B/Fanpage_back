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
@Table(name = "tb_report")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_idx")
    private Long id;

    @Column(name = "report_category", nullable = false, length = 20)
    private String reportCategory;

    @Column(name = "report_src", nullable = false, length = 20)
    private String reportSrc;

    @Column(name = "src_idx", nullable = false)
    private Long srcIdx;

    @Column(name = "report_content", nullable = false)
    private String reportContent;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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
