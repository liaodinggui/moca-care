package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateRequest {

    /**
     * 洗车券 ID
     */
    private Long couponId;

    /**
     * 购买数量
     */
    private Integer quantity;
}
