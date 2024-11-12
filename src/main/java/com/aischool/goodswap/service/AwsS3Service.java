package com.aischool.goodswap.service;

import com.aischool.goodswap.domain.File;
import com.aischool.goodswap.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String region;

  @Autowired
  private S3Client s3Client;
  @Autowired
  private FileRepository fileRepository;

  // 단일 파일을 업로드하고, 업로드된 파일의 URL과 파일 이름을 반환하는 메서드
  public String uploadSingleFile(MultipartFile multipartFile) {
    String filename = createFileName(multipartFile.getOriginalFilename());

    try (InputStream inputStream = multipartFile.getInputStream()) {
      // 객체에 대한 PutObject 요청 생성
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(filename)
        .acl(ObjectCannedACL.PUBLIC_READ) // 파일 공개 읽기 권한 설정
        .build();

      // 파일을 S3에 업로드
      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, multipartFile.getSize()));

    } catch (IOException e) {
      throw new IllegalStateException("파일 업로드 실패", e);
    }

    // 업로드된 파일 URL 생성
    String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, filename);

    // 업로드된 파일 정보 DB 저장
    File newFile = File.builder()
      .fileUrl(fileUrl)
      .fileName(filename)
      .build();

    fileRepository.save(newFile);

    return newFile.getFileUrl();
  }

  // 다중 파일을 업로드하고, 각 파일의 결과를 담은 리스트를 반환하는 메서드
  // 지금은 파일을 하나씩 보낼거같아서 일단 보류
  public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
    return multipartFiles.stream()
      .map(this::uploadSingleFile) // 각 파일에 대해 uploadSingleFile 메서드 호출
      .toList(); // 각 결과를 List로 수집
  }



  // 파일명을 난수화하기 위해 UUID 를 활용하여 난수를 돌린다.
  public String createFileName(String fileName){
    return UUID.randomUUID().toString().concat(getFileExtension(fileName));
  }

  //  "."의 존재 유무만 판단
  private String getFileExtension(String fileName){
    try{
      return fileName.substring(fileName.lastIndexOf("."));
    } catch (StringIndexOutOfBoundsException e){
      throw new IllegalStateException("잘못된 형식의 파일(" + fileName + ") 입니다.");
    }
  }

  // 파일 삭제 메서드
  public void deleteFile(Long fileId) throws IOException {

    File file = fileRepository.findById(fileId)
      .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

    String fileName = file.getFileName();

    // 객체 삭제 요청 생성
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
      .bucket(bucket)
      .key(fileName)
      .build();

    try {
      // 파일 삭제
      s3Client.deleteObject(deleteObjectRequest);
      log.info("파일 삭제 성공: " + fileName);
    } catch (SdkClientException e) {
      log.info("파일이 존재하지 않거나 삭제 중 오류 발생: " + fileName);
      throw new IOException("파일 삭제 중 오류 발생", e);
    }
  }
}