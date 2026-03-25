package com.moca.cardwash.controller;

import com.moca.cardwash.common.Result;
import com.moca.cardwash.config.UserContext;
import com.moca.cardwash.dto.request.RoleSelectRequest;
import com.moca.cardwash.dto.request.WechatLoginRequest;
import com.moca.cardwash.dto.response.UserLoginVO;
import com.moca.cardwash.service.WechatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WechatService wechatService;
    private final UserContext userContext;

    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    public Result<UserLoginVO> wechatLogin(@RequestBody WechatLoginRequest request) {
        UserLoginVO result = wechatService.login(request);
        return Result.success(result);
    }

    /**
     * 选择角色
     */
    @PostMapping("/role-select")
    public Result<Void> roleSelect(@RequestBody RoleSelectRequest request) {
        Long userId = userContext.getCurrentUserId();
        wechatService.selectRole(userId, request.getRole(), request.getMerchantName());
        return Result.success();
    }
}
