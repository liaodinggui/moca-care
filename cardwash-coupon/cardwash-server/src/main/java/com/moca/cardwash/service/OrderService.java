package com.moca.cardwash.service;

import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.entity.Coupon;
import com.moca.cardwash.entity.Order;
import com.moca.cardwash.mapper.CouponMapper;
import com.moca.cardwash.mapper.OrderMapper;
import com.moca.cardwash.util.OrderNoUtil;
import com.moca.cardwash.dto.response.OrderStatsVO;
import com.moca.cardwash.dto.response.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CouponMapper couponMapper;
    private final OrderMapper orderMapper;

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long couponId, Integer quantity) {
        // 1. 获取优惠券信息
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("洗车券不存在");
        }
        if (coupon.getStatus() != 1) {
            throw new BusinessException("该洗车券已下架");
        }
        if (coupon.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }

        // 2. 计算买送数量
        int paidQty = quantity;
        int sendQty = 0;
        if (coupon.getBuyAmount() > 0 && coupon.getSendAmount() > 0) {
            // 每买 buyAmount 张，赠送 sendAmount 张
            // 用户下单 quantity 张，需要计算实际赠送多少张
            sendQty = (quantity / coupon.getBuyAmount()) * coupon.getSendAmount();
        }

        // 3. 创建订单
        String orderNo = OrderNoUtil.generate();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCouponId(couponId);
        order.setCouponName(coupon.getName());
        order.setCouponPrice(coupon.getPrice());
        order.setPaidQuantity(paidQty);
        order.setSendQuantity(sendQty);
        order.setTotalQuantity(paidQty + sendQty);
        order.setUsedQuantity(0);
        order.setTotalAmount(coupon.getPrice().multiply(BigDecimal.valueOf(paidQty)));
        order.setStatus(0); // 待付款
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
        log.info("创建订单：orderNo={}, userId={}, couponId={}, quantity={}", orderNo, userId, couponId, quantity);
        log.info("订单数量详情：paidQuantity={}, sendQuantity={}, totalQuantity={}", paidQty, sendQty, order.getTotalQuantity());

        // 4. 扣减库存
        couponMapper.decreaseStock(couponId, paidQty);

        return order;
    }

    /**
     * 支付订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不可支付");
        }

        // 更新订单状态为使用中
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.update(order);

        log.info("订单支付成功：orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    /**
     * 获取用户订单列表
     */
    public List<OrderVO> getUserOrders(Long userId, Integer status) {
        return orderMapper.selectByUserIdWithCouponImage(userId, status);
    }

    /**
     * 获取订单详情
     */
    public Order getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return order;
    }

    /**
     * 获取订单统计
     */
    public OrderStatsVO getOrderStats(Long userId) {
        List<OrderMapper.OrderStatusCount> stats = orderMapper.selectStatsByUser(userId);

        int pendingCount = 0;
        int usingCount = 0;
        int completedCount = 0;

        for (OrderMapper.OrderStatusCount s : stats) {
            if (s.getStatus() == 0) pendingCount = s.getCount();
            else if (s.getStatus() == 1) usingCount = s.getCount();
            else if (s.getStatus() == 2) completedCount = s.getCount();
        }

        return OrderStatsVO.builder()
                .pendingCount(pendingCount)
                .usingCount(usingCount)
                .completedCount(completedCount)
                .build();
    }
}
