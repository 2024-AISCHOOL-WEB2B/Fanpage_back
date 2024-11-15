package com.aischool.goodswap.service.board;


import com.aischool.goodswap.domain.File;
import com.aischool.goodswap.repository.FileRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  @Autowired
  private FileRepository fileRepository;
  @Autowired
  private AwsS3Service awsS3Service;

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
