package com.moca.cardwash.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
public class Order {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long couponId;
    private String couponName;
    private BigDecimal couponPrice;
    private Integer totalQuantity;
    private Integer paidQuantity;
    private Integer sendQuantity;
    private Integer usedQuantity;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
