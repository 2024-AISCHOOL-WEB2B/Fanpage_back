package com.aischool.goodswap.controller;

import com.aischool.goodswap.domain.AddressInfoResponseDTO;
import com.aischool.goodswap.domain.PaymentInfoRequestDTO;
import com.aischool.goodswap.domain.PaymentInfoResponseDTO;
import com.aischool.goodswap.service.PaymentService;

import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payment/{goodsId}")
    public CompletableFuture<ResponseEntity<PaymentInfoResponseDTO>> getPaymentInfo(
      @PathVariable Long goodsId,
      @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {

        String user = paymentInfoRequest.getUser();

        return paymentService.getPaymentInfo(user, goodsId)
          .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/addr")
    public ResponseEntity<List<AddressInfoResponseDTO>> getAddressInfo(@RequestBody PaymentInfoRequestDTO paymentInfoRequest){

        String user = paymentInfoRequest.getUser();

        List<AddressInfoResponseDTO> addressInfo = paymentService.getAddressInfo(user);
        return ResponseEntity.ok(addressInfo);
    }

    @DeleteMapping("/addr/{addrId}")
    public ResponseEntity<List<AddressInfoResponseDTO>> removeDeliveryAddress(
      @PathVariable Long addrId, @RequestBody PaymentInfoRequestDTO
      paymentInfoRequest){

        String user = paymentInfoRequest.getUser();
        List<AddressInfoResponseDTO> addressInfo = paymentService.removeDeliveryAddress(user, addrId);
        return ResponseEntity.ok(addressInfo);
    }

    @PutMapping("/addr/{addrId}")
    public ResponseEntity<List<AddressInfoResponseDTO>> updateDeliveryAddress(
      @PathVariable Long addrId, @RequestBody PaymentInfoRequestDTO
      paymentInfoRequest){

        List<AddressInfoResponseDTO> addressInfo = paymentService.updateDeliveryAddress(addrId, paymentInfoRequest);
        return ResponseEntity.ok(addressInfo);
    }


}
