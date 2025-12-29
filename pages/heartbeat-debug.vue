<template>
  <view class="debug-container">
    <!-- 顶部操作栏 -->
    <view class="toolbar">
      <button 
        class="tool-btn refresh-btn"
        @click="refreshLogs"
        :disabled="loading"
      >
        刷新日志
      </button>
      
      <button 
        class="tool-btn test-btn"
        @click="sendTestHeartbeat"
        :disabled="loading || !serviceRunning"
      >
        手动心跳
      </button>
      
      <button 
        class="tool-btn upload-btn"
        @click="uploadCachedHeartbeats"
        :disabled="loading || cachedCount === 0"
      >
        上传缓存({{ cachedCount }})
      </button>
      
      <button 
        class="tool-btn clear-btn"
        @click="clearLogs"
        :disabled="loading"
      >
        清空日志
      </button>
    </view>

    <!-- 服务状态卡片 -->
    <view class="status-card">
      <view class="status-row">
        <text class="status-label">服务状态:</text>
        <text :class="['status-value', serviceRunning ? 'running' : 'stopped']">
          {{ serviceRunning ? '运行中' : '已停止' }}
        </text>
      </view>
      
      <view class="status-row">
        <text class="status-label">心跳总数:</text>
        <text class="status-value">{{ totalHeartbeats }}</text>
      </view>
      
      <view class="status-row">
        <text class="status-label">日志数量:</text>
        <text class="status-value">{{ heartbeatLogs.length }} / 100</text>
      </view>
      
      <view class="status-row">
        <text class="status-label">缓存数量:</text>
        <text class="status-value">{{ cachedCount }}</text>
      </view>
      
      <view class="status-row">
        <text class="status-label">最后更新:</text>
        <text class="status-value">{{ lastUpdateTime }}</text>
      </view>
    </view>

    <!-- 筛选器 -->
    <view class="filter-section">
      <text class="filter-label">筛选:</text>
      <picker 
        mode="selector" 
        :range="filterOptions" 
        :value="filterIndex"
        @change="onFilterChange"
      >
        <view class="filter-picker">
          <text>{{ filterOptions[filterIndex] }}</text>
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
    </view>

    <!-- 心跳日志列表 -->
    <view class="logs-section">
      <text class="section-title">心跳日志 ({{ filteredLogs.length }}条)</text>
      
      <scroll-view 
        class="logs-scroll"
        scroll-y="true"
        @scrolltolower="loadMoreLogs"
      >
        <view 
          v-for="(log, index) in displayedLogs" 
          :key="index"
          class="log-card"
          @click="showLogDetail(log)"
        >
          <view class="log-header">
            <text class="log-time">{{ formatTime(log.timestamp) }}</text>
            <text :class="['log-status', log.status]">
              {{ getStatusText(log.status) }}
            </text>
          </view>
          
          <view class="log-content">
            <view class="log-item">
              <text class="log-key">设备ID:</text>
              <text class="log-value">{{ log.deviceId || 'N/A' }}</text>
            </view>
            
            <view class="log-item">
              <text class="log-key">应用版本:</text>
              <text class="log-value">{{ log.appVersion || 'N/A' }}</text>
            </view>
            
            <view class="log-item">
              <text class="log-key">系统版本:</text>
              <text class="log-value">{{ log.systemVersion || 'N/A' }}</text>
            </view>
            
            <view class="log-item">
              <text class="log-key">电池电量:</text>
              <text class="log-value">{{ log.batteryLevel || 'N/A' }}%</text>
            </view>
            
            <view class="log-item">
              <text class="log-key">网络类型:</text>
              <text class="log-value">{{ log.networkType || 'N/A' }}</text>
            </view>
          </view>
        </view>
        
        <view v-if="filteredLogs.length === 0" class="empty-state">
          <text class="empty-text">暂无心跳日志</text>
          <text class="empty-hint">启动保活服务后将自动记录</text>
        </view>
        
        <view v-if="hasMore" class="load-more">
          <text class="load-more-text">加载更多...</text>
        </view>
      </scroll-view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section">
      <text class="section-title">统计信息</text>
      
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-label">成功率</text>
          <text class="stat-value success">{{ statistics.successRate }}</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-label">失败次数</text>
          <text class="stat-value error">{{ statistics.failureCount }}</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-label">平均间隔</text>
          <text class="stat-value">{{ statistics.avgInterval }}</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-label">最大间隔</text>
          <text class="stat-value">{{ statistics.maxInterval }}</text>
        </view>
      </view>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script>
