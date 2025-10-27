package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("포인트 충전 실패 테스트 - 입력금액이 음수인 경우")
    void charge_point_fail_if_minus() {

        // Given
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();

        PointRepository pointRepository = new PointRepository(userPointTable, pointHistoryTable);
        PointService pointService = new PointService(pointRepository);

        long userId = 1L;
        long amount = -5000L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargePoint(userId, amount);
        });



    }
}
