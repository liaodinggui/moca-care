// utils/request.js
const app = getApp();

/**
 * 封装网络请求
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const token = app.getToken();

    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data);
          } else if (res.data.code === 401) {
            // Token 失效，跳转登录
            wx.removeStorageSync('token');
            app.globalData.token = null;
            wx.reLaunch({
              url: '/pages/login/index'
            });
            reject(new Error('登录已过期'));
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            });
            reject(new Error(res.data.message));
          }
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
          reject(new Error('网络错误'));
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络异常',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

module.exports = {
  request
};
