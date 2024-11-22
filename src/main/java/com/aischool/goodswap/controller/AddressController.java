package com.aischool.goodswap.controller;

import com.aischool.goodswap.DTO.order.DeliveryAddressRequestDTO;
import com.aischool.goodswap.DTO.order.DeliveryAddressResponseDTO;
import com.aischool.goodswap.service.order.DeliveryAddressService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Delivery Address", description = "배송지 관리 API")
@RestController
@RequestMapping("/api/order/addr")
@RequiredArgsConstructor
public class AddressController {

  private final DeliveryAddressService deliveryAddressService;

  @GetMapping
  @Operation(
    summary = "배송지 정보 조회",
    description = "사용자의 이메일을 기반으로 모든 배송지 정보를 조회합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "배송지 조회 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryAddressResponseDTO.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "배송지 정보 조회 중 오류가 발생했습니다.")))
    }
  )
  public ResponseEntity<Object> getAddressInfo(@RequestHeader String userEmail) {
    try {
      List<DeliveryAddressResponseDTO> addressInfo = deliveryAddressService.getInfo(userEmail);
      return ResponseEntity.ok(addressInfo);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 정보 조회 중 오류가 발생했습니다.");
    }
  }

  @PostMapping
  @Operation(
    summary = "배송지 추가",
    description = "새로운 배송지 정보를 추가합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "배송지 추가 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryAddressResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "유효하지 않은 배송지 요청이 포함되어 있습니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "배송지 추가 중 서버 오류가 발생했습니다.")))
    }
  )
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
  @Operation(
    summary = "배송지 삭제",
    description = "특정 ID를 가진 배송지를 삭제합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "배송지 삭제 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryAddressResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "유효하지 않은 주소 ID 또는 사용자 이메일입니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "배송지 삭제 처리 중 서버 오류가 발생했습니다.")))
    }
  )
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
  @Operation(
    summary = "배송지 수정",
    description = "특정 ID를 가진 배송지 정보를 수정합니다.",
    responses = {
      @ApiResponse(responseCode = "200", description = "배송지 수정 성공",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryAddressResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "유효하지 않은 배송지 ID 또는 수정 요청이 잘못되었습니다."))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "배송지 수정 중 서버 오류가 발생했습니다.")))
    }
  )
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
