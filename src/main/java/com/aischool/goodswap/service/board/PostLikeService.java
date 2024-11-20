package com.aischool.goodswap.service.board;

import com.aischool.goodswap.DTO.post.LikeDTO;
import com.aischool.goodswap.domain.Like;
import com.aischool.goodswap.domain.Post;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.LikeRepository;
import com.aischool.goodswap.repository.PostRepository;
import com.aischool.goodswap.repository.UserRepository;
import com.aischool.goodswap.util.LikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    private static final String LIKES_KEY_PREFIX = "post:likes:"; // Redis에서 사용될 키 접두어

    // 좋아요 처리
    public void toggleLike(Long postId, UserDetails userDetails, LikeDTO likeDTO) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Boolean isLike = likeDTO.getIsLike();
        String username = userDetails.getUsername();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // Redis에 좋아요 상태 변경 (실시간 반영)
        if (isLike) {
            setOps.add(LIKES_KEY_PREFIX + postId, username); // 좋아요 추가
        } else {
            setOps.remove(LIKES_KEY_PREFIX + postId, username); // 좋아요 취소
        }

        // MySQL에 좋아요 상태 저장 (동기화될 때만 반영)
        User user = userRepository.findByUserEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존에 해당 게시글에 대한 사용자의 좋아요 여부를 확인
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            Like like = existingLike.get();
            if (like.getIsLike() != isLike) {
                like.updateIsLike(isLike);  // 좋아요 상태 변경
                likeRepository.save(like);  // 상태 변경 저장
            }
        } else {
            // 새로운 좋아요 추가
            Like newLike = Like.builder()
                    .post(post)
                    .user(user)
                    .likeType(LikeType.LIKE)  // 기본적으로 좋아요
                    .isLike(isLike)
                    .build();
            likeRepository.save(newLike);
        }

        // 좋아요 상태가 변경되었으므로 해당 게시글에 대해 동기화가 필요함을 알리는 플래그 설정
        redisTemplate.opsForSet().add("post:likes:updated", postId.toString());
    }

    // Redis에서 좋아요 개수 가져오기 (동적 계산)
    public int getPostLikesCount(Long postId) {
        Set<String> likedUsers = redisTemplate.opsForSet().members(LIKES_KEY_PREFIX + postId);
        return likedUsers != null ? likedUsers.size() : 0; // Redis에서 좋아요 개수 반환
    }

    // Redis에서 좋아요를 누른 사용자 목록을 가져오는 메서드
    public Set<String> getLikedUsers(Long postId) {
        return redisTemplate.opsForSet().members(LIKES_KEY_PREFIX + postId);  // Redis에서 좋아요 누른 사용자들 반환
    }

    // 5분마다 Redis에 저장된 좋아요 개수를 MySQL로 동기화
    @Scheduled(fixedRate = 300000)  // 5분마다 실행
    public void syncPostLikesToDB() {
        Set<String> updatedPostIds = redisTemplate.opsForSet().members("post:likes:updated");

        if (updatedPostIds != null && !updatedPostIds.isEmpty()) {
            for (String postIdStr : updatedPostIds) {
                Long postId = Long.parseLong(postIdStr);

                // Redis에서 좋아요 개수 가져오기
                int redisLikes = getPostLikesCount(postId);

                // MySQL에서 해당 게시글 가져오기
                Post post = postRepository.findById(postId).orElse(null);
                if (post != null) {
                    // Redis에서 좋아요 상태 가져오기
                    Set<String> likedUsers = redisTemplate.opsForSet().members(LIKES_KEY_PREFIX + postId);
                    if (likedUsers != null) {
                        for (String userEmail : likedUsers) {
                            // 사용자 정보 조회
                            User user = userRepository.findByUserEmail(userEmail)
                                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

                            // 좋아요 객체 생성하여 저장
                            Like like = new Like(post, user, LikeType.LIKE, true);
                            likeRepository.save(like);  // MySQL에 좋아요 저장
                        }
                    }
                    // 게시글의 좋아요 개수 업데이트 (Redis에서 계산한 값으로)
                    post.updateLikeCount(redisLikes);  // DB에 반영할 게시글에 좋아요 개수 설정
                    postRepository.save(post);  // MySQL에 좋아요 개수 저장
                }
            }

            // 동기화가 완료된 게시글 ID들을 Redis에서 제거
            redisTemplate.opsForSet().remove("post:likes:updated", updatedPostIds.toArray());
        }
    }
}
