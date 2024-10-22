package com.aischool.goodswap.domain;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "tb_user")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_email")
    private String email;

    @Column(name = "user_pw", nullable = false)
    private String pw;

    @Column(unique = true, name = "user_nick", nullable = false)
    private String nick;

    @Column(unique = true, name = "user_phone", nullable = false)
    private String phone;

    @Column(name = "user_role")
    private String role;

    @Column(name = "is_active")
    private int is_active;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joined_at;

    
}
