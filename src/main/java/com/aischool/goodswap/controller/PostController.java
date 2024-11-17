package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.post.PostDTO;
import com.aischool.goodswap.DTO.post.LikeDTO;
import com.aischool.goodswap.DTO.post.PostListResponseDTO;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.service.board.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Validated  // 전체 컨트롤러에서 유효성 검사를 활성화
public class PostController {

    private final PostListService postListService;
    private final RedisPostService redisPostService;
    private final PostService postService;
    private final PostNavigationService postNavigationService;
    private final PopularPostSseService popularPostSseService;

    // 게시글 목록 조회 (페이징 처리)
    @GetMapping("/")
    public ResponseEntity<PostListResponseDTO> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,  // 기본값 0 (첫 번째 페이지)
            @RequestParam(defaultValue = "50") int size) {  // 기본값 50 (페이지 크기)

        // 게시글 목록과 페이지 정보 반환
        PostListResponseDTO response = postListService.getPostsWithDefaultValues(category, name, searchTerm, sort, page, size);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 생성
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/")
    public ResponseEntity<Post> createPost(
            @RequestBody PostDTO postDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Post post = postService.postSave(postDTO, userDetails);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId())
                .toUri();

        return ResponseEntity.created(location).body(post);
    }

    // 게시글 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDTO postDTO,  // @Valid로 DTO 검증
            @AuthenticationPrincipal UserDetails userDetails) {  // @AuthenticationPrincipal로 인증된 사용자 이메일 가져오기

        Post updatedPost = postService.postUpdate(id, postDTO, userDetails);

        // 수정된 게시글의 URI
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updatedPost.getId())
                .toUri();

        // 메시지와 함께 Location 헤더에 수정된 게시글의 URI를 반환
        return ResponseEntity.status(HttpStatus.OK)
                .location(location)  // Location 헤더에 수정된 게시글의 URI
                .body(updatedPost);
    }

    // 게시글 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {  // @AuthenticationPrincipal로 인증된 사용자 이메일 가져오기

        postService.postDelete(id, userDetails);

        // 게시글 삭제 후 게시글 목록 페이지로 리다이렉션
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/posts")  // 게시글 목록 페이지 URI
                .build()
                .toUri();

        // 게시글 목록 페이지로 리다이렉션하는 URI를 Location 헤더에 담아서 반환
        return ResponseEntity.noContent()
                .location(location)  // 게시글 목록 페이지 URI
                .build();
    }

    // 게시글 조회수 증가
    @GetMapping("/{postId}/views")
    public ResponseEntity<String> incrementPostViews(@PathVariable Long postId) {
        redisPostService.incrementPostViewsInRedis(postId);
        return ResponseEntity.ok("조회수가 증가했습니다.");
    }

    // 게시글 조회수 가져오기
    @GetMapping("/{postId}/views/count")
    public ResponseEntity<Integer> getPostViews(@PathVariable Long postId) {
        Integer views = redisPostService.getPostViewsFromRedis(postId);
        return ResponseEntity.ok(views);
    }

    // 게시글 좋아요 처리
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,  // @AuthenticationPrincipal로 인증된 사용자 이메일 가져오기
            @Valid @RequestBody LikeDTO likeDTO) {

        redisPostService.togglePostLikeInRedis(postId, userDetails, likeDTO);
        System.out.println("Post ID: " + postId + " - Current Views in Redis: " + likeDTO);
        return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
    }

    // 게시글 좋아요 사용자 목록 가져오기
    @GetMapping("/{postId}/likes")
    public ResponseEntity<Set<String>> getPostLikes(@PathVariable Long postId) {
        Set<String> likedUsers = redisPostService.getPostLikedUsersFromRedis(postId);
        System.out.println("ok");
        return ResponseEntity.ok(likedUsers);
    }

    // 이전글 조회
    @GetMapping("/{postId}/previous")
    public ResponseEntity<Post> getPreviousPost(@PathVariable Long postId) {
        return postNavigationService.getPreviousPost(postId)
                .map(post -> ResponseEntity.ok(post))  // 이전글이 있으면 반환
                .orElseGet(() -> ResponseEntity.notFound().build());  // 없으면 404 반환
    }

    // 다음글 조회
    @GetMapping("/{postId}/next")
    public ResponseEntity<Post> getNextPost(@PathVariable Long postId) {
        return postNavigationService.getNextPost(postId)
                .map(post -> ResponseEntity.ok(post))  // 다음글이 있으면 반환
                .orElseGet(() -> ResponseEntity.notFound().build());  // 없으면 404 반환
    }

    // 실시간 인기글을 전송하는 SSE 메소드
    @GetMapping("/popular-sse")
    public SseEmitter getPopularPosts() {
        return popularPostSseService.sendPopularPostsSse();  // 인기글을 SSE로 전송
    }
}
