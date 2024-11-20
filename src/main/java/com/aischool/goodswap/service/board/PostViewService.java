package com.aischool.goodswap.service.board;

import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewService {

    private final PostRepository postRepository;

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VIEWS_KEY_PREFIX = "post:views:";

    // 조회수 증가
    public void incrementViews(Long postId) {
        redisTemplate.opsForHash().increment(VIEWS_KEY_PREFIX + postId, "views", 1);
    }

    // 조회수 가져오기
    public Integer getViews(Long postId) {
        Integer views = (Integer) redisTemplate.opsForHash().get(VIEWS_KEY_PREFIX + postId, "views");
        return views != null ? views : 0; // 조회수가 없으면 0으로 반환
    }

    @Scheduled(fixedRate = 300000)  // 5분마다 실행
    public void syncViewsToDB() {
        Iterable<Post> posts = postRepository.findAll();

        for (Post post : posts) {
            Integer redisViews = getViews(post.getId());
            post.updatePostViews(redisViews);
            postRepository.save(post);
        }
    }

}


