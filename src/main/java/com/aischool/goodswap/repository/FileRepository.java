package com.aischool.goodswap.repository;

import com.aischool.goodswap.domain.File;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
  List<File> findBySrcIdxIsNullAndUploadedAtBefore(LocalDateTime uploadedAt);
}
