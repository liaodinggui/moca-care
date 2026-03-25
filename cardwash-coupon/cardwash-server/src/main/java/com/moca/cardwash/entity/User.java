package com.moca.cardwash.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {

    private Long id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
