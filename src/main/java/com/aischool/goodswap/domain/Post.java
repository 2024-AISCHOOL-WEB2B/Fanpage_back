package com.aischool.goodswap.domain;

import java.time.LocalDateTime;

import com.aischool.goodswap.repository.LikeRepository;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Builder;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user"})
@Table(name = "tb_post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_idx")
    private Long id;

    @Column(name = "board_name", nullable = false, length = 50)
    private String boardName;

    @Column(name = "post_cate", nullable = false, length = 20)
    private String postCate;

    @Column(name = "post_title", nullable = false, length = 1000)
    private String postTitle;

    @Column(name = "post_content", nullable = false, columnDefinition = "text")
    private String postContent;

    @Column(name = "post_views", nullable = false)
    private int postViews;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 좋아요 개수 (동적으로 계산)
    @Transient  // 이 필드는 DB에 저장되지 않도록 설정
    private int likeCount;

    @Builder
    public Post(String boardName, String postCate, String postTitle, String postContent, int postViews, User user, Boolean isHidden, Boolean isDeleted, LocalDateTime updatedAt) {
        this.boardName = boardName;
        this.postCate = postCate;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postViews = postViews;
        this.user = user;
        this.isHidden = isHidden;
        this.isDeleted = isDeleted;
        this.updatedAt = updatedAt;
    }

    public void updateBoardName(String boardName) {
        this.boardName = boardName;
    }

    public void updatePostCate(String postCate) {
        this.postCate = postCate;
    }

    public void updatePostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void updatePostContent(String postContent) {
        this.postContent = postContent;
    }

    public void updatePostViews(int postViews) {
        this.postViews = postViews;
    }

    public void updateIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public void updateIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
