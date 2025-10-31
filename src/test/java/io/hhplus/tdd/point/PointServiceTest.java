package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


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

    @Test
    @DisplayName("포인트 충전 내역 저장 테스트")
    void test_save_history(){

        // Given
        PointRepository pointRepository = mock(PointRepository.class);
        PointService pointService = new PointService(pointRepository);

        long userId = 1L;
        long amount = 5000L;

        // 1NPE 방지용 stub: selectById(1L)가 호출되면, 빈 객체를 반환
        when(pointRepository.selectById(userId)).thenReturn(UserPoint.empty(userId));
        // NPE 방지용 stub: save(1L, 5000L)가 호출되면, 5000점짜리 객체를 반환
        when(pointRepository.save(userId, amount)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // When
        pointService.chargePoint(userId, amount);

        // Then
        // saveHistory가 1번 호출되었는지 검증
        verify(pointRepository, times(1)).saveHistory(userId, amount, TransactionType.CHARGE);
    }
}
