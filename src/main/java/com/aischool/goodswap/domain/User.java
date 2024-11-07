package com.aischool.goodswap.domain;

import java.time.LocalDateTime;

import com.aischool.goodswap.util.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Builder;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "tb_user")
public class User {

    @Id
    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "user_pw", nullable = false, length = 100)
    private String userPw;

    @Column(unique = true, name = "user_nick", nullable = false, length = 20)
    private String userNick;

    @Column(unique = true, name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "user_role", length = 10)
    @Enumerated(EnumType.STRING)
    private Role userRole = Role.USER;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Builder
    public User(String userEmail, String userPw, String userNick, String userPhone, Role userRole, Boolean isActive) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNick = userNick;
        this.userPhone = userPhone;
        this.userRole = userRole;
        this.isActive = isActive;
    }

    public User(String userEmail) {
        this.userEmail = userEmail;
    }
}
