package com.aischool.goodswap.service.board;

import com.aischool.goodswap.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

@Service
@RequiredArgsConstructor
public class PopularPostCacheService {

    private final PopularPostDataService popularPostDataService;

    private List<Post> currentPopularPosts;

    // 인기글 캐시 갱신 메소드
    public void refreshCache() {
        // 최근 1시간 내의 게시글 조회
        List<Post> recentPosts = popularPostDataService.findRecentPostsInLastHour();  // 최근 1시간 내 게시글 가져오기

        // 최근 게시글을 필터링하고 우선순위대로 정렬
        List<Post> newPosts = popularPostDataService.filterAndSortPosts(recentPosts);

        // 기존 인기글 목록에 새로운 게시글들을 우선순위대로 병합
        mergePostsWithPriority(newPosts);

        // 최대 5개의 인기글만 유지
        currentPopularPosts = popularPostDataService.maintainMaxPosts(currentPopularPosts);
    }

    // 인기글 목록을 갱신하는 메소드
    private void mergePostsWithPriority(List<Post> newPosts) {
        for (Post newPost : newPosts) {
            int position = popularPostDataService.findInsertPosition(currentPopularPosts, newPost);  // 새로운 게시글이 들어갈 우선순위 위치를 찾음
            currentPopularPosts.add(position, newPost);  // 인기글 목록에 추가
        }
    }

    // 인기글 목록 반환
    public List<Post> getPopularPosts() {
        return currentPopularPosts;  // 현재 캐시된 인기글 목록을 반환
    }

    // 1시간마다 캐시를 갱신하는 스케줄러
    @Scheduled(fixedRate = 3600000)  // 1시간(3600000ms)마다 실행
    public void scheduledCacheRefresh() {
        refreshCache();  // 캐시 갱신 메소드 호출
    }
}


