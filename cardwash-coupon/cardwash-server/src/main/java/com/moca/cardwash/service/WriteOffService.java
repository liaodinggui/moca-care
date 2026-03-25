package com.moca.cardwash.service;

import com.moca.cardwash.common.exception.BusinessException;
import com.moca.cardwash.dto.response.OrderInfoVO;
import com.moca.cardwash.dto.response.WriteOffResult;
import com.moca.cardwash.entity.CouponWriteOff;
import com.moca.cardwash.entity.Order;
import com.moca.cardwash.entity.QrcodeToken;
import com.moca.cardwash.mapper.CouponWriteOffMapper;
import com.moca.cardwash.mapper.OrderMapper;
import com.moca.cardwash.mapper.QrcodeTokenMapper;
import com.moca.cardwash.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 核销服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WriteOffService {

    private final OrderMapper orderMapper;
    private final CouponWriteOffMapper writeOffMapper;
    private final QrcodeTokenMapper qrcodeTokenMapper;
    private final AesUtil aesUtil;

    /**
     * 生成二维码 token
     */
    public String generateQrCode(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("该订单不可核销");
        }

        // AES 加密订单 ID
        String token = aesUtil.encrypt(String.valueOf(orderId));

        // 保存 token
        QrcodeToken qrcodeToken = new QrcodeToken();
        qrcodeToken.setOrderId(orderId);
        qrcodeToken.setToken(token);
        qrcodeToken.setExpireTime(LocalDateTime.now().plusHours(24));
        qrcodeToken.setStatus(1);
        qrcodeTokenMapper.insert(qrcodeToken);

        log.info("生成二维码：orderId={}, token={}", orderId, token);
        return token;
    }

    /**
     * 解析二维码
     */
    public OrderInfoVO parseQrCode(String qrContent) {
        try {
            // 解密二维码内容
            String decryptData = aesUtil.decrypt(qrContent);
            Long orderId = Long.parseLong(decryptData);

            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            return OrderInfoVO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .couponName(order.getCouponName())
                    .totalQty(order.getTotalQuantity())
                    .usedQty(order.getUsedQuantity())
                    .remainingQty(order.getTotalQuantity() - order.getUsedQuantity())
                    .status(order.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("解析二维码失败：{}", e.getMessage());
            throw new BusinessException("无效的二维码");
        }
    }

    /**
     * 核销洗车券
     */
    @Transactional(rollbackFor = Exception.class)
    public WriteOffResult writeOff(Long orderId, Integer quantity, Long merchantId, Long operatorId) {
        // 1. 验证订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不可核销");
        }

        // 2. 验证剩余数量
        int availableQty = order.getTotalQuantity() - order.getUsedQuantity();
        if (availableQty < quantity) {
            throw new BusinessException("剩余洗车券数量不足");
        }

        // 3. 更新订单核销数量
        orderMapper.increaseUsedQuantity(orderId, quantity);

        // 4. 记录核销日志
        CouponWriteOff record = new CouponWriteOff();
        record.setOrderId(orderId);
        record.setUserId(order.getUserId());
        record.setMerchantId(merchantId);
        record.setQuantity(quantity);
        record.setWriteOffTime(LocalDateTime.now());
        record.setOperatorId(operatorId);
        writeOffMapper.insert(record);

        // 5. 检查是否全部核销完成
        Order updatedOrder = orderMapper.selectById(orderId);
        boolean isCompleted = false;
        String message;
        int remainingQty = updatedOrder.getTotalQuantity() - updatedOrder.getUsedQuantity();

        if (remainingQty <= 0) {
            orderMapper.updateStatus(orderId, 2); // 已完成
            isCompleted = true;
            message = "洗车券已用完，订单已完成";
        } else {
            message = "核销成功，剩余" + remainingQty + "张";
        }

        log.info("核销成功：orderId={}, quantity={}, completed={}", orderId, quantity, isCompleted);

        return WriteOffResult.builder()
                .success(true)
                .completed(isCompleted)
                .message(message)
                .remainingQty(remainingQty)
                .build();
    }

    /**
     * 获取核销记录
     */
    public List<CouponWriteOff> getWriteOffRecords(Long orderId) {
        return writeOffMapper.selectByOrderId(orderId);
    }
}
