package com.aischool.goodswap.service.board;

import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostNavigationService {

    private final PostRepository postRepository;

    // 이전 글 조회
    public Optional<Post> getPreviousPost(Long currentPostId) {
        List<Post> previousPosts = postRepository.findByIdBefore(currentPostId);
        return previousPosts.isEmpty() ? Optional.empty() : Optional.of(previousPosts.get(0)); // 첫 번째 결과를 반환
    }

    // 다음 글 조회
    public Optional<Post> getNextPost(Long currentPostId) {
        List<Post> nextPosts = postRepository.findByIdAfter(currentPostId);
        return nextPosts.isEmpty() ? Optional.empty() : Optional.of(nextPosts.get(0)); // 첫 번째 결과를 반환
    }
}
