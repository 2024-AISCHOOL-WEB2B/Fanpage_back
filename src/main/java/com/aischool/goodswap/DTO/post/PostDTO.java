package com.aischool.goodswap.DTO.post;

import com.aischool.goodswap.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private Long id;

    @NotBlank(message = "게시글 이름은 필수입니다.")
    private String boardName;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String postCate;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다.")
    private String postTitle;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 1000, message = "내용은 최대 1000자까지 입력 가능합니다.")
    private String postContent;

    @Min(value = 0, message = "조회수는 0 이상이어야 합니다.")
    private int postViews;

    private Boolean isHidden;
    private Boolean isDeleted;

}
