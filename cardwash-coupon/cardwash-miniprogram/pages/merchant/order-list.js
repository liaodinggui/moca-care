// pages/merchant/order-list.js
const { merchant } = require('../../api/index');

Page({
  data: {
    statusFilter: '',
    orderList: []
  },

  onLoad() {
    this.loadOrderList();
  },

  onShow() {
    this.loadOrderList();
  },

  // 切换 Tab
  onTabChange(e) {
    const status = e.currentTarget.dataset.status;
    this.setData({ statusFilter: status });
    this.loadOrderList();
  },

  // 加载订单列表
  async loadOrderList() {
    try {
      const status = this.data.statusFilter;
      const list = await merchant.getOrders(status === '' ? undefined : parseInt(status));
      this.setData({ orderList: list });
    } catch (error) {
      console.error('加载订单列表失败:', error);
    }
  },

  // 去核销
  goToWriteOff(e) {
    const id = e.currentTarget.dataset.id;
    // 这里可以跳转到核销页面，或者扫码页面
    wx.navigateTo({
      url: `/pages/merchant/order-detail?id=${id}`
    });
  },

  // 查看详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/merchant/order-detail?id=${id}`
    });
  }
});
