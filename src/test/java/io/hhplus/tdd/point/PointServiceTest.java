package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class PointServiceTest {

    private PointRepository pointRepository;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        pointService = new PointService(pointRepository);
    }

    @Test
    @DisplayName("포인트 충전 성공 테스트")
    void charge_point_success() {

        // Given
        long userId = 1L;
        long amount = 5000L;
        UserPoint expected = new UserPoint(userId, amount, System.currentTimeMillis());

        when(pointRepository.selectById(userId)).thenReturn(UserPoint.empty(userId));
        when(pointRepository.save(userId, amount)).thenReturn(expected);

        // When
        UserPoint chargePoint = pointService.chargePoint(userId, amount);

        // Then
        assertEquals(amount, chargePoint.point());
    }

    @Test
    @DisplayName("포인트 충전 실패 테스트 - 입력금액이 음수인 경우")
    void charge_point_fail_if_minus() {

        // Given
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

    @Test
    @DisplayName("포인트 조회 성공 테스트")
    void select_point_success(){

        // Given
        long userId = 1L;

        UserPoint expected = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(pointRepository.selectById(userId)).thenReturn(expected);

        // When
        UserPoint actual = pointService.selectPoint(userId);

        // Then
        assertEquals(expected.point(), actual.point());
    }

    @Test
    @DisplayName("포인트 사용 성공 테스트")
    void use_point_success(){

        // Given
        long userId = 1L;
        long amount = 3000L;

        UserPoint userInfo = new UserPoint(userId, 5000L, System.currentTimeMillis());
        when(pointRepository.selectById(userId)).thenReturn(userInfo);
        long beforePoint = userInfo.point() - amount;
        when(pointRepository.save(userId, beforePoint)).thenReturn(
                new UserPoint(userId, beforePoint, System.currentTimeMillis())
        );

        // When
        UserPoint afterPoint = pointService.usePoint(userId, amount);

        // Then
        // 포인트 사용 로직 테스트
        assertEquals(beforePoint, afterPoint.point());
        // 사용 후에 대한 정보 DB에 저장 테스트
        verify(pointRepository, times(1)).save(userId, afterPoint.point());
        // 사용 기록 DB에 저장 테스트
        verify(pointRepository, times(1)).saveHistory(userId, amount, TransactionType.USE);
    }

    @Test
    @DisplayName("포인트 사용 실패 테스트 - 잔액 부족인 경우")
    void use_point_fail_if_no_money() {

        // Given
        long userId = 1L;

        when(pointRepository.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.usePoint(userId, 2000L);
        });

    }

    @Test
    @DisplayName("포인트 사용 실패 테스트 - 마이너스 금액인 경우")
    void use_point_fail_if_minus(){

        // Given
        long userId = 1L;

        when(pointRepository.selectById(userId)).thenReturn(UserPoint.empty(userId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.usePoint(userId, -1000L);
        });
    }

    @Test
    @DisplayName("특정 유저의 포인트 내역 조회 성공 테스트")
    void select_user_point_history_Success(){

        // Given
        long userId = 1L;

        // 테스트할 리스트
        List<PointHistory> mockHistories = List.of(
                new PointHistory(1L, userId, 3000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 900L, TransactionType.USE, System.currentTimeMillis())
        );

        // stub
        when(pointRepository.findAllHistoriesByUserId(userId)).thenReturn(mockHistories);

        // When
        List<PointHistory> histories = pointService.getPointHistory(userId);

        // Then
        assertEquals(2, histories.size()); // 크기 검증
        assertEquals(mockHistories, histories); // 내용 검증
    }
}
