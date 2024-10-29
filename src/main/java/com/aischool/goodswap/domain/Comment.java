package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
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
@ToString(exclude = {"user", "post"})
@Table(name = "tb_comment")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cmt_idx")
  private Long id;

  @ManyToOne
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(name = "post_idx", nullable = false)
  private Post post;

  @Column(name = "cmt_content", nullable = false, columnDefinition = "text")
  private String cmtContent;

  @Column(name = "cmt_likes")
  private int cmtLikes;

  @ManyToOne
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(name = "user_email", nullable = false)
  private User user;

  @Column(name = "is_secret")
  private Boolean isSecret = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Builder
  public Comment(Post post, String cmtContent, User user, Boolean isSecret, LocalDateTime updatedAt) {
    this.post = post;
    this.cmtContent = cmtContent;
    this.cmtLikes = 0; // 기본값을 0으로 설정
    this.user = user;
    this.isSecret = isSecret != null ? isSecret : false; // 기본값을 false로 설정
    this.updatedAt = updatedAt;
  }

  public void incrementLikes() {
    this.cmtLikes++;
  }

}
