package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@ToString(exclude = {"user"})
@Table(name = "tb_post")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_idx")
    private Long id;

    @Column(name = "board_name", nullable = false)
    private String boardName;

    @Column(name = "post_cate", nullable = false)
    private String postCate;

    @Column(name = "post_title", nullable = false)
    private String postTitle;

    @Column(name = "post_content", nullable = false, columnDefinition = "text")
    private String postContent;

    @Column(name = "post_views", nullable = false)
    private int postViews;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Builder
    public Post(String boardName, String postCate, String postTitle, String postContent, int postViews, User user, Boolean isHidden, Boolean isDeleted) {
        this.boardName = boardName;
        this.postCate = postCate;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postViews = postViews;
        this.user = user;
        this.isHidden = isHidden;
        this.isDeleted = isDeleted;
    }
}
