package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointService (PointRepository pointRepository) {
       this.pointRepository = pointRepository;;
    }

    public UserPoint chargePoint(long userId, long amount) {
        long currentPoint = pointRepository.selectById(userId).point();
        long chargedPoint = currentPoint+ amount;
        UserPoint updatedPoint = pointRepository.save(userId, chargedPoint);
        return updatedPoint;
    }
}
