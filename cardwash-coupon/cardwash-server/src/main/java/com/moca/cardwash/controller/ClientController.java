package com.moca.cardwash.controller;

import com.moca.cardwash.common.Result;
import com.moca.cardwash.config.UserContext;
import com.moca.cardwash.dto.request.OrderCreateRequest;
import com.moca.cardwash.dto.response.CouponWithMerchantVO;
import com.moca.cardwash.dto.response.OrderStatsVO;
import com.moca.cardwash.entity.Coupon;
import com.moca.cardwash.entity.Order;
import com.moca.cardwash.entity.User;
import com.moca.cardwash.mapper.UserMapper;
import com.moca.cardwash.service.CouponService;
import com.moca.cardwash.service.OrderService;
import com.moca.cardwash.service.WriteOffService;
import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.dto.response.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端控制器
 */
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final CouponService couponService;
    private final OrderService orderService;
    private final WriteOffService writeOffService;
    private final UserContext userContext;
    private final UserMapper userMapper;

    /**
     * 获取洗车券列表
     */
    @GetMapping("/coupons")
    public Result<List<CouponWithMerchantVO>> getCoupons() {
        return Result.success(couponService.getAvailableListWithMerchant());
    }

    /**
     * 获取洗车券详情
     */
    @GetMapping("/coupons/{id}")
    public Result<Coupon> getCouponDetail(@PathVariable Long id) {
        return Result.success(couponService.getCouponDetail(id));
    }

    /**
     * 创建订单
     */
    @PostMapping("/orders")
    public Result<Order> createOrder(@RequestBody OrderCreateRequest request) {
        Long userId = userContext.getCurrentUserId();
        Order order = orderService.createOrder(userId, request.getCouponId(), request.getQuantity());
        return Result.success(order);
    }

    /**
     * 支付订单
     */
    @PostMapping("/orders/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        orderService.payOrder(userId, id);
        return Result.success();
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public Result<List<OrderVO>> getOrders(@RequestParam(required = false) Integer status) {
        Long userId = userContext.getCurrentUserId();
        List<OrderVO> list = orderService.getUserOrders(userId, status);
        // 设置状态文本和处理图片
        for (OrderVO vo : list) {
            vo.setStatusText(getStatusText(vo.getStatus()));
            vo.setCouponImage(processCouponImage(vo.getCouponImage()));
        }
        return Result.success(list);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == 0) return "待付款";
        if (status == 1) return "使用中";
        if (status == 2) return "已完成";
        return "未知";
    }

    /**
     * 处理优惠券图片（解析 JSON 并返回第一张）
     */
    private String processCouponImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return null;
        }
        try {
            cn.hutool.json.JSONArray images = cn.hutool.json.JSONUtil.parseArray(imagesJson);
            if (images != null && !images.isEmpty()) {
                return images.getStr(0);
            }
        } catch (Exception e) {
            // 如果不是 JSON 格式，直接返回原值
            return imagesJson;
        }
        return null;
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        return Result.success(orderService.getOrderDetail(userId, id));
    }

    /**
     * 获取订单统计（用于我的 Tab 状态卡片）
     */
    @GetMapping("/orders/stats")
    public Result<OrderStatsVO> getOrderStats() {
        Long userId = userContext.getCurrentUserId();
        return Result.success(orderService.getOrderStats(userId));
    }

    /**
     * 获取订单核销二维码
     */
    @GetMapping("/orders/{id}/qrcode")
    public Result<String> getQrCode(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        Order order = orderService.getOrderDetail(userId, id);
        String qrCode = writeOffService.generateQrCode(id);
        return Result.success(qrCode);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    public Result<User> getUserInfo() {
        Long userId = userContext.getCurrentUserId();
        return Result.success(userMapper.selectById(userId));
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/user/profile")
    public Result<Void> updateProfile(@RequestBody User user) {
        Long userId = userContext.getCurrentUserId();
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }
        // 只在有值时更新，避免覆盖为空
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        userMapper.update(existingUser);
        return Result.success();
    }
}
