<template>
  <view class="container">
    <view class="header">
      <text class="title">Android保活功能演示</text>
    </view>
    
    <view class="status-section">
      <text class="section-title">服务状态</text>
      <view class="status-item">
        <text class="label">初始化状态：</text>
        <text :class="['value', status.initialized ? 'success' : 'error']">
          {{ status.initialized ? '已初始化' : '未初始化' }}
        </text>
      </view>
      <view class="status-item">
        <text class="label">运行状态：</text>
        <text :class="['value', status.running ? 'success' : 'error']">
          {{ status.running ? '运行中' : '已停止' }}
        </text>
      </view>
      <view class="status-item">
        <text class="label">心跳次数：</text>
        <text class="value">{{ status.heartbeatCount }}</text>
      </view>
      <view class="status-item">
        <text class="label">错误次数：</text>
        <text class="value">{{ status.errorCount }}</text>
      </view>
    </view>
    
    <view class="button-section">
      <button 
        class="action-btn init-btn" 
        @click="initKeepAlive"
        :disabled="loading"
      >
        {{ loading ? '初始化中...' : '初始化保活服务' }}
      </button>
      
      <button 
        class="action-btn start-btn" 
        @click="startKeepAlive"
        :disabled="!status.initialized || status.running || loading"
      >
        启动保活服务
      </button>
      
      <button 
        class="action-btn stop-btn" 
        @click="stopKeepAlive"
        :disabled="!status.running || loading"
      >
        停止保活服务
      </button>
      
      <button 
        class="action-btn check-btn" 
        @click="checkPermissions"
        :disabled="!status.initialized || loading"
      >
        检查权限
      </button>
      
      <button 
        class="action-btn request-btn" 
        @click="requestPermissions"
        :disabled="!status.initialized || loading"
      >
        申请权限
      </button>
    </view>
    
    <view class="log-section">
      <text class="section-title">操作日志</text>
      <scroll-view class="log-container" scroll-y="true">
        <view 
          v-for="(log, index) in logs" 
          :key="index" 
          class="log-item"
        >
          <text class="log-time">{{ log.time }}</text>
          <text :class="['log-message', log.type]">{{ log.message }}</text>
        </view>
      </scroll-view>
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
      status: {
        initialized: false,
        running: false,
        heartbeatCount: 0,
        errorCount: 0
      },
      logs: []
    };
  },
  
  onLoad() {
    this.addLog('页面加载完成', 'info');
    this.keepAliveManager = new KeepAliveManager();
    this.setupEventListeners();
  },
  
  onUnload() {
    // 清理事件监听
    uni.$off('heartbeat');
    uni.$off('keepAliveStatusChanged');
    uni.$off('keepAliveError');
  },
  
  methods: {
    /**
     * 初始化保活服务
     */
    async initKeepAlive() {
      try {
        this.loading = true;
        this.addLog('开始初始化保活服务...', 'info');
        
        const config = {
          enabled: true,
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
        };
        
        const result = await this.keepAliveManager.init(config);
        
        if (result.success) {
          this.status.initialized = true;
          this.addLog('保活服务初始化成功', 'success');
        } else {
          this.addLog(`初始化失败: ${result.message}`, 'error');
        }
        
      } catch (error) {
        this.addLog(`初始化异常: ${error.message}`, 'error');
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 启动保活服务
     */
    async startKeepAlive() {
      try {
        this.loading = true;
        this.addLog('启动保活服务...', 'info');
        
        const result = await this.keepAliveManager.start();
        
        if (result.success) {
          this.status.running = true;
          this.addLog('保活服务启动成功', 'success');
        } else {
          this.addLog(`启动失败: ${result.message}`, 'error');
        }
        
      } catch (error) {
        this.addLog(`启动异常: ${error.message}`, 'error');
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 停止保活服务
     */
    async stopKeepAlive() {
      try {
        this.loading = true;
        this.addLog('停止保活服务...', 'info');
        
        const result = await this.keepAliveManager.stop();
        
        if (result.success) {
          this.status.running = false;
          this.addLog('保活服务已停止', 'success');
        } else {
          this.addLog(`停止失败: ${result.message}`, 'error');
        }
        
      } catch (error) {
        this.addLog(`停止异常: ${error.message}`, 'error');
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 检查权限
     */
    async checkPermissions() {
      try {
        this.loading = true;
        this.addLog('检查权限状态...', 'info');
        
        const result = await this.keepAliveManager.checkPermissions();
        
        if (result.success) {
          const permissions = result.permissions;
          this.addLog(`权限检查完成:`, 'info');
          this.addLog(`- 前台服务: ${permissions.foregroundService ? '已授权' : '未授权'}`, 'info');
          this.addLog(`- 通知权限: ${permissions.notification ? '已授权' : '未授权'}`, 'info');
          this.addLog(`- 电池优化: ${permissions.batteryOptimization ? '已忽略' : '未忽略'}`, 'info');
          this.addLog(`- 自启动权限: ${permissions.autoStart ? '已允许' : '需设置'}`, 'info');
        } else {
          this.addLog(`权限检查失败: ${result.message}`, 'error');
        }
        
      } catch (error) {
        this.addLog(`权限检查异常: ${error.message}`, 'error');
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
        this.addLog('申请保活权限...', 'info');
        
        const result = await this.keepAliveManager.requestPermissions();
        
        if (result.success) {
          this.addLog('权限申请完成，请在系统设置中确认授权', 'success');
        } else {
          this.addLog(`权限申请失败: ${result.message}`, 'error');
        }
        
      } catch (error) {
        this.addLog(`权限申请异常: ${error.message}`, 'error');
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 设置事件监听器
     */
    setupEventListeners() {
      // 监听心跳事件
      uni.$on('heartbeat', (data) => {
        this.status.heartbeatCount++;
        this.addLog(`收到心跳信号 #${this.status.heartbeatCount}`, 'success');
      });
      
      // 监听状态变化
      uni.$on('keepAliveStatusChanged', (status) => {
        this.status = { ...this.status, ...status };
        this.addLog('保活状态已更新', 'info');
      });
      
      // 监听错误事件
      uni.$on('keepAliveError', (error) => {
        this.status.errorCount++;
        this.addLog(`保活错误: ${error.message}`, 'error');
      });
    },
    
    /**
     * 添加日志
     */
    addLog(message, type = 'info') {
      const now = new Date();
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
      
      this.logs.unshift({
        time,
        message,
        type
      });
      
      // 限制日志数量
      if (this.logs.length > 50) {
        this.logs = this.logs.slice(0, 50);
      }
      
      console.log(`[${time}] ${message}`);
    }
  }
};
</script>

<style lang="scss" scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.status-section {
  background-color: white;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
  border-bottom: 1rpx solid #eee;
}

.status-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 28rpx;
  color: #666;
}

.value {
  font-size: 28rpx;
  font-weight: bold;
}

.value.success {
  color: #4CAF50;
}

.value.error {
  color: #F44336;
}

.button-section {
  margin-bottom: 20rpx;
}

.action-btn {
  width: 100%;
  margin-bottom: 20rpx;
  padding: 20rpx;
  border-radius: 10rpx;
  font-size: 30rpx;
  font-weight: bold;
  border: none;
}

.init-btn {
  background-color: #2196F3;
  color: white;
}

.start-btn {
  background-color: #4CAF50;
  color: white;
}

.stop-btn {
  background-color: #F44336;
  color: white;
}

.check-btn {
  background-color: #FF9800;
  color: white;
}

.request-btn {
  background-color: #9C27B0;
  color: white;
}

.action-btn:disabled {
  background-color: #ccc !important;
  color: #999 !important;
}

.log-section {
  background-color: white;
  border-radius: 10rpx;
  padding: 20rpx;
  height: 600rpx;
}

.log-container {
  height: 500rpx;
  border: 1rpx solid #eee;
  border-radius: 5rpx;
  padding: 10rpx;
}

.log-item {
  display: flex;
  margin-bottom: 10rpx;
  padding: 5rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.log-time {
  font-size: 24rpx;
  color: #999;
  margin-right: 20rpx;
  min-width: 120rpx;
}

.log-message {
  font-size: 26rpx;
  flex: 1;
}

.log-message.info {
  color: #333;
}

.log-message.success {
  color: #4CAF50;
}

.log-message.error {
  color: #F44336;
}
</style>