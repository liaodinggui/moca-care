# 洗车券平台设计文档

## 1. 项目概述

### 1.1 项目背景
基于 MOCA Care 项目架构，开发一个洗车券销售与核销平台，支持商家管理、用户购买、订单核销等功能。

### 1.2 技术栈

**后端技术栈**：
- Spring Boot 3.2.0
- MyBatis
- JDK 17
- Lombok
- Hutool
- JWT Token 认证

**前端技术栈**：
- 微信小程序原生开发（WXML/WXSS/JavaScript）

**数据库**：MySQL

---

## 2. 数据库设计

### 2.1 核心表结构

#### user - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| nickname | VARCHAR | 昵称 |
| avatar | VARCHAR | 头像 URL |
| phone | VARCHAR | 手机号 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### coupon - 洗车券表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 商家 ID |
| name | VARCHAR | 券名称 |
| description | VARCHAR | 描述 |
| price | DECIMAL | 单价 |
| original_price | DECIMAL | 原价 |
| buy_amount | INT | 买 X 张 |
| send_amount | INT | 送 X 张 |
| stock | INT | 库存 |
| status | INT | 状态：0=下架，1=上架 |
| images | VARCHAR | 图片（JSON 数组） |

#### order - 订单表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR | 订单号 |
| user_id | BIGINT | 用户 ID |
| coupon_id | BIGINT | 洗车券 ID |
| coupon_name | VARCHAR | 洗车券名称 |
| coupon_price | DECIMAL | 洗车券单价 |
| total_quantity | INT | 总数量（含赠送） |
| paid_quantity | INT | 实际支付数量 |
| send_quantity | INT | 赠送数量 |
| used_quantity | INT | 已核销数量 |
| total_amount | DECIMAL | 订单总金额 |
| status | INT | 状态：0=待付款，1=使用中，2=已完成 |
| pay_time | DATETIME | 支付时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### coupon_write_off - 核销记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 订单 ID |
| user_id | BIGINT | 用户 ID |
| merchant_id | BIGINT | 商家 ID |
| quantity | INT | 本次核销数量 |
| write_off_time | DATETIME | 核销时间 |
| operator_id | BIGINT | 操作员 ID |

#### merchant - 商家表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 关联用户 ID |
| name | VARCHAR | 店铺名称 |
| create_time | DATETIME | 创建时间 |

---

## 3. 后端设计

### 3.1 实体类（Entity）

#### Order.java
```java
@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long couponId;
    private String couponName;
    private BigDecimal couponPrice;
    private Integer totalQuantity;      // 总数量（含赠送）
    private Integer paidQuantity;       // 实际支付数量
    private Integer sendQuantity;       // 赠送数量
    private Integer usedQuantity;       // 已核销数量
    private BigDecimal totalAmount;     // 订单总金额
    private Integer status;             // 0=待付款，1=使用中，2=已完成
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### CouponWriteOff.java
```java
@Data
public class CouponWriteOff {
    private Long id;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Integer quantity;           // 本次核销数量
    private LocalDateTime writeOffTime; // 核销时间
    private Long operatorId;            // 操作员 ID
}
```

### 3.2 响应对象（VO）

#### TodayStatsVO.java - 今日数据
```java
@Data
@NoArgsConstructor
public class TodayStatsVO {
    private Integer totalSales;         // 总销售量（当天卖出的洗车券数量）
    private Integer usedWriteOff;       // 已核销（当天核销的洗车券数量）
    private String revenue;             // 营业额（当天卖出的订单金额）

    @Data
    @NoArgsConstructor
    public static class StatsData {
        private Integer count;
    }
}
```

#### StatsVO.java - 统计数据
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsVO {
    private Integer totalWriteOff;      // 总核销数量
    private String totalRevenue;        // 总营业额
    private List<String> dates;         // 日期列表
    private List<Integer> writeOffCounts; // 核销数量列表
    private List<String> revenues;      // 营业额列表

    @Data
    @NoArgsConstructor
    public static class DailyStats {
        private String date;
        private Integer writeOffCount;
        private BigDecimal revenue;
    }
}
```

