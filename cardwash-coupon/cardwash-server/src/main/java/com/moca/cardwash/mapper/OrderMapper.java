package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.Order;
import com.moca.cardwash.dto.response.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据 ID 查询订单
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户订单列表
     */
    List<Order> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户订单列表（包含优惠券图片）
     */
    List<OrderVO> selectByUserIdWithCouponImage(
        @Param("userId") Long userId,
        @Param("status") Integer status
    );

    /**
     * 查询商家客户订单列表
     */
    List<Order> selectByMerchantId(@Param("merchantId") Long merchantId,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 查询用户订单统计
     */
    List<OrderStatusCount> selectStatsByUser(@Param("userId") Long userId);

    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 更新订单
     */
    int update(Order order);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加已核销数量
     */
    int increaseUsedQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 订单状态计数
     */
    class OrderStatusCount {
        private Integer status;
        private Integer count;

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}
