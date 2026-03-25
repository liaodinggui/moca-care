package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 角色选择请求
 */
@Data
public class RoleSelectRequest {

    /**
     * 角色：1-客户，2-商家
     */
    private Integer role;

    /**
     * 商家名称（选择商家角色时必填）
     */
    private String merchantName;
}