#### OrderVO.java - 订单视图
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private String statusText;
    private String couponName;
    private String couponImage;         // 洗车券图片
    private Integer totalQuantity;
    private Integer usedQuantity;
    private BigDecimal totalAmount;
    private String userName;
    private String userPhone;
    private LocalDateTime createTime;
}
```

### 3.3 Mapper XML 设计

#### CouponWriteOffMapper.xml

```xml
<!-- 按核销时间统计每天的核销数量 -->
<select id="selectWriteOffCountByDate" resultType="com.example.cardwash.dto.response.StatsVO$DailyStats">
    SELECT
        DATE_FORMAT(write_off_time, '%Y-%m-%d') as date,
        SUM(quantity) as writeOffCount,
        0 as revenue
    FROM coupon_write_off
    WHERE merchant_id = #{merchantId}
    AND DATE_FORMAT(write_off_time, '%Y-%m-%d') BETWEEN #{startDate} AND #{endDate}
    GROUP BY DATE_FORMAT(write_off_time, '%Y-%m-%d')
    ORDER BY date
</select>

<!-- 按订单创建时间统计每天的营业额（只统计已支付订单） -->
<select id="selectRevenueByDate" resultType="com.example.cardwash.dto.response.StatsVO$DailyStats">
    SELECT
        DATE_FORMAT(o.create_time, '%Y-%m-%d') as date,
        0 as writeOffCount,
        SUM(o.total_amount) as revenue
    FROM `order` o
    INNER JOIN coupon c ON o.coupon_id = c.id
    WHERE c.merchant_id = #{merchantId}
    AND o.status IN (1, 2)
    AND DATE_FORMAT(o.create_time, '%Y-%m-%d') BETWEEN #{startDate} AND #{endDate}
    GROUP BY DATE_FORMAT(o.create_time, '%Y-%m-%d')
    ORDER BY date
</select>

<!-- 查询今日销售数量 -->
<select id="selectSalesCountByDate" resultType="com.example.cardwash.dto.response.TodayStatsVO$StatsData">
    SELECT SUM(o.paid_quantity) as count
    FROM `order` o
    INNER JOIN coupon c ON o.coupon_id = c.id
    WHERE c.merchant_id = #{merchantId}
    AND o.status IN (1, 2)
    AND DATE_FORMAT(o.create_time, '%Y-%m-%d') = #{date}
</select>

<!-- 查询今日核销数量 -->
<select id="selectWriteOffCountOnlyByDate" resultType="com.example.cardwash.dto.response.TodayStatsVO$StatsData">
    SELECT SUM(quantity) as count
    FROM coupon_write_off
    WHERE merchant_id = #{merchantId}
    AND DATE_FORMAT(write_off_time, '%Y-%m-%d') = #{date}
</select>

<!-- 查询今日营业额 -->
<select id="selectRevenueByDateOnly" resultType="java.math.BigDecimal">
    SELECT IFNULL(SUM(o.total_amount), 0) as revenue
    FROM `order` o
    INNER JOIN coupon c ON o.coupon_id = c.id
    WHERE c.merchant_id = #{merchantId}
    AND o.status IN (1, 2)
    AND DATE_FORMAT(o.create_time, '%Y-%m-%d') = #{date}
</select>
```

#### OrderMapper.xml

```xml
<!-- 查询用户订单列表（包含优惠券图片） -->
<select id="selectByUserIdWithCouponImage" resultMap="OrderVOResultMap">
    SELECT
        o.id, o.order_no, o.status, o.coupon_name,
        c.images as coupon_image,
        o.total_quantity, o.used_quantity, o.total_amount, o.create_time
    FROM `order` o
    LEFT JOIN coupon c ON o.coupon_id = c.id
    WHERE o.user_id = #{userId}
    <if test="status != null">
        AND o.status = #{status}
    </if>
    ORDER BY o.create_time DESC
