# 洗车优惠券系统 - 详细需求文档与技术实现方案

## 一、项目概述

### 1.1 项目背景
基于微信小程序的洗车优惠券管理平台，连接商家与消费者，支持优惠券的购买、核销和管理。

### 1.2 技术栈
| 层次 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.x + JDK 17 + MyBatis |
| 前端 | 微信小程序 + Vue 3 |
| 数据库 | MySQL 8.0 |
| 认证 | 微信扫码登录（小程序） |

---

## 二、详细功能需求

### 2.1 用户端功能

#### 2.1.1 登录模块
| 功能点 | 详细描述 |
|--------|----------|
| 微信扫码登录 | 用户通过微信扫码授权登录小程序 |
| 角色选择 | 登录后弹出角色选择框：商家角色 / 客户角色 |
| 身份绑定 | 首次登录自动注册用户信息，绑定微信 openid |

#### 2.1.2 首页 Tab
| 功能点 | 详细描述 |
|--------|----------|
| 洗车券列表 | 展示所有可售洗车券类型，卡片式展示（名称、价格、描述） |
| 筛选功能 | 可按价格区间、优惠券类型筛选 |
| 优惠券详情 | 点击卡片进入详情页，展示：详情描述、图片轮播、价格、购买数量选择器、下单按钮 |
| 优惠活动 | 支持"买 10 送 1"等促销活动标识 |

#### 2.1.3 我的 Tab
| 功能点 | 详细描述 |
|--------|----------|
| 订单状态卡片 | **顶部展示 3 个状态卡片：待付款、使用中、已完成，每个卡片显示该状态的订单数量** |
| 订单列表 | 点击状态卡片后进入对应状态的订单列表，支持按状态筛选 |
| 订单详情 | 展示：订单号、下单时间、状态、洗车券总数、已核销数量、剩余百分比 |
| 订单核销码 | **使用中状态的订单展示核销二维码，包含订单 ID 和用户信息的加密串** |
| 订单操作 | 待付款订单支持支付操作，使用中订单支持查看核销记录 |

#### 2.1.4 订单状态流转
```
待付款 → 使用中 → 已完成
   ↑          ↓
   └── 取消 ──┘
```

| 状态 | 触发条件 |
|------|----------|
| 待付款 | 用户提交订单但未完成支付 |
| 使用中 | 用户已完成支付，洗车券可用 |
| 已完成 | 所有洗车券已全部核销完毕 |

---

### 2.2 商家端功能

#### 2.2.1 登录模块
| 功能点 | 详细描述 |
|--------|----------|
| 微信扫码登录 | 同用户端，共用登录入口 |
| 角色选择 | 选择"商家角色"进入商家管理后台 |
| 权限验证 | 验证用户是否已绑定商家身份 |

#### 2.2.2 洗车券管理
| 功能点 | 详细描述 |
|--------|----------|
| 创建洗车券 | 设置：名称、单价、描述、图片、库存数量 |
| 促销配置 | 配置"买 X 送 Y"活动规则 |
| 上下架管理 | 控制洗车券的销售状态 |
| 价格调整 | 随时修改洗车券价格（不影响历史订单） |

#### 2.2.3 订单管理
| 功能点 | 详细描述 |
|--------|----------|
| 客户订单列表 | **查看本商家所有客户的订单，支持按状态（待付款/使用中/已完成）筛选** |
| 订单详情 | **查看客户订单的详细信息：订单号、用户信息、洗车券类型、数量、核销记录等** |
| 扫码核销 | **商家使用小程序扫一扫功能扫描用户订单二维码，自动解析订单信息** |
| 核销操作 | **扫码后进入核销界面，显示订单信息和剩余数量，商家选择核销数量后确认** |
| 核销记录 | 查看每笔订单的核销明细和时间 |
| 状态提示 | **核销后自动判断：如洗车券已用完，订单状态改为已完成并提示"洗车券已用完"** |

---

## 三、数据库设计

