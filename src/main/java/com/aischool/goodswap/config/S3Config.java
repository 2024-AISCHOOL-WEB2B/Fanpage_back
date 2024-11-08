package com.aischool.goodswap.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
  @Value("${S3_ACCESS_KEY}")
  private String accessKey;
  @Value("${S3_SECRET_KEY}")
  private String secretKey;
  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  public AmazonS3 amazonS3Client() {  // Amazon S3 클라이언트 객체를 생성하고 구성하는 빈을 정의
    // AWS에 접근하기 위한 accessKey와 secretKey를 사용하여 생성,  다형성을 활용해 객체를 생성
    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

    //클라이언트 객체를 구성
    return AmazonS3ClientBuilder
      .standard()  // standard() 메서드를 호출하여 기본 빌더를 생성한 뒤, 자격 증명 객체와 region을 지정
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .withRegion(region)
      .build();
  }
}
