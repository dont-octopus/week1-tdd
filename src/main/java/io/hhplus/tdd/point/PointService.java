package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointService (PointRepository pointRepository) {
       this.pointRepository = pointRepository;;
    }

    public UserPoint chargePoint(long userId, long amount) {
        if(amount < 0) {
            throw new IllegalArgumentException("음수는 입력할 수 없습니다.");
        }

        long currentPoint = pointRepository.selectById(userId).point();
        long chargedPoint = currentPoint+ amount;
        UserPoint updatedPoint = pointRepository.save(userId, chargedPoint);
        pointRepository.saveHistory(userId, amount, TransactionType.CHARGE); // 기록 저장

        return updatedPoint;
    }

    public UserPoint selectPoint(long userId){
        UserPoint currentInfo = pointRepository.selectById(userId);
        return currentInfo;
    }

    public UserPoint usePoint(long userId, long amount) {
        // 사용한 포인트 계산
        UserPoint beforePoint = pointRepository.selectById(userId);
        long afterPoint = beforePoint.point() - amount;
        // 사용 후 정보 DB에 저장
        UserPoint updatedInfo = pointRepository.save(userId, afterPoint);
        // 사용 내역 DB에 저장
        pointRepository.saveHistory(userId, amount, TransactionType.USE);

        return updatedInfo;
    }
}
