package com.aischool.goodswap.domain;

// src/main/com/aischool/goodswap/domain/User

import java.time.LocalDateTime;
import com.aischool.goodswap.util.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "tb_user")
public class User implements UserDetails {

    @Id
    @Column(name = "user_email", length = 50)
    private String userEmail;

    // 비밀번호 재설정 메서드
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

    // 리프레시 토큰을 설정하는 메서드 추가
    @Column(name = "refresh_token")
    private String refreshToken;

    @Builder
    public User(String userEmail, String userPw, String userNick, String userPhone, Role userRole, Boolean isActive, String refreshToken) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userNick = userNick;
        this.userPhone = userPhone;
        this.userRole = userRole;
        this.isActive = isActive;
        this.refreshToken = refreshToken;
    }

    @SuppressWarnings("Lombok") // Lombok 알림 억제
    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    // refreshToken을 설정하는 메서드 추가
    @SuppressWarnings("Lombok")
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> userRole.name());
    }

    @Override
    public String getPassword() {
        return this.userPw;
    }

    @Override
    public String getUsername() {
        return this.userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
