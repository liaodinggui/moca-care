package com.moca.cardwash.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.dto.request.WechatLoginRequest;
import com.moca.cardwash.dto.response.UserLoginVO;
import com.moca.cardwash.entity.Merchant;
import com.moca.cardwash.entity.User;
import com.moca.cardwash.mapper.MerchantMapper;
import com.moca.cardwash.mapper.UserMapper;
import com.moca.cardwash.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final JwtUtil jwtUtil;

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";

    /**
     * 微信登录
     */
    public UserLoginVO login(WechatLoginRequest request) {
        log.info("收到登录请求：code={}, nickname={}, avatar={}",
                 request.getCode(), request.getNickname(), request.getAvatar());

        String openid;
        String unionid = null;
        String sessionKey = null;

        // 开发模式：使用测试账号
        if ("dev_mock_code".equals(request.getCode())) {
            openid = "dev_openid_" + System.currentTimeMillis();
            log.info("开发模式登录：openid={}", openid);
        } else {
            // 正式模式：调用微信接口获取 openid
            log.info("使用正式模式登录，code={}", request.getCode());
            JSONObject response = getWechatAuthResponse(request.getCode());
            openid = response.getStr("openid");
            unionid = response.getStr("unionid");
            sessionKey = response.getStr("session_key");

            log.info("微信登录响应：openid={}, unionid={}", openid, unionid);

            if (openid == null) {
                throw new BusinessException("微信登录失败");
            }
        }

        // 查询或创建用户
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            // 首次登录，创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname(request.getNickname());
            user.setAvatar(request.getAvatar());
            user.setRole(1); // 默认为客户角色
            userMapper.insert(user);
            log.info("创建新用户：id={}, openid={}, nickname={}", user.getId(), openid, request.getNickname());
        } else {
            // 已存在用户，更新微信昵称和头像（如果当前为空或请求中有新值）
            boolean needUpdate = false;

            // 如果用户昵名为空或请求中有新昵称，则更新
            if ((user.getNickname() == null || user.getNickname().isEmpty()) && request.getNickname() != null) {
                user.setNickname(request.getNickname());
                needUpdate = true;
            }

            // 如果用户头像为空或请求中有新头像，则更新
            if ((user.getAvatar() == null || user.getAvatar().isEmpty()) && request.getAvatar() != null) {
                user.setAvatar(request.getAvatar());
                needUpdate = true;
            }

            if (needUpdate) {
                userMapper.update(user);
                log.info("更新用户微信信息：id={}, nickname={}, avatar={}", user.getId(), user.getNickname(), user.getAvatar());
            }
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId());

        // 检查是否有商家身份（是否有至少一个店铺）
        java.util.List<Merchant> merchantList = merchantMapper.selectAllByUserId(user.getId());
        boolean hasMerchantRole = merchantList != null && !merchantList.isEmpty();

        return UserLoginVO.builder()
                .userId(user.getId())
                .token(token)
                .hasMerchantRole(hasMerchantRole)
                .build();
    }

    /**
     * 获取商家信息
     */
    public Merchant getMerchantInfo(Long userId) {
        return merchantMapper.selectByUserId(userId);
    }

    /**
     * 选择角色
     */
    @Transactional
    public void selectRole(Long userId, Integer role, String merchantName) {
        log.info("选择角色：userId={}, role={}, merchantName={}", userId, role, merchantName);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户角色
        user.setRole(role);
        userMapper.update(user);

        // 如果是商家角色，创建商家记录（允许一个用户拥有多个店铺）
        if (role == 2 && merchantName != null && !merchantName.isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setUserId(userId);
            merchant.setName(merchantName);
            merchant.setStatus(1);
            merchantMapper.insert(merchant);
            log.info("创建新商家：userId={}, name={}, merchantId={}", userId, merchantName, merchant.getId());
        }
    }

    /**
     * 获取微信授权响应
     */
    private JSONObject getWechatAuthResponse(String code) {
        String url = LOGIN_URL + "?appid=" + appid +
                     "&secret=" + secret +
                     "&js_code=" + code +
                     "&grant_type=authorization_code";

        String result = HttpUtil.get(url);
        log.info("微信授权响应：{}", result);

        return JSONUtil.parseObj(result);
    }
}
