package com.aischool.goodswap.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;
  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;
  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  public S3Client amazonS3Client() {  // Amazon S3 클라이언트 객체를 생성하고 구성하는 빈을 정의
    // AWS에 접근하기 위한 accessKey와 secretKey를 사용하여 생성,  다형성을 활용해 객체를 생성
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    //클라이언트 객체를 구성
    return S3Client.builder()
      .region(Region.of(region))
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .build();
  }
}