### 3.1 ER 图
```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    user     │       │    order    │       │ order_item  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id          │───┐   │ id          │───┐   │ id          │
│ openid      │   │   │ user_id     │───┘   │ order_id    │───┐
│ nickname    │   └──>│ coupon_id   │       │ coupon_id   │   │
│ avatar      │       │ status      │       │ quantity    │   │
│ phone       │       │ total_amt   │       │ used_qty    │   │
│ role        │       │ create_time │       │ create_time │   │
└─────────────┘       └─────────────┘       └─────────────┘───┘
       │                      │
       │                      │
┌──────┴──────┐      ┌────────┴────────┐
│   merchant  │      │     coupon      │
├─────────────┤      ├─────────────────┤
│ id          │      │ id              │
│ user_id     │      │ merchant_id     │
│ name        │      │ name            │
│ status      │      │ price           │
└─────────────┘      │ buy_rule        │
                     │ send_rule       │
                     └─────────────────┘
```

### 3.2 表结构设计

#### user（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| openid | VARCHAR(64) | 微信 openid |
| unionid | VARCHAR(64) | 微信 unionid |
| nickname | VARCHAR(64) | 昵称 |
| avatar | VARCHAR(255) | 头像 URL |
| phone | VARCHAR(20) | 手机号 |
| role | TINYINT | 角色：1-客户，2-商家 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### merchant（商家表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 关联用户 ID |
| name | VARCHAR(128) | 商家名称 |
| status | TINYINT | 状态：0-停用，1-启用 |
| create_time | DATETIME | 创建时间 |

#### coupon（洗车券类型表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 商家 ID |
| name | VARCHAR(64) | 券名称 |
| description | TEXT | 描述 |
| images | JSON | 图片 URL 数组 |
| price | DECIMAL(10,2) | 单价 |
| buy_amount | INT | 买 X 张 |
| send_amount | INT | 送 Y 张 |
| stock | INT | 库存 |
| status | TINYINT | 状态：0-下架，1-上架 |
| create_time | DATETIME | 创建时间 |

#### order（订单表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单号（唯一） |
| user_id | BIGINT | 用户 ID |
| coupon_id | BIGINT | 洗车券 ID |
| total_quantity | INT | 总数量（含赠送） |
| paid_quantity | INT | 实际支付数量 |
| send_quantity | INT | 赠送数量 |
| used_quantity | INT | 已核销数量 |
| total_amount | DECIMAL(10,2) | 订单总额 |
| status | TINYINT | 状态：0-待付款，1-使用中，2-已完成，3-已取消 |
| pay_time | DATETIME | 支付时间 |
| create_time | DATETIME | 创建时间 |

#### coupon核销记录表（coupon_write_off）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 订单 ID |
| user_id | BIGINT | 用户 ID |
| merchant_id | BIGINT | 核销商家 ID |
| quantity | INT | 核销数量 |
| write_off_time | DATETIME | 核销时间 |
| operator_id | BIGINT | 操作人 ID |

#### qrcode_token（二维码令牌表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 订单 ID |
| token | VARCHAR(128) | 加密令牌 |
| expire_time | DATETIME | 过期时间 |
| status | TINYINT | 0-无效，1-有效 |
| create_time | DATETIME | 创建时间 |

---

## 四、API 接口设计

### 4.1 认证接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /api/auth/wechat-login | POST | 微信小程序登录，传入 code |
| /api/auth/role-select | POST | 选择登录角色 |
| /api/auth/refresh-token | POST | 刷新 token |

### 4.2 用户端接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /api/client/coupons | GET | 获取洗车券列表 |
| /api/client/coupons/{id} | GET | 获取洗车券详情 |
| /api/client/orders | GET | 获取订单列表 |
| /api/client/orders/{id} | GET | 获取订单详情 |
| /api/client/orders | POST | 创建订单 |
| /api/client/orders/{id}/pay | POST | 发起支付 |
| /api/client/orders/{id}/qrcode | GET | 获取订单核销二维码 |
| /api/client/orders/stats | GET | **获取订单统计（各状态订单数量）** |

### 4.3 商家端接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /api/merchant/coupons | GET | 获取洗车券列表（自己的） |
| /api/merchant/coupons | POST | 创建洗车券 |
| /api/merchant/coupons/{id} | PUT | 更新洗车券 |
| /api/merchant/coupons/{id}/status | PUT | 上下架洗车券 |
| /api/merchant/orders | GET | **获取客户订单列表（支持状态筛选）** |
| /api/merchant/orders/{id} | GET | **查看客户订单详情** |
| /api/merchant/scan/parse | POST | **扫描二维码解析订单信息** |
| /api/merchant/write-off | POST | 核销洗车券 |
| /api/merchant/write-off/records | GET | 获取核销记录 |

---

## 五、技术实现方案

