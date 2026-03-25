package com.moca.cardwash.service;

import com.moca.cardwash.dto.response.StatsVO;
import com.moca.cardwash.dto.response.TodayStatsVO;
import com.moca.cardwash.mapper.CouponWriteOffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家数据统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantStatsService {

    private final CouponWriteOffMapper couponWriteOffMapper;

    /**
     * 获取商家统计数据
     * @param merchantId 商家 ID
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate 结束日期 yyyy-MM-dd
     * @return 统计数据
     */
    public StatsVO getStats(Long merchantId, String startDate, String endDate) {
        log.info("获取商家统计数据：merchantId={}, startDate={}, endDate={}", merchantId, startDate, endDate);

        // 分别查询核销数量和营业额
        List<StatsVO.DailyStats> writeOffList = couponWriteOffMapper.selectWriteOffCountByDate(merchantId, startDate, endDate);
        List<StatsVO.DailyStats> revenueList = couponWriteOffMapper.selectRevenueByDate(merchantId, startDate, endDate);

        // 将两个列表按日期合并
        java.util.Map<String, StatsVO.DailyStats> map = new java.util.HashMap<>();

        // 先放入核销数据
        for (StatsVO.DailyStats stats : writeOffList) {
            StatsVO.DailyStats daily = new StatsVO.DailyStats();
            daily.setDate(stats.getDate());
            daily.setWriteOffCount(stats.getWriteOffCount());
            daily.setRevenue(java.math.BigDecimal.ZERO);
            map.put(stats.getDate(), daily);
        }

        // 再合并营业额数据
        for (StatsVO.DailyStats stats : revenueList) {
            StatsVO.DailyStats daily = map.get(stats.getDate());
            if (daily == null) {
                daily = new StatsVO.DailyStats();
                daily.setDate(stats.getDate());
                daily.setWriteOffCount(0);
                map.put(stats.getDate(), daily);
            }
            daily.setRevenue(stats.getRevenue());
        }

        // 按日期排序
        List<StatsVO.DailyStats> dailyStats = new ArrayList<>(map.values());
        dailyStats.sort(java.util.Comparator.comparing(StatsVO.DailyStats::getDate));

        // 计算总核销数量和总营业额
        int totalWriteOff = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        List<String> dates = new ArrayList<>();
        List<Integer> writeOffCounts = new ArrayList<>();
        List<String> revenues = new ArrayList<>();

        for (StatsVO.DailyStats stats : dailyStats) {
            dates.add(stats.getDate());
            writeOffCounts.add(stats.getWriteOffCount());
            revenues.add(stats.getRevenue().toString());

            totalWriteOff += stats.getWriteOffCount();
            totalRevenue = totalRevenue.add(stats.getRevenue());
        }

        return new StatsVO(
                totalWriteOff,
                totalRevenue.toString(),
                dates,
                writeOffCounts,
                revenues
        );
    }

    /**
     * 获取默认日期范围（近 7 天）
     */
    public String[] getDefaultDateRange() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new String[]{
                startDate.format(formatter),
                endDate.format(formatter)
        };
    }

    /**
     * 获取今日数据
     * @param merchantId 商家 ID
     * @return 今日统计数据
     */
    public TodayStatsVO getTodayStats(Long merchantId) {
        log.info("获取今日数据：merchantId={}", merchantId);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 查询今日销售数据（从 order 表，status IN (1,2)）
        TodayStatsVO.StatsData salesData = couponWriteOffMapper.selectSalesCountByDate(merchantId, today);

        // 查询今日核销数据（从 coupon_write_off 表）
        TodayStatsVO.StatsData writeOffData = couponWriteOffMapper.selectWriteOffCountOnlyByDate(merchantId, today);

        // 查询今日营业额（从 order 表，status IN (1,2)）
        BigDecimal revenue = couponWriteOffMapper.selectRevenueByDateOnly(merchantId, today);

        TodayStatsVO stats = new TodayStatsVO();
        stats.setTotalSales(salesData != null ? salesData.getCount() : 0);
        stats.setUsedWriteOff(writeOffData != null ? writeOffData.getCount() : 0);
        stats.setRevenue(revenue != null ? revenue.toString() : "0.00");

        return stats;
    }
}
