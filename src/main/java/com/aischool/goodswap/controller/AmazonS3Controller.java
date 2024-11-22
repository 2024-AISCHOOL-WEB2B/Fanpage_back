package com.aischool.goodswap.controller;

import com.aischool.goodswap.service.board.AwsS3Service;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class AmazonS3Controller {

  private final AwsS3Service awsS3Service;

  @PostMapping("/upload")
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
  public ResponseEntity<String> deleteFile(@PathVariable Long fileId) throws IOException {
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
