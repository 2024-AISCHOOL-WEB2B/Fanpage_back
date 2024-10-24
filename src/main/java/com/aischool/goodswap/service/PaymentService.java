package com.aischool.goodswap.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.aischool.goodswap.DTO.response.payment.PaymentInfoResponseDTO;
import com.aischool.goodswap.domain.CreditCard;
import com.aischool.goodswap.domain.DeliveryAddress;
import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.repository.payment.CardRepository;
import com.aischool.goodswap.repository.payment.DeliveryAddressRepository;
import com.aischool.goodswap.repository.payment.GoodsRepository;
import com.aischool.goodswap.repository.payment.PointRepository;

@Service
public class PaymentService {
    
    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private CardRepository cardRepository;
    
    @Autowired
    private GoodsRepository goodsRepository;

    @Transactional(readOnly = true)
    public PaymentInfoResponseDTO getPaymentInfo(String user, Long id) {

        Integer totalPoints = pointRepository.findTotalPointsByUserEmail(user);
        int point = (totalPoints != null) ? totalPoints : 0;

        DeliveryAddress deliveryAddress = deliveryAddressRepository.findFirstByUserEmail(user);
        String address = (deliveryAddress != null) ? deliveryAddress.getAddress() : "배송지 정보 없음";

        CreditCard card = cardRepository.findFirstByUserEmail(user);
        String cardNum = (card != null) ? card.getCardNumber() : "카드 정보 없음";
        String expiredAt = (card != null) ? card.getExpiredAt() : "유효기간 없음";
        String cardCvc = (card != null) ? card.getCardCvc() : "CVC 정보 없음";

        Goods goods = goodsRepository.findById(id).orElse(null);
        String goodsName = (goods != null) ? goods.getName() : "상품 정보 없음";
        int goodsPrice = (goods != null) ? goods.getPrice() : 0;
        int shippingFee = (goods != null) ? goods.getFee() : 0;
    
        return new PaymentInfoResponseDTO(
            user,
            point,
            address,
            cardNum,
            expiredAt,
            cardCvc,
            goodsName, 
            goodsPrice,
            shippingFee
        );


    }

}
