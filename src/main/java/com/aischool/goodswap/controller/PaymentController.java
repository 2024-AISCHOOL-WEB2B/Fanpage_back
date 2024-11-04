package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.AddressInfoResponseDTO;
import com.aischool.goodswap.DTO.OrderRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoRequestDTO;
import com.aischool.goodswap.DTO.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.*;
import com.aischool.goodswap.service.PaymentService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/info/{goodsId}")
    public CompletableFuture<ResponseEntity<PaymentInfoResponseDTO>> getPaymentInfo(
      @PathVariable Long goodsId,
      @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {

        String user = paymentInfoRequest.getUser().getUserEmail();

        return paymentService.getPaymentInfo(user, goodsId)
          .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/addr")
    public ResponseEntity<List<AddressInfoResponseDTO>> getAddressInfo(@RequestBody PaymentInfoRequestDTO paymentInfoRequest){

        String user = paymentInfoRequest.getUser().getUserEmail();

        List<AddressInfoResponseDTO> addressInfo = paymentService.getAddressInfo(user);
        return ResponseEntity.ok(addressInfo);
    }

    @PostMapping("/addr")
    public ResponseEntity<List<AddressInfoResponseDTO>> addDeliveryAddress(
      @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {

        List<AddressInfoResponseDTO> addressInfo = paymentService.addDeliveryAddress(paymentInfoRequest);
        return ResponseEntity.ok(addressInfo);
    }

    @DeleteMapping("/addr/{addrId}")
    public ResponseEntity<List<AddressInfoResponseDTO>> removeDeliveryAddress(
      @PathVariable Long addrId, @RequestBody PaymentInfoRequestDTO
      paymentInfoRequest){

        String user = paymentInfoRequest.getUser().getUserEmail();
        List<AddressInfoResponseDTO> addressInfo = paymentService.removeDeliveryAddress(user, addrId);
        return ResponseEntity.ok(addressInfo);
//        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/addr/{addrId}")
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
    public ResponseEntity<List<Map<String, String>>> addCreditCard(@RequestBody CreditCard CreditCard) {
        List<Map<String, String>> cardInfo = paymentService.addCreditCard(CreditCard);
        return ResponseEntity.ok(cardInfo);
    }

    @DeleteMapping("/card/{cardId}")
    public ResponseEntity<List<Map<String, String>>> removeCreditCard(@PathVariable Long cardId, @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {
        String userEmail = paymentInfoRequest.getUser().getUserEmail();
        List<Map<String, String>> cardInfo = paymentService.removeCreditCard(userEmail, cardId);
        return ResponseEntity.ok(cardInfo);
    }

    @PostMapping("/payment/pre-registration")
    public ResponseEntity<String> saveOrderInfo(@RequestBody OrderRequestDTO orderRequestDTO) {
        int quantity = orderRequestDTO.getQuantity();

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("수량이 유효하지 않습니다.");
        }

        // OrderRequestDTO를 Order로 변환하여 저장
        String merchantUid = paymentService.saveOrderInfo(orderRequestDTO);
        return ResponseEntity.ok(merchantUid);
    }
}