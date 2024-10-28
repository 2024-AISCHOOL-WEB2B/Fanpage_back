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
@ToString(exclude = {"user", "post"})
@Table(name = "tb_comment")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cmt_idx")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "post_idx", nullable = false)
  private Post post;

  @Column(name = "cmt_content", nullable = false)
  private String cmtContent;

  @Column(name = "cmt_likes")
  private int cmtLikes;

  @ManyToOne
  @JoinColumn(name = "user_email", nullable = false)
  private User user;

  @Column(name = "is_secret")
  private Boolean isSecret = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public Comment(Post post, String cmtContent, User user, Boolean isSecret) {
    this.post = post;
    this.cmtContent = cmtContent;
    this.cmtLikes = 0; // 기본값을 0으로 설정
    this.user = user;
    this.isSecret = isSecret != null ? isSecret : false; // 기본값을 false로 설정
  }

  public void incrementLikes() {
    this.cmtLikes++;
  }

}
