package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString
@Table(name = "tb_file")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_idx")
    private Long id;

    @Column(name = "post_idx")
    private Long postIdx;

    @Column(name = "file_url", nullable = false, length = 1500)
    private String fileUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Builder
    public File(Long postIdx, String fileUrl, String fileName) {
        this.postIdx = postIdx;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
