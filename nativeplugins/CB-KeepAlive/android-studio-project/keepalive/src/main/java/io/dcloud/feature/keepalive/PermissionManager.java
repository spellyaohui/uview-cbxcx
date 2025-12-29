package io.dcloud.feature.keepalive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;

/**
 * 权限管理器
 * 
 * 功能说明：
 * - 检查各种权限状态
 * - 申请必要的权限
 * - 引导用户进行权限设置
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    
    // 单例实例
    private static volatile PermissionManager instance;
    
    // 上下文
    private Context context;
    
    /**
     * 私有构造函数（无参数）
     */
    private PermissionManager() {
    }
    
    /**
     * 带上下文的构造函数
     * @param context 应用上下文
     */
    public PermissionManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * 获取单例实例
     */
    public static PermissionManager getInstance() {
        if (instance == null) {
            synchronized (PermissionManager.class) {
                if (instance == null) {
                    instance = new PermissionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化
     */
    public void init(Context context) {
        this.context = context.getApplicationContext();
        Log.d(TAG, "权限管理器初始化完成");
    }
    
    /**
     * 检查前台服务权限
     * Android 9.0+ 需要 FOREGROUND_SERVICE 权限
     */
    public boolean checkForegroundServicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9.0+ 检查前台服务权限
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 检查通知权限
     * Android 13+ 需要 POST_NOTIFICATIONS 权限
     */
    public boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 检查通知权限
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 检查是否在电池优化白名单中
     */
    @SuppressLint("BatteryLife")
    public boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return true;
    }
    
    /**
     * 申请忽略电池优化
     */
    @SuppressLint("BatteryLife")
    public boolean requestIgnoreBatteryOptimizations(Context activityContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!isIgnoringBatteryOptimizations()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activityContext.startActivity(intent);
                    Log.d(TAG, "已跳转到电池优化设置");
                    return true;
                } else {
                    Log.d(TAG, "已在电池优化白名单中");
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "跳转电池优化设置失败: " + e.getMessage(), e);
                // 尝试打开电池优化设置页面
                return openBatteryOptimizationSettings(activityContext);
            }
        }
        return true;
    }
    
    /**
     * 打开电池优化设置页面
     */
    public boolean openBatteryOptimizationSettings(Context activityContext) {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityContext.startActivity(intent);
            Log.d(TAG, "已打开电池优化设置页面");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "打开电池优化设置失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查自启动权限（厂商特定）
     */
    public boolean checkAutoStartPermission() {
        // 自启动权限无法直接检查，返回true表示未知
        return true;
    }
    
    /**
     * 打开应用详情设置页面
     */
    public boolean openAppSettings(Context activityContext) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityContext.startActivity(intent);
            Log.d(TAG, "已打开应用详情设置");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "打开应用详情设置失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 打开通知设置页面
     */
    public boolean openNotificationSettings(Context activityContext) {
        try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            } else {
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityContext.startActivity(intent);
            Log.d(TAG, "已打开通知设置");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "打开通知设置失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取所有权限状态（字符串格式）
     */
    public String getAllPermissionsStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("前台服务权限: ").append(checkForegroundServicePermission() ? "已授权" : "未授权").append("\n");
        sb.append("通知权限: ").append(checkNotificationPermission() ? "已授权" : "未授权").append("\n");
        sb.append("电池优化白名单: ").append(isIgnoringBatteryOptimizations() ? "已加入" : "未加入").append("\n");
        return sb.toString();
    }
    
    /**
     * 检查所有权限状态（JSON格式）
     * @return JSONObject 包含所有权限状态
     */
    public JSONObject checkAllPermissions() {
        JSONObject permissions = new JSONObject();
        
        try {
            permissions.put("foregroundService", checkForegroundServicePermission());
            permissions.put("notification", checkNotificationPermission());
            permissions.put("batteryOptimization", isIgnoringBatteryOptimizations());
            permissions.put("autoStart", checkAutoStartPermission());
            
            // 添加状态描述
            JSONObject descriptions = new JSONObject();
            descriptions.put("foregroundService", checkForegroundServicePermission() ? "已授权" : "未授权");
            descriptions.put("notification", checkNotificationPermission() ? "已授权" : "未授权");
            descriptions.put("batteryOptimization", isIgnoringBatteryOptimizations() ? "已加入白名单" : "未加入白名单");
            descriptions.put("autoStart", "未知（需要用户手动检查）");
            
            permissions.put("descriptions", descriptions);
            
        } catch (Exception e) {
            Log.e(TAG, "检查权限状态异常: " + e.getMessage(), e);
        }
        
        return permissions;
    }
}
