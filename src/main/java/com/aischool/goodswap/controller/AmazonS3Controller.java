package com.aischool.goodswap.controller;

import com.aischool.goodswap.service.board.AwsS3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File Management", description = "AWS S3 파일 업로드 및 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class AmazonS3Controller {

  private final AwsS3Service awsS3Service;

  @PostMapping("/upload")
  @Operation(
    summary = "파일 업로드",
    description = "AWS S3에 파일을 업로드합니다. 업로드된 파일의 URL을 반환합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "업로드 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "파일을 업로드 했습니다."))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "잘못된 요청입니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "서버 오류가 발생했습니다.")))
    }
  )
  public ResponseEntity<String> uploadFile(@RequestParam MultipartFile multipartFile, @RequestParam String imgSrc) {
    try {
      // 파일 업로드 성공 시 URL 반환
      String fileUrl = awsS3Service.uploadSingleFile(multipartFile, imgSrc);
      return ResponseEntity.ok(fileUrl);
    } catch (IllegalArgumentException e) {
      // 잘못된 요청 처리 (예: null 값이나 형식 오류)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      // 서버 오류 처리
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
    }
  }

  @DeleteMapping("/delete/{fileId}")
  @Operation(
    summary = "파일 삭제",
    description = "AWS S3에서 파일을 삭제합니다.",
    responses = {
      @ApiResponse(responseCode = "202", description = "삭제 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "파일을 삭제하였습니다."))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "파일을 찾을 수 없습니다."))),
      @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "잘못된 요청입니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "서버 오류가 발생했습니다.")))
    }
  )
  public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
    try {
      awsS3Service.deleteFile(fileId);
      return ResponseEntity.accepted().build();
    } catch (FileNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일을 찾을 수 없습니다.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
