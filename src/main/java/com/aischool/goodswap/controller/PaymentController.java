package com.aischool.goodswap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aischool.goodswap.DTO.request.payment.PaymentInfoRequestDTO;
import com.aischool.goodswap.DTO.response.payment.PaymentInfoResponseDTO;
import com.aischool.goodswap.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;

    @PostMapping("/payment/{goodsId}")
    public ResponseEntity<PaymentInfoResponseDTO> getPaymentInfo(
        @PathVariable Long goodsId,
        @RequestBody PaymentInfoRequestDTO paymentInfoRequest) {
        
        String user = paymentInfoRequest.getUser();
        
        PaymentInfoResponseDTO paymentInfo = paymentService.getPaymentInfo(user, goodsId);
        
        return ResponseEntity.ok(paymentInfo);
    }

}
