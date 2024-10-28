package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString
@Table(name = "tb_user")
public class User {

    @Id
    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(unique = true, name = "user_nick", nullable = false)
    private String userNick;

    @Column(unique = true, name = "user_phone", nullable = false)
    private String userPhone;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public User(String userEmail, String userPw, String userNick, String userPhone, String userRole, Boolean isActive) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNick = userNick;
        this.userPhone = userPhone;
        this.userRole = userRole;
        this.isActive = isActive;
    }
}
