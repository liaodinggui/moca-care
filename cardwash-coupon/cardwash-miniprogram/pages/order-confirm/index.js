// pages/order-confirm/index.js
const { client } = require('../../api/index');

Page({
  data: {
    orderId: null,
    order: {}
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
      this.setData({ order });
    } catch (error) {
      console.error('加载订单详情失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  // 处理支付
  async handlePay() {
    wx.showModal({
      title: '确认支付',
      content: `确认支付 ¥${this.data.order.totalAmount} 吗？`,
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '支付中...' });
          try {
            await client.payOrder(this.data.orderId);
            wx.hideLoading();
            // 支付成功，跳转到订单详情页
            wx.showModal({
              title: '支付成功',
              content: '订单已支付，请在「我的」页面查看订单',
              showCancel: false,
              success: () => {
                wx.switchTab({
                  url: '/pages/order/list'
                });
              }
            });
          } catch (error) {
            wx.hideLoading();
            console.error('支付失败:', error);
          }
        }
      }
    });
  }
});
