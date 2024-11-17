package com.aischool.goodswap.repository;

import com.aischool.goodswap.domain.Like;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // 특정 게시글에 대해 좋아요 상태가 true인 개수 조회
    int countByPostAndIsLikeTrue(Post post);

    // 특정 게시글과 사용자의 좋아요 상태 조회
    Optional<Like> findByPostAndUser(Post post, User user);
}
