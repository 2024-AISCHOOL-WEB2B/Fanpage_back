package com.aischool.goodswap.service.order;

import com.aischool.goodswap.domain.Goods;
import com.aischool.goodswap.exception.order.PaymentException;
import com.aischool.goodswap.exception.order.GoodsException;
import com.aischool.goodswap.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoodsService {

  @Autowired
  private GoodsRepository goodsRepository;

  public Goods findGoods(Long goodsId) {
    return goodsRepository.findById(goodsId)
      .orElseThrow(() -> {
        log.info("상품 ID " + goodsId + "에 해당하는 상품을 찾을 수 없습니다.");
        return new PaymentException(GoodsException.GOODS_NOT_FOUND);
      });
  }


  public void decreaseGoodsStockWithLock(Goods goods, int quantity) {
    try {
      // Pessimistic Lock을 사용하여 동시성 문제를 방지
      Goods lockedGoods = goodsRepository.lockGoodsForUpdate(goods.getId());

      if (lockedGoods.getGoodsStock() < quantity) {
        log.info("상품 재고가 부족합니다. 상품 재고: " + lockedGoods.getGoodsStock() + ", 주문 수량: " + quantity);
        // GoodsException을 사용하여 예외 처리
        throw new GoodsException(GoodsException.GOODS_STOCK_INSUFFICIENT);
      }
      goodsRepository.updateGoodsQuantity(goods.getId(), quantity); // 변경된 재고 저장
    } catch (GoodsException e) {
      // GoodsException 발생 시 처리
      PaymentException.logErrorAndThrow(GoodsException.GOODS_STOCK_INSUFFICIENT, e);
      throw e;
    } catch (RuntimeException e) {
      // 일반적인 예외 발생 시 처리
      PaymentException.logErrorAndThrow(GoodsException.GOODS_PROCESS_ERROR, e);
      throw new GoodsException(GoodsException.GOODS_PROCESS_ERROR);
    }
  }


  public void restoreGoodsStockWithLock(Goods goods, int quantity) {
    try {
      goodsRepository.restoreGoodsQuantity(goods.getId(), quantity);
    } catch (RuntimeException e) {
      // 예외 발생 시 GoodsException을 던져서 처리
      PaymentException.logErrorAndThrow(GoodsException.GOODS_RESTORE_FAILED, e);
      throw new GoodsException(GoodsException.GOODS_RESTORE_FAILED);
    }
  }


}
