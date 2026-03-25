package com.moca.cardwash.dto.response;

import com.moca.cardwash.entity.CouponWriteOff;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO
 */
@Data
@Builder
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Integer status;
    private String couponName;
    private Integer totalQuantity;
    private Integer usedQuantity;
    private Integer remainingQuantity;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private UserVO user;
    private List<CouponWriteOff> writeOffRecords;
}
