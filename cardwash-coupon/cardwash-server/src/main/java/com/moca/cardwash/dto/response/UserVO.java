package com.moca.cardwash.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 VO
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String nickname;
    private String phone;
    private String avatar;
}
