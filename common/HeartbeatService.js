import ServiceBase from './ServiceBase';
import Session from './Session';

/**
 * 心跳服务类
 * 用于Android保活功能的心跳监控
 */
class HeartbeatService extends ServiceBase {
  constructor() {
    super();
    // 关键：ServiceBase.setUrl() 会拼接 this.$$prefix
    // 若不设置，可能变成 basePath + undefined + /api/xxx，导致请求无响应
    this.$$prefix = '';
    this.heartbeatTimer = null;
    this.isRunning = false;
    this.retryCount = 0;
    this.maxRetryCount = 3;
    this.heartbeatInterval = 30000; // 30秒
    this.localLogs = [];
    this.maxLocalLogs = 100;
    
    // 加载本地配置
    this.loadConfig();
  }

  /**
   * 标准化接口返回，避免出现 response 为 undefined/字符串/非对象导致的异常
   * @param {any} raw 原始返回
   * @param {string} api 接口标识（用于日志）
   * @returns {object|null}
   */
  normalizeApiResponse(raw, api = '') {
    try {
      if (raw == null) return null;

      // 兼容字符串返回（极端情况下 dataType 未生效）
      if (typeof raw === 'string') {
        try {
          const parsed = JSON.parse(raw);
          return parsed && typeof parsed === 'object' ? parsed : null;
        } catch (_) {
          console.warn(`心跳接口返回非JSON字符串${api ? `（${api}）` : ''}:`, raw);
          return null;
        }
      }

      if (typeof raw !== 'object') return null;
      return raw;
    } catch (e) {
      console.warn(`标准化接口返回失败${api ? `（${api}）` : ''}:`, e);
      return null;
    }
  }

  /**
   * 安全读取响应 code（兼容 string/number）
   * @param {any} response 标准化后的响应对象
   * @returns {number|null}
   */
  getResponseCode(response) {
    if (!response || typeof response !== 'object') return null;
    const code = Number(response.code);
    return Number.isFinite(code) ? code : null;
  }

  /**
   * 启动心跳服务
   */
  start() {
    if (this.isRunning) {
      console.log('心跳服务已在运行');
      return;
    }

    this.isRunning = true;
    this.loadLocalLogs();
    this.scheduleNextHeartbeat();
    console.log('心跳服务已启动');
  }

