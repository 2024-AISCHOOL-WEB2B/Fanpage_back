package com.aischool.goodswap.DTO.post;

import com.aischool.goodswap.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponseDTO {
    private List<Post> posts;   // 게시글 목록
    private long totalPosts;    // 전체 게시글 수
    private int currentPage;    // 현재 페이지 번호
    private int pageSize;       // 페이지 크기
}