</select>
```

### 3.4 Service 层设计

#### MerchantStatsService.java

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantStatsService {
    private final CouponWriteOffMapper couponWriteOffMapper;

    /**
     * 获取商家统计数据（近 7 天/自定义日期范围）
     */
    public StatsVO getStats(Long merchantId, String startDate, String endDate) {
        // 分别查询核销数量和营业额
        List<StatsVO.DailyStats> writeOffList = couponWriteOffMapper.selectWriteOffCountByDate(merchantId, startDate, endDate);
        List<StatsVO.DailyStats> revenueList = couponWriteOffMapper.selectRevenueByDate(merchantId, startDate, endDate);

        // 将两个列表按日期合并
        Map<String, StatsVO.DailyStats> map = new HashMap<>();
        for (StatsVO.DailyStats stats : writeOffList) {
            map.put(stats.getDate(), stats);
        }
        for (StatsVO.DailyStats stats : revenueList) {
            StatsVO.DailyStats existing = map.get(stats.getDate());
            if (existing != null) {
                existing.setRevenue(stats.getRevenue());
            } else {
                map.put(stats.getDate(), stats);
            }
        }

        // 计算总和并提取分离的列表
        int totalWriteOff = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<String> dates = new ArrayList<>();
        List<Integer> writeOffCounts = new ArrayList<>();
        List<String> revenues = new ArrayList<>();

        for (StatsVO.DailyStats stats : map.values()) {
            totalWriteOff += stats.getWriteOffCount();
            totalRevenue = totalRevenue.add(stats.getRevenue());
            dates.add(stats.getDate());
            writeOffCounts.add(stats.getWriteOffCount());
            revenues.add(stats.getRevenue().toString());
        }

        return StatsVO.builder()
            .totalWriteOff(totalWriteOff)
            .totalRevenue(totalRevenue.toString())
            .dates(dates)
            .writeOffCounts(writeOffCounts)
            .revenues(revenues)
            .build();
    }

    /**
     * 获取今日数据
     */
    public TodayStatsVO getTodayStats(Long merchantId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        TodayStatsVO.StatsData salesData = couponWriteOffMapper.selectSalesCountByDate(merchantId, today);
        TodayStatsVO.StatsData writeOffData = couponWriteOffMapper.selectWriteOffCountOnlyByDate(merchantId, today);
        BigDecimal revenue = couponWriteOffMapper.selectRevenueByDateOnly(merchantId, today);

        TodayStatsVO stats = new TodayStatsVO();
        stats.setTotalSales(salesData != null ? salesData.getCount() : 0);
        stats.setUsedWriteOff(writeOffData != null ? writeOffData.getCount() : 0);
        stats.setRevenue(revenue != null ? revenue.toString() : "0.00");
        return stats;
    }
}
```

#### OrderService.java

```java
@Transactional(rollbackFor = Exception.class)
public Order createOrder(Long userId, Long couponId, Integer quantity) {
    Coupon coupon = couponMapper.selectById(couponId);

    // 计算买送数量：用户下单 quantity 张，根据买 send 规则计算赠送
    int paidQty = quantity;
    int sendQty = 0;
    if (coupon.getBuyAmount() > 0 && coupon.getSendAmount() > 0) {
        sendQty = (quantity / coupon.getBuyAmount()) * coupon.getSendAmount();
    }

    Order order = new Order();
    order.setPaidQuantity(paidQty);
    order.setSendQuantity(sendQty);
    order.setTotalQuantity(paidQty + sendQty);
    order.setTotalAmount(coupon.getPrice().multiply(BigDecimal.valueOf(paidQty)));
    order.setStatus(0); // 待付款

    return order;
}
```

### 3.5 Controller 层设计

#### MerchantController.java

```java
@RestController
@RequestMapping("/merchant")
public class MerchantController {
    private final MerchantStatsService merchantStatsService;

    @GetMapping("/today-stats")
    public Result<TodayStatsVO> getTodayStats() {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        return Result.success(merchantStatsService.getTodayStats(merchantId));
    }

    @GetMapping("/stats")
    public Result<StatsVO> getStats(@ModelAttribute StatsRequest request) {
        Long userId = userContext.getCurrentUserId();
        Long merchantId = orderService.getMerchantIdByUserId(userId);
        return Result.success(merchantStatsService.getStats(merchantId, request.getStartDate(), request.getEndDate()));
    }
}
```

