/**
 * 保活管理器 - JavaScript接口层
 * 提供uni-app应用层与Android原生保活插件的通信接口
 */
class KeepAliveManager {
  constructor() {
    this.isInitialized = false;
    this.config = null;
    this.status = {
      isRunning: false,
      lastHeartbeat: 0,
      heartbeatCount: 0,
      errorCount: 0
    };
    this.keepAliveModule = null;
    this.isPluginReady = false;
    // 原生方法调用超时时间（毫秒），防止原生未回调导致 Promise 永不返回
    this.nativeCallTimeout = 5000;
  }

  /**
   * 尝试加载原生插件（仅 App-Plus / Android）
   * @returns {Object|null}
   */
  loadNativePlugin() {
    try {
      // #ifdef APP-PLUS
      const systemInfo = uni.getSystemInfoSync();
      if (systemInfo.platform !== 'android') return null;

      // uni.requireNativePlugin 在插件缺失时：可能返回空，也可能触发框架层提示
      const plugin = uni.requireNativePlugin('CB-KeepAlive');
      if (!plugin) return null;
      return plugin;
      // #endif

      // #ifndef APP-PLUS
      return null;
      // #endif
    } catch (e) {
      return null;
    }
  }
  
  /**
   * 初始化保活服务
   * @param {Object} config 配置参数
   * @returns {Promise<Object>} 初始化结果
   */
  async init(config = {}) {
    try {
      console.log('初始化保活管理器', config);
      
      // 检查平台支持
      // #ifdef APP-PLUS
      const systemInfo = uni.getSystemInfoSync();
      if (systemInfo.platform !== 'android') {
        return { success: false, code: 'PLATFORM_NOT_SUPPORTED', message: '保活功能仅支持Android平台' };
      }
      
      // 获取原生插件
      this.keepAliveModule = this.loadNativePlugin();
      if (!this.keepAliveModule) {
        this.isPluginReady = false;
        // 这里不要抛异常，避免影响应用启动；同时给出明确的修复指引
        console.warn(
          '[CB-KeepAlive] 原生插件未加载到当前基座。' +
          '请在 HBuilderX 确认 manifest.json 已配置 nativePlugins.package/class，' +
          '并重新制作/安装“自定义运行基座”（运行 → 运行到手机或模拟器 → 制作自定义调试基座）。'
        );
        return { success: false, code: 'PLUGIN_MISSING', message: '保活插件未正确安装或当前基座未包含该插件' };
      }
      this.isPluginReady = true;
      
      // 合并默认配置
      this.config = { ...this.getDefaultConfig(), ...config };
      
      // 调用原生初始化方法
      const result = await this.callNativeMethod('init', this.config);
      if (result.success) {
        this.isInitialized = true;
        this.setupEventListeners();
        console.log('保活管理器初始化成功');
      } else {
        console.error('保活管理器初始化失败:', result.message);
      }
      
      return result;
      // #endif
      
      // #ifndef APP-PLUS
      console.warn('保活功能仅在APP平台可用');
      return { success: false, code: 'PLATFORM_NOT_SUPPORTED', message: '平台不支持' };
      // #endif
    } catch (error) {
      console.error('保活管理器初始化异常:', error);
      return { success: false, code: 'INIT_ERROR', message: error.message };
    }
  }
  
  /**
   * 启动保活服务
   * @returns {Promise<Object>} 启动结果
   */
  async start() {
    try {
      console.log('启动保活服务');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('start');
      if (result.success) {
        this.status.isRunning = true;
        console.log('保活服务启动成功');
      } else {
        console.error('保活服务启动失败:', result.message);
      }
      
      return result;
    } catch (error) {
      console.error('启动保活服务异常:', error);
      throw error;
    }
  }
  
  /**
   * 停止保活服务
   * @returns {Promise<Object>} 停止结果
   */
  async stop() {
    try {
      console.log('停止保活服务');
      
      if (!this.isInitialized) {
        return { success: true, message: '服务未运行' };
      }
      
      const result = await this.callNativeMethod('stop');
      if (result.success) {
        this.status.isRunning = false;
        console.log('保活服务已停止');
      } else {
        console.error('停止保活服务失败:', result.message);
      }
      
      return result;
    } catch (error) {
      console.error('停止保活服务异常:', error);
      throw error;
    }
  }
  
