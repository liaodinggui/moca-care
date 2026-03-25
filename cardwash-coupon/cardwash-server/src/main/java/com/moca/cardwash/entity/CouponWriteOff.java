package com.moca.cardwash.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 洗车券核销记录实体
 */
@Data
public class CouponWriteOff {

    private Long id;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Integer quantity;
    private LocalDateTime writeOffTime;
    private Long operatorId;
}
