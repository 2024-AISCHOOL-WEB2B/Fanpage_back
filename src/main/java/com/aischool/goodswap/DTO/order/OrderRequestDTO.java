package com.aischool.goodswap.DTO.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class OrderRequestDTO {

  private String user;
  private Long goods; // Goods 객체 대신 goodsIdx로 받기
  private int quantity;
  private int totalAmount;
  private int discountAmount;
  private String payMethod;
  private String deliveryAddr;
  private String deliveryDetailAddr;
  private String postCode;
  private String receiverName;
  private String receiverPhone;
  private String request;
  private String orderStatus = "ready";

  @Builder
  public OrderRequestDTO(String user, Long goods, int quantity, int totalAmount, int discountAmount, String payMethod, String deliveryAddr, String deliveryDetailAddr, String postCode, String receiverName, String receiverPhone, String request, String orderStatus) {
    this.user = user;
    this.goods = goods;
    this.quantity = quantity;
    this.totalAmount = totalAmount;
    this.discountAmount = discountAmount;
    this.payMethod = payMethod;
    this.deliveryAddr = deliveryAddr;
    this.deliveryDetailAddr = deliveryDetailAddr;
    this.postCode = postCode;
    this.receiverName = receiverName;
    this.receiverPhone = receiverPhone;
    this.request = request;
    this.orderStatus = orderStatus;
  }
  public void updateDiscountAmount(int discountAmount) {
    this.discountAmount = discountAmount;
  }
}