package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import com.aischool.goodswap.util.LikeType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tb_like")
public class Like {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_idx")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_idx", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Column(name = "like_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    @Column(name = "is_like", nullable = false)
    private Boolean isLike = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Like(Post post, User user, LikeType likeType, Boolean isLike) {
        this.post = post;
        this.user = user;
        this.likeType = likeType;
        this.isLike = isLike;
    }
}
