package com.aischool.goodswap.DTO.post;

import com.aischool.goodswap.util.LikeType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@AllArgsConstructor
public class LikeDTO {
    private Long postId;      // 연관된 Post의 ID
    private String userEmail; // 유저의 이메일 (고유한 식별자로 사용)
    private LikeType likeType; // 좋아요/싫어요 종류
    private Boolean isLike;   // 실
}
