package io.dcloud.feature.keepalive;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * CB-KeepAlive 保活插件主模块
 * 
 * 功能说明：
 * - 提供JavaScript与Android原生代码的通信桥梁
 * - 管理前台服务的启动和停止
 * - 提供权限检查和申请接口
 * - 提供设备适配和白名单引导接口
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class KeepAliveModule extends UniModule {
    
    private static final String TAG = "CB-KeepAlive";
    
    // 保活配置
    private KeepAliveConfig config;
    
    // 是否已初始化
    private boolean isInitialized = false;
    
    /**
     * 初始化保活服务
     * 
     * @param options 配置参数
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void init(JSONObject options, UniJSCallback callback) {
        Log.d(TAG, "初始化保活服务，配置: " + (options != null ? options.toJSONString() : "null"));
        
        try {
            Context context = mUniSDKInstance.getContext();
            if (context == null) {
                invokeError(callback, "获取上下文失败");
                return;
            }
            
            // 解析配置
            config = parseConfig(options);
            
            // 初始化各个管理器
            PermissionManager.getInstance().init(context);
            DeviceAdapterManager.getInstance().init(context);
            HeartbeatManager.getInstance().init(context, config.getHeartbeatInterval());
            NotificationHelper.getInstance().init(context, config.getNotificationConfig());
            
            isInitialized = true;
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("message", "初始化成功");
            result.put("deviceInfo", DeviceAdapterManager.getInstance().getDeviceInfoJson());
            
            callback.invoke(result);
            Log.d(TAG, "保活服务初始化成功");
            
        } catch (Exception e) {
            Log.e(TAG, "初始化异常: " + e.getMessage(), e);
            invokeError(callback, "初始化异常: " + e.getMessage());
        }
    }
    
    /**
     * 启动保活服务
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void start(UniJSCallback callback) {
        Log.d(TAG, "启动保活服务");
        
        try {
            if (!isInitialized) {
                invokeError(callback, "保活服务未初始化，请先调用init方法");
                return;
            }
            
            Context context = mUniSDKInstance.getContext();
            if (context == null) {
                invokeError(callback, "获取上下文失败");
                return;
            }
            
            // 启动前台服务
            Intent serviceIntent = new Intent(context, KeepAliveService.class);
            serviceIntent.setAction(KeepAliveService.ACTION_START);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            // 启动心跳监控
            HeartbeatManager.getInstance().start();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("isRunning", true);
            result.put("message", "保活服务启动成功");
            
            callback.invoke(result);
            Log.d(TAG, "保活服务启动成功");
            
        } catch (Exception e) {
            Log.e(TAG, "启动保活服务失败: " + e.getMessage(), e);
            invokeError(callback, "启动失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止保活服务
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void stop(UniJSCallback callback) {
        Log.d(TAG, "停止保活服务");
        
        try {
            Context context = mUniSDKInstance.getContext();
            if (context == null) {
                invokeError(callback, "获取上下文失败");
                return;
            }
            
            // 停止前台服务
            Intent serviceIntent = new Intent(context, KeepAliveService.class);
            serviceIntent.setAction(KeepAliveService.ACTION_STOP);
            context.startService(serviceIntent);
            
            // 停止心跳监控
            HeartbeatManager.getInstance().stop();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("isRunning", false);
            result.put("message", "保活服务已停止");
            
            callback.invoke(result);
            Log.d(TAG, "保活服务已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "停止保活服务失败: " + e.getMessage(), e);
            invokeError(callback, "停止失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取保活状态
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void getStatus(UniJSCallback callback) {
        Log.d(TAG, "获取保活状态");
        
        try {
            Context context = mUniSDKInstance.getContext();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("isRunning", KeepAliveService.isRunning());
            result.put("isInitialized", isInitialized);
            result.put("lastHeartbeat", HeartbeatManager.getInstance().getLastHeartbeatTime());
            result.put("heartbeatCount", HeartbeatManager.getInstance().getHeartbeatCount());
            result.put("errorCount", HeartbeatManager.getInstance().getErrorCount());
            
            // 权限状态
            JSONObject permissions = new JSONObject();
            permissions.put("foregroundService", PermissionManager.getInstance().checkForegroundServicePermission());
            permissions.put("notification", PermissionManager.getInstance().checkNotificationPermission());
            permissions.put("batteryOptimization", PermissionManager.getInstance().isIgnoringBatteryOptimizations());
            result.put("permissions", permissions);
            
            // 设备信息
            result.put("deviceInfo", DeviceAdapterManager.getInstance().getDeviceInfoJson());
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "获取状态失败: " + e.getMessage(), e);
            invokeError(callback, "获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查权限
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void checkPermissions(UniJSCallback callback) {
        Log.d(TAG, "检查权限");
        
        try {
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("foregroundService", PermissionManager.getInstance().checkForegroundServicePermission());
            result.put("notification", PermissionManager.getInstance().checkNotificationPermission());
            result.put("batteryOptimization", PermissionManager.getInstance().isIgnoringBatteryOptimizations());
            result.put("autoStart", PermissionManager.getInstance().checkAutoStartPermission());
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "检查权限失败: " + e.getMessage(), e);
            invokeError(callback, "检查权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 申请电池优化白名单
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = true)
    public void requestBatteryOptimization(UniJSCallback callback) {
        Log.d(TAG, "申请电池优化白名单");
        
        try {
            Context context = mUniSDKInstance.getContext();
            boolean success = PermissionManager.getInstance().requestIgnoreBatteryOptimizations(context);
            
            JSONObject result = new JSONObject();
            result.put("success", success);
            result.put("message", success ? "已跳转到电池优化设置" : "跳转失败");
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "申请电池优化白名单失败: " + e.getMessage(), e);
            invokeError(callback, "申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 打开自启动设置
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = true)
    public void openAutoStartSettings(UniJSCallback callback) {
        Log.d(TAG, "打开自启动设置");
        
        try {
            Context context = mUniSDKInstance.getContext();
            boolean success = DeviceAdapterManager.getInstance().openAutoStartSettings(context);
            
            JSONObject result = new JSONObject();
            result.put("success", success);
            result.put("message", success ? "已跳转到自启动设置" : "当前设备不支持或跳转失败");
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "打开自启动设置失败: " + e.getMessage(), e);
            invokeError(callback, "打开失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取白名单引导信息
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void getWhitelistGuide(UniJSCallback callback) {
        Log.d(TAG, "获取白名单引导信息");
        
        try {
            JSONObject guide = DeviceAdapterManager.getInstance().getWhitelistGuide();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("guide", guide);
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "获取白名单引导失败: " + e.getMessage(), e);
            invokeError(callback, "获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新通知内容
     * 
     * @param options 通知配置
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void updateNotification(JSONObject options, UniJSCallback callback) {
        Log.d(TAG, "更新通知内容");
        
        try {
            String title = options.getString("title");
            String content = options.getString("content");
            
            NotificationHelper.getInstance().updateNotification(title, content);
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("message", "通知已更新");
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "更新通知失败: " + e.getMessage(), e);
            invokeError(callback, "更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取心跳日志
     * 
     * @param options 查询参数
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void getHeartbeatLogs(JSONObject options, UniJSCallback callback) {
        Log.d(TAG, "获取心跳日志");
        
        try {
            int limit = options != null ? options.getIntValue("limit") : 100;
            if (limit <= 0) limit = 100;
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("logs", HeartbeatManager.getInstance().getHeartbeatLogs(limit));
            result.put("count", HeartbeatManager.getInstance().getHeartbeatCount());
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "获取心跳日志失败: " + e.getMessage(), e);
            invokeError(callback, "获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发心跳
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void triggerHeartbeat(UniJSCallback callback) {
        Log.d(TAG, "手动触发心跳");
        
        try {
            JSONObject heartbeatData = HeartbeatManager.getInstance().triggerHeartbeat();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("heartbeat", heartbeatData);
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "触发心跳失败: " + e.getMessage(), e);
            invokeError(callback, "触发失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除心跳日志
     * 
     * @param callback 回调函数
     */
    @UniJSMethod(uiThread = false)
    public void clearHeartbeatLogs(UniJSCallback callback) {
        Log.d(TAG, "清除心跳日志");
        
        try {
            HeartbeatManager.getInstance().clearLogs();
            
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("message", "心跳日志已清除");
            
            callback.invoke(result);
            
        } catch (Exception e) {
            Log.e(TAG, "清除心跳日志失败: " + e.getMessage(), e);
            invokeError(callback, "清除失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析配置
     */
    private KeepAliveConfig parseConfig(JSONObject options) {
        KeepAliveConfig config = new KeepAliveConfig();
        
        if (options != null) {
            config.setEnabled(options.getBooleanValue("enabled"));
            
            if (options.containsKey("heartbeatInterval")) {
                config.setHeartbeatInterval(options.getIntValue("heartbeatInterval"));
            }
            
            if (options.containsKey("maxRetryCount")) {
                config.setMaxRetryCount(options.getIntValue("maxRetryCount"));
            }
            
            JSONObject notificationConfig = options.getJSONObject("notificationConfig");
            if (notificationConfig != null) {
                config.setNotificationConfig(notificationConfig);
            }
        }
        
        return config;
    }
    
    /**
     * 返回错误结果
     */
    private void invokeError(UniJSCallback callback, String message) {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("message", message);
        callback.invoke(result);
    }
    
    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        Log.d(TAG, "Activity销毁，保活服务继续运行");
    }
}
