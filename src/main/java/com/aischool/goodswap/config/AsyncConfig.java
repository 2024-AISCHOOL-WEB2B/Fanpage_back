package com.aischool.goodswap.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
  private int CORE_POOL_SIZE = 3;
  private int MAX_POOL_SIZE = 10;
  private int QUEUE_CAPACITY = 10000;

  @Bean(name = "sampleExecutor")
  public Executor threadPoolTaskExecutor(){
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

    taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
    taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
    taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
    taskExecutor.setThreadNamePrefix("Executor-");

    //1. 데코레이터 적용
    taskExecutor.setTaskDecorator(new CustomDecorator());
    //2. 거부 작업 처리
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    return taskExecutor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
    //3. 핸들러 생성해 예외처리
    return new AsyncExceptionHandler();
  }
}
