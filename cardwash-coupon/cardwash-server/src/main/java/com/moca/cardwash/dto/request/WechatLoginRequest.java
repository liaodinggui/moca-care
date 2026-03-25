package com.moca.cardwash.dto.request;

import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WechatLoginRequest {

    /**
     * 微信登录 code
     */
    private String code;

    /**
     * 用户昵称（可选）
     */
    private String nickname;

    /**
     * 用户头像（可选）
     */
    private String avatar;
}
