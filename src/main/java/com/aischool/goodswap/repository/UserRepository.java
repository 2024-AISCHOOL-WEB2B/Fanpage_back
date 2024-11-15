package com.aischool.goodswap.repository;

// src/main/com/aischool/goodswap/repository/user/UserRepsitory
import com.aischool.goodswap.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findOneByUserEmailIgnoreCase(String userEmail);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserNick(String userNick);

    // 추가된 메서드: 이메일을 통한 사용자 검색
    Optional<User> findByUserEmail(String userEmail);
}