  /**
   * 停止心跳服务
   */
  stop() {
    if (this.heartbeatTimer) {
      clearTimeout(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
    this.isRunning = false;
    console.log('心跳服务已停止');
  }

  /**
   * 调度下一次心跳
   */
  scheduleNextHeartbeat() {
    if (!this.isRunning) return;

    this.heartbeatTimer = setTimeout(() => {
      this.sendHeartbeat();
    }, this.heartbeatInterval);
  }

  /**
   * 发送心跳信号
   */
  async sendHeartbeat() {
    try {
      const heartbeatData = await this.collectHeartbeatData();
      
      // 记录本地日志
      this.recordLocalHeartbeat(heartbeatData, 'sending');

      // 检查网络状态
      const networkStatus = await this.checkNetworkStatus();
      if (!networkStatus.isConnected) {
        console.log('网络未连接，缓存心跳数据');
        this.cacheHeartbeatData(heartbeatData);
        this.handleHeartbeatError(new Error('网络未连接'));
        this.scheduleNextHeartbeat();
        return;
      }

      // 发送到服务器
      const rawResponse = await this.post('/api/heartbeat', heartbeatData, {
        'Content-Type': 'application/json'
      }, 'json');

      const response = this.normalizeApiResponse(rawResponse, '/api/heartbeat');
      const code = this.getResponseCode(response);
      if (code == null) {
        this.handleHeartbeatError(new Error('心跳接口无响应或返回格式异常'));
        return;
      }

      if (code === 200) {
        // 心跳成功
        this.handleHeartbeatSuccess(response);
        this.retryCount = 0;
      } else if (code === 301) {
        // 用户未登录，停止心跳
        console.log('用户未登录，停止心跳服务');
        this.stop();
        return;
      } else {
        // 其他错误
        this.handleHeartbeatError(new Error(response?.message || `心跳失败(code=${code})`));
      }
    } catch (error) {
      this.handleHeartbeatError(error);
      
      // 网络错误时缓存数据
      if (error.message && error.message.includes('网络')) {
        const heartbeatData = await this.collectHeartbeatData();
        this.cacheHeartbeatData(heartbeatData);
      }
    }

    // 调度下一次心跳
    this.scheduleNextHeartbeat();
  }

  /**
   * 收集心跳数据
   */
  async collectHeartbeatData() {
    return new Promise((resolve) => {
      uni.getSystemInfo({
        success: (systemInfo) => {
          // 获取设备唯一标识
          const deviceId = this.getDeviceId();
          
          // 获取应用版本
          const appVersion = this.getAppVersion();
          
          // 获取保活状态
          const keepAliveStatus = this.getKeepAliveStatus();

          const heartbeatData = {
            deviceId: deviceId,
            appVersion: appVersion,
            systemVersion: `${systemInfo.platform} ${systemInfo.system}`,
            deviceModel: systemInfo.model || 'Unknown',
            keepAliveStatus: keepAliveStatus,
            timestamp: Date.now(),
            batteryLevel: systemInfo.batteryLevel || -1,
            networkType: systemInfo.networkType || 'unknown',
            screenWidth: systemInfo.screenWidth,
            screenHeight: systemInfo.screenHeight
          };

          resolve(heartbeatData);
        },
        fail: () => {
          // 降级数据
          resolve({
            deviceId: this.getDeviceId(),
            appVersion: this.getAppVersion(),
            systemVersion: 'Unknown',
            deviceModel: 'Unknown',
            keepAliveStatus: 'unknown',
            timestamp: Date.now(),
            batteryLevel: -1,
            networkType: 'unknown'
          });
        }
      });
    });
  }

  /**
   * 获取设备唯一标识
   */
  getDeviceId() {
    let deviceId = uni.getStorageSync('deviceId');
    if (!deviceId) {
      // 生成设备ID
      deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      uni.setStorageSync('deviceId', deviceId);
    }
    return deviceId;
  }

  /**
   * 获取应用版本
   */
  getAppVersion() {
    // 从manifest.json或全局配置获取版本号
    const app = getApp();
    return app.globalData.appVersion || '1.0.0';
  }

  /**
   * 获取保活状态
   */
  getKeepAliveStatus() {
    // 检查前台服务是否运行
    // 这里需要与Android原生插件配合
    const app = getApp();
    if (app.globalData.keepAliveService) {
      return app.globalData.keepAliveService.isRunning() ? 'active' : 'inactive';
    }
    return 'unknown';
  }

  /**
   * 处理心跳成功
   */
  handleHeartbeatSuccess(response) {
    console.log('心跳发送成功:', response.message);
    
    // 记录成功日志
    this.recordLocalHeartbeat(null, 'success', response);

    // 更新配置
    if (response.data && response.data.config) {
      this.updateServerConfig(response.data.config);
    }
    
    // 处理服务器返回的其他数据
    if (response.data && response.data.alerts) {
      this.handleServerAlerts(response.data.alerts);
    }
  }
  
  /**
   * 更新服务器配置
   */
  updateServerConfig(config) {
    try {
      console.log('收到服务器配置更新:', config);
      
      let configChanged = false;
      
      // 更新心跳间隔
      if (config.heartbeatInterval && config.heartbeatInterval !== this.heartbeatInterval) {
        const oldInterval = this.heartbeatInterval;
        this.heartbeatInterval = config.heartbeatInterval;
        console.log(`心跳间隔已更新: ${oldInterval}ms -> ${this.heartbeatInterval}ms`);
        configChanged = true;
      }
      
      // 更新最大重试次数
      if (config.maxRetryCount && config.maxRetryCount !== this.maxRetryCount) {
        const oldMaxRetry = this.maxRetryCount;
        this.maxRetryCount = config.maxRetryCount;
        console.log(`最大重试次数已更新: ${oldMaxRetry} -> ${this.maxRetryCount}`);
        configChanged = true;
      }
      
      // 更新最大本地日志数
      if (config.maxLocalLogs && config.maxLocalLogs !== this.maxLocalLogs) {
        const oldMaxLogs = this.maxLocalLogs;
        this.maxLocalLogs = config.maxLocalLogs;
        console.log(`最大本地日志数已更新: ${oldMaxLogs} -> ${this.maxLocalLogs}`);
        configChanged = true;
      }
      
      // 如果配置有变化，保存到本地
      if (configChanged) {
        this.saveConfig();
        
        // 触发配置更新事件
        uni.$emit('heartbeatConfigUpdated', {
          heartbeatInterval: this.heartbeatInterval,
          maxRetryCount: this.maxRetryCount,
          maxLocalLogs: this.maxLocalLogs
        });
      }
    } catch (error) {
      console.error('更新服务器配置失败:', error);
    }
  }
  
  /**
   * 处理服务器告警
   */
  handleServerAlerts(alerts) {
    try {
      if (!Array.isArray(alerts) || alerts.length === 0) {
        return;
      }
      
      console.log('收到服务器告警:', alerts);
      
      alerts.forEach(alert => {
        console.warn(`服务器告警 [${alert.level}]: ${alert.message}`);
        
        // 触发告警事件
        uni.$emit('heartbeatAlert', {
          level: alert.level,
          message: alert.message,
          timestamp: Date.now()
        });
        
        // 如果是严重告警，显示通知
        if (alert.level === 'critical' || alert.level === 'error') {
          uni.showToast({
            title: alert.message,
            icon: 'none',
            duration: 3000
          });
        }
      });
    } catch (error) {
      console.error('处理服务器告警失败:', error);
    }
  }
  
  /**
   * 检查网络状态
   */
  checkNetworkStatus() {
    return new Promise((resolve) => {
      uni.getNetworkType({
        success: (res) => {
          const isConnected = res.networkType !== 'none';
          resolve({
            isConnected: isConnected,
            networkType: res.networkType
          });
        },
        fail: () => {
          resolve({
            isConnected: false,
            networkType: 'unknown'
          });
        }
      });
    });
  }
  
  /**
   * 缓存心跳数据
   */
  cacheHeartbeatData(data) {
    try {
      // 获取缓存的心跳数据
      let cachedHeartbeats = uni.getStorageSync('cached_heartbeats') || [];
      
      // 添加新数据
      cachedHeartbeats.push({
        data: data,
        cachedAt: Date.now()
      });
      
      // 限制缓存数量（最多50条）
      if (cachedHeartbeats.length > 50) {
        cachedHeartbeats = cachedHeartbeats.slice(-50);
      }
      
      // 保存缓存
      uni.setStorageSync('cached_heartbeats', cachedHeartbeats);
      console.log('心跳数据已缓存，当前缓存数量:', cachedHeartbeats.length);
    } catch (error) {
      console.error('缓存心跳数据失败:', error);
    }
  }
  
  /**
   * 上传缓存的心跳数据（已移除批量上传功能，仅保留缓存逻辑）
   * 注意：后端暂不支持批量接口，缓存的心跳数据会在网络恢复后通过正常心跳流程逐步发送
   */
  async uploadCachedHeartbeats() {
    // 批量上传功能已移除，避免调用不存在的 /api/heartbeat/batch 接口
    // 缓存的心跳数据会在网络恢复后，通过正常的单次心跳流程逐步发送
    const cachedCount = (uni.getStorageSync('cached_heartbeats') || []).length;
    if (cachedCount > 0) {
      console.log(`当前有 ${cachedCount} 条缓存心跳数据，将在网络恢复后通过正常心跳流程发送`);
    }
  }
  
  /**
   * 保存配置到本地
   */
  saveConfig() {
    try {
      const config = {
        heartbeatInterval: this.heartbeatInterval,
        maxRetryCount: this.maxRetryCount,
        maxLocalLogs: this.maxLocalLogs
      };
      uni.setStorageSync('heartbeat_config', config);
      console.log('心跳配置已保存到本地');
    } catch (error) {
      console.error('保存配置失败:', error);
    }
  }
  
  /**
   * 加载本地配置
   */
  loadConfig() {
    try {
      const config = uni.getStorageSync('heartbeat_config');
      if (config) {
        this.heartbeatInterval = config.heartbeatInterval || this.heartbeatInterval;
        this.maxRetryCount = config.maxRetryCount || this.maxRetryCount;
        this.maxLocalLogs = config.maxLocalLogs || this.maxLocalLogs;
        console.log('已加载本地心跳配置:', config);
      }
    } catch (error) {
      console.error('加载配置失败:', error);
    }
  }

  /**
   * 处理心跳错误
   */
  handleHeartbeatError(error) {
    const msg =
      error?.message ||
      error?.errMsg ||
      (typeof error === 'string' ? error : '') ||
      (() => {
        try {
          return JSON.stringify(error);
        } catch (_) {
          return '未知错误';
        }
      })();

    console.error('心跳发送失败:', msg);
    
    // 记录错误日志
    this.recordLocalHeartbeat(null, 'error', { error: msg });

    this.retryCount++;
    
    // 如果重试次数过多，增加心跳间隔
    if (this.retryCount >= this.maxRetryCount) {
      this.heartbeatInterval = Math.min(this.heartbeatInterval * 2, 300000); // 最大5分钟
      console.log('心跳重试次数过多，间隔调整为:', this.heartbeatInterval);
      this.retryCount = 0;
    }
  }

  /**
   * 记录本地心跳日志
   */
  recordLocalHeartbeat(data, status, response = null) {
    const logEntry = {
      timestamp: Date.now(),
      status: status, // sending/success/error
      data: data,
      response: response,
      retryCount: this.retryCount
    };

    this.localLogs.push(logEntry);

    // 只保留最近的日志
    if (this.localLogs.length > this.maxLocalLogs) {
      this.localLogs = this.localLogs.slice(-this.maxLocalLogs);
    }

    // 保存到本地存储
    this.saveLocalLogs();
  }

  /**
   * 保存本地日志
   */
  saveLocalLogs() {
    try {
      uni.setStorageSync('heartbeat_logs', this.localLogs);
    } catch (error) {
      console.error('保存心跳日志失败:', error);
    }
  }

  /**
   * 加载本地日志
   */
  loadLocalLogs() {
    try {
      const logs = uni.getStorageSync('heartbeat_logs');
      if (logs && Array.isArray(logs)) {
        this.localLogs = logs;
      }
    } catch (error) {
      console.error('加载心跳日志失败:', error);
      this.localLogs = [];
    }
  }

  /**
   * 获取心跳统计信息
   */
  getHeartbeatStats() {
    const now = Date.now();
    const oneHourAgo = now - 3600000; // 1小时前
    const oneDayAgo = now - 86400000; // 1天前

    const recentLogs = this.localLogs.filter(log => log.timestamp > oneHourAgo);
    const todayLogs = this.localLogs.filter(log => log.timestamp > oneDayAgo);

    const successCount = recentLogs.filter(log => log.status === 'success').length;
    const errorCount = recentLogs.filter(log => log.status === 'error').length;

    return {
      isRunning: this.isRunning,
      heartbeatInterval: this.heartbeatInterval,
      totalLogs: this.localLogs.length,
      recentSuccess: successCount,
      recentErrors: errorCount,
      successRate: recentLogs.length > 0 ? (successCount / recentLogs.length * 100).toFixed(1) : 0,
      todayTotal: todayLogs.length,
      lastHeartbeat: this.localLogs.length > 0 ? this.localLogs[this.localLogs.length - 1] : null
    };
  }

  /**
   * 清除本地日志
   */
  clearLocalLogs() {
    this.localLogs = [];
    this.saveLocalLogs();
  }
  
  /**
   * 获取缓存的心跳数量
   */
  getCachedHeartbeatCount() {
    try {
      const cachedHeartbeats = uni.getStorageSync('cached_heartbeats') || [];
      return cachedHeartbeats.length;
    } catch (error) {
      console.error('获取缓存心跳数量失败:', error);
      return 0;
    }
  }
  
  /**
   * 手动触发心跳上传
   */
  async manualHeartbeat() {
    try {
      console.log('手动触发心跳上传');
      await this.sendHeartbeat();
      return { success: true, message: '心跳发送成功' };
    } catch (error) {
      console.error('手动心跳失败:', error);
      return { success: false, message: error.message };
    }
  }
}

export default HeartbeatService;