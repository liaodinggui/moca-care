// pages/pay-result/index.js
Page({
  data: {
    amount: '',
    orderNo: '',
    orderId: ''
  },

  onLoad(options) {
    this.setData({
      amount: options.amount || '0.00',
      orderNo: options.orderNo || '',
      orderId: options.orderId || ''
    });
  },

  // 查看订单详情
  viewOrder() {
    if (this.data.orderId) {
      wx.navigateTo({
        url: `/pages/order-detail/index?id=${this.data.orderId}`
      });
    } else {
      wx.navigateTo({
        url: '/pages/order/list'
      });
    }
  },

  // 返回首页
  goHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
