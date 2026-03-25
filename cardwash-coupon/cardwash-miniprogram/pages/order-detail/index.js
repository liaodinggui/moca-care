// pages/order-detail/index.js
const { client, merchant } = require('../../api/index');
const app = getApp();

Page({
  data: {
    orderId: null,
    order: {},
    qrcodeUrl: '',
    writeOffRecords: [],
    progressPercent: 0
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ orderId: options.id });
      this.loadOrderDetail();
    }
  },

  // 加载订单详情
  async loadOrderDetail() {
    try {
      const order = await client.getOrderDetail(this.data.orderId);

      // 计算进度百分比
      const progressPercent = order.totalQuantity > 0
        ? Math.round((order.usedQuantity / order.totalQuantity) * 100)
        : 0;

      this.setData({
        order,
        progressPercent
      });

      // 如果是使用中状态，加载二维码和核销记录
      if (order.status === 1) {
        this.loadQrCode();
        this.loadWriteOffRecords();
      }
    } catch (error) {
      console.error('加载订单详情失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  // 加载二维码
  async loadQrCode() {
    try {
      // 调用后端接口获取 token
      const token = await client.getQrCode(this.data.orderId);
      console.log('获取到 token:', token);

      // 使用二维码生成 API 生成二维码图片
      // 注意：实际项目中应该使用后端提供的二维码图片接口
      const qrcodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=${encodeURIComponent(token)}`;

      this.setData({ qrcodeUrl });
    } catch (error) {
      console.error('加载二维码失败:', error);
      wx.showToast({
        title: '二维码加载失败',
        icon: 'none'
      });
    }
  },

  // 加载核销记录
  async loadWriteOffRecords() {
    try {
      // 调用后端接口获取核销记录
      const records = await merchant.getWriteOffRecords(this.data.orderId);
      this.setData({ writeOffRecords: records || [] });
    } catch (error) {
      console.error('加载核销记录失败:', error);
      // 加载失败不影响主流程，使用空数组
      this.setData({ writeOffRecords: [] });
    }
  },

  // 分享
  onShareAppMessage() {
    return {
      title: '我的洗车券订单',
      path: `/pages/order-detail/index?id=${this.data.orderId}`
    };
  }
});
