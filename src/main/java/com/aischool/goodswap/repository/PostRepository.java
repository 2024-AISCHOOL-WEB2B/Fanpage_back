package com.aischool.goodswap.repository;

import com.aischool.goodswap.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 페이징 및 정렬을 고려한 게시글 조회
    @Query("SELECT p FROM Post p WHERE " +
            "( :postCate IS NULL OR p.postCate = :postCate ) " +
            "AND ( :boardName IS NULL OR p.boardName = :boardName ) " +
            "AND ( :searchTerm IS NULL OR LOWER(p.postTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.postContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) )")
    Page<Post> findByPostCateAndBoardNameAndSearchTerm(
            @Param("postCate") String postCate,
            @Param("boardName") String boardName,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);  // Pageable을 추가하여 페이징 처리


    // 게시글 ID와 사용자 이메일을 기준으로 조회
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.user.userEmail = :userEmail")
    Optional<Post> findByIdAndUserEmail(@Param("id") Long id, @Param("userEmail") String userEmail);

    // 이전글 조회 (currentPostId보다 작은 값 중에서 가장 큰 값)
    @Query("SELECT p FROM Post p WHERE p.id < :currentPostId ORDER BY p.id DESC")
    List<Post> findByIdBefore(@Param("currentPostId") Long currentPostId);

    // 다음글 조회 (currentPostId보다 큰 값 중에서 가장 작은 값)
    @Query("SELECT p FROM Post p WHERE p.id > :currentPostId ORDER BY p.id ASC")
    List<Post> findByIdAfter(@Param("currentPostId") Long currentPostId);


    // 최근 1시간 내에 작성된 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :oneHourAgo")
    List<Post> findRecentPostsInLastHour(@Param("oneHourAgo") LocalDateTime oneHourAgo);

}



