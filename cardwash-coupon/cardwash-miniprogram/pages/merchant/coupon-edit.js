// pages/merchant/coupon-edit.js
const { merchant } = require('../../api/index');

Page({
  data: {
    couponId: null,
    formData: {
      name: '',
      price: '',
      buyAmount: 0,
      sendAmount: 0,
      stock: 100,
      description: '',
      images: ''
    },
    imageUrl: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ couponId: options.id });
      this.loadCouponDetail();
    }
  },

  // 加载洗车券详情
  async loadCouponDetail() {
    try {
      const coupon = await merchant.getCouponDetail(this.data.couponId);
      this.setData({
        formData: {
          name: coupon.name,
          price: String(coupon.price),
          buyAmount: coupon.buyAmount || 0,
          sendAmount: coupon.sendAmount || 0,
          stock: String(coupon.stock),
          description: coupon.description || '',
          images: coupon.images || ''
        },
        imageUrl: coupon.images ? JSON.parse(coupon.images)[0] : ''
      });
    } catch (error) {
      console.error('加载洗车券详情失败:', error);
    }
  },

  // 输入处理
  onInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    this.setData({
      [`formData.${field}`]: value
    });
  },

  // 选择图片
  chooseImage() {
    wx.chooseImage({
      count: 1,
      success: (res) => {
        this.setData({
          imageUrl: res.tempFilePaths[0]
        });
        // 这里可以添加上传图片到服务器的逻辑
      }
    });
  },

  // 提交表单
  async submitForm() {
    const { name, price, stock } = this.data.formData;

    if (!name) {
      wx.showToast({ title: '请输入名称', icon: 'none' });
      return;
    }
    if (!price) {
      wx.showToast({ title: '请输入价格', icon: 'none' });
      return;
    }
    if (!stock) {
      wx.showToast({ title: '请输入库存', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    try {
      const data = {
        ...this.data.formData,
        price: parseFloat(this.data.formData.price),
        stock: parseInt(this.data.formData.stock),
        buyAmount: parseInt(this.data.formData.buyAmount) || 0,
        sendAmount: parseInt(this.data.formData.sendAmount) || 0,
        images: this.data.imageUrl ? JSON.stringify([this.data.imageUrl]) : ''
      };

      if (this.data.couponId) {
        await merchant.updateCoupon(this.data.couponId, data);
      } else {
        await merchant.createCoupon(data);
      }

      wx.hideLoading();
      wx.showToast({ title: '保存成功', icon: 'success' });

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      console.error('保存失败:', error);
    }
  }
});
