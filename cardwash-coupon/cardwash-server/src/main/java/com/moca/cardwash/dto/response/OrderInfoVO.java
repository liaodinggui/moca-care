package com.moca.cardwash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 订单信息响应（扫码解析用）
 */
@Data
@Builder
public class OrderInfoVO {

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 洗车券名称
     */
    private String couponName;

    /**
     * 总数量
     */
    private Integer totalQty;

    /**
     * 已核销数量
     */
    private Integer usedQty;

    /**
     * 剩余数量
     */
    private Integer remainingQty;

    /**
     * 订单状态
     */
    private Integer status;
}
