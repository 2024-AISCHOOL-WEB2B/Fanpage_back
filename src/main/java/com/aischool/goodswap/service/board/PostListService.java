package com.aischool.goodswap.service.board;

import com.aischool.goodswap.DTO.post.PostListResponseDTO;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostListService {

    private final PostRepository postRepository;

    public PostListResponseDTO getPostsWithDefaultValues(String category, String name, String searchTerm, String sort, int page, int size) {
        if (category == null) category = "";
        if (name == null) name = "";
        if (searchTerm == null) searchTerm = "";

        // Pageable 객체 생성 (페이징 및 정렬 처리)
        Pageable pageable = PageRequest.of(page, size, getSortOrder(sort));

        // 페이징된 게시글 조회
        Page<Post> postsPage = postRepository.findByPostCateAndBoardNameAndSearchTerm(category, name, searchTerm, pageable);

        // 전체 게시글 수 조회
        long totalPosts = postsPage.getTotalElements();  // Page 객체에서 총 게시글 수

        return new PostListResponseDTO(postsPage.getContent(), totalPosts, page, size);
    }

    // 정렬 기준 설정
    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "oldest":
                return Sort.by("createdAt").ascending();
            case "likes":
                return Sort.by("postLikes").descending();
            case "views":
                return Sort.by("postViews").descending();
            case "latest":
            default:
                return Sort.by("createdAt").descending();  // 최신순 (기본값)
        }
    }

}
