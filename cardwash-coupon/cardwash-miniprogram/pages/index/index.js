// pages/index/index.js
const { client } = require('../../api/index');

Page({
  data: {
    couponList: []
  },

  onLoad() {
    this.loadCoupons();
  },

  onShow() {
    // 每次显示刷新列表
    this.loadCoupons();
  },

  // 加载洗车券列表
  async loadCoupons() {
    try {
      const list = await client.getCoupons();
      // 处理图片数据，解析 JSON 字符串
      const processedList = list.map(item => {
        let firstImage = '/images/default-coupon.png';
        if (item.images) {
          try {
            const images = JSON.parse(item.images);
            if (images && images.length > 0) {
              firstImage = images[0];
            }
          } catch (e) {
            console.error('解析图片失败:', e);
          }
        }
        return {
          ...item,
          firstImage: firstImage
        };
      });
      this.setData({ couponList: processedList });
    } catch (error) {
      console.error('加载洗车券失败:', error);
    }
  },

  // 跳转详情页
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/coupon-detail/index?id=${id}`
    });
  }
});
