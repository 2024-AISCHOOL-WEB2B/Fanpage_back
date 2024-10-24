package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_post")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_idx")
    private Long id;

    @Column(name = "board_name", nullable = false)
    private String name;

    @Column(name = "post_cate", nullable = false)
    private String category;

    @Column(name = "post_title", nullable = false)
    private String title;

    @Column(name = "post_content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "post_views", nullable = false)
    private int views;

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

}
