package com.moca.cardwash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据统计 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsVO {

    /**
     * 总核销数量
     */
    private Integer totalWriteOff;

    /**
     * 总营业额
     */
    private String totalRevenue;

    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 核销数量列表
     */
    private List<Integer> writeOffCounts;

    /**
     * 营业额列表
     */
    private List<String> revenues;

    /**
     * 每日统计数据
     */
    @Data
    @NoArgsConstructor
    public static class DailyStats {
        private String date;
        private Integer writeOffCount;
        private BigDecimal revenue;
    }
}
