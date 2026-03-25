package com.moca.cardwash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户登录响应
 */
@Data
@Builder
public class UserLoginVO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 登录 token
     */
    private String token;

    /**
     * 是否有商家角色
     */
    private Boolean hasMerchantRole;

    /**
     * 商家 ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;
}
