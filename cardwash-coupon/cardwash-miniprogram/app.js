// app.js
App({
  onLaunch() {
    // 检查登录状态
    this.checkLogin();
  },

  globalData: {
    userInfo: null,
    token: null,
    hasMerchantRole: false,
    selectedMerchantId: null,  // 选中的店铺 ID
    orderStatusFilter: '',  // 订单状态筛选（从「我的」页面跳转时传递）
    baseUrl: 'http://localhost:8080/api',
    devMode: false,  // 开发模式：跳过微信登录，直接模拟登录
    devUserId: 1    // 开发模式用户 ID
  },

  // 检查登录状态
  checkLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
  },

  // 设置 token
  setToken(token) {
    this.globalData.token = token;
    wx.setStorageSync('token', token);
  },

  // 获取 token
  getToken() {
    return this.globalData.token;
  },

  // 退出登录
  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.hasMerchantRole = false;
    this.globalData.selectedMerchantId = null;
    wx.removeStorageSync('token');
  }
})
