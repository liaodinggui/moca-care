// api/index.js
const { request } = require('../utils/request');

// 认证接口
const auth = {
  // 微信登录
  wechatLogin: (data) => request({ url: '/auth/wechat-login', method: 'POST', data }),
  // 选择角色
  roleSelect: (data) => request({ url: '/auth/role-select', method: 'POST', data })
};

// 用户端接口
const client = {
  // 获取洗车券列表
  getCoupons: () => request({ url: '/client/coupons' }),
  // 获取洗车券详情
  getCouponDetail: (id) => request({ url: `/client/coupons/${id}` }),
  // 创建订单
  createOrder: (data) => request({ url: '/client/orders', method: 'POST', data }),
  // 支付订单
  payOrder: (id) => request({ url: `/client/orders/${id}/pay`, method: 'POST' }),
  // 获取订单列表
  getOrders: (status) => request({ url: `/client/orders${status !== undefined ? `?status=${status}` : ''}` }),
  // 获取订单详情
  getOrderDetail: (id) => request({ url: `/client/orders/${id}` }),
  // 获取订单统计
  getOrderStats: () => request({ url: '/client/orders/stats' }),
  // 获取订单二维码
  getQrCode: (id) => request({ url: `/client/orders/${id}/qrcode` }),
  // 获取用户信息
  getUserInfo: () => request({ url: '/client/user/info' }),
  // 更新用户信息
  updateProfile: (data) => request({ url: '/client/user/profile', method: 'POST', data })
};

// 商家端接口
const merchant = {
  // 获取商家列表
  getShops: () => request({ url: '/merchant/shops' }),
  // 获取洗车券列表
  getCoupons: () => request({ url: '/merchant/coupons' }),
  // 获取洗车券详情
  getCouponDetail: (id) => request({ url: `/merchant/coupons/${id}` }),
  // 创建洗车券
  createCoupon: (data) => request({ url: '/merchant/coupons', method: 'POST', data }),
  // 更新洗车券
  updateCoupon: (id, data) => request({ url: `/merchant/coupons/${id}`, method: 'PUT', data }),
  // 上下架洗车券
  updateCouponStatus: (id, status) => request({ url: `/merchant/coupons/${id}/status?status=${status}`, method: 'PUT' }),
  // 获取订单列表
  getOrders: (status, pageNum = 1, pageSize = 10) => request({
    url: `/merchant/orders?status=${status || ''}&pageNum=${pageNum}&pageSize=${pageSize}`
  }),
  // 获取订单详情
  getOrderDetail: (id) => request({ url: `/merchant/orders/${id}` }),
  // 解析二维码
  parseQrCode: (data) => request({ url: '/merchant/scan/parse', method: 'POST', data }),
  // 核销洗车券
  writeOff: (data) => request({ url: '/merchant/write-off', method: 'POST', data }),
  // 获取核销记录
  getWriteOffRecords: (orderId) => request({ url: `/merchant/write-off/records?orderId=${orderId}` }),
  // 获取统计数据
  getStats: (params) => request({ url: `/merchant/stats?startDate=${params.startDate}&endDate=${params.endDate}` }),
  // 获取今日数据
  getTodayStats: () => request({ url: '/merchant/today-stats' })
};

module.exports = {
  auth,
  client,
  merchant
};
