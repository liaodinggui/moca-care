// pages/login/index.js
const app = getApp();
const { auth, merchant } = require('../../api/index');

Page({
  data: {
    showRoleModal: false,
    showMerchantModal: false,
    merchantList: [],
    selectedMerchantId: null,
    userId: null,
    hasMerchantRole: false
  },

  onLoad() {
    // 清除之前选中的店铺
    this.setData({ selectedMerchantId: null });

    // 检查是否已登录
    const token = app.globalData.token;
    if (token) {
      // 已登录，根据角色跳转到对应的首页
      this.redirectByRole();
      return;
    }

    // 开发模式：自动模拟登录
    if (app.globalData.devMode) {
      this.devAutoLogin();
    }
  },

  // 根据角色跳转
  redirectByRole() {
    const hasMerchantRole = app.globalData.hasMerchantRole || false;
    if (hasMerchantRole) {
      // 商家角色，跳转到商家首页
      wx.reLaunch({ url: '/pages/merchant/index' });
    } else {
      // 客户角色，跳转到客户首页
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  // 开发模式自动登录
  async devAutoLogin() {
    wx.showLoading({ title: '模拟登录中...' });

    try {
      // 调用后端登录接口，使用测试账号
      const result = await auth.wechatLogin({
        code: 'dev_mock_code',
        nickname: '',
        avatar: ''
      });

      // 保存 token
      app.setToken(result.token);
      this.setData({
        userId: result.userId,
        hasMerchantRole: result.hasMerchantRole
      });
      app.globalData.hasMerchantRole = result.hasMerchantRole;

      wx.hideLoading();

      // 显示角色选择弹窗
      this.setData({ showRoleModal: true });
    } catch (error) {
      wx.hideLoading();
      console.error('模拟登录失败:', error);
      wx.showModal({
        title: '提示',
        content: '模拟登录失败，请手动登录',
        showCancel: false
      });
    }
  },

  // 微信登录（入口）
  async handleWechatLogin() {
    // 先获取用户信息（必须在用户点击事件中同步调用）
    try {
      const userInfoRes = await wx.getUserProfile({
        desc: '用于完善用户资料'
      });
      console.log('获取到用户信息:', userInfoRes.userInfo);

      // 获取到用户信息后，继续登录流程
      this.doLogin(userInfoRes.userInfo.nickName, userInfoRes.userInfo.avatarUrl);
    } catch (e) {
      console.log('用户拒绝授权，使用默认信息');
      console.log(e);
      // 用户拒绝授权，继续使用 code 登录
      this.doLogin('', '');
    }
  },

  // 执行登录流程
  async doLogin(nickname, avatar) {
    wx.showLoading({ title: '登录中...' });

    try {
      // 获取微信登录 code
      const res = await wx.login();
      console.log('微信登录 code:', res.code);

      // 调用后端登录接口
      const result = await auth.wechatLogin({
        code: res.code,
        nickname: nickname,
        avatar: avatar
      });

      console.log('登录成功:', result);

      // 保存 token
      app.setToken(result.token);
      this.setData({
        userId: result.userId,
        hasMerchantRole: result.hasMerchantRole
      });
      app.globalData.hasMerchantRole = result.hasMerchantRole;

      wx.hideLoading();

      // 显示角色选择弹窗
      this.setData({ showRoleModal: true });
    } catch (error) {
      wx.hideLoading();
      console.error('登录失败:', error);
    }
  },

  // 选择角色
  async selectRole(e) {
    const role = e.currentTarget.dataset.role;
    console.log('选择角色:', role);

    // 如果选择客户角色，直接跳转
    if (role === '1' || parseInt(role) === 1) {
      wx.showLoading({ title: '设置中...' });
      try {
        // 更新用户角色为客户
        await auth.roleSelect({
          role: 1,
          merchantName: ''
        });

        wx.hideLoading();
        // 更新全局状态
        app.globalData.hasMerchantRole = false;
        this.setData({ showRoleModal: false });
        wx.reLaunch({ url: '/pages/index/index' });
      } catch (error) {
        wx.hideLoading();
        console.error('设置角色失败:', error);
      }
      return;
    }

    // 如果选择商家角色
    if (role === '2' || parseInt(role) === 2) {
      this.setData({ showRoleModal: false });

      // 检查是否有商家身份
      if (this.data.hasMerchantRole) {
        // 有商家身份，加载店铺列表
        this.loadMerchantList();
        this.setData({ showMerchantModal: true });
      } else {
        // 没有商家身份，直接输入新店铺名称
        this.inputNewMerchant();
      }
    }
  },

  // 输入新店铺名称
  inputNewMerchant() {
    wx.showModal({
      title: '输入店铺名称',
      editable: true,
      placeholderText: '请输入您的店铺名称',
      success: async (res) => {
        if (res.confirm && res.content) {
          wx.showLoading({ title: '创建中...' });

          try {
            // 调用后端接口创建商家
            await auth.roleSelect({
              role: 2,
              merchantName: res.content
            });

            wx.hideLoading();
            app.globalData.hasMerchantRole = true;

            // 重新加载商家列表获取新创建的店铺 ID
            const list = await merchant.getShops();
            if (list && list.length > 0) {
              // 获取最后创建的店铺（通常是列表中最新的）
              const newShop = list[0];
              app.globalData.selectedMerchantId = newShop.id;
            }

            // 跳转到商家首页
            wx.reLaunch({ url: '/pages/merchant/index' });
          } catch (error) {
            wx.hideLoading();
            console.error('创建商家失败:', error);
            wx.showToast({
              title: '创建失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // 加载商家列表
  async loadMerchantList() {
    try {
      const list = await merchant.getShops();
      this.setData({
        merchantList: list || [],
        // 清空选中的店铺，强制用户重新选择
        selectedMerchantId: null
      });
    } catch (error) {
      console.error('加载商家列表失败:', error);
      this.setData({ merchantList: [] });
    }
  },

  // 选择商家
  selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.id;
    this.setData({ selectedMerchantId: merchantId });
    console.log('选中店铺 ID:', merchantId);
  },

  // 确认选择商家
  confirmSelectMerchant() {
    if (!this.data.selectedMerchantId) {
      wx.showToast({ title: '请选择店铺', icon: 'none' });
      return;
    }

    this.setData({ showMerchantModal: false });

    // 保存选中的店铺 ID 到全局变量
    app.globalData.selectedMerchantId = this.data.selectedMerchantId;

    // 跳转到商家首页
    wx.reLaunch({ url: '/pages/merchant/index' });
  },

  // 添加新店铺
  addNewMerchant() {
    this.setData({ showMerchantModal: false });
    this.inputNewMerchant();
  },

  closeRoleModal() {
    this.setData({ showRoleModal: false });
  },

  closeMerchantModal() {
    this.setData({ showMerchantModal: false });
  }
});
