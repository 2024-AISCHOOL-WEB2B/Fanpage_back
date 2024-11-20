package com.aischool.goodswap.service.board;

import com.aischool.goodswap.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularPostSseService {

    private final PopularPostCacheService popularPostCacheService;

    // 인기글을 SSE로 전송하는 메소드
    public SseEmitter sendPopularPostsSse() {
        List<Post> popularPosts = popularPostCacheService.getPopularPosts();  // 캐시된 인기글 목록 가져오기
        SseEmitter emitter = new SseEmitter();

        try {
            // 인기글 목록을 SSE로 전송
            for (Post post : popularPosts) {
                emitter.send(post);  // SSE로 전송
            }
        } catch (Exception e) {
            emitter.completeWithError(e);  // 오류 발생 시
        } finally {
            emitter.complete();  // 완료 처리
        }

        return emitter;
    }
}

