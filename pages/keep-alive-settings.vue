<template>
  <view class="settings-container">
    <!-- 顶部状态卡片 -->
    <view class="status-card">
      <view class="status-header">
        <text class="status-title">保活服务状态</text>
        <view :class="['status-badge', serviceStatus.isRunning ? 'running' : 'stopped']">
          <text class="status-badge-text">{{ serviceStatus.isRunning ? '运行中' : '已停止' }}</text>
        </view>
      </view>
      
      <view class="status-info">
        <view class="info-item">
          <text class="info-label">心跳次数</text>
          <text class="info-value">{{ serviceStatus.heartbeatCount || 0 }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">最后心跳</text>
          <text class="info-value">{{ lastHeartbeatTime }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">运行时长</text>
          <text class="info-value">{{ runningDuration }}</text>
        </view>
      </view>
    </view>

    <!-- 保活功能开关 -->
    <view class="settings-section">
      <text class="section-title">基础设置</text>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">启用保活功能</text>
          <text class="setting-desc">确保应用在后台持续运行</text>
        </view>
        <switch 
          :checked="settings.enabled" 
          @change="onEnableChange"
          :disabled="loading"
          color="#07c160"
        />
      </view>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">心跳间隔</text>
          <text class="setting-desc">当前: {{ settings.heartbeatInterval / 1000 }}秒</text>
        </view>
        <picker 
          mode="selector" 
          :range="heartbeatIntervals" 
          :value="heartbeatIntervalIndex"
          @change="onHeartbeatIntervalChange"
          :disabled="!settings.enabled || loading"
        >
          <view class="picker-value">
            <text>{{ heartbeatIntervals[heartbeatIntervalIndex] }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 通知设置 -->
    <view class="settings-section">
      <text class="section-title">通知设置</text>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">通知标题</text>
        </view>
        <input 
          class="setting-input"
          v-model="settings.notificationConfig.title"
          placeholder="请输入通知标题"
          :disabled="!settings.enabled || loading"
          @blur="onNotificationConfigChange"
        />
      </view>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">通知内容</text>
        </view>
        <input 
          class="setting-input"
          v-model="settings.notificationConfig.content"
          placeholder="请输入通知内容"
          :disabled="!settings.enabled || loading"
          @blur="onNotificationConfigChange"
        />
      </view>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">显示进度条</text>
          <text class="setting-desc">在通知中显示心跳进度</text>
        </view>
        <switch 
          :checked="settings.notificationConfig.showProgress" 
          @change="onShowProgressChange"
          :disabled="!settings.enabled || loading"
          color="#07c160"
        />
      </view>
    </view>

    <!-- 高级设置 -->
    <view class="settings-section">
      <text class="section-title">高级设置</text>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">厂商优化</text>
          <text class="setting-desc">启用设备厂商特定优化</text>
        </view>
        <switch 
          :checked="settings.adaptationConfig.enableManufacturerOptimization" 
          @change="onManufacturerOptimizationChange"
          :disabled="!settings.enabled || loading"
          color="#07c160"
        />
      </view>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">电池白名单</text>
          <text class="setting-desc">引导用户添加到白名单</text>
        </view>
        <switch 
          :checked="settings.adaptationConfig.enableBatteryWhitelist" 
          @change="onBatteryWhitelistChange"
          :disabled="!settings.enabled || loading"
          color="#07c160"
        />
      </view>
      
      <view class="setting-item">
        <view class="setting-left">
          <text class="setting-label">自启动管理</text>
          <text class="setting-desc">引导用户允许自启动</text>
        </view>
        <switch 
          :checked="settings.adaptationConfig.enableAutoStart" 
          @change="onAutoStartChange"
          :disabled="!settings.enabled || loading"
          color="#07c160"
        />
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="settings-section">
      <text class="section-title">统计信息</text>
      
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ statistics.totalHeartbeats || 0 }}</text>
          <text class="stat-label">总心跳数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ statistics.successRate || '0%' }}</text>
          <text class="stat-label">成功率</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ statistics.errorCount || 0 }}</text>
          <text class="stat-label">错误次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ statistics.avgInterval || '0s' }}</text>
          <text class="stat-label">平均间隔</text>
        </view>
      </view>
      
      <button 
        class="refresh-btn"
        @click="refreshStatistics"
        :disabled="loading"
      >
        刷新统计
      </button>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <button 
        class="action-btn primary-btn"
        @click="checkPermissions"
        :disabled="loading"
      >
        检查权限
      </button>
      
      <button 
        class="action-btn secondary-btn"
        @click="requestPermissions"
        :disabled="loading"
      >
        申请权限
      </button>
      
      <button 
        class="action-btn info-btn"
        @click="goToDebugPage"
      >
        查看调试信息
      </button>
    </view>

    <!-- 加载提示 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-content">
        <text class="loading-text">处理中...</text>
      </view>
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
      settings: {
        enabled: false,
        heartbeatInterval: 30000,
        maxRetryCount: 3,
        notificationConfig: {
          title: '崔博小程序正在后台运行',
          content: '确保及时接收健康报告通知',
          icon: 'ic_notification',
          showProgress: false
        },
        adaptationConfig: {
          enableManufacturerOptimization: true,
          enableBatteryWhitelist: true,
          enableAutoStart: true
        }
      },
      serviceStatus: {
        isRunning: false,
        heartbeatCount: 0,
        lastHeartbeat: 0,
        errorCount: 0
      },
      statistics: {
        totalHeartbeats: 0,
        successRate: '0%',
        errorCount: 0,
        avgInterval: '0s'
      },
      heartbeatIntervals: ['15秒', '30秒', '60秒', '120秒'],
      heartbeatIntervalIndex: 1,
      startTime: 0
    };
  },
  
  computed: {
    lastHeartbeatTime() {
      if (!this.serviceStatus.lastHeartbeat) {
        return '暂无';
      }
      const now = Date.now();
      const diff = Math.floor((now - this.serviceStatus.lastHeartbeat) / 1000);
      if (diff < 60) {
        return `${diff}秒前`;
      } else if (diff < 3600) {
        return `${Math.floor(diff / 60)}分钟前`;
      } else {
        return `${Math.floor(diff / 3600)}小时前`;
      }
    },
    
    runningDuration() {
      if (!this.serviceStatus.isRunning || !this.startTime) {
        return '0分钟';
      }
      const now = Date.now();
      const diff = Math.floor((now - this.startTime) / 1000);
      if (diff < 60) {
        return `${diff}秒`;
      } else if (diff < 3600) {
        return `${Math.floor(diff / 60)}分钟`;
      } else {
        const hours = Math.floor(diff / 3600);
        const minutes = Math.floor((diff % 3600) / 60);
        return `${hours}小时${minutes}分钟`;
      }
    }
  },
  
  onLoad() {
    this.initPage();
  },
  
  onShow() {
    this.refreshStatus();
  },
  
  onUnload() {
    // 清理事件监听
    uni.$off('heartbeat');
    uni.$off('keepAliveStatusChanged');
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
        
        // 加载保存的设置
        this.loadSettings();
        
        // 刷新状态
        await this.refreshStatus();
        
        // 设置事件监听
        this.setupEventListeners();
        
        // 刷新统计信息
        await this.refreshStatistics();
        
      } catch (error) {
        console.error('初始化页面失败:', error);
        uni.showToast({
          title: '初始化失败',
          icon: 'none'
        });
      }
    },
    
    /**
     * 加载设置
     */
    loadSettings() {
      try {
        const savedSettings = uni.getStorageSync('keepAliveSettings');
        if (savedSettings) {
          this.settings = JSON.parse(savedSettings);
          
          // 更新心跳间隔选择器索引
          const intervals = [15000, 30000, 60000, 120000];
          this.heartbeatIntervalIndex = intervals.indexOf(this.settings.heartbeatInterval);
          if (this.heartbeatIntervalIndex === -1) {
            this.heartbeatIntervalIndex = 1;
          }
        }
      } catch (error) {
        console.error('加载设置失败:', error);
      }
    },
    
    /**
     * 保存设置
     */
    saveSettings() {
      try {
        uni.setStorageSync('keepAliveSettings', JSON.stringify(this.settings));
      } catch (error) {
        console.error('保存设置失败:', error);
      }
    },
    
    /**
     * 刷新状态
     */
    async refreshStatus() {
      try {
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          return;
        }
        
        const status = await this.keepAliveManager.getStatus();
        this.serviceStatus = {
          isRunning: status.isRunning || false,
          heartbeatCount: status.heartbeatCount || 0,
          lastHeartbeat: status.lastHeartbeat || 0,
          errorCount: status.errorCount || 0
        };
        
        // 更新启用状态
        this.settings.enabled = status.isRunning;
        
        // 记录启动时间
        if (status.isRunning && !this.startTime) {
          this.startTime = Date.now();
        } else if (!status.isRunning) {
          this.startTime = 0;
        }
        
      } catch (error) {
        console.error('刷新状态失败:', error);
      }
    },
    
    /**
     * 刷新统计信息
     */
    async refreshStatistics() {
      try {
        this.loading = true;
        
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          return;
        }
        
        const result = await this.keepAliveManager.getHeartbeatStats();
        
        if (result.success && result.stats) {
          const stats = result.stats;
          this.statistics = {
            totalHeartbeats: stats.totalCount || 0,
            successRate: stats.successRate ? `${(stats.successRate * 100).toFixed(1)}%` : '0%',
            errorCount: stats.errorCount || 0,
            avgInterval: stats.avgInterval ? `${(stats.avgInterval / 1000).toFixed(0)}s` : '0s'
          };
        }
        
        uni.showToast({
          title: '统计信息已更新',
          icon: 'success'
        });
        
      } catch (error) {
        console.error('刷新统计信息失败:', error);
        uni.showToast({
          title: '刷新失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 启用/禁用保活功能
     */
    async onEnableChange(e) {
      try {
        this.loading = true;
        const enabled = e.detail.value;
        
        if (enabled) {
          // 启动保活服务
          if (!this.keepAliveManager.isReady()) {
            await this.keepAliveManager.init(this.settings);
          }
          
          const result = await this.keepAliveManager.start();
          
          if (result.success) {
            this.settings.enabled = true;
            this.startTime = Date.now();
            uni.showToast({
              title: '保活服务已启动',
              icon: 'success'
            });
          } else {
            this.settings.enabled = false;
            uni.showToast({
              title: result.message || '启动失败',
              icon: 'none'
            });
          }
        } else {
          // 停止保活服务
          const result = await this.keepAliveManager.stop();
          
          if (result.success) {
            this.settings.enabled = false;
            this.startTime = 0;
            uni.showToast({
              title: '保活服务已停止',
              icon: 'success'
            });
          } else {
            uni.showToast({
              title: result.message || '停止失败',
              icon: 'none'
            });
          }
        }
        
        this.saveSettings();
        await this.refreshStatus();
        
      } catch (error) {
        console.error('切换保活状态失败:', error);
        this.settings.enabled = !enabled;
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 心跳间隔变化
     */
    async onHeartbeatIntervalChange(e) {
      try {
        this.loading = true;
        const index = e.detail.value;
        this.heartbeatIntervalIndex = index;
        
        const intervals = [15000, 30000, 60000, 120000];
        this.settings.heartbeatInterval = intervals[index];
        
        // 更新配置
        if (this.keepAliveManager && this.keepAliveManager.isReady()) {
          await this.keepAliveManager.updateConfig({
            heartbeatInterval: this.settings.heartbeatInterval
          });
        }
        
        this.saveSettings();
        
        uni.showToast({
          title: '心跳间隔已更新',
          icon: 'success'
        });
        
      } catch (error) {
        console.error('更新心跳间隔失败:', error);
        uni.showToast({
          title: '更新失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 通知配置变化
     */
    async onNotificationConfigChange() {
      try {
        // 更新配置
        if (this.keepAliveManager && this.keepAliveManager.isReady()) {
          await this.keepAliveManager.updateConfig({
            notificationConfig: this.settings.notificationConfig
          });
        }
        
        this.saveSettings();
        
      } catch (error) {
        console.error('更新通知配置失败:', error);
      }
    },
    
    /**
     * 显示进度变化
     */
    async onShowProgressChange(e) {
      try {
        this.settings.notificationConfig.showProgress = e.detail.value;
        await this.onNotificationConfigChange();
        
        uni.showToast({
          title: '通知设置已更新',
          icon: 'success'
        });
      } catch (error) {
        console.error('更新显示进度失败:', error);
      }
    },
    
    /**
     * 厂商优化变化
     */
    async onManufacturerOptimizationChange(e) {
      try {
        this.settings.adaptationConfig.enableManufacturerOptimization = e.detail.value;
        
        if (this.keepAliveManager && this.keepAliveManager.isReady()) {
          await this.keepAliveManager.updateConfig({
            adaptationConfig: this.settings.adaptationConfig
          });
        }
        
        this.saveSettings();
        
      } catch (error) {
        console.error('更新厂商优化失败:', error);
      }
    },
    
    /**
     * 电池白名单变化
     */
    async onBatteryWhitelistChange(e) {
      try {
        this.settings.adaptationConfig.enableBatteryWhitelist = e.detail.value;
        
        if (this.keepAliveManager && this.keepAliveManager.isReady()) {
          await this.keepAliveManager.updateConfig({
            adaptationConfig: this.settings.adaptationConfig
          });
        }
        
        this.saveSettings();
        
      } catch (error) {
        console.error('更新电池白名单失败:', error);
      }
    },
    
    /**
     * 自启动变化
     */
    async onAutoStartChange(e) {
      try {
        this.settings.adaptationConfig.enableAutoStart = e.detail.value;
        
        if (this.keepAliveManager && this.keepAliveManager.isReady()) {
          await this.keepAliveManager.updateConfig({
            adaptationConfig: this.settings.adaptationConfig
          });
        }
        
        this.saveSettings();
        
      } catch (error) {
        console.error('更新自启动失败:', error);
      }
    },
    
    /**
     * 检查权限
     */
    async checkPermissions() {
      try {
        this.loading = true;
        
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          uni.showToast({
            title: '请先启用保活功能',
            icon: 'none'
          });
          return;
        }
        
        const result = await this.keepAliveManager.checkPermissions();
        
        if (result.success) {
          const permissions = result.permissions;
          let message = '权限检查完成:\n';
          message += `前台服务: ${permissions.foregroundService ? '✓' : '✗'}\n`;
          message += `通知权限: ${permissions.notification ? '✓' : '✗'}\n`;
          message += `电池优化: ${permissions.batteryOptimization ? '✓' : '✗'}\n`;
          message += `自启动: ${permissions.autoStart ? '✓' : '✗'}`;
          
          uni.showModal({
            title: '权限状态',
            content: message,
            showCancel: false
          });
        } else {
          uni.showToast({
            title: result.message || '检查失败',
            icon: 'none'
          });
        }
        
      } catch (error) {
        console.error('检查权限失败:', error);
        uni.showToast({
          title: '检查失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 申请权限
     */
    async requestPermissions() {
      try {
        this.loading = true;
        
        if (!this.keepAliveManager || !this.keepAliveManager.isReady()) {
          uni.showToast({
            title: '请先启用保活功能',
            icon: 'none'
          });
          return;
        }
        
        const result = await this.keepAliveManager.requestPermissions();
        
        if (result.success) {
          uni.showModal({
            title: '权限申请',
            content: '已引导到系统设置页面，请手动授予相关权限',
            showCancel: false
          });
        } else {
          uni.showToast({
            title: result.message || '申请失败',
            icon: 'none'
          });
        }
        
      } catch (error) {
        console.error('申请权限失败:', error);
        uni.showToast({
          title: '申请失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 跳转到调试页面
     */
    goToDebugPage() {
      uni.navigateTo({
        url: '/pages/heartbeat-debug'
      });
    },
    
    /**
     * 设置事件监听器
     */
    setupEventListeners() {
      // 监听心跳事件
      uni.$on('heartbeat', (data) => {
        this.serviceStatus.heartbeatCount++;
        this.serviceStatus.lastHeartbeat = Date.now();
      });
      
      // 监听状态变化
      uni.$on('keepAliveStatusChanged', (status) => {
        this.serviceStatus = { ...this.serviceStatus, ...status };
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.status-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  background-color: rgba(255, 255, 255, 0.2);
}

.status-badge.running {
  background-color: rgba(76, 175, 80, 0.3);
}

.status-badge.stopped {
  background-color: rgba(244, 67, 54, 0.3);
}

.status-badge-text {
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.status-info {
  display: flex;
  justify-content: space-between;
}

.info-item {
  flex: 1;
  text-align: center;
}

.info-label {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 10rpx;
}

.info-value {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: white;
}

.settings-section {
  background-color: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-left {
  flex: 1;
}

.setting-label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.setting-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.setting-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #333;
}

.picker-value {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #666;
}

.picker-arrow {
  margin-left: 10rpx;
  font-size: 32rpx;
  color: #999;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.stat-item {
  text-align: center;
  padding: 20rpx;
  background-color: #f8f9fa;
  border-radius: 10rpx;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.refresh-btn {
  width: 100%;
  padding: 20rpx;
  background-color: #f0f0f0;
  color: #666;
  border-radius: 10rpx;
  font-size: 28rpx;
  border: none;
}

.action-section {
  margin-top: 20rpx;
}

.action-btn {
  width: 100%;
  margin-bottom: 20rpx;
  padding: 25rpx;
  border-radius: 15rpx;
  font-size: 30rpx;
  font-weight: bold;
  border: none;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.secondary-btn {
  background-color: #4CAF50;
  color: white;
}

.info-btn {
  background-color: #2196F3;
  color: white;
}

.action-btn:disabled {
  background: #ccc !important;
  color: #999 !important;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  background-color: white;
  padding: 40rpx 60rpx;
  border-radius: 10rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #333;
}
</style>
