package com.aischool.goodswap.service.board;

import com.aischool.goodswap.DTO.post.LikeDTO;
import com.aischool.goodswap.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisPostService {

    private final PostViewService postViewService;

    private final PostLikeService postLikeService;

    // 조회수 증가
    public void incrementPostViewsInRedis(Long postId) {
        postViewService.incrementViews(postId);
    }

    // 좋아요 상태 토글
    public void togglePostLikeInRedis(Long postId, UserDetails userDetails, LikeDTO likeDTO) {
        postLikeService.toggleLike(postId, userDetails, likeDTO);
    }

    // 조회수 가져오기
    public Integer getPostViewsFromRedis(Long postId) {
        return postViewService.getViews(postId);
    }

    // 좋아요 상태 가져오기
    public Set<String> getPostLikedUsersFromRedis(Long postId) {
        return postLikeService.getLikedUsers(postId);
    }

    // 좋아요 개수 가져오기
    public int getPostLikesCountFromRedis(Long postId) {
        return postLikeService.getPostLikesCount(postId);  // PostLikeService 호출
    }
}




