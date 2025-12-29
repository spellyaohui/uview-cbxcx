package io.dcloud.feature.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * 开机自启动广播接收器
 * 
 * 功能说明：
 * - 监听系统开机完成广播
 * - 自动重启保活服务
 * - 支持快速启动模式
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    // 配置存储
    private static final String PREFS_NAME = "cb_keepalive_prefs";
    private static final String KEY_AUTO_START_ENABLED = "auto_start_enabled";
    private static final String KEY_KEEP_ALIVE_ENABLED = "keep_alive_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);
        
        // 检查是否是开机完成广播
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
            "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            handleBootCompleted(context);
        }
    }
    
    /**
     * 处理开机完成事件
     */
    private void handleBootCompleted(Context context) {
        Log.d(TAG, "系统启动完成，检查是否需要启动保活服务");
        
        // 检查是否启用了自启动
        if (!isAutoStartEnabled(context)) {
            Log.d(TAG, "自启动未启用，跳过");
            return;
        }
        
        // 检查是否启用了保活功能
        if (!isKeepAliveEnabled(context)) {
            Log.d(TAG, "保活功能未启用，跳过");
            return;
        }
        
        // 启动保活服务
        startKeepAliveService(context);
        
        // 记录启动事件
        recordBootStartEvent(context);
    }
    
    /**
     * 启动保活服务
     */
    private void startKeepAliveService(Context context) {
        try {
            Log.d(TAG, "开机自启动保活服务");
            
            Intent serviceIntent = new Intent(context, KeepAliveService.class);
            serviceIntent.setAction(KeepAliveService.ACTION_START);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 需要使用 startForegroundService
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            Log.d(TAG, "保活服务启动命令已发送");
            
        } catch (Exception e) {
            Log.e(TAG, "启动保活服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查是否启用了自启动
     */
    private boolean isAutoStartEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_AUTO_START_ENABLED, true); // 默认启用
        } catch (Exception e) {
            Log.e(TAG, "检查自启动状态失败: " + e.getMessage());
            return true; // 出错时默认启用
        }
    }
    
    /**
     * 检查是否启用了保活功能
     */
    private boolean isKeepAliveEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_KEEP_ALIVE_ENABLED, true); // 默认启用
        } catch (Exception e) {
            Log.e(TAG, "检查保活状态失败: " + e.getMessage());
            return true; // 出错时默认启用
        }
    }
    
    /**
     * 记录开机启动事件
     */
    private void recordBootStartEvent(Context context) {
        try {
            HeartbeatManager.getInstance().init(context, 30000);
            HeartbeatManager.getInstance().recordAnomalyEvent(
                "BOOT_START",
                "系统开机后自动启动保活服务"
            );
        } catch (Exception e) {
            Log.e(TAG, "记录启动事件失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置自启动状态（静态方法，供外部调用）
     */
    public static void setAutoStartEnabled(Context context, boolean enabled) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_AUTO_START_ENABLED, enabled).apply();
            Log.d(TAG, "自启动状态设置为: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "设置自启动状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置保活功能状态（静态方法，供外部调用）
     */
    public static void setKeepAliveEnabled(Context context, boolean enabled) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_KEEP_ALIVE_ENABLED, enabled).apply();
            Log.d(TAG, "保活功能状态设置为: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "设置保活功能状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取自启动状态（静态方法，供外部调用）
     */
    public static boolean getAutoStartEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_AUTO_START_ENABLED, true);
        } catch (Exception e) {
            Log.e(TAG, "获取自启动状态失败: " + e.getMessage());
            return true;
        }
    }
    
    /**
     * 获取保活功能状态（静态方法，供外部调用）
     */
    public static boolean getKeepAliveEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_KEEP_ALIVE_ENABLED, true);
        } catch (Exception e) {
            Log.e(TAG, "获取保活功能状态失败: " + e.getMessage());
            return true;
        }
    }
}
