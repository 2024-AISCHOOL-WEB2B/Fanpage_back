package com.aischool.goodswap.service.order;

import com.aischool.goodswap.domain.Point;
import com.aischool.goodswap.domain.User;
import com.aischool.goodswap.repository.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {

  @Autowired
  private PointRepository pointRepository;

  public void updatePoints(User user, String pointType, String reason, int point) {
    Point newPoint = Point.builder()
      .user(user)
      .pointType(pointType)
      .reason(reason)
      .point(point)
      .build();

    // 포인트 감소 처리
    pointRepository.save(newPoint);
  }


}