### 5.1 项目结构
```
cardwash-coupon/
├── cardwash-server/           # 后端 Spring Boot 项目
│   ├── src/main/java/
│   │   └── com/moca/cardwash/
│   │       ├── CardwashApplication.java
│   │       ├── config/        # 配置类
│   │       │   ├── MybatisConfig.java
│   │       │   ├── WechatConfig.java
│   │       │   └── SecurityConfig.java
│   │       ├── controller/    # 控制器
│   │       │   ├── AuthController.java
│   │       │   ├── ClientController.java
│   │       │   └── MerchantController.java
│   │       ├── service/       # 服务层
│   │       │   ├── UserService.java
│   │       │   ├── CouponService.java
│   │       │   ├── OrderService.java
│   │       │   └── WechatService.java
│   │       ├── mapper/        # MyBatis Mapper
│   │       │   ├── UserMapper.java
│   │       │   ├── CouponMapper.java
│   │       │   └── OrderMapper.java
│   │       ├── entity/        # 实体类
│   │       │   ├── User.java
│   │       │   ├── Coupon.java
│   │       │   └── Order.java
│   │       ├── dto/           # 数据传输对象
│   │       │   ├── request/
│   │       │   └── response/
│   │       ├── common/        # 公共类
│   │       │   ├── Result.java
│   │       │   └── exception/
│   │       └── util/          # 工具类
│   │           └── OrderNoUtil.java
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/            # MyBatis XML
├── cardwash-miniprogram/      # 微信小程序项目
│   ├── pages/
│   │   ├── index/             # 首页
│   │   ├── mine/              # 我的
│   │   ├── coupon-detail/     # 券详情
│   │   ├── order-detail/      # 订单详情
│   │   └── login/             # 登录页
│   ├── components/            # 组件
│   ├── utils/                 # 工具函数
│   └── api/                   # API 请求封装
└── sql/
    └── schema.sql             # 数据库初始化脚本
```

### 5.2 核心业务逻辑实现

#### 5.2.1 微信登录流程
```java
@Service
public class WechatService {

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    public UserLoginVO login(String code) {
        // 1. 调用微信接口获取 openid
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        Map<String, String> params = Map.of(
            "appid", appid,
            "secret", secret,
            "js_code", code,
            "grant_type", "authorization_code"
        );

        // 2. 获取用户信息
        WechatAuthResponse response = wechatApiClient.getAuthResponse(params);

        // 3. 查询或创建用户
        User user = userMapper.selectByOpenid(response.getOpenid());
        if (user == null) {
            user = createUser(response);
        }

        // 4. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId());

        return UserLoginVO.builder()
            .userId(user.getId())
            .token(token)
            .hasMerchantRole(user.getRole() == Role.MERCHANT.getCode())
            .build();
    }
}
```

#### 5.2.2 订单创建（含买送逻辑）
```java
@Service
public class OrderService {

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(OrderCreateRequest request) {
        // 1. 获取优惠券信息
        Coupon coupon = couponMapper.selectById(request.getCouponId());

        // 2. 计算买送数量
        int buyQty = request.getQuantity();
        int sendQty = 0;
        if (coupon.getBuyAmount() > 0 && coupon.getSendAmount() > 0) {
            sendQty = (buyQty / coupon.getBuyAmount()) * coupon.getSendAmount();
        }

        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(getCurrentUserId());
        order.setCouponId(coupon.getId());
        order.setPaidQuantity(buyQty);
        order.setSendQuantity(sendQty);
        order.setTotalQuantity(buyQty + sendQty);
        order.setTotalAmount(coupon.getPrice().multiply(BigDecimal.valueOf(buyQty)));
        order.setStatus(OrderStatus.UNPAID.getCode());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 4. 扣减库存
        couponMapper.decreaseStock(coupon.getId(), buyQty);

        return convertToVO(order);
    }

    private String generateOrderNo() {
        return "CW" + System.currentTimeMillis() +
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
```

