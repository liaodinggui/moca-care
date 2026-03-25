package com.moca.cardwash.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 洗车券类型实体
 */
@Data
public class Coupon {

    private Long id;
    private Long merchantId;
    private String name;
    private String description;
    private String images;
    private BigDecimal price;
    private Integer buyAmount;
    private Integer sendAmount;
    private Integer stock;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
