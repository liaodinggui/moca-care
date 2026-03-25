// pages/merchant/scan-result.js
const { merchant } = require('../../api/index');

Page({
  data: {
    orderInfo: {},
    writeOffQty: 1
  },

  onLoad(options) {
    if (options.orderInfo) {
      const orderInfo = JSON.parse(decodeURIComponent(options.orderInfo));
      this.setData({
        orderInfo,
        writeOffQty: 1
      });
    }
  },

  // 减少数量
  decreaseQty() {
    if (this.data.writeOffQty > 1) {
      this.setData({ writeOffQty: this.data.writeOffQty - 1 });
    }
  },

  // 增加数量
  increaseQty() {
    if (this.data.writeOffQty < this.data.orderInfo.remainingQty) {
      this.setData({ writeOffQty: this.data.writeOffQty + 1 });
    }
  },

  // 设置最大数量
  setMaxQty() {
    this.setData({ writeOffQty: this.data.orderInfo.remainingQty });
  },

  // 提交核销
  async submitWriteOff() {
    if (this.data.writeOffQty > this.data.orderInfo.remainingQty) {
      wx.showToast({
        title: '核销数量超过剩余数量',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '核销中...' });

    try {
      const result = await merchant.writeOff({
        orderId: this.data.orderInfo.orderId,
        quantity: this.data.writeOffQty
      });

      wx.hideLoading();

      // 显示结果
      if (result.completed) {
        wx.showModal({
          title: '核销完成',
          content: result.message,
          showCancel: false,
          success: () => {
            wx.navigateBack();
          }
        });
      } else {
        wx.showModal({
          title: '核销成功',
          content: result.message,
          showCancel: false,
          success: () => {
            wx.navigateBack();
          }
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('核销失败:', error);
    }
  }
});