#### 5.2.3 洗车券核销
```java
@Service
public class CouponWriteOffService {

    @Transactional(rollbackFor = Exception.class)
    public WriteOffResult writeOff(WriteOffRequest request) {
        // 1. 验证订单
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || order.getStatus() != OrderStatus.USING.getCode()) {
            throw new BusinessException("订单状态不可核销");
        }

        // 2. 验证剩余数量
        int availableQty = order.getTotalQuantity() - order.getUsedQuantity();
        if (availableQty < request.getQuantity()) {
            throw new BusinessException("剩余洗车券数量不足");
        }

        // 3. 更新订单核销数量
        orderMapper.increaseUsedQuantity(order.getId(), request.getQuantity());

        // 4. 记录核销日志
        CouponWriteOff record = new CouponWriteOff();
        record.setOrderId(order.getId());
        record.setUserId(order.getUserId());
        record.setMerchantId(request.getMerchantId());
        record.setQuantity(request.getQuantity());
        record.setWriteOffTime(LocalDateTime.now());
        record.setOperatorId(getCurrentUserId());
        writeOffMapper.insert(record);

        // 5. 检查是否全部核销完成
        boolean isCompleted = false;
        String message = "核销成功，剩余" + (availableQty - request.getQuantity()) + "张";
        if (order.getUsedQuantity() + request.getQuantity() >= order.getTotalQuantity()) {
            orderMapper.updateStatus(order.getId(), OrderStatus.COMPLETED.getCode());
            isCompleted = true;
            message = "洗车券已用完，订单已完成";
        }

        return WriteOffResult.builder()
            .success(true)
            .completed(isCompleted)
            .message(message)
            .remainingQty(availableQty - request.getQuantity())
            .build();
    }

    /**
     * 解析二维码内容，获取订单信息
     */
    public OrderInfoVO parseQrCode(String qrContent) {
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
    }
}
```

### 5.2.4 二维码生成与核销流程

#### 二维码生成（用户端）
```java
@Service
public class QrCodeService {

    @Value("${qr.aes.key}")
    private String aesKey;

    /**
     * 生成订单核销二维码
     */
    public String generateQrCode(Long orderId) {
        // 1. 验证订单状态
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != OrderStatus.USING.getCode()) {
            throw new BusinessException("该订单不可核销");
        }

        // 2. AES 加密订单 ID
        String encryptData = aesUtil.encrypt(String.valueOf(orderId), aesKey);

        // 3. 返回二维码内容（前端使用小程序 qrcode 库生成图片）
        return encryptData;
    }
}
```

#### 订单统计服务
```java
@Service
public class OrderStatsService {

    /**
     * 获取用户订单统计
     */
    public OrderStatsVO getUserOrderStats(Long userId) {
        List<OrderStats> stats = orderMapper.selectStatsByUser(userId);

        return OrderStatsVO.builder()
            .pendingCount(stats.stream()
                .filter(s -> s.getStatus() == OrderStatus.UNPAID.getCode())
                .mapToInt(OrderStats::getCount)
                .sum())
            .usingCount(stats.stream()
                .filter(s -> s.getStatus() == OrderStatus.USING.getCode())
                .mapToInt(OrderStats::getCount)
                .sum())
            .completedCount(stats.stream()
                .filter(s -> s.getStatus() == OrderStatus.COMPLETED.getCode())
                .mapToInt(OrderStats::getCount)
                .sum())
            .build();
    }
}
```

#### 商家订单管理服务
```java
@Service
public class MerchantOrderService {

    /**
     * 获取客户订单列表（支持状态筛选）
     */
    public PageResult<OrderVO> getCustomerOrders(MerchantOrderQuery query) {
        // 查询订单列表
        List<Order> orders = orderMapper.selectByMerchantId(
            query.getMerchantId(),
            query.getStatus(),
            query.getPageNum(),
            query.getPageSize()
        );

        // 转换 VO
        List<OrderVO> orderVOList = orders.stream().map(order -> {
            User user = userMapper.selectById(order.getUserId());
            return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusText(getStatusText(order.getStatus()))
                .couponName(order.getCouponName())
                .totalQuantity(order.getTotalQuantity())
                .usedQuantity(order.getUsedQuantity())
                .userName(user.getNickname())
                .userPhone(user.getPhone())
                .createTime(order.getCreateTime())
                .build();
        }).collect(Collectors.toList());

        return PageResult.of(orderVOList, query.getPageNum(), query.getPageSize());
    }

    /**
     * 获取客户订单详情
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
```

#### 商家扫码核销流程
```
1. 商家点击"扫一扫" → 调用 wx.scanCode()
2. 解析二维码内容 → 后端 API /api/merchant/scan/parse
3. 显示订单信息（券类型、剩余数量等）
4. 商家输入核销数量 → 提交核销
5. 后端处理核销逻辑 → 返回结果
6. 前端根据结果提示:
   - 如果 completed=true → 显示"洗车券已用完，订单已完成"
   - 如果 completed=false → 显示"核销成功，剩余 X 张"
```

