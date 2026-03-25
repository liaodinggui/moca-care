package com.moca.cardwash.service;

import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.entity.Coupon;
import com.moca.cardwash.mapper.CouponMapper;
import com.moca.cardwash.dto.response.CouponWithMerchantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 洗车券服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;

    /**
     * 获取可购买的洗车券列表
     */
    public List<Coupon> getAvailableList() {
        return couponMapper.selectAvailableList();
    }

    /**
     * 获取可购买的洗车券列表（包含商家信息）
     */
    public List<CouponWithMerchantVO> getAvailableListWithMerchant() {
        return couponMapper.selectAvailableListWithMerchant();
    }

    /**
     * 获取洗车券详情
     */
    public Coupon getCouponDetail(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("洗车券不存在");
        }
        return coupon;
    }

    /**
     * 获取商家洗车券列表
     */
    public List<Coupon> getMerchantCoupons(Long merchantId) {
        return couponMapper.selectByMerchantId(merchantId);
    }

    /**
     * 创建洗车券
     */
    public Coupon createCoupon(Coupon coupon) {
        coupon.setStatus(1);
        couponMapper.insert(coupon);
        log.info("创建洗车券：id={}, merchantId={}, name={}", coupon.getId(), coupon.getMerchantId(), coupon.getName());
        return coupon;
    }

    /**
     * 更新洗车券
     */
    public Coupon updateCoupon(Coupon coupon) {
        Coupon existing = couponMapper.selectById(coupon.getId());
        if (existing == null) {
            throw new BusinessException("洗车券不存在");
        }
        couponMapper.update(coupon);
        log.info("更新洗车券：id={}", coupon.getId());
        return coupon;
    }

    /**
     * 上下架洗车券
     */
    public void updateStatus(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("洗车券不存在");
        }
        couponMapper.updateStatus(id, status);
        log.info("上下架洗车券：id={}, status={}", id, status);
    }
}
