package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepository {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointRepository(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    public UserPoint save(long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }

    public UserPoint selectById(long userId) {
        return userPointTable.selectById(userId);
    }

    public void saveHistory(long userId, long amount, TransactionType type) {
        pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
    }
}
