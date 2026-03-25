package com.moca.cardwash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 订单统计响应
 */
@Data
@Builder
public class OrderStatsVO {

    /**
     * 待付款订单数
     */
    private Integer pendingCount;

    /**
     * 使用中订单数
     */
    private Integer usingCount;

    /**
     * 已完成订单数
     */
    private Integer completedCount;
}
