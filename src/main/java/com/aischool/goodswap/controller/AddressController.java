package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.DeliveryAddressRequestDTO;
import com.aischool.goodswap.DTO.order.DeliveryAddressResponseDTO;
import com.aischool.goodswap.service.order.DeliveryAddressService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Address", description = "배송지 관련 API 정보")
@RestController
@RequestMapping("/api/order/addr")
@RequiredArgsConstructor
public class AddressController {

  private final DeliveryAddressService deliveryAddressService;

  @GetMapping
  @Operation(summary = "배송지 정보", description = "회원의 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<List<DeliveryAddressResponseDTO>> getAddressInfo(@RequestHeader String userEmail) {
    List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.getInfo(userEmail);
    return ResponseEntity.ok(addressInfo);
  }

  @PostMapping
  @Operation(summary = "배송지 추가", description = "회원의 배송지를 추가하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<List<DeliveryAddressResponseDTO>> addDeliveryAddress(@RequestBody DeliveryAddressRequestDTO deliveryAddressRequestDTO) {
    List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.addInfo(deliveryAddressRequestDTO);
    return ResponseEntity.ok(addressInfo);
  }

  @DeleteMapping("/{addrId}")
  @Operation(summary = "배송지 삭제", description = "회원의 특정 배송지 정보를 제거하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<List<DeliveryAddressResponseDTO>> removeDeliveryAddress(
    @PathVariable Long addrId, @RequestBody String userEmail) {
    List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.removeInfo(userEmail, addrId);
    return ResponseEntity.ok(addressInfo);
  }

  @PutMapping("/{addrId}")
  @Operation(summary = "배송지 수정", description = "회원의 특정 배송지 정보를 수정하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<List<DeliveryAddressResponseDTO>> updateDeliveryAddress(
    @PathVariable Long addrId, @RequestBody DeliveryAddressRequestDTO deliveryAddressRequestDTO) {
    List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.updateInfo(addrId, deliveryAddressRequestDTO);
    return ResponseEntity.ok(addressInfo);
  }
}
