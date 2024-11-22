package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.DeliveryAddressRequestDTO;
import com.aischool.goodswap.DTO.order.DeliveryAddressResponseDTO;
import com.aischool.goodswap.service.order.DeliveryAddressService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<Object> getAddressInfo(@RequestHeader String userEmail) {
    try {
      List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.getInfo(userEmail);
      return ResponseEntity.ok(addressInfo);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 정보 조회 중 오류가 발생했습니다.");
    }
  }

  @PostMapping
  @Operation(summary = "배송지 추가", description = "회원의 배송지를 추가하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<Object> addDeliveryAddress(@RequestBody DeliveryAddressRequestDTO deliveryAddressRequestDTO) {
    try {
      List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.addInfo(deliveryAddressRequestDTO);
      return ResponseEntity.ok(addressInfo);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 추가 중 서버 오류가 발생했습니다.");  // 500 Internal Server Error
    }
  }

  @DeleteMapping("/{addrId}")
  @Operation(summary = "배송지 삭제", description = "회원의 특정 배송지 정보를 제거하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<Object> removeDeliveryAddress(
    @PathVariable Long addrId, @RequestBody String userEmail) {
    try {
      List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.removeInfo(userEmail, addrId);
      return ResponseEntity.ok(addressInfo);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 삭제 처리 중 서버 오류가 발생했습니다.");  // 500 Internal Server Error
    }
  }

  @PutMapping("/{addrId}")
  @Operation(summary = "배송지 수정", description = "회원의 특정 배송지 정보를 수정하고 다시 전체 배송지 정보를 전달하는 API")
  public ResponseEntity<Object> updateDeliveryAddress(
    @PathVariable Long addrId, @RequestBody DeliveryAddressRequestDTO deliveryAddressRequestDTO) {
    try {
      List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.updateInfo(addrId, deliveryAddressRequestDTO);
      return ResponseEntity.ok(addressInfo);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 수정 중 서버 오류가 발생했습니다.");  // 500 Internal Server Error
    }
  }
}
