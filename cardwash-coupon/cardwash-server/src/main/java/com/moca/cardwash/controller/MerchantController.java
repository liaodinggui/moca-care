package com.moca.cardwash.controller;

import com.moca.cardwash.common.Result;
import com.moca.cardwash.config.UserContext;
import com.moca.cardwash.dto.request.ScanParseRequest;
import com.moca.cardwash.dto.request.StatsRequest;
import com.moca.cardwash.dto.request.WriteOffRequest;
import com.moca.cardwash.dto.response.OrderDetailVO;
import com.moca.cardwash.dto.response.OrderInfoVO;
import com.moca.cardwash.dto.response.OrderVO;
import com.moca.cardwash.dto.response.StatsVO;
import com.moca.cardwash.dto.response.TodayStatsVO;
import com.moca.cardwash.dto.response.WriteOffResult;
import com.moca.cardwash.entity.Coupon;
import com.moca.cardwash.entity.Merchant;
import com.moca.cardwash.service.CouponService;
import com.moca.cardwash.service.MerchantOrderService;
import com.moca.cardwash.service.MerchantService;
import com.moca.cardwash.service.MerchantStatsService;
import com.moca.cardwash.service.WriteOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家端控制器
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final CouponService couponService;
    private final MerchantOrderService orderService;
    private final WriteOffService writeOffService;
    private final UserContext userContext;
    private final MerchantService merchantService;
    private final MerchantStatsService merchantStatsService;

    /**
     * 获取商家列表（用户关联的所有商家）
     */
    @GetMapping("/shops")
    public Result<List<Merchant>> getShops() {
        Long userId = userContext.getCurrentUserId();
        return Result.success(merchantService.getMerchantListByUserId(userId));
    }

    /**
     * 获取洗车券列表
     */
    @GetMapping("/coupons")
    public Result<List<Coupon>> getCoupons() {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        List<Coupon> coupons = couponService.getMerchantCoupons(merchantId);
        // 确保返回包含图片信息
        return Result.success(coupons);
    }

    /**
     * 获取洗车券详情
     */
    @GetMapping("/coupons/{id}")
    public Result<Coupon> getCouponDetail(@PathVariable Long id) {
        return Result.success(couponService.getCouponDetail(id));
    }

    /**
     * 创建洗车券
     */
    @PostMapping("/coupons")
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        coupon.setMerchantId(merchantId);
        return Result.success(couponService.createCoupon(coupon));
    }

    /**
     * 更新洗车券
     */
    @PutMapping("/coupons/{id}")
    public Result<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        return Result.success(couponService.updateCoupon(coupon));
    }

    /**
     * 上下架洗车券
     */
    @PutMapping("/coupons/{id}/status")
    public Result<Void> updateCouponStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 获取客户订单列表
     */
    @GetMapping("/orders")
    public Result<List<OrderVO>> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        return Result.success(orderService.getCustomerOrders(merchantId, status, pageNum, pageSize));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 扫描二维码解析订单信息
     */
    @PostMapping("/scan/parse")
    public Result<OrderInfoVO> parseQrCode(@RequestBody ScanParseRequest request) {
        OrderInfoVO orderInfo = writeOffService.parseQrCode(request.getQrContent());
        return Result.success(orderInfo);
    }

    /**
     * 核销洗车券
     */
    @PostMapping("/write-off")
    public Result<WriteOffResult> writeOff(@RequestBody WriteOffRequest request) {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        WriteOffResult result = writeOffService.writeOff(
            request.getOrderId(),
            request.getQuantity(),
            merchantId,
            userId
        );
        return Result.success(result);
    }

    /**
     * 获取核销记录
     */
    @GetMapping("/write-off/records")
    public Result<List> getWriteOffRecords(@RequestParam Long orderId) {
        return Result.success(writeOffService.getWriteOffRecords(orderId));
    }

    /**
     * 获取商家统计数据
     */
    @GetMapping("/stats")
    public Result<StatsVO> getStats(@ModelAttribute StatsRequest request) {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);

        // 如果没有传入日期，使用默认近 7 天
        if (request.getStartDate() == null || request.getEndDate() == null) {
            String[] defaultRange = merchantStatsService.getDefaultDateRange();
            request.setStartDate(defaultRange[0]);
            request.setEndDate(defaultRange[1]);
        }

        return Result.success(merchantStatsService.getStats(merchantId, request.getStartDate(), request.getEndDate()));
    }

    /**
     * 获取今日数据
     */
    @GetMapping("/today-stats")
    public Result<TodayStatsVO> getTodayStats() {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        return Result.success(merchantStatsService.getTodayStats(merchantId));
    }
}
