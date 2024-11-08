package com.aischool.goodswap.service;

import com.aischool.goodswap.domain.File;
import com.aischool.goodswap.repository.FileRepository;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String region;

  @Autowired
  private AmazonS3 amazonS3;
  @Autowired
  private FileRepository fileRepository;

  // 단일 파일을 업로드하고, 업로드된 파일의 URL과 파일 이름을 반환하는 메서드
  public String uploadSingleFile(MultipartFile multipartFile) {
    String filename = createFileName(multipartFile.getOriginalFilename());
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(multipartFile.getSize());
    objectMetadata.setContentType(multipartFile.getContentType());

    try (InputStream inputStream = multipartFile.getInputStream()) {
      amazonS3.putObject(new PutObjectRequest(bucket, filename, inputStream, objectMetadata)
        .withCannedAcl(CannedAccessControlList.PublicRead));
    } catch (IOException e) {
      throw new IllegalStateException("파일 업로드 실패");
    }
    File newFile = File.builder()
      .fileUrl(amazonS3.getUrl(bucket, filename).toString())
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


  public void deleteFile(Long fileId) throws IOException {

    File file = fileRepository.findById(fileId)
      .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

    String fileName = file.getFileName();
    boolean isObjectExist = amazonS3.doesObjectExist(bucket, fileName);
    if (isObjectExist) {
      log.info("저장정보 확인");
      try{
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
      }catch (SdkClientException e){
        throw new IOException("Error deleting file from S3",e);
      }
    } else {
      log.info("존재하지 않음");
    }
  }
}