import KeepAliveManager from '@/common/KeepAliveManager.js';

export default {
  data() {
    return {
      keepAliveManager: null,
      loading: false,
      serviceRunning: false,
      totalHeartbeats: 0,
      cachedCount: 0,
      heartbeatLogs: [],
      displayedLogs: [],
      filterOptions: ['全部', '成功', '失败', '最近1小时', '最近24小时'],
      filterIndex: 0,
      pageSize: 20,
      currentPage: 1,
      hasMore: false,
      lastUpdateTime: '暂无',
      statistics: {
        successRate: '0%',
        failureCount: 0,
        avgInterval: '0s',
        maxInterval: '0s'
      }
    };
  },
  
  computed: {
    filteredLogs() {
      let logs = [...this.heartbeatLogs];
      
      switch (this.filterIndex) {
        case 1: // 成功
          logs = logs.filter(log => log.status === 'success');
          break;
        case 2: // 失败
          logs = logs.filter(log => log.status === 'failure');
          break;
        case 3: // 最近1小时
          const oneHourAgo = Date.now() - 3600000;
          logs = logs.filter(log => log.timestamp >= oneHourAgo);
          break;
        case 4: // 最近24小时
          const oneDayAgo = Date.now() - 86400000;
          logs = logs.filter(log => log.timestamp >= oneDayAgo);
          break;
      }
      
      return logs;
    }
  },
  
  onLoad() {
    this.initPage();
  },
  
  onShow() {
    this.refreshLogs();
  },
  
  onUnload() {
    // 清理事件监听
    uni.$off('heartbeat', this.onHeartbeatReceived);
  },
  
  methods: {
    /**
     * 初始化页面
     */
    async initPage() {
      try {
        // 获取全局保活管理器实例
        const app = getApp();
        if (app.globalData && app.globalData.keepAliveManager) {
          this.keepAliveManager = app.globalData.keepAliveManager;
        } else {
          this.keepAliveManager = new KeepAliveManager();
        }
        
        // 检查服务状态
        await this.checkServiceStatus();
        
        // 加载心跳日志
        await this.refreshLogs();
        
        // 获取缓存数量
        await this.updateCachedCount();
        
        // 设置事件监听
        this.setupEventListeners();
        
        // 计算统计信息
        this.calculateStatistics();
        
      } catch (error) {
        console.error('初始化页面失败:', error);
        uni.showToast({
          title: '初始化失败',
          icon: 'none'
        });
      }
    },
    
    /**
     * 检查服务状态
     */
    async checkServiceStatus() {
      try {
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          this.serviceRunning = false;
          return;
        }
        
        const status = await this.keepAliveManager.getStatus();
        this.serviceRunning = status.isRunning || false;
        this.totalHeartbeats = status.heartbeatCount || 0;
        
      } catch (error) {
        console.error('检查服务状态失败:', error);
        this.serviceRunning = false;
      }
    },
    
    /**
     * 刷新日志
     */
    async refreshLogs() {
      try {
        this.loading = true;
        
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          uni.showToast({
            title: '保活服务未初始化',
            icon: 'none'
          });
          return;
        }
        
        const result = await this.keepAliveManager.getHeartbeatLogs({
          count: 100
        });
        
        if (result.success && result.logs) {
          this.heartbeatLogs = result.logs.sort((a, b) => b.timestamp - a.timestamp);
          this.currentPage = 1;
          this.updateDisplayedLogs();
          this.calculateStatistics();
          
          this.lastUpdateTime = this.formatTime(Date.now());
          
          uni.showToast({
            title: `已加载 ${this.heartbeatLogs.length} 条日志`,
            icon: 'success'
          });
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'none'
          });
        }
        
      } catch (error) {
        console.error('刷新日志失败:', error);
        uni.showToast({
          title: '刷新失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 发送测试心跳
     */
    async sendTestHeartbeat() {
      try {
        this.loading = true;
        
        // 获取心跳服务实例
        const app = getApp();
        if (!app.globalData || !app.globalData.heartbeatService) {
          uni.showToast({
            title: '心跳服务未初始化',
            icon: 'none'
          });
          return;
        }
        
        const heartbeatService = app.globalData.heartbeatService;
        const result = await heartbeatService.manualHeartbeat();
        
        if (result.success) {
          uni.showToast({
            title: '测试心跳已发送',
            icon: 'success'
          });
          
          // 延迟刷新日志和缓存数量
          setTimeout(() => {
            this.refreshLogs();
            this.updateCachedCount();
          }, 1000);
        } else {
          uni.showToast({
            title: result.message || '发送失败',
            icon: 'none'
          });
        }
        
      } catch (error) {
        console.error('发送测试心跳失败:', error);
        uni.showToast({
          title: '发送失败: ' + error.message,
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 上传缓存的心跳数据（批量上传功能已移除）
     */
    async uploadCachedHeartbeats() {
      try {
        // 显示当前缓存数量
        const app = getApp();
        const cachedCount = (uni.getStorageSync('cached_heartbeats') || []).length;
        
        if (cachedCount > 0) {
          console.log(`当前有 ${cachedCount} 条缓存心跳数据`);
          uni.showToast({
            title: `批量上传功能已移除，当前有 ${cachedCount} 条缓存数据将在网络恢复后自动发送`,
            icon: 'none',
            duration: 3000
          });
        } else {
          uni.showToast({
            title: '批量上传功能已移除，缓存数据将在网络恢复后自动发送',
            icon: 'none',
            duration: 3000
          });
        }
        
        // 更新缓存数量显示
        await this.updateCachedCount();
      } catch (error) {
        console.error('获取缓存心跳数据失败:', error);
        uni.showToast({
          title: '获取缓存数据失败',
          icon: 'none'
        });
      }
    },
    
    /**
     * 更新缓存数量
     */
    async updateCachedCount() {
      try {
        // 获取心跳服务实例
        const app = getApp();
        if (!app.globalData || !app.globalData.heartbeatService) {
          this.cachedCount = 0;
          return;
        }
        
        const heartbeatService = app.globalData.heartbeatService;
        this.cachedCount = heartbeatService.getCachedHeartbeatCount();
        
      } catch (error) {
        console.error('更新缓存数量失败:', error);
        this.cachedCount = 0;
      }
    },
    
    /**
     * 清空日志
     */
    async clearLogs() {
      try {
        uni.showModal({
          title: '确认清空',
          content: '确定要清空所有心跳日志吗？',
          success: async (res) => {
            if (res.confirm) {
              this.loading = true;
              
              if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
                uni.showToast({
                  title: '保活服务未初始化',
                  icon: 'none'
                });
                return;
              }
              
              const result = await this.keepAliveManager.clearHeartbeatLogs();
              
              if (result.success) {
                this.heartbeatLogs = [];
                this.displayedLogs = [];
                this.calculateStatistics();
                
                uni.showToast({
                  title: '日志已清空',
                  icon: 'success'
                });
              } else {
                uni.showToast({
                  title: result.message || '清空失败',
                  icon: 'none'
                });
              }
              
              this.loading = false;
            }
          }
        });
        
      } catch (error) {
        console.error('清空日志失败:', error);
        uni.showToast({
          title: '清空失败',
          icon: 'none'
        });
        this.loading = false;
      }
    },
    
    /**
     * 筛选变化
     */
    onFilterChange(e) {
      this.filterIndex = e.detail.value;
      this.currentPage = 1;
      this.updateDisplayedLogs();
    },
    
    /**
     * 更新显示的日志
     */
    updateDisplayedLogs() {
      const start = 0;
      const end = this.currentPage * this.pageSize;
      this.displayedLogs = this.filteredLogs.slice(start, end);
      this.hasMore = end < this.filteredLogs.length;
    },
    
    /**
     * 加载更多日志
     */
    loadMoreLogs() {
      if (this.hasMore && !this.loading) {
        this.currentPage++;
        this.updateDisplayedLogs();
      }
    },
    
    /**
     * 显示日志详情
     */
    showLogDetail(log) {
      const content = `设备ID: ${log.deviceId || 'N/A'}\n` +
                     `应用版本: ${log.appVersion || 'N/A'}\n` +
                     `系统版本: ${log.systemVersion || 'N/A'}\n` +
                     `设备型号: ${log.deviceModel || 'N/A'}\n` +
                     `保活状态: ${log.keepAliveStatus || 'N/A'}\n` +
                     `电池电量: ${log.batteryLevel || 'N/A'}%\n` +
                     `网络类型: ${log.networkType || 'N/A'}\n` +
                     `屏幕尺寸: ${log.screenWidth || 'N/A'}x${log.screenHeight || 'N/A'}\n` +
                     `时间戳: ${this.formatTime(log.timestamp)}`;
      
      uni.showModal({
        title: '心跳详情',
        content: content,
        showCancel: false
      });
    },
    
    /**
     * 计算统计信息
     */
    calculateStatistics() {
      if (this.heartbeatLogs.length === 0) {
        this.statistics = {
          successRate: '0%',
          failureCount: 0,
          avgInterval: '0s',
          maxInterval: '0s'
        };
        return;
      }
      
      // 计算成功率
      const successCount = this.heartbeatLogs.filter(log => log.status === 'success').length;
      const successRate = ((successCount / this.heartbeatLogs.length) * 100).toFixed(1);
      
      // 计算失败次数
      const failureCount = this.heartbeatLogs.filter(log => log.status === 'failure').length;
      
      // 计算平均间隔和最大间隔
      let totalInterval = 0;
      let maxInterval = 0;
      let intervalCount = 0;
      
      for (let i = 0; i < this.heartbeatLogs.length - 1; i++) {
        const interval = this.heartbeatLogs[i].timestamp - this.heartbeatLogs[i + 1].timestamp;
        if (interval > 0) {
          totalInterval += interval;
          intervalCount++;
          if (interval > maxInterval) {
            maxInterval = interval;
          }
        }
      }
      
      const avgInterval = intervalCount > 0 ? Math.floor(totalInterval / intervalCount / 1000) : 0;
      const maxIntervalSeconds = Math.floor(maxInterval / 1000);
      
      this.statistics = {
        successRate: `${successRate}%`,
        failureCount: failureCount,
        avgInterval: `${avgInterval}s`,
        maxInterval: `${maxIntervalSeconds}s`
      };
    },
    
    /**
     * 设置事件监听器
     */
    setupEventListeners() {
      // 监听心跳事件
      uni.$on('heartbeat', this.onHeartbeatReceived);
    },
    
    /**
     * 心跳接收处理
     */
    onHeartbeatReceived(data) {
      console.log('收到新心跳:', data);
      this.totalHeartbeats++;
      
      // 自动刷新日志（限流）
      if (!this.loading) {
        setTimeout(() => {
          this.refreshLogs();
        }, 500);
      }
    },
    
    /**
     * 格式化时间
     */
    formatTime(timestamp) {
      if (!timestamp) return 'N/A';
      
      const date = new Date(timestamp);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    /**
     * 获取状态文本
     */
    getStatusText(status) {
      const statusMap = {
        'success': '成功',
        'failure': '失败',
        'pending': '待处理',
        'timeout': '超时'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style lang="scss" scoped>
.debug-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.toolbar {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.tool-btn {
  padding: 20rpx;
  border-radius: 10rpx;
  font-size: 26rpx;
  font-weight: bold;
  border: none;
}

.refresh-btn {
  background-color: #2196F3;
  color: white;
}

.test-btn {
  background-color: #4CAF50;
  color: white;
}

.upload-btn {
  background-color: #FF9800;
  color: white;
}

.clear-btn {
  background-color: #F44336;
  color: white;
}

.tool-btn:disabled {
  background-color: #ccc !important;
  color: #999 !important;
}

.status-card {
  background-color: white;
  border-radius: 15rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.status-row:last-child {
  border-bottom: none;
}

.status-label {
  font-size: 28rpx;
  color: #666;
}

.status-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.status-value.running {
  color: #4CAF50;
}

.status-value.stopped {
  color: #F44336;
}

.filter-section {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.filter-label {
  font-size: 28rpx;
  color: #666;
  margin-right: 20rpx;
}

.filter-picker {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
  color: #333;
}

.picker-arrow {
  color: #999;
  font-size: 24rpx;
}

.logs-section {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.logs-scroll {
  height: 800rpx;
}

.log-card {
  background-color: white;
  border-radius: 15rpx;
  padding: 25rpx;
  margin-bottom: 15rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.log-time {
  font-size: 24rpx;
  color: #999;
}

.log-status {
  font-size: 24rpx;
  font-weight: bold;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.log-status.success {
  color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.1);
}

.log-status.failure {
  color: #F44336;
  background-color: rgba(244, 67, 54, 0.1);
}

.log-status.pending {
  color: #FF9800;
  background-color: rgba(255, 152, 0, 0.1);
}

.log-content {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-key {
  font-size: 26rpx;
  color: #666;
}

.log-value {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #ccc;
}

.load-more {
  text-align: center;
  padding: 30rpx 0;
}

.load-more-text {
  font-size: 26rpx;
  color: #999;
}

.stats-section {
  background-color: white;
  border-radius: 15rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15rpx;
}

.stat-card {
  background-color: #f8f9fa;
  border-radius: 10rpx;
  padding: 20rpx;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.stat-value.success {
  color: #4CAF50;
}

.stat-value.error {
  color: #F44336;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: white;
}
</style>
