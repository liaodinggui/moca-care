package com.moca.cardwash.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 今日数据 VO
 */
@Data
@NoArgsConstructor
public class TodayStatsVO {

    /**
     * 总销售量（当天卖出的洗车券数量）
     */
    private Integer totalSales;

    /**
     * 已核销（当天核销的洗车券数量）
     */
    private Integer usedWriteOff;

    /**
     * 营业额（当天卖出的订单金额）
     */
    private String revenue;

    /**
     * 内部类：统计数据（用于 Mapper 返回）
     */
    @Data
    @NoArgsConstructor
    public static class StatsData {
        private Integer count;
    }
}
