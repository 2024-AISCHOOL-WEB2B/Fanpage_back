package com.aischool.goodswap.controller;

import com.aischool.goodswap.service.AwsS3Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AmazonS3Controller {

  private final AwsS3Service awsS3Service;

  @PostMapping("/upload")
  public ResponseEntity<String> uploadFile(MultipartFile multipartFile){
    return ResponseEntity.ok(awsS3Service.uploadSingleFile(multipartFile));
  }

  @DeleteMapping("/delete/{fileId}")
  public ResponseEntity<String> deleteFile(@PathVariable Long fileId) throws IOException {
    awsS3Service.deleteFile(fileId);
    return ResponseEntity.accepted().build();
  } 
  // 게시글의 회원id를 받아서 권한이 있는지 확인하기

}
