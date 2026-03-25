package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 数据统计请求
 */
@Data
public class StatsRequest {

    /**
     * 开始日期 (yyyy-MM-dd)
     */
    private String startDate;

    /**
     * 结束日期 (yyyy-MM-dd)
     */
    private String endDate;
}
