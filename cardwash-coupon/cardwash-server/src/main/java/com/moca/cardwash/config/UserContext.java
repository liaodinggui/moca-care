package com.moca.cardwash.config;

import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final JwtUtil jwtUtil;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 获取当前用户 ID
     */
    public Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("未登录");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(token)) {
            throw new BusinessException("未登录");
        }

        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
        }

        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("登录已过期");
        }

        return jwtUtil.getUserIdFromToken(token);
    }
}
