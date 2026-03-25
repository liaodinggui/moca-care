// pages/merchant/coupon-list.js
const { merchant } = require('../../api/index');

Page({
  data: {
    couponList: []
  },

  onLoad() {
    this.loadCouponList();
  },

  onShow() {
    this.loadCouponList();
  },

  // 加载洗车券列表
  async loadCouponList() {
    try {
      const list = await merchant.getCoupons();
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
      console.error('加载洗车券列表失败:', error);
    }
  },

  // 编辑洗车券
  editCoupon(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/merchant/coupon-edit?id=${id}`
    });
  },

  // 切换上下架状态
  async toggleStatus(e) {
    const id = e.currentTarget.dataset.id;
    const status = e.currentTarget.dataset.status;
    const newStatus = status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '上架' : '下架';

    wx.showModal({
      title: '确认操作',
      content: `确定要${actionText}该洗车券吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await merchant.updateCouponStatus(id, newStatus);
            wx.showToast({ title: '操作成功', icon: 'success' });
            this.loadCouponList();
          } catch (error) {
            console.error('操作失败:', error);
          }
        }
      }
    });
  },

  // 创建新洗车券
  goToEdit() {
    wx.navigateTo({
      url: '/pages/merchant/coupon-edit'
    });
  }
});
