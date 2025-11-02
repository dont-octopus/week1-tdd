package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포인트 조회 API 성공 테스트")
    void select_point_apt_success() throws Exception {

        // Given
        long userId =  1L;
        long amount = 5000L;

        UserPoint expected = new UserPoint(userId, amount, System.currentTimeMillis());
        when(pointService.selectPoint(userId)).thenReturn(expected); // stub

        // When & Then
        mockMvc.perform(get("/point/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    @DisplayName("포인트 충전 API 성공 테스트 -" +
            "2000점을 보유한 유저가 3000점을 충전하여 5000점을 만든다")
    void charge_point_api_success() throws Exception {

        // Given
        long userId = 1L;
        long chargeAmount = 3000L;
        long expectedAmount = 5000L;

        // 기대값
        UserPoint expected = new UserPoint(userId, expectedAmount, System.currentTimeMillis());
        // chargePoint를 실행하면 chargeAmount(3000점)을 충전한다
        when(pointService.chargePoint(userId, chargeAmount)).thenReturn(expected);

        // When & Then
        mockMvc.perform(
                patch("/point/" + userId + "/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        // body에 chargeAmount값을 담아서 JSON으로 변환 후 전달
                        .content(objectMapper.writeValueAsString(chargeAmount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(expectedAmount));
    }

    @Test
    @DisplayName("포인트 충전 API 실패 테스트")
    void charge_point_api_fail_if_minus() throws Exception {

        // Given
        long userId = 1L;
        long amount = -1000L;

        when(pointService.chargePoint(userId, amount))
                .thenThrow(new IllegalArgumentException("음수는 입력할 수 없습니다."));

        // When & Then
        mockMvc.perform(
                patch("/point/" + userId + "/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isInternalServerError())
                // ExceptionHandler 가 500 이기 때문에 500으로 검증
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @DisplayName("포인트 사용 API 성공 테스트" +
            "2000점을 보유한 유저가 500점을 사용하여 1500점이 남는다")
    void use_point_api_success() throws Exception {

        // Given
        long userId = 1L;
        long usePoint = 500L;
        long afterUsePoint = 1500L;

        UserPoint expected = new UserPoint(userId, afterUsePoint, System.currentTimeMillis());
        when(pointService.usePoint(userId, usePoint)).thenReturn(expected);

        // When & Then
        mockMvc.perform(
                patch("/point/" + userId + "/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(afterUsePoint));
    }

    @Test
    @DisplayName("포인트 사용 API 실패 테스트 - 잔액 부족인 경우")
    void use_point_api_fail_if_no_money() throws Exception {
        /*
         유저가 현재 얼마인지는 상관이 없음.
         이유: 해당 부분은 service 에서의 역할과 책임이고, controller 에서는
              "service의 응답을 잘 받아서 기대하는(500 에러) 결과를 잘 반환하는가"
              부분에 대한 역할과 책임을 검증하기 때문.
         */


        // Given
        long userId = 1L;
        long wantUsePoint = 5000L;

        when(pointService.usePoint(userId, wantUsePoint))
                .thenThrow(new IllegalArgumentException("잔액이 부족합니다"));

        // When & Then
        mockMvc.perform(
                patch("/point/" + userId + "/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wantUsePoint)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @DisplayName("포인트 사용 API 실패 테스트 - 음수 입력한 경우")
    void use_point_api_fail_if_minus() throws Exception {

        // Given
        long userId = 1L;
        long wantUsePoint = -5000L;

        when(pointService.usePoint(userId, wantUsePoint))
                .thenThrow(new IllegalArgumentException("음수는 입력할 수 없습니다."));

        // When & Then
        mockMvc.perform(
                        patch("/point/" + userId + "/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wantUsePoint)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @DisplayName("특정 유저의 포인트 내역 조회 API 성공 테스트 ")
    void get_point_history_api_success() throws Exception{

        // Given
        long userId = 1L;

        List<PointHistory> histories = List.of(
                new PointHistory(1L, userId, 7000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 200L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointService.getPointHistory(userId)).thenReturn(histories);

        // When & Then
        mockMvc.perform(get("/point/" + userId + "/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].type").value("USE"));

    }
}