#### ClientController.java

```java
@RestController
@RequestMapping("/client")
public class ClientController {
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
}
```

---

## 4. 前端设计

### 4.1 页面结构

```
cardwash-miniprogram/
├── pages/
│   ├── index/           # 首页（洗车券列表）
│   ├── coupon-detail/   # 券详情页（购买页）
│   ├── order/
│   │   ├── list/        # 订单列表
│   │   └── detail/      # 订单详情
│   ├── mine/            # 我的页面
│   ├── merchant/        # 商家页面
│   │   ├── index/       # 商家首页
│   │   └── stats/       # 数据统计
│   └── login/           # 登录页
├── api/
│   └── index.js         # API 封装
├── utils/
│   └── request.js       # 请求封装
└── app.js               # 全局配置
```

### 4.2 核心页面逻辑

#### 商家首页 - pages/merchant/index.js

```javascript
Page({
  data: {
    merchantName: '我的店铺',
    todayStats: {
      totalSales: 0,      // 总销售量
      usedWriteOff: 0,    // 已核销
      revenue: '0.00'     // 营业额
    }
  },

  onShow() {
    this.loadMerchantInfo();
    this.loadTodayStats();  // 加载今日数据
  },

  async loadTodayStats() {
    try {
      const stats = await merchant.getTodayStats();
      this.setData({
        todayStats: {
          totalSales: stats.totalSales || 0,
          usedWriteOff: stats.usedWriteOff || 0,
          revenue: stats.revenue || '0.00'
        }
      });
    } catch (error) {
      console.error('加载今日数据失败:', error);
    }
  }
})
```

#### 我的页面 - pages/mine/index.js

```javascript
Page({
  data: {
    userInfo: null,
    pendingCount: 0,
    usingCount: 0,
    completedCount: 0
  },

  onShow() {
    this.loadUserInfo();
    this.loadOrderStats();
  },

  // 跳转订单列表（支持状态筛选）
  goToOrderList(e) {
    const status = e.currentTarget.dataset.status;
    // 通过全局变量传递状态
    app.globalData.orderStatusFilter = String(status);
    // 使用 wx.reLaunch 强制重新加载页面
    wx.reLaunch({ url: '/pages/order/list' });
  }
})
```

#### 订单列表页 - pages/order/list.js

```javascript
const app = getApp();

Page({
  data: {
    statusFilter: '',
    orderList: []
  },

  onLoad(options) {
    // 优先从 URL 参数获取，其次从全局变量获取
    let status = '';
    if (options.status !== undefined) {
      status = String(options.status);
    } else if (app.globalData.orderStatusFilter !== undefined) {
      status = app.globalData.orderStatusFilter;
    }
    this.setData({ statusFilter: status });
    this.loadOrderList();
  },

  onShow() {
    // 从全局变量读取状态（用于 switchTab 跳转）
    if (app.globalData.orderStatusFilter && app.globalData.orderStatusFilter !== '') {
      const status = app.globalData.orderStatusFilter;
      this.setData({ statusFilter: status }, () => {
        this.loadOrderList();
      });
      app.globalData.orderStatusFilter = '';  // 清空全局变量
    } else {
      this.loadOrderList();
    }
  },

  async loadOrderList() {
    try {
      const status = this.data.statusFilter;
      const list = await client.getOrders(status === '' ? undefined : parseInt(status));

      // 处理图片数据
      const processedList = list.map(item => {
        let couponImage = '/images/default-coupon.png';
        if (item.couponImage) {
          couponImage = item.couponImage;
        }
        return { ...item, couponImage };
      });

      this.setData({ orderList: processedList });
    } catch (error) {
      console.error('加载订单列表失败:', error);
    }
  }
})
```

#### 全局配置 - app.js

