package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.OrderRequestDTO;
import com.aischool.goodswap.DTO.order.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.Order;
import com.aischool.goodswap.exception.order.GoodsException;
import com.aischool.goodswap.exception.order.OrderException;
import com.aischool.goodswap.exception.order.PaymentException;
import com.aischool.goodswap.service.order.PaymentService;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.CompletableFuture;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.response.AccessToken;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Payment", description = "결제 관련 API 정보")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class PaymentController {

  @Autowired
  private PaymentService paymentService;
  private IamportClient iamportClient;

  @Value("${imp_api.key}")
  private String apiKey;

  @Value("${imp_api.secretkey}")
  private String secretKey;

  @PostConstruct
  public void init() {
      this.iamportClient = new IamportClient(apiKey, secretKey);
  }

  @ResponseBody
  @GetMapping("/orders")
  @Operation(summary = "주문 목록 확인", description = "회원의 주문 목록을 확인하는 API")
  public ResponseEntity<Object> getUserOrders(@RequestHeader String userEmail) {
    try{
      List<Order> orders = paymentService.getUserOrders(userEmail);
      return ResponseEntity.ok(orders);
    } catch (RuntimeException e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);  // 500 Internal Server Error
    }
  }

  @PostMapping("/pre-registration")
  @Operation(summary = "결제 사전등록", description = "결제 검증을 위해 회원의 주문 정보를 저장하는 API")
  public ResponseEntity<Object> saveOrderInfo(@RequestBody OrderRequestDTO orderRequestDTO) {
    try{
      String merchantUid = paymentService.registerOrder(orderRequestDTO);
      return ResponseEntity.ok(merchantUid);
    } catch (IllegalArgumentException | OrderException e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (GoodsException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (RuntimeException e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/info/{goodsId}")
  @Operation(summary = "결제정보", description = "결제시 회원에게 보여줄 정보를 송신하는 API")
  public CompletableFuture<ResponseEntity<PaymentInfoResponseDTO>> getPaymentInfo(
    @PathVariable Long goodsId,
    @RequestHeader String userEmail) {
    return paymentService.getPaymentInfo(userEmail, goodsId)
      .thenApply(ResponseEntity::ok)
      .exceptionally(e -> {
        log.error("서버 오류가 발생했습니다.", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
      });
  }

  @GetMapping("/token")
  public ResponseEntity<String> getIamportToken() {
    try {
      IamportResponse<AccessToken> authResponse = iamportClient.getAuth();
      String accessToken = authResponse.getResponse().getToken();  // getResponse()나 token이 null일 경우 NPE 발생
      return ResponseEntity.ok(accessToken);
    } catch (IamportResponseException | IOException e) {
      log.error("아임포트 토큰을 가져오는 중 오류가 발생했습니다.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("아임포트 토큰을 가져오는 중 오류가 발생했습니다.");
    } catch (NullPointerException e) {
      log.error("응답 구조가 올바르지 않습니다.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("응답 구조가 올바르지 않습니다.");
    }
  }

  @PostMapping("/validate/{imp_uid}")
  @Operation(summary = "결제 검증", description = "회원의 주문 정보와 실제 결과를 비교하여 결제에 문제가 없었는지 검증하는 API")
  public ResponseEntity<Object> validatePayment(@PathVariable String imp_uid, @RequestBody String userEmail) {
    IamportResponse<Payment> payment;
    try {
      payment = iamportClient.paymentByImpUid(imp_uid);
      paymentService.validateOrderPayment(payment, userEmail);
      return ResponseEntity.ok(payment);
    }
     catch (IllegalArgumentException e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (PaymentException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (IamportResponseException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("아이엠포트 응답 오류: " + e.getMessage());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("입출력 오류: " + e.getMessage());
    }
  }

  @PostMapping("/cancel/{merchantUid}")
  @Operation(summary = "결제 취소", description = "주문을 취소하는 API")
  public ResponseEntity<String> cancelOrder(@PathVariable String merchantUid, @RequestBody String userEmail) {
    try{
      paymentService.cancelOrder(merchantUid, userEmail);
      return ResponseEntity.ok("주문이 취소되었습니다.");
    } catch (IllegalArgumentException | OrderException e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (GoodsException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (RuntimeException e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}