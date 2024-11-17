package com.aischool.goodswap.service.board;

import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.repository.PostRepository;  // PostRepository import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularPostDataService {

    private final RedisPostService redisPostService;
    private final PostRepository postRepository;

    // 최근 1시간 내의 게시글을 조회하는 메소드
    public List<Post> findRecentPostsInLastHour() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);  // 현재 시간에서 1시간 전
        return postRepository.findRecentPostsInLastHour(oneHourAgo);  // 쿼리 호출
    }

    // 게시글을 좋아요와 조회수로 필터링하고 정렬하는 메소드
    public List<Post> filterAndSortPosts(List<Post> posts) {
        List<Post> filteredPosts = new ArrayList<>();

        for (Post post : posts) {
            int likesCount = redisPostService.getPostLikesCountFromRedis(post.getId());
            int viewsCount = redisPostService.getPostViewsFromRedis(post.getId());

            // 조건을 만족하는 게시글만 필터링
            if (likesCount >= 30 && viewsCount >= 100) {
                filteredPosts.add(post);
            }
        }

        // 좋아요와 조회수 기준으로 내림차순 정렬 (우선순위: 좋아요 > 조회수)
        filteredPosts.sort((p1, p2) -> {
            int likeComparison = Integer.compare(redisPostService.getPostLikesCountFromRedis(p2.getId()), redisPostService.getPostLikesCountFromRedis(p1.getId()));
            if (likeComparison != 0) return likeComparison;
            return Integer.compare(redisPostService.getPostViewsFromRedis(p2.getId()), redisPostService.getPostViewsFromRedis(p1.getId()));
        });

        return filteredPosts;
    }

    // 새로운 게시글을 삽입할 적절한 위치 찾기 (우선순위에 맞게)
    public int findInsertPosition(List<Post> currentPopularPosts, Post newPost) {
        // currentPopularPosts가 null인 경우 빈 리스트로 초기화
        if (currentPopularPosts == null) {
            currentPopularPosts = new ArrayList<>();
        }

        for (int i = 0; i < currentPopularPosts.size(); i++) {
            Post existingPost = currentPopularPosts.get(i);
            int compareLikes = Integer.compare(redisPostService.getPostLikesCountFromRedis(newPost.getId()), redisPostService.getPostLikesCountFromRedis(existingPost.getId()));
            if (compareLikes > 0) {
                return i;  // 새로운 게시글의 좋아요 수가 더 많으면 그 위치에 삽입
            } else if (compareLikes == 0) {
                int compareViews = Integer.compare(redisPostService.getPostViewsFromRedis(newPost.getId()), redisPostService.getPostViewsFromRedis(existingPost.getId()));
                if (compareViews > 0) {
                    return i;  // 좋아요 수가 같다면 조회수로 비교하여 더 큰 값을 우선
                }
            }
        }
        return currentPopularPosts.size();  // 우선순위가 가장 낮다면 마지막에 삽입
    }

    // 최대 5개의 인기글만 유지하도록 처리
    public List<Post> maintainMaxPosts(List<Post> currentPopularPosts) {
        // currentPopularPosts가 null인 경우 빈 리스트로 초기화
        if (currentPopularPosts == null) {
            currentPopularPosts = new ArrayList<>();
        }

        // 현재 리스트의 크기가 5보다 크면 최대 5개로 유지
        if (currentPopularPosts.size() > 5) {
            currentPopularPosts = currentPopularPosts.subList(0, 5);  // 5개를 초과하면 잘라냄
        }
        return currentPopularPosts;
    }
}


