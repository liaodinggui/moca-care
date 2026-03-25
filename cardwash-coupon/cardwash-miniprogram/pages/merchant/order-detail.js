// pages/merchant/order-detail.js
const { merchant } = require('../../api/index');

Page({
  data: {
    orderId: null,
    orderDetail: {}
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
      const detail = await merchant.getOrderDetail(this.data.orderId);

      // 添加状态文本
      const statusTextMap = {
        0: '待付款',
        1: '使用中',
        2: '已完成',
        3: '已取消'
      };
      detail.statusText = statusTextMap[detail.status] || '未知';

      this.setData({ orderDetail: detail });
    } catch (error) {
      console.error('加载订单详情失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  }
});