```javascript
App({
  globalData: {
    userInfo: null,
    token: null,
    hasMerchantRole: false,
    selectedMerchantId: null,
    orderStatusFilter: '',  // 订单状态筛选（从「我的」页面跳转时传递）
    baseUrl: 'http://localhost:8080/api',
    devMode: false,  // 开发模式：跳过微信登录，直接模拟登录
    devUserId: 1     // 开发模式用户 ID
  },

  setToken(token) {
    this.globalData.token = token;
    wx.setStorageSync('token', token);
  },

  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.hasMerchantRole = false;
    this.globalData.selectedMerchantId = null;
    wx.removeStorageSync('token');
  }
})
```

### 4.3 API 封装

#### api/index.js

```javascript
const merchant = {
  getTodayStats: () => request({ url: '/merchant/today-stats' }),
  getStats: (params) => request({ url: `/merchant/stats?startDate=${params.startDate}&endDate=${params.endDate}` })
};

const client = {
  getOrders: (status) => request({ url: `/client/orders${status !== undefined ? `?status=${status}` : ''}` })
};
```

---

## 5. 核心业务逻辑

### 5.1 统计逻辑

**核销数量统计**：
- 按核销时间（`write_off_time`）统计
- 从 `coupon_write_off`表聚合`quantity`
- 支持按日期范围分组

**营业额统计**：
- 按订单创建时间（`create_time`）统计
- 只统计已支付订单（`status IN (1, 2)`）
- 从 `order` 表聚合`total_amount`

**今日数据**：
- 总销售量 = 当天卖出的洗车券数量（`SUM(paid_quantity)`）
- 已核销 = 当天核销的洗车券数量（`SUM(quantity)`）
- 营业额 = 当天卖出的订单金额（`SUM(total_amount)`）

### 5.2 买送逻辑

用户下单 `quantity` 张，根据买送规则计算赠送：
```java
int paidQty = quantity;  // 实际支付数量 = 下单数量
int sendQty = 0;
if (coupon.getBuyAmount() > 0 && coupon.getSendAmount() > 0) {
    sendQty = (quantity / coupon.getBuyAmount()) * coupon.getSendAmount();
}
order.setTotalQuantity(paidQty + sendQty);
order.setTotalAmount(coupon.getPrice().multiply(BigDecimal.valueOf(paidQty)));
```

### 5.3 订单状态流转

| 状态 | 说明 | 触发条件 |
|------|------|----------|
| 0 | 待付款 | 订单创建 |
| 1 | 使用中 | 支付成功 |
| 2 | 已完成 | 全部核销完成 |

---

## 6. 常见问题与解决方案

### 6.1 TabBar 页面跳转传参

**问题**：从「我的」页面点击订单状态卡片跳转到订单列表，参数丢失。

**原因**：
- `wx.navigateTo` 不支持 TabBar 页面
- `wx.switchTab` 不支持 URL 参数
- TabBar 页面已存在时不触发 `onLoad/onShow`

**解决方案**：
1. 使用全局变量 `app.globalData.orderStatusFilter` 传递状态
2. 使用 `wx.reLaunch` 强制重新加载页面
3. 目标页面在 `onLoad`和`onShow` 中读取全局变量

### 6.2 营业额重复统计

**问题**：一个订单被多次核销，`SUM(o.total_amount)` 重复计算。

**解决方案**：
- 核销数量从 `coupon_write_off` 表按核销时间统计
- 营业额从 `order` 表按创建时间统计（只统计已支付订单）
- 两个独立 SQL 查询，Service 层合并结果

### 6.3 MyBatis 类型映射

**问题**：`Could not resolve type alias` 或类型转换错误。

**解决方案**：
- VO 类添加`@NoArgsConstructor` 和`@AllArgsConstructor`
- 使用 `resultMap` 明确指定列与属性的映射
- 设置 `autoMapping="false"` 避免自动映射错误

---

## 7. 待办功能

- [ ] 商家设置页面（店铺信息编辑）
- [ ] 手工录入核销功能
- [ ] 统计图表可视化（ECharts）
- [ ] 数据导出功能（Excel）
- [ ] 优惠券库存管理
- [ ] 订单退款流程

---

## 8. 附录

### 8.1 相关文档
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

### 8.2 变更记录
- 2026-03-25：初始版本，基于用户需求整理
