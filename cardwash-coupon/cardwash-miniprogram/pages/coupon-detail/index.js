// pages/coupon-detail/index.js
const { client } = require('../../api/index');

Page({
  data: {
    couponId: null,
    coupon: {},
    images: [],
    quantity: 1,
    totalPrice: '0.00'
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ couponId: options.id });
      this.loadCouponDetail();
    }
  },

  // 加载优惠券详情
  async loadCouponDetail() {
    try {
      const coupon = await client.getCouponDetail(this.data.couponId);

      console.log('优惠券详情:', coupon);
      console.log('coupon.price 类型:', typeof coupon.price);
      console.log('coupon.price 值:', coupon.price);

      // 解析图片
      let images = ['/images/default-coupon.png'];
      if (coupon.images) {
        try {
          images = JSON.parse(coupon.images);
        } catch (e) {
          images = [coupon.images];
        }
      }

      // 确保 price 是数字类型（后端可能返回字符串）
      if (coupon.price && typeof coupon.price === 'string') {
        coupon.price = parseFloat(coupon.price);
      }

      this.setData({
        coupon,
        images: images.length > 0 ? images : ['/images/default-coupon.png']
      });

      // 计算初始总价
      this.calculateTotalPrice();
    } catch (error) {
      console.error('加载优惠券详情失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  // 减少数量
  decreaseQty() {
    if (this.data.quantity > 1) {
      this.setData({ quantity: this.data.quantity - 1 }, () => {
        this.calculateTotalPrice();
      });
    }
  },

  // 增加数量
  increaseQty() {
    this.setData({ quantity: this.data.quantity + 1 }, () => {
      this.calculateTotalPrice();
    });
  },

  // 计算总价
  calculateTotalPrice() {
    const coupon = this.data.coupon;
    const quantity = this.data.quantity;

    if (!coupon || !coupon.price) {
      this.setData({ totalPrice: '0.00' });
      return;
    }

    // 确保 price 是数字类型
    const price = typeof coupon.price === 'string' ? parseFloat(coupon.price) : coupon.price;
    const total = (price * quantity).toFixed(2);

    this.setData({ totalPrice: total });
  },

  // 提交订单
  async submitOrder() {
    console.log('提交订单，couponId:', this.data.couponId, 'quantity:', this.data.quantity);

    wx.showLoading({ title: '创建订单中...' });

    try {
      const order = await client.createOrder({
        couponId: this.data.couponId,
        quantity: this.data.quantity
      });

      wx.hideLoading();

      console.log('订单创建成功:', order);

      // 跳转到订单支付页面
      wx.navigateTo({
        url: `/pages/order-confirm/index?id=${order.id}`
      });
    } catch (error) {
      wx.hideLoading();
      console.error('创建订单失败:', error);
    }
  }
});
