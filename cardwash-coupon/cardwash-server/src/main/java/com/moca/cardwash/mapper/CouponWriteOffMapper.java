package com.moca.cardwash.mapper;

import com.moca.cardwash.entity.CouponWriteOff;
import com.moca.cardwash.dto.response.StatsVO;
import com.moca.cardwash.dto.response.TodayStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 洗车券核销记录 Mapper 接口
 */
@Mapper
public interface CouponWriteOffMapper {

    /**
     * 根据 ID 查询核销记录
     */
    CouponWriteOff selectById(@Param("id") Long id);

    /**
     * 根据订单 ID 查询核销记录列表
     */
    List<CouponWriteOff> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 插入核销记录
     */
    int insert(CouponWriteOff writeOff);

    /**
     * 按核销时间统计每天的核销数量
     */
    List<StatsVO.DailyStats> selectWriteOffCountByDate(
        @Param("merchantId") Long merchantId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 按订单创建时间统计每天的营业额
     */
    List<StatsVO.DailyStats> selectRevenueByDate(
        @Param("merchantId") Long merchantId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 查询今日销售数量
     */
    TodayStatsVO.StatsData selectSalesCountByDate(
        @Param("merchantId") Long merchantId,
        @Param("date") String date
    );

    /**
     * 查询今日核销数量
     */
    TodayStatsVO.StatsData selectWriteOffCountOnlyByDate(
        @Param("merchantId") Long merchantId,
        @Param("date") String date
    );

    /**
     * 查询今日营业额
     */
    java.math.BigDecimal selectRevenueByDateOnly(
        @Param("merchantId") Long merchantId,
        @Param("date") String date
    );
}
