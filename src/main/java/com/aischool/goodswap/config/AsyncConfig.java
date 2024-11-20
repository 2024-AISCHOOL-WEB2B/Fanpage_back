package com.aischool.goodswap.config;

import com.aischool.goodswap.exception.order.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  // Thread pool configuration parameters
  private int CORE_POOL_SIZE = 10;  // 기본 스레드 수 증가
  private int MAX_POOL_SIZE = 50;   // 최대 스레드 수 증가
  private int QUEUE_CAPACITY = 5000;  // 대기 큐 용량 증가

  @Bean(name = "Executor")
  public Executor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

    taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
    taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
    taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
    taskExecutor.setThreadNamePrefix("Async-");

    // 1. 데코레이터 적용: 비동기 작업의 컨텍스트 전달
    taskExecutor.setTaskDecorator(new CustomDecorator());

    // 2. 거부 작업 처리: 요청이 거부되면 호출자 쓰레드에서 실행하도록 설정
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // 3. 스레드 풀 모니터링을 위한 로그
    taskExecutor.setWaitForTasksToCompleteOnShutdown(true); // Shutdown 시 대기
    taskExecutor.setAwaitTerminationSeconds(60); // 최대 60초까지 대기

    return taskExecutor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    // 비동기 예외 처리 핸들러
    return new AsyncExceptionHandler();
  }
}