### 5.3.1 关键配置

#### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cardwash_coupon?useSSL=false&serverTimezone=UTC
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.example.cardwash.entity

wechat:
  miniapp:
    appid: ${WECHAT_APPID}
    secret: ${WECHAT_SECRET}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 小时

qr:
  aes:
    key: ${QR_AES_KEY}
```

---

## 六、小程序页面结构

### 6.1 页面导航
```
小程序
├── 登录页 (pages/login)
│   └── 角色选择弹窗
├── TabBar
│   ├── 首页 (pages/index)
│   │   ├── 洗车券列表
│   │   └── 点击进入详情
│   └── 我的 (pages/mine)
│       ├── 订单列表
│       └── 点击进入订单详情
└── 公共页面
    ├── 洗车券详情 (pages/coupon-detail)
    ├── 订单详情 (pages/order-detail)
    ├── 支付结果 (pages/pay-result)
    └── 商家扫码核销 (pages/merchant/scan-result)
```

### 6.1.1 我的 Tab 页（订单状态卡片）
```html
<!-- pages/mine/index.wxml -->
<view class="mine-page">
  <!-- 订单状态卡片 -->
  <view class="order-cards">
    <view class="order-card" bindtap="goToOrderList" data-status="0">
      <view class="card-num {{pendingCount > 0 ? 'highlight' : ''}}">{{pendingCount}}</view>
      <view class="card-label">待付款</view>
    </view>
    <view class="order-card" bindtap="goToOrderList" data-status="1">
      <view class="card-num {{usingCount > 0 ? 'highlight' : ''}}">{{usingCount}}</view>
      <view class="card-label">使用中</view>
    </view>
    <view class="order-card" bindtap="goToOrderList" data-status="2">
      <view class="card-num {{completedCount > 0 ? 'highlight' : ''}}">{{completedCount}}</view>
      <view class="card-label">已完成</view>
    </view>
  </view>
</view>
```

```javascript
// pages/mine/index.js
Page({
  data: {
    pendingCount: 0,      // 待付款订单数
    usingCount: 0,        // 使用中订单数
    completedCount: 0     // 已完成订单数
  },

  onLoad() {
    this.fetchOrderStats();
  },

  // 获取订单统计
  fetchOrderStats() {
    wx.request({
      url: '/api/client/orders/stats',
      success: (res) => {
        this.setData({
          pendingCount: res.data.pendingCount,
          usingCount: res.data.usingCount,
          completedCount: res.data.completedCount
        });
      }
    });
  },

  // 跳转订单列表
  goToOrderList(e) {
    const status = e.currentTarget.dataset.status;
    wx.navigateTo({
      url: `/pages/order/list?status=${status}`
    });
  }
});
```

### 6.1.2 订单详情页二维码展示
```html
<!-- 订单详情页面 -->
<view class="order-detail" wx:if="{{order.status === 1}}">
  <!-- 使用中状态显示核销二维码 -->
  <view class="qrcode-section">
    <image src="{{qrcodeUrl}}" class="qrcode-image" />
    <text class="hint">请出示此二维码给商家核销</text>
  </view>
