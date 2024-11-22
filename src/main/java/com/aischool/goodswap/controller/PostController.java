package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.post.PostDTO;
import com.aischool.goodswap.DTO.post.LikeDTO;
import com.aischool.goodswap.DTO.post.PostListResponseDTO;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.service.board.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Set;

@Tag(name = "게시글 관리", description = "게시글 관련 API")
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
    @GetMapping
    @Operation(
      summary = "게시글 목록 조회",
      description = "카테고리, 이름, 검색어, 정렬 기준 및 페이지 정보를 기반으로 게시글 목록을 조회합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
          content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PostListResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(mediaType = "application/json"))
      }
    )
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
    @PostMapping
    @Operation(
      summary = "게시글 생성",
      description = "새 게시글을 생성합니다.",
      responses = {
        @ApiResponse(responseCode = "201", description = "게시글 생성 성공",
          content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(mediaType = "application/json"))
      }
    )
    public ResponseEntity<Object> createPost(
            @RequestBody PostDTO postDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        try{
            Post post = postService.postSave(postDTO, userDetails);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(post.getId())
                    .toUri();

            return ResponseEntity.created(location).body(post);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 게시글 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    @Operation(
      summary = "게시글 수정",
      description = "특정 ID의 게시글을 수정합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
          content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(mediaType = "application/json"))
      }
    )
    public ResponseEntity<Object> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDTO postDTO,  // @Valid로 DTO 검증
            @AuthenticationPrincipal UserDetails userDetails) {  // @AuthenticationPrincipal로 인증된 사용자 이메일

        try{
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
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    // 게시글 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    @Operation(
      summary = "게시글 삭제",
      description = "특정 ID의 게시글을 삭제합니다.",
      responses = {
        @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(mediaType = "application/json"))
      }
    )
    public ResponseEntity<String> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {  // @AuthenticationPrincipal로 인증된 사용자 이메일 가져오기
        try{
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
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 게시글 조회수 증가
    @GetMapping("/{postId}/views")
    @Operation(
      summary = "게시글 조회수 증가",
      description = "특정 ID의 게시글 조회수를 증가시킵니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "조회수 증가 성공")
      }
    )
    public ResponseEntity<String> incrementPostViews(@PathVariable Long postId) {
        redisPostService.incrementPostViewsInRedis(postId);
        return ResponseEntity.ok("조회수가 증가했습니다.");
    }

    // 게시글 조회수 가져오기
    @GetMapping("/{postId}/views/count")
    @Operation(
      summary = "게시글 조회수 가져오기",
      description = "특정 ID의 게시글 조회수를 가져옵니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "조회수 가져오기 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "123")))
      }
    )
    public ResponseEntity<Integer> getPostViews(@PathVariable Long postId) {
        Integer views = redisPostService.getPostViewsFromRedis(postId);
        return ResponseEntity.ok(views);
    }

    // 게시글 좋아요 처리
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{postId}/like")
    @Operation(
      summary = "게시글 좋아요 처리",
      description = "특정 ID의 게시글에 좋아요를 추가하거나 제거합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공")
      }
    )
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
    @Operation(
      summary = "게시글 좋아요 사용자 목록 조회",
      description = "특정 ID의 게시글에 좋아요를 누른 사용자 목록을 가져옵니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "좋아요 사용자 목록 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(type = "array", example = "[\"user1\", \"user2\"]")))
      }
    )
    public ResponseEntity<Set<String>> getPostLikes(@PathVariable Long postId) {
        Set<String> likedUsers = redisPostService.getPostLikedUsersFromRedis(postId);
        System.out.println("ok");
        return ResponseEntity.ok(likedUsers);
    }

    // 이전글 조회
    @GetMapping("/{postId}/previous")
    @Operation(
      summary = "이전 게시글 조회",
      description = "특정 ID의 게시글에 대해 이전 게시글을 가져옵니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "이전 게시글 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "404", description = "이전 게시글이 존재하지 않음")
      }
    )
    public ResponseEntity<Post> getPreviousPost(@PathVariable Long postId) {
        return postNavigationService.getPreviousPost(postId)
                .map(post -> ResponseEntity.ok(post))  // 이전글이 있으면 반환
                .orElseGet(() -> ResponseEntity.notFound().build());  // 없으면 404 반환
    }

    // 다음글 조회
    @GetMapping("/{postId}/next")
    @Operation(
      summary = "다음 게시글 조회",
      description = "특정 ID의 게시글에 대해 다음 게시글을 가져옵니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "다음 게시글 조회 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))),
        @ApiResponse(responseCode = "404", description = "다음 게시글이 존재하지 않음")
      }
    )
    public ResponseEntity<Post> getNextPost(@PathVariable Long postId) {
        return postNavigationService.getNextPost(postId)
                .map(post -> ResponseEntity.ok(post))  // 다음글이 있으면 반환
                .orElseGet(() -> ResponseEntity.notFound().build());  // 없으면 404 반환
    }

    // 실시간 인기글을 전송하는 SSE 메소드
    @GetMapping("/popular-sse")
    @Operation(
      summary = "실시간 인기 게시글 SSE 전송",
      description = "실시간으로 인기 게시글 정보를 전송합니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공")
      }
    )
    public SseEmitter getPopularPosts() {
        return popularPostSseService.sendPopularPostsSse();  // 인기글을 SSE로 전송
    }
}