  /**
   * 获取保活状态
   * @returns {Promise<Object>} 状态信息
   */
  async getStatus() {
    try {
      if (!this.isInitialized) {
        return this.status;
      }
      
      const nativeStatus = await this.callNativeMethod('getStatus');
      this.status = { ...this.status, ...nativeStatus };
      return this.status;
    } catch (error) {
      console.error('获取保活状态异常:', error);
      return this.status;
    }
  }
  
  /**
   * 更新配置
   * @param {Object} newConfig 新配置
   * @returns {Promise<Object>} 更新结果
   */
  async updateConfig(newConfig) {
    try {
      console.log('更新保活配置', newConfig);
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      // 合并配置
      this.config = { ...this.config, ...newConfig };
      
      const result = await this.callNativeMethod('updateConfig', this.config);
      if (result.success) {
        console.log('配置更新成功');
      } else {
        console.error('配置更新失败:', result.message);
      }
      
      return result;
    } catch (error) {
      console.error('更新配置异常:', error);
      throw error;
    }
  }
  
  /**
   * 检查权限状态
   * @returns {Promise<Object>} 权限状态
   */
  async checkPermissions() {
    try {
      console.log('检查权限状态');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('checkPermissions');
      console.log('权限检查结果:', result);
      return result;
    } catch (error) {
      console.error('检查权限异常:', error);
      throw error;
    }
  }
  
  /**
   * 申请权限
   * @returns {Promise<Object>} 申请结果
   */
  async requestPermissions() {
    try {
      console.log('申请保活权限');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('requestPermissions');
      console.log('权限申请结果:', result);
      return result;
    } catch (error) {
      console.error('申请权限异常:', error);
      throw error;
    }
  }
  
  /**
   * 获取心跳日志
   * @param {Object} options 查询选项 {count: 数量}
   * @returns {Promise<Object>} 心跳日志
   */
  async getHeartbeatLogs(options = {}) {
    try {
      console.log('获取心跳日志', options);
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('getHeartbeatLogs', options);
      console.log('获取心跳日志成功，共', result.totalCount, '条');
      return result;
    } catch (error) {
      console.error('获取心跳日志异常:', error);
      throw error;
    }
  }
  
  /**
   * 获取心跳统计信息
   * @returns {Promise<Object>} 统计信息
   */
  async getHeartbeatStats() {
    try {
      console.log('获取心跳统计信息');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('getHeartbeatStats');
      console.log('心跳统计信息:', result.stats);
      return result;
    } catch (error) {
      console.error('获取心跳统计信息异常:', error);
      throw error;
    }
  }
  
  /**
   * 清除心跳日志
   * @returns {Promise<Object>} 清除结果
   */
  async clearHeartbeatLogs() {
    try {
      console.log('清除心跳日志');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('clearHeartbeatLogs');
      console.log('心跳日志清除成功');
      return result;
    } catch (error) {
      console.error('清除心跳日志异常:', error);
      throw error;
    }
  }
  
  /**
   * 设置事件监听器
   */
  setupEventListeners() {
    try {
      // 监听心跳事件
      uni.$on('keepAliveHeartbeat', (data) => {
        this.status.lastHeartbeat = Date.now();
        this.status.heartbeatCount++;
        
        console.log('收到心跳事件:', data);
        
        // 触发心跳事件给应用层
        uni.$emit('heartbeat', data);
      });
      
      // 监听服务状态变化
      uni.$on('keepAliveStatusChanged', (status) => {
        this.status = { ...this.status, ...status };
        console.log('保活状态变化:', this.status);
        
        // 触发状态变化事件
        uni.$emit('keepAliveStatusChanged', this.status);
      });
      
      // 监听错误事件
      uni.$on('keepAliveError', (error) => {
        this.status.errorCount++;
        console.error('保活服务错误:', error);
        
        // 触发错误事件
        uni.$emit('keepAliveError', error);
      });
      
      console.log('事件监听器设置完成');
    } catch (error) {
      console.error('设置事件监听器异常:', error);
    }
  }
  
