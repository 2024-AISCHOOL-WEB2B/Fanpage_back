package com.aischool.goodswap.util;

import com.aischool.goodswap.DTO.order.PaymentInfoResponseDTO;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest // 이 어노테이션은 Spring Boot 컨텍스트에서 실행될 수 있도록 합니다.
public class PaymentServicePerformanceTest {

  private static final int TEST_REQUEST_COUNT = 50; // 비동기 요청 수

  @Autowired
  private WebClient.Builder webClientBuilder; // WebClient 생성용

  @Test
  public void testAsynchronousAddressRequests() {
    // WebClient의 baseUrl을 localhost:8081로 설정
    WebClient webClient = webClientBuilder.baseUrl("http://localhost:8081/api/order").build();
    String testUserEmail = "test"; // 테스트할 유저 이메일
    Long testGoodsId = 1L; // 테스트할 goodsId

    long startTime = System.currentTimeMillis();

    // CompletableFuture 배열 생성 (비동기 요청)
    CompletableFuture<?>[] futures = IntStream.range(0, TEST_REQUEST_COUNT)
      .mapToObj(i -> webClient.get()
        .uri("/info/{goodsId}", testGoodsId)
        .header("userEmail", testUserEmail)
        .retrieve()
        .bodyToMono(PaymentInfoResponseDTO.class) // 응답 타입을 정확히 지정
        .doOnError(ex -> System.err.println("Error in request: " + ex.getMessage()))
        .toFuture()
      ).toArray(CompletableFuture[]::new);

    // 모든 비동기 요청이 완료될 때까지 대기
    CompletableFuture.allOf(futures).join();

    long endTime = System.currentTimeMillis();
    System.out.println("Asynchronous Total Time: " + (endTime - startTime) + "ms");
  }
}