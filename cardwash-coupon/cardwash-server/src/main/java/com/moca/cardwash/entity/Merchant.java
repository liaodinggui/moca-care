package com.moca.cardwash.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商家实体
 */
@Data
public class Merchant {

    private Long id;
    private Long userId;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
}
