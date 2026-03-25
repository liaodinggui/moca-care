package com.moca.cardwash.service;

import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.dto.response.OrderDetailVO;
import com.moca.cardwash.dto.response.OrderVO;
import com.moca.cardwash.dto.response.UserVO;
import com.moca.cardwash.entity.CouponWriteOff;
import com.moca.cardwash.entity.Merchant;
import com.moca.cardwash.entity.Order;
import com.moca.cardwash.entity.User;
import com.moca.cardwash.mapper.CouponWriteOffMapper;
import com.moca.cardwash.mapper.MerchantMapper;
import com.moca.cardwash.mapper.OrderMapper;
import com.moca.cardwash.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商家订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantOrderService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final CouponWriteOffMapper writeOffMapper;

    /**
     * 获取客户订单列表
     */
    public List<OrderVO> getCustomerOrders(Long merchantId, Integer status, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;

        List<Order> orders = orderMapper.selectByMerchantId(merchantId, status, offset, pageSize);

        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            User user = userMapper.selectById(order.getUserId());
            OrderVO vo = new OrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStatus(order.getStatus());
            vo.setStatusText(getStatusText(order.getStatus()));
            vo.setCouponName(order.getCouponName());
            vo.setTotalQuantity(order.getTotalQuantity());
            vo.setUsedQuantity(order.getUsedQuantity());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setUserName(user != null ? user.getNickname() : "未知用户");
            vo.setUserPhone(user != null ? user.getPhone() : "");
            vo.setCreateTime(order.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    /**
     * 获取订单详情
     */
    public OrderDetailVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        User user = userMapper.selectById(order.getUserId());
        List<CouponWriteOff> writeOffRecords = writeOffMapper.selectByOrderId(orderId);

        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .couponName(order.getCouponName())
                .totalQuantity(order.getTotalQuantity())
                .usedQuantity(order.getUsedQuantity())
                .remainingQuantity(order.getTotalQuantity() - order.getUsedQuantity())
                .totalAmount(order.getTotalAmount())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .user(UserVO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .phone(user.getPhone())
                        .avatar(user.getAvatar())
                        .build())
                .writeOffRecords(writeOffRecords)
                .build();
    }

    /**
     * 获取商家 ID（从当前选择的店铺）
     */
    public Long getMerchantIdByUserId(Long userId) {
        // 从请求上下文中获取选中的店铺 ID
        // TODO: 需要从 UserContext 获取 selectedMerchantId
        // 暂时使用 selectByUserId 返回第一个商家
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || merchant.getStatus() != 1) {
            throw new BusinessException("您不是商家身份");
        }
        return merchant.getId();
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待付款";
            case 1: return "使用中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}
