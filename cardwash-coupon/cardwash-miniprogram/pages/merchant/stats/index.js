// pages/merchant/stats/index.js
const app = getApp();
const { merchant } = require('../../../api/index');

Page({
  data: {
    startDate: '',
    endDate: '',
    totalWriteOff: 0,
    totalRevenue: '0',
    dailyList: []
  },

  onLoad() {
    // 设置默认日期为近 7 天
    this.setDefaultDateRange();
  },

  onShow() {
    // 确保是商家角色
    if (!app.globalData.hasMerchantRole) {
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  // 格式化日期为 yyyy-MM-dd
  formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  // 设置默认日期范围（近 7 天）
  setDefaultDateRange() {
    const today = new Date();
    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(today.getDate() - 6);

    const startDate = this.formatDate(sevenDaysAgo);
    const endDate = this.formatDate(today);

    this.setData({ startDate, endDate }, () => {
      this.loadStats();
    });
  },

  // 选择开始日期
  selectStartDate() {
    this.showDatePicker('start');
  },

  // 选择结束日期
  selectEndDate() {
    this.showDatePicker('end');
  },

  // 显示日期选择器
  showDatePicker(type) {
    const currentDate = type === 'start' ? this.data.startDate : this.data.endDate;
    const title = type === 'start' ? '选择开始日期' : '选择结束日期';

    // 使用 wx.showModal 配合手动输入
    wx.showModal({
      title: title,
      editable: true,
      placeholderText: '格式：YYYY-MM-DD',
      content: currentDate,
      success: (res) => {
        if (res.confirm && res.content) {
          // 验证日期格式
          const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
          if (dateRegex.test(res.content)) {
            const data = {};
            data[type + 'Date'] = res.content;
            this.setData(data);
          } else {
            wx.showToast({ title: '格式错误', content: '请输入 YYYY-MM-DD', icon: 'none' });
          }
        }
      }
    });
  },

  // 加载统计数据
  async loadStats() {
    wx.showLoading({ title: '加载中...' });

    try {
      const res = await merchant.getStats({
        startDate: this.data.startDate,
        endDate: this.data.endDate
      });

      console.log('统计数据:', res);

      const stats = res || {};

      // 构建每日数据列表
      const dailyList = [];
      const dates = stats.dates || [];
      const writeOffCounts = stats.writeOffCounts || [];
      const revenues = stats.revenues || [];

      for (let i = 0; i < dates.length; i++) {
        dailyList.push({
          date: dates[i],
          writeOffCount: writeOffCounts[i] || 0,
          revenue: revenues[i] || '0'
        });
      }

      this.setData({
        totalWriteOff: stats.totalWriteOff || 0,
        totalRevenue: stats.totalRevenue || '0',
        dailyList
      }, () => {
        // 延迟绘制图表，确保 DOM 已渲染
        setTimeout(() => {
          this.drawWriteOffChart();
          this.drawRevenueChart();
        }, 100);
      });

      wx.hideLoading();
    } catch (error) {
      wx.hideLoading();
      console.error('加载统计数据失败:', error);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  // 绘制核销数量图表
  drawWriteOffChart() {
    const query = wx.createSelectorQuery();
    query.select('#writeOffChart')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) {
          console.error('未找到 canvas 节点');
          return;
        }

        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');

        const dpr = wx.getSystemInfoSync().pixelRatio;
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const { dailyList } = this.data;
        const labels = dailyList.map(item => item.date.slice(5)); // 只显示 MM-dd
        const data = dailyList.map(item => item.writeOffCount);

        this.drawBarChart(ctx, canvas.width / dpr, canvas.height / dpr, labels, data, '#1890FF', '核销数量');
      });
  },

  // 绘制营业额图表
  drawRevenueChart() {
    const query = wx.createSelectorQuery();
    query.select('#revenueChart')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) {
          console.error('未找到 canvas 节点');
          return;
        }

        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');

        const dpr = wx.getSystemInfoSync().pixelRatio;
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const { dailyList } = this.data;
        const labels = dailyList.map(item => item.date.slice(5)); // 只显示 MM-dd
        const data = dailyList.map(item => parseFloat(item.revenue));

        this.drawBarChart(ctx, canvas.width / dpr, canvas.height / dpr, labels, data, '#52C41A', '营业额');
      });
  },

  // 绘制柱状图
  drawBarChart(ctx, width, height, labels, data, color, label) {
    const padding = { top: 20, right: 20, bottom: 40, left: 50 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // 清空画布
    ctx.clearRect(0, 0, width, height);

    const maxVal = Math.max(...data, 1);
    const barWidth = (chartWidth / labels.length) * 0.6;
    const gap = (chartWidth / labels.length) * 0.4;

    // 绘制坐标轴
    ctx.beginPath();
    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = 1;
    ctx.moveTo(padding.left, padding.top);
    ctx.lineTo(padding.left, height - padding.bottom);
    ctx.lineTo(width - padding.right, height - padding.bottom);
    ctx.stroke();

    // 绘制柱子和标签
    ctx.font = '12px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';

    labels.forEach((labelText, i) => {
      const x = padding.left + (chartWidth / labels.length) * i + (chartWidth / labels.length - barWidth) / 2;
      const barHeight = (data[i] / maxVal) * chartHeight;
      const y = height - padding.bottom - barHeight;

      // 绘制柱子
      ctx.fillStyle = color;
      ctx.fillRect(x, y, barWidth, barHeight);

      // 绘制数值
      ctx.fillStyle = '#333';
      ctx.fillText(data[i].toString(), x + barWidth / 2, y - 15);

      // 绘制日期标签
      ctx.fillStyle = '#666';
      ctx.fillText(labelText, x + barWidth / 2, height - padding.bottom + 10);
    });

    // 绘制 Y 轴刻度
    ctx.textAlign = 'right';
    ctx.textBaseline = 'middle';
    for (let i = 0; i <= 4; i++) {
      const val = (maxVal / 4) * i;
      const y = height - padding.bottom - (chartHeight / 4) * i;
      ctx.fillStyle = '#999';
      ctx.fillText(val.toFixed(0), padding.left - 5, y);

      // 绘制网格线
      ctx.beginPath();
      ctx.strokeStyle = '#f0f0f0';
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();
    }
  }
});
