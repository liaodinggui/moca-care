// pages/merchant/index.js
const { merchant } = require('../../api/index');
const app = getApp();

Page({
  data: {
    merchantName: '我的店铺',
    merchantId: null,
    todayStats: {
      totalSales: 0,
      usedWriteOff: 0,
      revenue: '0.00'
    }
  },

  onShow() {
    // 加载商家数据
    this.loadMerchantInfo();
    // 加载今日数据
    this.loadTodayStats();
  },

  // 加载商家信息
  async loadMerchantInfo() {
    try {
      console.log('开始加载商家信息，token:', app.globalData.token);
      const list = await merchant.getShops();
      console.log('获取到商家列表:', list);

      if (list && list.length > 0) {
        // 优先使用选中的店铺 ID
        const selectedId = app.globalData.selectedMerchantId;
        let shop;

        if (selectedId) {
          // 查找选中的店铺
          shop = list.find(item => item.id === selectedId);
        }

        // 如果没有选中的店铺或选中的店铺不存在，使用第一个店铺
        if (!shop) {
          shop = list[0];
          app.globalData.selectedMerchantId = shop.id;
        }

        this.setData({
          merchantName: shop.name || '我的店铺',
          merchantId: shop.id
        });
      } else {
        console.log('商家列表为空');
        this.setData({
          merchantName: '我的店铺'
        });
      }
    } catch (error) {
      console.error('加载商家信息失败:', error);
      this.setData({
        merchantName: '我的店铺'
      });
    }
  },

  // 加载今日数据
  async loadTodayStats() {
    try {
      const stats = await merchant.getTodayStats();
      this.setData({
        todayStats: {
          totalSales: stats.totalSales || 0,
          usedWriteOff: stats.usedWriteOff || 0,
          revenue: stats.revenue || '0.00'
        }
      });
    } catch (error) {
      console.error('加载今日数据失败:', error);
    }
  },

  // 扫码核销
  scanCode() {
    wx.navigateTo({
      url: '/pages/merchant/scan'
    });
  },

  // 手工录入
  manualWriteOff() {
    wx.showModal({
      title: '手工录入',
      content: '此功能开发中',
      showCancel: false
    });
  },

  // 跳转订单列表
  goToOrderList() {
    wx.navigateTo({
      url: '/pages/merchant/order-list'
    });
  },

  // 跳转洗车券管理
  goToCouponList() {
    wx.navigateTo({
      url: '/pages/merchant/coupon-list'
    });
  },

  // 跳转数据统计
  goToStats() {
    wx.navigateTo({
      url: '/pages/merchant/stats/index'
    });
  },

  // 返回首页
  goToHome() {
    wx.reLaunch({
      url: '/pages/merchant/index'
    });
  },

  // 跳转店铺设置
  goToSettings() {
    wx.showModal({
      title: '店铺设置',
      content: '此功能开发中',
      showCancel: false
    });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除登录信息
          app.logout();
          // 清除选中的店铺 ID
          app.globalData.selectedMerchantId = null;
          // 跳转回登录页
          wx.reLaunch({
            url: '/pages/login/index'
          });
        }
      }
    });
  }
});
