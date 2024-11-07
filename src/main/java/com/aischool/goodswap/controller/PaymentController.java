package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.AddressInfoResponseDTO;
import com.aischool.goodswap.DTO.OrderRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.*;
import com.aischool.goodswap.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Slf4j
@Tag(name = "Payment", description = "결제 관련 API 정보")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private IamportClient iamportClient;

    @Value("${IMP_API_KEY}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @GetMapping("/info/{goodsId}")
    @Operation(summary = "결제정보", description = "결제시 회원에게 보여줄 정보를 송신하는 API")
    public CompletableFuture<ResponseEntity<PaymentInfoResponseDTO>> getPaymentInfo(
      @PathVariable Long goodsId,
      @RequestHeader String userEmail) {

        return paymentService.getPaymentInfo(userEmail, goodsId)
          .thenApply(ResponseEntity::ok)
          .exceptionally(e -> {
              log.error("Error getting payment info", e);
              return ResponseEntity.status(500).body(null); // 서버 오류 응답
          });
    }

    @GetMapping("/addr")
    @Operation(summary = "배송지 정보", description = "회원의 전체 배송지 정보를 전달하는 API")
    public ResponseEntity<List<AddressInfoResponseDTO>> getAddressInfo(@RequestHeader String userEmail){

        List<AddressInfoResponseDTO> addressInfo = paymentService.getAddressInfo(userEmail);
        return ResponseEntity.ok(addressInfo);
    }

    @PostMapping("/addr")
    @Operation(summary = "배송지 추가", description = "회원의 배송지를 추가하고 다시 전체 배송지 정보를 전달하는 API")
    public ResponseEntity<List<AddressInfoResponseDTO>> addDeliveryAddress(
      @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {

        List<AddressInfoResponseDTO> addressInfo = paymentService.addDeliveryAddress(paymentInfoRequest);
        return ResponseEntity.ok(addressInfo);
    }

    @DeleteMapping("/addr/{addrId}")
    @Operation(summary = "배송지 삭제", description = "회원의 특정 배송지 정보를 제거하고 다시 전체 배송지 정보를 전달하는 API")
    public ResponseEntity<List<AddressInfoResponseDTO>> removeDeliveryAddress(
      @PathVariable Long addrId, @RequestBody PaymentInfoRequestDTO
      paymentInfoRequest){

        String user = paymentInfoRequest.getUser().getUserEmail();
        List<AddressInfoResponseDTO> addressInfo = paymentService.removeDeliveryAddress(user, addrId);
        return ResponseEntity.ok(addressInfo);
//        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/addr/{addrId}")
    @Operation(summary = "배송지 수정", description = "회원의 특정 배송지 정보를 수정하고 다시 전체 배송지 정보를 전달하는 API")
    public ResponseEntity<List<AddressInfoResponseDTO>> updateDeliveryAddress(
      @PathVariable Long addrId, @RequestBody PaymentInfoRequestDTO
      paymentInfoRequest){

        List<AddressInfoResponseDTO> addressInfo = paymentService.updateDeliveryAddress(addrId, paymentInfoRequest);
        return ResponseEntity.ok(addressInfo);
    }
//         예시
//    {
//        "deliveryAddr" : "서울",
//            "deliveryDetailAddr" : "광명",
//            "postCode" : "12125",
//            "user" : "user",
//            "userName" : "누굴까",
//           "userPhone" : "000-1381-7222"
//    }

    @PostMapping("/card")
    @Operation(summary = "카드 등록", description = "회원의 카드 정보를 등록하고 다시 전체 카드 정보를 전달하는 API")
    public ResponseEntity<List<Map<String, String>>> addCreditCard(@RequestBody CreditCard creditCard) {
        try {
            List<Map<String, String>> cardInfo = paymentService.addCreditCard(creditCard);
            return ResponseEntity.ok(cardInfo);
        } catch (Exception e) {
            log.error("Error adding credit card", e);
            return ResponseEntity.badRequest().body(null); // 잘못된 요청 응답
        }
    }

//    예시
//    {
//        "cardNumber" : "0000-1212-2282-3333",
//      "expiredAt" : "01/01",
//      "cardCvc" : "111",
//      "user" : "user"
//    }

    @DeleteMapping("/card/{cardId}")
    @Operation(summary = "카드 삭제", description = "회원의 특정 카드 정보를 제거하고 다시 전체 카드 정보를 전달하는 API")
    public ResponseEntity<List<Map<String, String>>> removeCreditCard(@PathVariable Long cardId, @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser().getUserEmail();
        try {
            List<Map<String, String>> cardInfo = paymentService.removeCreditCard(userEmail, cardId);
            return ResponseEntity.ok(cardInfo);
        } catch (Exception e) {
            log.error("Error removing credit card", e);
            return ResponseEntity.badRequest().body(null); // 잘못된 요청 응답
        }
    }

    @PostMapping("/payment/pre-registration")
    @Operation(summary = "결제 사전등록", description = "결제 검증을 위해 회원의 주문 정보를 저장하는 API")
    public ResponseEntity<String> saveOrderInfo(@RequestBody OrderRequestDTO orderRequestDTO) {
        int quantity = orderRequestDTO.getQuantity();

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("수량이 유효하지 않습니다.");
        }

        // OrderRequestDTO를 Order로 변환하여 저장
        String merchantUid = paymentService.saveOrderInfo(orderRequestDTO);
        return ResponseEntity.ok(merchantUid);
    }

//    예시
//    {
//        "merchantUid" : "20241103-57c50e149d1041f3806afd0efd4915da",
//      "user" : "user",
//      "goods" : 1,
//      "quantity" : 5,
//      "totalAmount" : 175000,
//      "discountAmount" : 25000,
//      "deliveryAddr" : "목포시",
//      "deliveryDetailAddr" : "용해동",
//      "postCode" : "12345",
//      "receiverName" : "방찬혁",
//      "receiverPhone" : "000-1221-3535",
//      "request" : "알잘딱 부탁드립니다",
//      "orderStatus" : "ready"
//    }

    @PostMapping("/payment/validate/{imp_uid}")
    @Operation(summary = "결제 검증", description = "회원의 주문 정보와 실제 결과를 비교하여 결제에 문제가 없었는지 검증하는 API")
    public ResponseEntity<IamportResponse<Payment>> validateIamport(@PathVariable String imp_uid, @RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);
            log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());
            paymentService.processPaymentDone(orderRequestDTO);
            return ResponseEntity.ok(payment);
        } catch (IamportResponseException | IOException e) {
            log.error("Error validating payment", e);
            return ResponseEntity.status(500).body(null); // 서버 오류 응답
        }
    }
}