package com.aischool.goodswap.service.board;


import com.aischool.goodswap.DTO.post.PostDTO;
import com.aischool.goodswap.domain.File;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.FileRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.aischool.goodswap.repository.PostRepository;
import com.aischool.goodswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  @Autowired
  private FileRepository fileRepository;
  @Autowired
  private AwsS3Service awsS3Service;

  private final PostRepository postRepository;  // JPA Repository
  private final UserRepository userRepository;
  private final HtmlEscapeService htmlEscapeService;

  // 게시글 조회 (ID로 찾기)
  public Optional<Post> postFindById(Long id) {
    return postRepository.findById(id);
  }

  // 게시글 ID와 userEmail로 게시글 조회
  public Optional<Post> findByIdAndUserEmail(Long id, String userEmail) {
    return postRepository.findByIdAndUserEmail(id, userEmail);
  }

  @Transactional
  public Post postSave(PostDTO postDTO, UserDetails userDetails) {
    // 사용자 찾기
    String username = userDetails.getUsername();

    User user = userRepository.findByUserEmail(username)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    System.out.println("2 " + user);

    // PostDTO를 Post 엔티티로 변환
    Post post = convertToEntity(postDTO);

    // 게시글에 사용자 정보 설정
    post.updateUser(user);

    // 저장된 Post 객체 반환
    return postRepository.save(post);
  }

  // 게시글 수정
  @Transactional
  public Post postUpdate(Long id, PostDTO postDTO, UserDetails userDetails) {

    String username = userDetails.getUsername();

    Post existingPost = postFindById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

    if (!existingPost.getUser().getUserEmail().equals(username)) {
      throw new IllegalArgumentException("이 게시글을 수정할 권한이 없습니다.");
    }

    existingPost.updatePostTitle(htmlEscapeService.escapeHtml(postDTO.getPostTitle()));
    existingPost.updatePostContent(htmlEscapeService.escapeHtml(postDTO.getPostContent()));
    existingPost.updateBoardName(postDTO.getBoardName());
    existingPost.updatePostCate(postDTO.getPostCate());
    existingPost.updateIsHidden(postDTO.getIsHidden());
    existingPost.updateIsDeleted(postDTO.getIsDeleted());
    existingPost.updatePostViews(postDTO.getPostViews());

    return postRepository.save(existingPost);
  }

  // 게시글 삭제
  @Transactional
  public void postDelete(Long id, UserDetails userDetails) {

    String username = userDetails.getUsername();

    Optional<Post> postOpt = findByIdAndUserEmail(id, username);
    if (postOpt.isPresent()) {
      postRepository.deleteById(id);
    } else {
      throw new IllegalArgumentException("게시글을 찾을 수 없거나, 삭제 권한이 없습니다.");
    }
  }

  // 게시글 생성 시 DTO를 엔티티로 변환
  public Post convertToEntity(PostDTO postDTO) {
    return Post.builder()
            .boardName(postDTO.getBoardName())
            .postCate(postDTO.getPostCate())
            .postTitle(htmlEscapeService.escapeHtml(postDTO.getPostTitle()))
            .postContent(htmlEscapeService.escapeHtml(postDTO.getPostContent()))
            .postViews(postDTO.getPostViews())
            .isHidden(postDTO.getIsHidden())
            .isDeleted(postDTO.getIsDeleted())
            .build();
  }


  public void cleanUpOrphanFile() {
// 현재로부터 24시간 전 시간 계산
    LocalDateTime thresholdTime = LocalDateTime.now().minusHours(24);

    // post_idx가 null이고, 생성된 지 24시간이 지난 파일들을 조회
    List<File> orphanFiles = fileRepository.findBySrcIdxIsNullAndUploadedAtBefore(thresholdTime);

    for (File orphanFile : orphanFiles) {
      Long fileId = orphanFile.getId();
      try {
        // S3에서 파일 삭제
        awsS3Service.deleteFile(fileId);

        // DB에서도 해당 파일 삭제
        fileRepository.deleteById(fileId);

        log.info("Deleted orphan file with ID: {} from S3 and database.", fileId);
      } catch (Exception e) {
        log.error("Failed to delete orphan file with ID: {} due to {}", fileId, e.getMessage());
      }
    }
  }
}
