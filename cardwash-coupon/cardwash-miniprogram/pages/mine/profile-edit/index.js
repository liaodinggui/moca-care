// pages/mine/profile-edit/index.js
const { client } = require('../../../api/index');
const app = getApp();

Page({
  data: {
    formData: {
      nickname: '',
      phone: '',
      avatar: ''
    }
  },

  onLoad() {
    this.loadUserInfo();
  },

  // 加载用户信息
  async loadUserInfo() {
    try {
      const userInfo = await client.getUserInfo();
      this.setData({
        formData: {
          nickname: userInfo.nickname || '',
          phone: userInfo.phone || '',
          avatar: userInfo.avatar || ''
        }
      });
    } catch (error) {
      console.error('加载用户信息失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
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

  // 选择头像
  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      success: (res) => {
        this.setData({
          'formData.avatar': res.tempFilePaths[0]
        });
        // TODO: 这里可以添加上传图片到服务器的逻辑
        wx.showToast({ title: '头像已选择', icon: 'success' });
      }
    });
  },

  // 保存用户信息
  async saveProfile() {
    const { nickname, phone } = this.data.formData;

    if (!nickname || nickname.trim() === '') {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }

    // 验证手机号
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    try {
      await client.updateProfile({
        nickname: nickname.trim(),
        phone: phone || null,
        avatar: this.data.formData.avatar
      });

      wx.hideLoading();
      wx.showToast({ title: '保存成功', icon: 'success' });

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      console.error('保存失败:', error);
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  }
});
