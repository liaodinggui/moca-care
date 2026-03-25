// pages/order/list.js
const { client } = require('../../api/index');
const app = getApp();

Page({
  data: {
    statusFilter: '',
    orderList: []
  },

  onLoad(options) {
    console.log('订单列表 onLoad options:', options);
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
    if (app.globalData.orderStatusFilter !== undefined) {
      const status = app.globalData.orderStatusFilter;
      console.log('onShow 读取全局状态:', status);
      this.setData({ statusFilter: status }, () => {
        this.loadOrderList();
      });
      // 清空全局变量，避免影响后续操作
      app.globalData.orderStatusFilter = '';
    } else {
      this.loadOrderList();
    }
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
  },

  // 去支付
  goToPay(e) {
    const id = e.currentTarget.dataset.id;
    wx.showLoading({ title: '支付中...' });
    client.payOrder(id)
      .then(() => {
        wx.hideLoading();
        wx.showToast({ title: '支付成功', icon: 'success' });
        this.loadOrderList();
      })
      .catch(() => {
        wx.hideLoading();
      });
  },

  // 查看详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order-detail/index?id=${id}`
    });
  }
});
