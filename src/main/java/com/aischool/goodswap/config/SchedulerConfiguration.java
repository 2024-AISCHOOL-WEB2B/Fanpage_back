package com.aischool.goodswap.config;

import com.aischool.goodswap.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfiguration {

  @Autowired
  private PostService postService;

  @Scheduled(cron = "0 0 0/6 * * ?")
  public void run() {
    postService.cleanUpOrphanFile();
  }
}
