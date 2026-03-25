package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.Coupon;
import com.moca.cardwash.dto.response.CouponWithMerchantVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 洗车券 Mapper 接口
 */
@Mapper
public interface CouponMapper {

    /**
     * 根据 ID 查询洗车券
     */
    Coupon selectById(@Param("id") Long id);

    /**
     * 查询商家洗车券列表
     */
    List<Coupon> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 查询可购买的洗车券列表
     */
    List<Coupon> selectAvailableList();

    /**
     * 查询可购买的洗车券列表（包含商家信息）
     */
    List<CouponWithMerchantVO> selectAvailableListWithMerchant();

    /**
     * 插入洗车券
     */
    int insert(Coupon coupon);

    /**
     * 更新洗车券
     */
    int update(Coupon coupon);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 扣减库存
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加库存
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
