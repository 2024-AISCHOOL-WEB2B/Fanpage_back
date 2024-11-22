package com.aischool.goodswap.service.order;

import com.aischool.goodswap.DTO.order.DeliveryAddressRequestDTO;
import com.aischool.goodswap.domain.DeliveryAddress;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.DeliveryAddressRepository;
import com.aischool.goodswap.DTO.order.DeliveryAddressResponseDTO;
import com.aischool.goodswap.repository.UserRepository;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DeliveryAddressService{

  @Autowired
  private DeliveryAddressRepository deliveryAddressRepository;

  @Autowired
  private UserRepository userRepository;

  // 회원 이메일을 기준으로 등록된 주소를 가져옴
  @Transactional(readOnly = true)
  public List<DeliveryAddressResponseDTO> getInfo(String userEmail) {
    try {
      // 사용자의 모든 배송 주소 조회
      List<DeliveryAddress> deliveryAddresses = deliveryAddressRepository.findAllByUser_UserEmail(userEmail);
      List<DeliveryAddressResponseDTO> addressInfo = new ArrayList<>();

      // 각 주소를 DTO 객체로 변환하여 리스트에 추가
      for (DeliveryAddress address : deliveryAddresses) {
        DeliveryAddressResponseDTO dto = DeliveryAddressResponseDTO.builder()
          .id(address.getId())
          .address(address.getDeliveryAddr())
          .deliveryDetailAddr(address.getDeliveryDetailAddr())
          .postCode(address.getPostCode())
          .userEmail(address.getUser().getUserEmail())
          .userName(address.getUserName())
          .userPhone(address.getUserPhone())
          .build();
        addressInfo.add(dto);
      }
      return addressInfo;
    } catch (Exception e) {
      log.error("Error fetching delivery addresses for user {}: {}", userEmail, e.getMessage(), e);
      throw new RuntimeException("Error fetching delivery addresses", e);
    }
  }

  // 회원 이메일을 기준으로 등록된 주소를 제거
  @Transactional
  public List<DeliveryAddressResponseDTO> removeInfo(String userEmail, Long addrId) {
    try {
      // 주소 ID와 사용자 이메일을 기준으로 주소 조회
      DeliveryAddress address = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, userEmail)
        .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));  // 예외조건 설정

      // 주소 삭제
      deliveryAddressRepository.delete(address);
      return getInfo(userEmail);
    } catch (Exception e) {
      log.error("Error removing delivery address with ID: {} for user {}: {}", addrId, userEmail, e.getMessage(), e);
      throw new RuntimeException("Error removing delivery address", e);
    }
  }

  // 회원 이메일을 기준으로 등록된 주소 수정
  @Transactional
  public List<DeliveryAddressResponseDTO> updateInfo(Long addrId, DeliveryAddressRequestDTO requestDTO) {
    try {
      // 주소 ID와 사용자 이메일을 기준으로 주소 조회
      DeliveryAddress existingAddress = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, requestDTO.getUserEmail())
        .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));

      // 주소 수정
      existingAddress.builder()
        .deliveryAddr(requestDTO.getAddress().getAddress())
        .deliveryDetailAddr(requestDTO.getAddress().getDeliveryDetailAddr())
        .postCode(requestDTO.getAddress().getPostCode())
        .userName(requestDTO.getUserName())
        .userPhone(requestDTO.getUserPhone())
        .build();

      // 수정된 주소 저장
      deliveryAddressRepository.save(existingAddress);
      return getInfo(requestDTO.getUserEmail());
    } catch (Exception e) {
      log.error("Error updating delivery address with ID: {} for user {}: {}", addrId, requestDTO.getUserEmail(), e.getMessage(), e);
      throw new RuntimeException("Error updating delivery address", e);
    }
  }

  // 회원 이메일을 기준으로 주소 추가 등록
  @Transactional
  public List<DeliveryAddressResponseDTO> addInfo(DeliveryAddressRequestDTO requestDTO) {
    try {
      // 사용자의 정보를 이메일로 조회
      User user = userRepository.findByUserEmail(requestDTO.getUserEmail())
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

      // 새 배송 주소 객체 생성
      DeliveryAddress newAddress = DeliveryAddress.builder()
        .deliveryAddr(requestDTO.getAddress().getAddress())
        .deliveryDetailAddr(requestDTO.getAddress().getDeliveryDetailAddr())
        .postCode(requestDTO.getAddress().getPostCode())
        .user(user)
        .userName(requestDTO.getUserName())
        .userPhone(requestDTO.getUserPhone())
        .build();

      // 새 주소 저장
      deliveryAddressRepository.save(newAddress);
      return getInfo(user.getUserEmail());
    } catch (Exception e) {
      log.error("Error adding new delivery address for user {}: {}", requestDTO.getUserEmail(), e.getMessage(), e);
      throw new RuntimeException("Error adding new delivery address", e);
    }
  }
}