  /**
   * 调用原生方法
   * @param {string} method 方法名
   * @param {Object} params 参数
   * @returns {Promise<Object>} 调用结果
   */
  callNativeMethod(method, params = {}) {
    return new Promise((resolve, reject) => {
      try {
        if (!this.keepAliveModule) {
          reject(new Error('原生插件未初始化'));
          return;
        }
        if (typeof this.keepAliveModule[method] !== 'function') {
          reject(new Error(`原生插件缺少方法: ${method}`));
          return;
        }
        
        console.log(`调用原生方法: ${method}`, params);
        const timer = setTimeout(() => {
          console.warn(`原生方法 ${method} 超时（>${this.nativeCallTimeout}ms）`);
          resolve({ success: false, message: '原生方法调用超时' });
        }, this.nativeCallTimeout);

        const invokeCallback = (cbResult) => {
          clearTimeout(timer);
          console.log(`原生方法 ${method} 返回:`, cbResult);
          if (cbResult && cbResult.success !== false) {
            resolve(cbResult);
          } else {
            reject(new Error(cbResult?.message || '原生方法调用失败'));
          }
        };

        // 某些原生方法签名只有 callback（如 start/checkPermissions），若无参数则只传回调，避免签名不匹配导致不回调
        const hasParams = params && typeof params === 'object' && Object.keys(params).length > 0;
        if (hasParams) {
          this.keepAliveModule[method](params, invokeCallback);
        } else {
          this.keepAliveModule[method](invokeCallback);
        }
      } catch (error) {
        console.error(`调用原生方法 ${method} 异常:`, error);
        reject(error);
      }
    });
  }
  
  /**
   * 获取默认配置
   * @returns {Object} 默认配置
   */
  getDefaultConfig() {
    return {
      enabled: true,
      heartbeatInterval: 30000, // 30秒
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
  }
  
  /**
   * 检查是否已初始化
   * @returns {boolean} 是否已初始化
   */
  isReady() {
    return this.isInitialized;
  }
  
  /**
   * 获取当前配置
   * @returns {Object} 当前配置
   */
  getConfig() {
    return this.config;
  }
  
  /**
   * 销毁保活管理器
   */
  destroy() {
    try {
      console.log('销毁保活管理器');
      
      // 移除事件监听
      uni.$off('keepAliveHeartbeat');
      uni.$off('keepAliveStatusChanged');
      uni.$off('keepAliveError');
      
      // 停止服务
      if (this.isInitialized && this.status.isRunning) {
        this.stop();
      }
      
      // 重置状态
      this.isInitialized = false;
      this.keepAliveModule = null;
      this.config = null;
      this.status = {
        isRunning: false,
        lastHeartbeat: 0,
        heartbeatCount: 0,
        errorCount: 0
      };
      
      console.log('保活管理器已销毁');
    } catch (error) {
      console.error('销毁保活管理器异常:', error);
    }
  }
  
  /**
   * 获取异常统计信息
   * @returns {Promise<Object>} 异常统计信息
   */
  async getExceptionStats() {
    try {
      console.log('获取异常统计信息');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('getExceptionStats');
      console.log('异常统计信息:', result.stats);
      return result;
    } catch (error) {
      console.error('获取异常统计信息异常:', error);
      throw error;
    }
  }
  
  /**
   * 上传缓存的心跳数据
   * @returns {Promise<Object>} 上传结果
   */
  async uploadCachedHeartbeats() {
    try {
      console.log('上传缓存的心跳数据');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('uploadCachedHeartbeats');
      console.log('上传结果:', result.message);
      return result;
    } catch (error) {
      console.error('上传缓存心跳异常:', error);
      throw error;
    }
  }
  
  /**
   * 获取内存状态
   * @returns {Promise<Object>} 内存状态信息
   */
  async getMemoryStatus() {
    try {
      console.log('获取内存状态');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('getMemoryStatus');
      console.log('内存状态:', result.memoryStatus);
      return result;
    } catch (error) {
      console.error('获取内存状态异常:', error);
      throw error;
    }
  }
  
  /**
   * 获取服务重启统计信息
   * @returns {Promise<Object>} 重启统计信息
   */
  async getRestartStats() {
    try {
      console.log('获取服务重启统计信息');
      
      if (!this.isInitialized) {
        throw new Error('保活管理器未初始化，请先调用init方法');
      }
      
      const result = await this.callNativeMethod('getRestartStats');
      console.log('重启统计信息:', result.restartStats);
      return result;
    } catch (error) {
      console.error('获取服务重启统计信息异常:', error);
      throw error;
    }
  }
}

export default KeepAliveManager;