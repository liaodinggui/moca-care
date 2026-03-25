// pages/merchant/scan.js
const { merchant } = require('../../api/index');

Page({
  data: {
    scanned: false
  },

  // 开始扫码
  startScan() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码成功:', res.result);
        // 解析二维码内容
        this.parseQrCode(res.result);
      },
      fail: (err) => {
        console.error('扫码失败:', err);
        wx.showToast({
          title: '扫码取消',
          icon: 'none'
        });
      }
    });
  },

  // 解析二维码
  async parseQrCode(qrContent) {
    wx.showLoading({ title: '解析中...' });

    try {
      const orderInfo = await merchant.parseQrCode({ qrContent });
      wx.hideLoading();

      // 跳转到扫码结果页
      wx.navigateTo({
        url: `/pages/merchant/scan-result?orderInfo=${encodeURIComponent(JSON.stringify(orderInfo))}`
      });
    } catch (error) {
      wx.hideLoading();
      console.error('解析二维码失败:', error);
    }
  },

  // 手动输入
  manualInput() {
    wx.showModal({
      title: '输入订单号',
      editable: true,
      placeholderText: '请输入订单号',
      success: (res) => {
        if (res.confirm && res.content) {
          // 这里可以添加根据订单号查询订单的逻辑
          wx.showToast({
            title: '功能开发中',
            icon: 'none'
          });
        }
      }
    });
  }
});
