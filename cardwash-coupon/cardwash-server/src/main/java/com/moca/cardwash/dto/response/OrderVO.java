package com.moca.cardwash.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表 VO
 */
@Data
@NoArgsConstructor
public class OrderVO {

    private Long id;
    private String orderNo;
    private Integer status;
    private String statusText;
    private String couponName;
    private String couponImage;
    private Integer totalQuantity;
    private Integer usedQuantity;
    private BigDecimal totalAmount;
    private String userName;
    private String userPhone;
    private LocalDateTime createTime;
}
