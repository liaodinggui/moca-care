# 洗车优惠券系统

基于 Spring Boot 3.x + 微信小程序的洗车优惠券管理平台。

## 项目结构

```
cardwash-coupon/
├── cardwash-server/          # 后端 Spring Boot 项目
│   ├── src/main/java/
│   │   └── com/moca/cardwash/
│   │       ├── config/       # 配置类
│   │       ├── controller/   # 控制器
│   │       ├── service/      # 服务层
│   │       ├── mapper/       # MyBatis Mapper
│   │       ├── entity/       # 实体类
│   │       ├── dto/          # 数据传输对象
│   │       ├── common/       # 公共类
│   │       └── util/         # 工具类
│   └── src/main/resources/
│       ├── application.yml   # 配置文件
│       └── mapper/           # MyBatis XML
├── cardwash-miniprogram/     # 微信小程序项目
│   ├── pages/                # 页面
│   ├── api/                  # API 封装
│   └── utils/                # 工具函数
└── sql/
    └── schema.sql            # 数据库初始化脚本
```

## 技术栈

### 后端
- Spring Boot 3.2.0
- JDK 17
- MyBatis 3.0.3
- MySQL 8.0
- JWT (jjwt 0.12.3)
- Hutool 5.8.23

### 前端
- 微信小程序
- 原生 WXML/WXSS/JavaScript

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库并执行初始化脚本
mysql -u root -p < sql/schema.sql
```

### 2. 后端启动

```bash
# 进入后端项目目录
cd cardwash-server

# 配置环境变量（可选）
export DB_PASSWORD=your_password
export WECHAT_APPID=your_wechat_appid
export WECHAT_SECRET=your_wechat_secret
export JWT_SECRET=your_jwt_secret
export QR_AES_KEY=1234567890123456

# 使用 Maven 启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/cardwash-server-1.0.0-SNAPSHOT.jar
```

### 3. 前端配置

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具，导入 `cardwash-miniprogram` 目录
3. 修改 `app.js` 中的 `baseUrl` 配置：
   ```javascript
   globalData: {
     baseUrl: 'http://localhost:8080/api'  // 改为你自己的服务器地址
   }
   ```
4. 在项目设置中，将后端 API 域名加入 request 合法域名（开发阶段可暂时关闭域名校验）
5. 编译运行即可

## API 接口文档

### 认证接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /auth/wechat-login | POST | 微信登录 |
| /auth/role-select | POST | 选择角色 |

### 用户端接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /client/coupons | GET | 获取洗车券列表 |
| /client/coupons/{id} | GET | 获取洗车券详情 |
| /client/orders | POST | 创建订单 |
| /client/orders/{id}/pay | POST | 支付订单 |
| /client/orders | GET | 获取订单列表 |
| /client/orders/{id} | GET | 获取订单详情 |
| /client/orders/stats | GET | 获取订单统计 |
| /client/orders/{id}/qrcode | GET | 获取核销二维码 |

### 商家端接口
| 接口 | 方法 | 描述 |
|------|------|------|
| /merchant/coupons | GET | 获取洗车券列表 |
| /merchant/coupons | POST | 创建洗车券 |
| /merchant/coupons/{id}/status | PUT | 上下架洗车券 |
| /merchant/orders | GET | 获取客户订单列表 |
| /merchant/orders/{id} | GET | 获取订单详情 |
| /merchant/scan/parse | POST | 解析二维码 |
| /merchant/write-off | POST | 核销洗车券 |

## 核心功能

### 用户端
- 微信登录，支持客户/商家角色切换
- 浏览和购买洗车券
- 订单管理（待付款、使用中、已完成）
- 订单状态卡片展示
- 核销二维码展示

### 商家端
- 商家登录
- 扫码核销用户订单
- 客户订单管理
- 洗车券管理（创建、编辑、上下架）

## 配置文件说明

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cardwash_coupon
    username: root
    password: ${DB_PASSWORD}

wechat:
  miniapp:
    appid: ${WECHAT_APPID}
    secret: ${WECHAT_SECRET}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 小时

qr:
  aes:
    key: ${QR_AES_KEY}  # 16 位 AES 密钥
```

## 环境变量

| 变量名 | 说明 |
|--------|------|
| DB_PASSWORD | 数据库密码 |
| WECHAT_APPID | 微信小程序 AppID |
| WECHAT_SECRET | 微信小程序 Secret |
| JWT_SECRET | JWT 密钥 |
| QR_AES_KEY | 二维码加密密钥（16 位） |

## 注意事项

1. 微信小程序需要配置合法的 AppID 和 Secret 才能正常登录
2. 生产环境请修改 JWT_SECRET 为复杂密钥
3. 二维码核销功能需要配置 HTTPS 域名
4. 支付功能需要接入微信支付 API

## 开发计划

- [ ] 数据统计页面
- [ ] 手工录入订单号核销
- [ ] 图片上传功能
- [ ] 微信支付接入
- [ ] 消息通知功能
