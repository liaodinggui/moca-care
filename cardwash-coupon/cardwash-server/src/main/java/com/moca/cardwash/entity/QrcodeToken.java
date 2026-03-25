package com.moca.cardwash.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 二维码令牌实体
 */
@Data
public class QrcodeToken {

    private Long id;
    private Long orderId;
    private String token;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime createTime;
}
