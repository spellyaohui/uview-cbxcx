package io.dcloud.feature.keepalive;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 保活管理器
 * 负责管理Android保活服务的生命周期和配置
 */
public class KeepAliveManager {
    
    private static final String TAG = "KeepAliveManager";
    private static KeepAliveManager instance;
    
    private Context context;
    private KeepAliveConfig config;
    private boolean isInitialized = false;
    private boolean isRunning = false;
    private LocalHeartbeatLogger heartbeatLogger;
    
    private KeepAliveManager() {
        // 私有构造函数，实现单例模式
    }
    
    /**
     * 获取保活管理器单例实例
     * @return KeepAliveManager实例
     */
    public static synchronized KeepAliveManager getInstance() {
        if (instance == null) {
            instance = new KeepAliveManager();
        }
        return instance;
    }
    
    /**
     * 初始化保活管理器
     * @param context 应用上下文
     * @param config 保活配置
     * @return 初始化是否成功
     */
    public boolean init(Context context, KeepAliveConfig config) {
        try {
            Log.d(TAG, "初始化保活管理器");
            
            if (context == null) {
                Log.e(TAG, "初始化失败：上下文为空");
                return false;
            }
            
            if (config == null) {
                Log.e(TAG, "初始化失败：配置为空");
                return false;
            }
            
            this.context = context.getApplicationContext();
            this.config = config;
            
            // 初始化心跳日志管理器
            this.heartbeatLogger = new LocalHeartbeatLogger(this.context);
            
            // 应用设备适配策略
            applyDeviceAdaptationStrategy();
            
            this.isInitialized = true;
            
            Log.d(TAG, "保活管理器初始化成功");
            Log.d(TAG, "配置信息: " + config.toString());
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "初始化保活管理器异常", e);
            this.isInitialized = false;
            return false;
        }
    }
    
    /**
     * 应用设备适配策略
     * 验证需求：3.1, 3.2 - 根据设备选择最优策略
     */
    private void applyDeviceAdaptationStrategy() {
        try {
            Log.d(TAG, "应用设备适配策略");
            
            DeviceAdaptationStrategy adaptationStrategy = new DeviceAdaptationStrategy(context);
            JSONObject strategy = adaptationStrategy.selectOptimalStrategy();
            
            Log.i(TAG, "选定的保活策略: " + strategy.getString("primaryStrategy"));
            Log.i(TAG, "策略原因: " + strategy.getString("reason"));
            
            // 根据策略调整配置
            if (strategy.containsKey("performanceStrategy")) {
                JSONObject performanceStrategy = strategy.getJSONObject("performanceStrategy");
                if (performanceStrategy.containsKey("recommendedHeartbeatInterval")) {
                    int recommendedInterval = performanceStrategy.getIntValue("recommendedHeartbeatInterval");
                    // 如果用户没有明确设置心跳间隔，使用推荐值
                    if (config.getHeartbeatInterval() == 30000) {  // 默认值
                        config.setHeartbeatInterval(recommendedInterval);
                        Log.d(TAG, "应用推荐心跳间隔: " + recommendedInterval + "ms");
                    }
                }
            }
            
            // 输出策略摘要
            String summary = adaptationStrategy.getStrategySummary();
            Log.i(TAG, summary);
            
        } catch (Exception e) {
            Log.e(TAG, "应用设备适配策略异常", e);
        }
    }
    
    /**
     * 启动保活服务
     * @return 启动是否成功
     */
    public boolean start() {
        try {
            Log.d(TAG, "启动保活服务");
            
            if (!isInitialized) {
                Log.e(TAG, "启动失败：管理器未初始化");
                return false;
            }
            
            if (isRunning) {
                Log.w(TAG, "保活服务已在运行中");
                return true;
            }
            
            if (!config.isEnabled()) {
                Log.w(TAG, "保活功能已禁用");
                return false;
            }
            
            // 启动前台服务
            Intent serviceIntent = new Intent(context, KeepAliveService.class);
            serviceIntent.putExtra("config", config.toJson().toString());
            
            try {
                context.startForegroundService(serviceIntent);
                isRunning = true;
                Log.d(TAG, "前台服务启动成功");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "启动前台服务失败", e);
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "启动保活服务异常", e);
            return false;
        }
    }
    
    /**
     * 停止保活服务
     */
    public void stop() {
        try {
            Log.d(TAG, "停止保活服务");
            
            if (!isRunning) {
                Log.w(TAG, "保活服务未运行");
                return;
            }
            
            // 停止前台服务
            Intent serviceIntent = new Intent(context, KeepAliveService.class);
            context.stopService(serviceIntent);
            
            isRunning = false;
            Log.d(TAG, "保活服务已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "停止保活服务异常", e);
        }
    }
    
    /**
     * 获取保活状态
     * @return KeepAliveStatus状态对象
     */
    public KeepAliveStatus getStatus() {
        KeepAliveStatus status = new KeepAliveStatus();
        
        status.setInitialized(isInitialized);
        status.setRunning(isRunning);
        status.setEnabled(config != null && config.isEnabled());
        
        if (context != null) {
            // 获取设备信息
            DeviceAdapter deviceAdapter = new DeviceAdapter(context);
            status.setDeviceInfo(deviceAdapter.getDeviceInfo());
            
            // 获取权限状态
            PermissionManager permissionManager = new PermissionManager(context);
            status.setPermissions(permissionManager.checkAllPermissions());
        }
        
        if (config != null) {
            status.setConfig(config.toJson());
        }
        
        return status;
    }
    
    /**
     * 更新配置
     * @param newConfig 新的配置
     * @return 更新是否成功
     */
    public boolean updateConfig(KeepAliveConfig newConfig) {
        try {
            Log.d(TAG, "更新保活配置");
            
            if (!isInitialized) {
                Log.e(TAG, "更新配置失败：管理器未初始化");
                return false;
            }
            
            if (newConfig == null) {
                Log.e(TAG, "更新配置失败：新配置为空");
                return false;
            }
            
            this.config = newConfig;
            
            // 如果服务正在运行，需要重启以应用新配置
            if (isRunning) {
                Log.d(TAG, "重启服务以应用新配置");
                stop();
                return start();
            }
            
            Log.d(TAG, "配置更新成功");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "更新配置异常", e);
            return false;
        }
    }
    
    /**
     * 获取当前配置
     * @return KeepAliveConfig配置对象
     */
    public KeepAliveConfig getConfig() {
        return config;
    }
    
    /**
     * 检查是否已初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 检查服务是否正在运行
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取应用上下文
     * @return Context应用上下文
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * 更新通知内容
     * @param title 通知标题
     * @param content 通知内容
     * @return 更新是否成功
     */
    public boolean updateNotification(String title, String content) {
        try {
            Log.d(TAG, "更新通知内容");
            
            if (!isRunning) {
                Log.w(TAG, "服务未运行，无法更新通知");
                return false;
            }
            
            // 更新配置
            if (config != null) {
                if (title != null && !title.trim().isEmpty()) {
                    config.setNotificationTitle(title);
                }
                if (content != null && !content.trim().isEmpty()) {
                    config.setNotificationContent(content);
                }
            }
            
            // 通过广播通知服务更新通知
            Intent updateIntent = new Intent("io.dcloud.feature.keepalive.UPDATE_NOTIFICATION");
            updateIntent.putExtra("title", title);
            updateIntent.putExtra("content", content);
            context.sendBroadcast(updateIntent);
            
            Log.d(TAG, "通知更新请求已发送");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "更新通知异常", e);
            return false;
        }
    }
    
    /**
     * 更新通知进度
     * @param progress 进度值 (0-100)
     * @return 更新是否成功
     */
    public boolean updateNotificationProgress(int progress) {
        try {
            Log.d(TAG, "更新通知进度: " + progress);
            
            if (!isRunning) {
                Log.w(TAG, "服务未运行，无法更新进度");
                return false;
            }
            
            if (config == null || !config.isShowProgress()) {
                Log.w(TAG, "未启用进度显示");
                return false;
            }
            
            // 通过广播通知服务更新进度
            Intent updateIntent = new Intent("io.dcloud.feature.keepalive.UPDATE_PROGRESS");
            updateIntent.putExtra("progress", progress);
            context.sendBroadcast(updateIntent);
            
            Log.d(TAG, "进度更新请求已发送");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "更新进度异常", e);
            return false;
        }
    }
    
    /**
     * 获取心跳日志管理器
     * @return LocalHeartbeatLogger
     */
    public LocalHeartbeatLogger getHeartbeatLogger() {
        return heartbeatLogger;
    }
    
    /**
     * 获取策略信息
     * @return 策略信息JSON
     */
    public JSONObject getStrategyInfo() {
        try {
            if (!isInitialized || context == null) {
                Log.w(TAG, "管理器未初始化，无法获取策略信息");
                return new JSONObject();
            }
            
            KeepAliveStrategy strategy = new KeepAliveStrategy(context);
            return strategy.getStrategyInfo();
            
        } catch (Exception e) {
            Log.e(TAG, "获取策略信息异常", e);
            return new JSONObject();
        }
    }
    
    /**
     * 手动调整策略
     * @return 新的策略级别
     */
    public int adjustStrategy() {
        try {
            if (!isInitialized || context == null) {
                Log.w(TAG, "管理器未初始化，无法调整策略");
                return -1;
            }
            
            KeepAliveStrategy strategy = new KeepAliveStrategy(context);
            return strategy.adjustStrategy();
            
        } catch (Exception e) {
            Log.e(TAG, "调整策略异常", e);
            return -1;
        }
    }
    
    /**
     * 获取性能统计信息
     * @return 性能统计JSON对象
     */
    public JSONObject getPerformanceStats() {
        JSONObject stats = new JSONObject();
        
        try {
            if (!isInitialized) {
                stats.put("initialized", false);
                stats.put("message", "管理器未初始化");
                return stats;
            }
            
            stats.put("initialized", true);
            stats.put("isRunning", isRunning);
            
            // 获取心跳日志统计
            if (heartbeatLogger != null) {
                JSONObject heartbeatStats = heartbeatLogger.getStatistics();
                stats.put("heartbeatStats", heartbeatStats);
            }
            
            // 获取内存状态
            MemoryPressureHandler memoryHandler = new MemoryPressureHandler(context);
            JSONObject memoryStatus = memoryHandler.checkMemoryStatus();
            stats.put("memoryStatus", memoryStatus);
            
            // 获取配置信息
            if (config != null) {
                stats.put("heartbeatInterval", config.getHeartbeatInterval());
                stats.put("maxRetryCount", config.getMaxRetryCount());
            }
            
            Log.d(TAG, "性能统计信息获取成功");
            
        } catch (Exception e) {
            Log.e(TAG, "获取性能统计信息异常", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}
