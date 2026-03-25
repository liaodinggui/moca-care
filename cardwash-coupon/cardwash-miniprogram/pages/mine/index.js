// pages/mine/index.js
const { client } = require('../../api/index');
const app = getApp();

Page({
  data: {
    userInfo: null,
    pendingCount: 0,
    usingCount: 0,
    completedCount: 0
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    // 每次显示时重新加载用户信息和订单统计
    this.loadUserInfo();
    this.loadOrderStats();
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      const userInfo = await client.getUserInfo();
      console.log('获取到用户信息:', userInfo);
      this.setData({ userInfo });
      // 保存到全局
      app.globalData.userInfo = userInfo;
    } catch (error) {
      console.error('加载用户信息失败:', error);
      // 使用本地缓存
      const userInfo = app.globalData.userInfo || {};
      this.setData({ userInfo });
    }
  },

  // 加载订单统计
  async loadOrderStats() {
    try {
      const stats = await client.getOrderStats();
      this.setData({
        pendingCount: stats.pendingCount || 0,
        usingCount: stats.usingCount || 0,
        completedCount: stats.completedCount || 0
      });
    } catch (error) {
      console.error('加载订单统计失败:', error);
    }
  },

  // 编辑个人信息
  editProfile() {
    console.log('点击编辑个人信息');
    wx.navigateTo({
      url: '/pages/mine/profile-edit/index'
    });
  },

  // 跳转订单列表
  goToOrderList(e) {
    console.log('goToOrderList 被调用', e);
    const status = e.currentTarget.dataset.status;
    console.log('status:', status);
    // 通过全局变量传递状态
    app.globalData.orderStatusFilter = String(status);
    console.log('设置全局变量 orderStatusFilter:', app.globalData.orderStatusFilter);
    // 使用 wx.reLaunch 强制重新加载页面
    wx.reLaunch({ url: '/pages/order/list' });
  },

  // 返回首页
  goToHome() {
    wx.reLaunch({
      url: '/pages/index/index'
    });
  },

  // 联系客服
  contact() {
    wx.makePhoneCall({
      phoneNumber: '12345678901'
    });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
          wx.reLaunch({
            url: '/pages/login/index'
          });
        }
      }
    });
  }
});
