package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 核销请求
 */
@Data
public class WriteOffRequest {

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 核销数量
     */
    private Integer quantity;
}
