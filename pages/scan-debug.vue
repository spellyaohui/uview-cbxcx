<template>
  <view class="debug-container">
    <view class="header">
      <text class="title">扫码功能调试</text>
    </view>

    <view class="debug-section">
      <text class="section-title">插件状态检测</text>
      <view class="status-item">
        <text class="status-label">支付宝扫码插件:</text>
        <text class="status-value" :class="pluginStatus.class">{{ pluginStatus.text }}</text>
      </view>
      <view class="status-item">
        <text class="status-label">相机权限:</text>
        <text class="status-value" :class="cameraStatus.class">{{ cameraStatus.text }}</text>
      </view>
    </view>

    <view class="debug-section">
      <text class="section-title">扫码测试</text>
      <button class="debug-btn primary" @click="testMpaasScan">测试支付宝扫码插件</button>
      <button class="debug-btn secondary" @click="testUniScan">测试 uni.scanCode</button>
      <button class="debug-btn tertiary" @click="checkPermissions">检查权限</button>
    </view>

    <view class="debug-section">
      <text class="section-title">调试日志</text>
      <scroll-view class="log-container" scroll-y>
        <view v-for="(log, index) in debugLogs" :key="index" class="log-item">
          <text class="log-time">{{ log.time }}</text>
          <text class="log-content" :class="log.type">{{ log.message }}</text>
        </view>
      </scroll-view>
      <button class="debug-btn clear" @click="clearLogs">清空日志</button>
    </view>

    <view class="debug-section">
      <text class="section-title">扫码结果</text>
      <view class="result-container">
        <text class="result-text">{{ scanResult || '暂无扫码结果' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      pluginStatus: { text: '检测中...', class: 'checking' },
      cameraStatus: { text: '检测中...', class: 'checking' },
      debugLogs: [],
      scanResult: ''
    }
  },

  mounted() {
    this.checkPluginStatus();
    this.checkCameraPermission();
  },

  methods: {
    // 添加调试日志
    addLog(message, type = 'info') {
      const now = new Date();
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
      
      this.debugLogs.unshift({
        time,
        message,
        type
      });

      // 限制日志数量
      if (this.debugLogs.length > 50) {
        this.debugLogs = this.debugLogs.slice(0, 50);
      }

      console.log(`[扫码调试] ${message}`);
    },

    // 清空日志
    clearLogs() {
      this.debugLogs = [];
      this.addLog('日志已清空');
    },

    // 检查插件状态
    checkPluginStatus() {
      this.addLog('开始检查支付宝扫码插件状态...');
      
      // #ifdef APP-PLUS
      try {
        const mpaasScanModule = uni.requireNativePlugin("Mpaas-Scan-Module");
        if (mpaasScanModule) {
          this.pluginStatus = { text: '已加载', class: 'success' };
          this.addLog('支付宝扫码插件加载成功', 'success');
          
          // 检查插件方法
          if (typeof mpaasScanModule.mpaasScan === 'function') {
            this.addLog('插件 mpaasScan 方法可用', 'success');
          } else {
            this.addLog('插件 mpaasScan 方法不可用', 'error');
          }
        } else {
          this.pluginStatus = { text: '未加载', class: 'error' };
          this.addLog('支付宝扫码插件加载失败', 'error');
        }
      } catch (error) {
        this.pluginStatus = { text: '加载异常', class: 'error' };
        this.addLog(`插件加载异常: ${error.message}`, 'error');
      }
      // #endif

      // #ifndef APP-PLUS
      this.pluginStatus = { text: '非App平台', class: 'warning' };
      this.addLog('当前为非App平台，将使用 uni.scanCode', 'warning');
      // #endif
    },

    // 检查相机权限
    checkCameraPermission() {
      this.addLog('开始检查相机权限...');
      
      // #ifdef APP-PLUS
      plus.android.requestPermissions(['android.permission.CAMERA'], (result) => {
        if (result.granted && result.granted.length > 0) {
          this.cameraStatus = { text: '已授权', class: 'success' };
          this.addLog('相机权限已授权', 'success');
        } else {
          this.cameraStatus = { text: '未授权', class: 'error' };
          this.addLog('相机权限未授权', 'error');
        }
      }, (error) => {
        this.cameraStatus = { text: '检查失败', class: 'error' };
        this.addLog(`权限检查失败: ${error.message}`, 'error');
      });
      // #endif

      // #ifndef APP-PLUS
      this.cameraStatus = { text: '无需检查', class: 'success' };
      this.addLog('非App平台无需检查相机权限', 'info');
      // #endif
    },

    // 检查权限
    checkPermissions() {
      this.addLog('重新检查所有权限...');
      this.checkPluginStatus();
      this.checkCameraPermission();
    },

    // 测试支付宝扫码插件
    testMpaasScan() {
      this.addLog('开始测试支付宝扫码插件...');
      
      // #ifdef APP-PLUS
      try {
        const mpaasScanModule = uni.requireNativePlugin("Mpaas-Scan-Module");
        
        if (!mpaasScanModule) {
          this.addLog('插件未加载，无法测试', 'error');
          return;
        }

        this.addLog('调用 mpaasScan 方法...');
        // 使用官方文档中的参数格式
        mpaasScanModule.mpaasScan(
          {
            scanType: ['qrCode', 'barCode'],
            hideAlbum: false,
            language: 'zh',
            failedMsg: '未识别到码，请重试',
            screenType: 'full',
            timeoutInterval: 30,
            timeoutText: '未识别到码？',
            continuous: false
          },
          (ret) => {
            this.addLog(`扫码回调: ${JSON.stringify(ret)}`);
            
            if (ret.resp_code === 1000) {
              this.scanResult = ret.resp_result;
              this.addLog(`扫码成功: ${ret.resp_result}`, 'success');
              uni.showToast({
                title: '扫码成功',
                icon: 'success'
              });
            } else if (ret.resp_code === 10) {
              this.addLog('用户取消扫码', 'warning');
            } else {
              this.addLog(`扫码失败: code=${ret.resp_code}, message=${ret.resp_message}`, 'error');
            }
          }
        );
      } catch (error) {
        this.addLog(`插件调用异常: ${error.message}`, 'error');
      }
      // #endif

      // #ifndef APP-PLUS
      this.addLog('非App平台，无法测试支付宝扫码插件', 'warning');
      // #endif
    },

    // 测试 uni.scanCode
    testUniScan() {
      this.addLog('开始测试 uni.scanCode...');
      
      uni.scanCode({
        onlyFromCamera: true,
        success: (res) => {
          this.scanResult = res.result;
          this.addLog(`uni.scanCode 成功: 类型=${res.scanType}, 内容=${res.result}`, 'success');
          uni.showToast({
            title: '扫码成功',
            icon: 'success'
          });
        },
        fail: (err) => {
          this.addLog(`uni.scanCode 失败: ${JSON.stringify(err)}`, 'error');
        }
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.debug-container {
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  min-height: 100vh;
}

.header {
  background: var(--bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-md);
  text-align: center;
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.debug-section {
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  border: 1px solid var(--border-color);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  display: block;
  margin-bottom: var(--spacing-md);
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--border-color);
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.status-value {
  font-size: 28rpx;
  font-weight: 500;
  
  &.success { color: var(--success-color); }
  &.error { color: var(--error-color); }
  &.warning { color: var(--warning-color); }
  &.checking { color: var(--text-tertiary); }
}

.debug-btn {
  width: 100%;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: var(--spacing-sm);
  border: none;
  
  &.primary {
    background: var(--primary-color);
    color: white;
  }
  
  &.secondary {
    background: var(--success-color);
    color: white;
  }
  
  &.tertiary {
    background: var(--warning-color);
    color: white;
  }
  
  &.clear {
    background: var(--error-color);
    color: white;
  }
}

.log-container {
  height: 400rpx;
  background: #f5f5f5;
  border-radius: var(--radius-sm);
  padding: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.log-item {
  margin-bottom: var(--spacing-xs);
  padding: var(--spacing-xs);
  background: white;
  border-radius: var(--radius-sm);
  border-left: 4rpx solid var(--border-color);
}

.log-time {
  font-size: 22rpx;
  color: var(--text-tertiary);
  display: block;
  margin-bottom: 4rpx;
}

.log-content {
  font-size: 24rpx;
  color: var(--text-primary);
  
  &.success { color: var(--success-color); }
  &.error { color: var(--error-color); }
  &.warning { color: var(--warning-color); }
}

.result-container {
  background: #f5f5f5;
  border-radius: var(--radius-sm);
  padding: var(--spacing-md);
  min-height: 120rpx;
}

.result-text {
  font-size: 28rpx;
  color: var(--text-primary);
  word-break: break-all;
}
</style>