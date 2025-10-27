package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointServiceTest {

    @Test
    @DisplayName("포인트 충전 성공 테스트")
    void charge_point_success() {

        // Given
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();

        PointRepository pointRepository = new PointRepository(userPointTable, pointHistoryTable);
        PointService pointService = new PointService(pointRepository);

        long userId = 1L;
        long amount = 5000L;

        // When
        UserPoint chargePoint = pointService.chargePoint(userId, amount);

        // Then
        assertEquals(amount, chargePoint.point());


    }
}