</view>
```

### 6.1.3 商家扫码页
```javascript
// pages/merchant/scan.js
Page({
  data: {
    orderInfo: null,
    writeOffQty: 1
  },

  // 调用扫码
  scanCode() {
    wx.scanCode({
      success: (res) => {
        const qrContent = res.result;
        this.parseQrCode(qrContent);
      }
    });
  },

  // 解析二维码
  parseQrCode(qrContent) {
    wx.request({
      url: '/api/merchant/scan/parse',
      method: 'POST',
      data: { qrContent },
      success: (res) => {
        this.setData({ orderInfo: res.data });
      }
    });
  },

  // 提交核销
  submitWriteOff() {
    wx.request({
      url: '/api/merchant/write-off',
      method: 'POST',
      data: {
        orderId: this.data.orderInfo.orderId,
        quantity: this.data.writeOffQty
      },
      success: (res) => {
        if (res.data.completed) {
          wx.showModal({
            title: '核销完成',
            content: '洗车券已用完，订单已完成',
            showCancel: false
          });
        } else {
          wx.showToast({
            title: `核销成功，剩余${res.data.remainingQty}张`
          });
        }
      }
    });
  }
});
```

### 6.1.4 商家客户订单列表
```javascript
// pages/merchant/order-list.js
Page({
  data: {
    orderList: [],
    statusFilter: 'all',  // all, unpaid, using, completed
    tabList: [
      { label: '全部', value: 'all' },
      { label: '待付款', value: '0' },
      { label: '使用中', value: '1' },
      { label: '已完成', value: '2' }
    ]
  },

  onLoad() {
    this.fetchOrderList();
  },

  // 切换状态筛选
  onTabChange(e) {
    const status = e.currentTarget.dataset.value;
    this.setData({ statusFilter: status });
    this.fetchOrderList();
  },

  // 获取订单列表
  fetchOrderList() {
    wx.request({
      url: '/api/merchant/orders',
      data: { status: this.data.statusFilter },
      success: (res) => {
        this.setData({ orderList: res.data.list });
      }
    });
  },

  // 查看订单详情
  goToOrderDetail(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/merchant/order-detail?id=${orderId}`
    });
  },

  // 核销订单
  goToWriteOff(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/merchant/write-off?orderId=${orderId}`
    });
  }
});
```

```html
<!-- pages/merchant/order-list.wxml -->
<view class="order-list-page">
  <!-- 状态筛选 Tab -->
  <view class="tab-bar">
    <view
      class="tab-item {{statusFilter === item.value ? 'active' : ''}}"
      wx:for="{{tabList}}"
      wx:key="value"
      data-value="{{item.value}}"
      bindtap="onTabChange">
      {{item.label}}
    </view>
  </view>

  <!-- 订单列表 -->
  <view class="order-list">
    <view
      class="order-card"
      wx:for="{{orderList}}"
      wx:key="id"
      data-id="{{item.id}}"
      bindtap="goToOrderDetail">
      <view class="order-header">
        <text class="order-no">订单号：{{item.orderNo}}</text>
        <text class="order-status {{'status-' + item.status}}">{{item.statusText}}</text>
      </view>
      <view class="order-body">
        <view class="coupon-info">
          <text class="coupon-name">{{item.couponName}}</text>
          <text class="coupon-qty">已核销：{{item.usedQuantity}}/{{item.totalQuantity}}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{item.userName}}</text>
          <text class="user-phone">{{item.userPhone}}</text>
        </view>
      </view>
      <view class="order-footer">
        <view class="order-time">{{item.createTime}}</view>
        <button
          wx:if="{{item.status === 1}}"
          class="write-off-btn"
          data-id="{{item.id}}"
          bindtap="goToWriteOff">
          核销
        </button>
      </view>
    </view>
  </view>
</view>
```

---

### 6.2 核心组件
| 组件名 | 功能描述 |
|--------|----------|
| CouponCard | 洗车券卡片展示 |
| OrderCard | 订单卡片展示 |
| RoleSelector | 角色选择弹窗 |
| ImageUploader | 图片上传组件（商家端） |
| QrCodeDisplay | **订单核销二维码展示组件** |
| ScanReader | **商家扫码核销组件（调用微信小程序 wx.scanCode）** |

---

## 七、安全与性能

### 7.1 安全措施
- JWT Token 认证，有效期 24 小时
- 接口幂等性设计（订单创建、核销）
- 敏感数据加密存储
- SQL 注入防护（MyBatis 参数绑定）
- 接口限流（防刷）

### 7.2 性能优化
-  Redis 缓存热门洗车券信息
-  数据库索引优化（user_id、order_no、status）
-  分页查询（订单列表）
-  图片 CDN 加速

---

## 八、开发计划

| 阶段 | 时间 | 内容 |
|------|------|------|
| Phase 1 | Week 1 | 数据库设计、项目搭建、微信登录 |
| Phase 2 | Week 2 | 用户端：首页、订单创建、支付 |
| Phase 3 | Week 3 | 商家端：洗车券管理、核销功能 |
| Phase 4 | Week 4 | 联调测试、Bug 修复、上线准备 |

---

## 九、附录

### 9.1 状态码定义
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token 失效 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

### 9.2 数据字典
| 字段 | 枚举值 |
|------|--------|
| user.role | 1-客户，2-商家 |
| order.status | 0-待付款，1-使用中，2-已完成，3-已取消 |
| coupon.status | 0-下架，1-上架 |
