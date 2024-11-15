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
  public List<DeliveryAddressResponseDTO> getInfo(String user) {
    List<DeliveryAddress> deliveryAddresses = deliveryAddressRepository.findAllByUser_UserEmail(
      user);
    List<DeliveryAddressResponseDTO> addressInfo = new ArrayList<>();

    for (DeliveryAddress address : deliveryAddresses) {
      // 빌더 패턴을 사용하여 AddressInfoResponseDTO 객체를 생성
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

    log.info("addressInfo Info: {}", addressInfo);

    return addressInfo;
  }

  // 회원 이메일을 기준으로 등록된 주소를 제거
  @Transactional
  public List<DeliveryAddressResponseDTO> removeInfo(String user, Long addrId) {
    DeliveryAddress address = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId, user)
      .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));  // 예외조건 설정
    deliveryAddressRepository.delete(address);
    return getInfo(user);
  }

  // 회원 이메일을 기준으로 등록된 주소 수정
  @Transactional
  public List<DeliveryAddressResponseDTO> updateInfo(Long addrId,
    DeliveryAddressRequestDTO deliveryAddressRequestDTO) {

    String userEmail = deliveryAddressRequestDTO.getUserEmail();
    String newAddress = deliveryAddressRequestDTO.getAddress().getAddress();
    String newDetailAddress = deliveryAddressRequestDTO.getAddress().getDeliveryDetailAddr();
    String newPostCode = deliveryAddressRequestDTO.getAddress().getPostCode();
    String newUserName = deliveryAddressRequestDTO.getUserName();
    String newUserPhone = deliveryAddressRequestDTO.getUserPhone();

    // 기존 주소 엔티티 가져오기
    DeliveryAddress existingAddress = deliveryAddressRepository.findByIdAndUser_UserEmail(addrId,
        userEmail)
      .orElseThrow(() -> new IllegalArgumentException("해당 주소를 찾을 수 없습니다."));

    existingAddress.builder()
      .deliveryAddr(newAddress)
      .deliveryDetailAddr(newDetailAddress)
      .postCode(newPostCode)
      .userName(newUserName)
      .userPhone(newUserPhone)
      .build();

    // 수정된 객체 저장
    deliveryAddressRepository.save(existingAddress);
    return getInfo(userEmail);
  }

  // 회원 이메일을 기준으로 주소 추가 등록
  @Transactional
  public List<DeliveryAddressResponseDTO> addInfo(DeliveryAddressRequestDTO deliveryAddressRequestDTO) {

    User userEmail = userRepository.findByUserEmail(deliveryAddressRequestDTO.getUserEmail()).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    String newAddress = deliveryAddressRequestDTO.getAddress().getAddress();
    String newDetailAddress = deliveryAddressRequestDTO.getAddress().getDeliveryDetailAddr();
    String newPostCode = deliveryAddressRequestDTO.getAddress().getPostCode();
    String newUserName = deliveryAddressRequestDTO.getUserName();
    String newUserPhone = deliveryAddressRequestDTO.getUserPhone();

    // 빌더 패턴을 사용하여 새로운 DeliveryAddress 객체 생성
    DeliveryAddress newDeliveryAddress = DeliveryAddress.builder()
      .deliveryAddr(newAddress)    // 배송지 주소 설정
      .deliveryDetailAddr(newDetailAddress)    // 배송지 주소 설정
      .postCode(newPostCode)    // 배송지 주소 설정
      .user(userEmail) // User 객체 생성
      .userName(newUserName)    // 배송지 주소 설정
      .userPhone(newUserPhone)    // 배송지 주소 설정
      .build();

    // 배송지 저장
    deliveryAddressRepository.save(newDeliveryAddress);
    return getInfo(userEmail.getUserEmail());
  }
}