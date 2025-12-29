package io.dcloud.feature.keepalive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 服务重启管理器
 * 负责检测服务异常终止并实现自动重启机制
 * 
 * 功能特性：
 * 1. 服务异常终止检测
 * 2. 自动重启机制
 * 3. 重启次数限制和退避策略
 * 4. 心跳监控和通知恢复
 * 
 * 验证需求：1.2, 4.4
 */
public class ServiceRestartManager {
    
    private static final String TAG = "ServiceRestartManager";
    
    // 重启相关常量
    private static final String ACTION_RESTART_SERVICE = "io.dcloud.feature.keepalive.RESTART_SERVICE";
    private static final int MAX_RESTART_ATTEMPTS = 5; // 最大重启尝试次数
    private static final long RESTART_DELAY_BASE = 5000; // 基础重启延迟（5秒）
    private static final long RESTART_DELAY_MAX = 300000; // 最大重启延迟（5分钟）
    private static final long RESTART_WINDOW = 600000; // 重启窗口期（10分钟）
    
    private Context context;
    private int restartAttempts = 0;
    private long lastRestartTime = 0;
    private long lastServiceStopTime = 0;
    
    public ServiceRestartManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * 检测服务是否异常终止
     * @return 是否异常终止
     */
    public boolean detectServiceTermination() {
        try {
            // 检查服务是否正在运行
            boolean isRunning = isServiceRunning();
            
            if (!isRunning && shouldServiceBeRunning()) {
                Log.w(TAG, "检测到服务异常终止");
                lastServiceStopTime = System.currentTimeMillis();
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "检测服务终止状态异常", e);
            return false;
        }
    }
    
    /**
     * 检查服务是否应该运行
     * @return 是否应该运行
     */
    private boolean shouldServiceBeRunning() {
        try {
            // 检查保活管理器状态
            KeepAliveManager manager = KeepAliveManager.getInstance();
            return manager.isInitialized() && manager.isRunning();
            
        } catch (Exception e) {
            Log.e(TAG, "检查服务运行状态异常", e);
            return false;
        }
    }
    
    /**
     * 检查服务是否正在运行
     * @return 是否正在运行
     */
    private boolean isServiceRunning() {
        try {
            android.app.ActivityManager manager = 
                (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            
            if (manager == null) {
                return false;
            }
            
            for (android.app.ActivityManager.RunningServiceInfo service : 
                 manager.getRunningServices(Integer.MAX_VALUE)) {
                if (KeepAliveService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "检查服务运行状态异常", e);
            return false;
        }
    }
    
    /**
     * 尝试重启服务
     * @return 是否成功调度重启
     */
    public boolean attemptRestart() {
        try {
            Log.i(TAG, "尝试重启服务，当前尝试次数: " + restartAttempts);
            
            // 检查是否超过最大重启次数
            if (restartAttempts >= MAX_RESTART_ATTEMPTS) {
                Log.e(TAG, "已达到最大重启次数限制: " + MAX_RESTART_ATTEMPTS);
                
                // 记录重启失败事件
                recordRestartFailure();
                
                return false;
            }
            
            // 检查重启窗口期
            long currentTime = System.currentTimeMillis();
            if (lastRestartTime > 0 && (currentTime - lastRestartTime) > RESTART_WINDOW) {
                // 超过窗口期，重置重启计数
                Log.d(TAG, "超过重启窗口期，重置重启计数");
                restartAttempts = 0;
            }
            
            // 计算重启延迟（指数退避）
            long restartDelay = calculateRestartDelay();
            
            Log.i(TAG, "调度服务重启，延迟: " + restartDelay + "ms");
            
            // 调度重启
            scheduleRestart(restartDelay);
            
            // 更新重启状态
            restartAttempts++;
            lastRestartTime = currentTime;
            
            // 记录重启事件
            recordRestartAttempt();
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "尝试重启服务异常", e);
            return false;
        }
    }
    
    /**
     * 计算重启延迟（指数退避策略）
     * @return 延迟时间（毫秒）
     */
    private long calculateRestartDelay() {
        // 使用指数退避算法：delay = base * 2^attempts
        long delay = RESTART_DELAY_BASE * (long) Math.pow(2, restartAttempts);
        
        // 限制最大延迟
        return Math.min(delay, RESTART_DELAY_MAX);
    }
    
    /**
     * 调度服务重启
     * @param delayMillis 延迟时间（毫秒）
     */
    private void scheduleRestart(long delayMillis) {
        try {
            Intent restartIntent = new Intent(context, ServiceRestartReceiver.class);
            restartIntent.setAction(ACTION_RESTART_SERVICE);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                restartIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT :
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                long triggerTime = SystemClock.elapsedRealtime() + delayMillis;
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Android 6.0+ 使用 setExactAndAllowWhileIdle 确保在低电量模式下也能触发
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    );
                }
                
                Log.d(TAG, "重启已调度，将在 " + delayMillis + "ms 后执行");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "调度重启异常", e);
        }
    }
    
    /**
     * 执行服务重启
     * @return 是否重启成功
     */
    public boolean restartService() {
        try {
            Log.i(TAG, "执行服务重启");
            
            // 获取保活管理器
            KeepAliveManager manager = KeepAliveManager.getInstance();
            
            if (!manager.isInitialized()) {
                Log.e(TAG, "保活管理器未初始化，无法重启服务");
                return false;
            }
            
            // 先停止现有服务（如果还在运行）
            if (isServiceRunning()) {
                Log.d(TAG, "停止现有服务");
                manager.stop();
                
                // 等待服务完全停止
                Thread.sleep(1000);
            }
            
            // 启动服务
            Log.d(TAG, "启动保活服务");
            boolean success = manager.start();
            
            if (success) {
                Log.i(TAG, "服务重启成功");
                
                // 重置重启计数（成功重启后）
                restartAttempts = 0;
                
                // 记录重启成功事件
                recordRestartSuccess();
                
                // 恢复心跳监控和通知
                restoreServiceState();
                
            } else {
                Log.e(TAG, "服务重启失败");
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "执行服务重启异常", e);
            return false;
        }
    }
    
    /**
     * 恢复服务状态
     * 包括心跳监控和通知显示
     */
    private void restoreServiceState() {
        try {
            Log.d(TAG, "恢复服务状态");
            
            // 获取保活管理器
            KeepAliveManager manager = KeepAliveManager.getInstance();
            
            // 获取配置
            KeepAliveConfig config = manager.getConfig();
            if (config == null) {
                Log.w(TAG, "配置为空，使用默认配置");
                return;
            }
            
            // 恢复通知内容
            String title = config.getNotificationTitle();
            String content = config.getNotificationContent();
            
            if (title != null && content != null) {
                Log.d(TAG, "恢复通知内容: " + title);
                manager.updateNotification(title, content);
            }
            
            Log.d(TAG, "服务状态恢复完成");
            
        } catch (Exception e) {
            Log.e(TAG, "恢复服务状态异常", e);
        }
    }
    
    /**
     * 记录重启尝试事件
     */
    private void recordRestartAttempt() {
        try {
            KeepAliveManager manager = KeepAliveManager.getInstance();
            LocalHeartbeatLogger logger = manager.getHeartbeatLogger();
            
            if (logger != null) {
                JSONObject event = new JSONObject();
                event.put("eventType", "service_restart_attempt");
                event.put("attemptNumber", restartAttempts);
                event.put("timestamp", System.currentTimeMillis());
                event.put("lastStopTime", lastServiceStopTime);
                
                logger.logEvent(event);
                Log.d(TAG, "重启尝试事件已记录");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "记录重启尝试事件异常", e);
        }
    }
    
    /**
     * 记录重启成功事件
     */
    private void recordRestartSuccess() {
        try {
            KeepAliveManager manager = KeepAliveManager.getInstance();
            LocalHeartbeatLogger logger = manager.getHeartbeatLogger();
            
            if (logger != null) {
                JSONObject event = new JSONObject();
                event.put("eventType", "service_restart_success");
                event.put("totalAttempts", restartAttempts);
                event.put("timestamp", System.currentTimeMillis());
                event.put("downtime", System.currentTimeMillis() - lastServiceStopTime);
                
                logger.logEvent(event);
                Log.d(TAG, "重启成功事件已记录");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "记录重启成功事件异常", e);
        }
    }
    
    /**
     * 记录重启失败事件
     */
    private void recordRestartFailure() {
        try {
            KeepAliveManager manager = KeepAliveManager.getInstance();
            LocalHeartbeatLogger logger = manager.getHeartbeatLogger();
            
            if (logger != null) {
                JSONObject event = new JSONObject();
                event.put("eventType", "service_restart_failure");
                event.put("totalAttempts", restartAttempts);
                event.put("timestamp", System.currentTimeMillis());
                event.put("reason", "max_attempts_reached");
                
                logger.logEvent(event);
                Log.e(TAG, "重启失败事件已记录");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "记录重启失败事件异常", e);
        }
    }
    
    /**
     * 重置重启计数
     */
    public void resetRestartAttempts() {
        Log.d(TAG, "重置重启计数");
        restartAttempts = 0;
        lastRestartTime = 0;
    }
    
    /**
     * 获取重启统计信息
     * @return 统计信息
     */
    public JSONObject getRestartStats() {
        JSONObject stats = new JSONObject();
        
        try {
            stats.put("restartAttempts", restartAttempts);
            stats.put("lastRestartTime", lastRestartTime);
            stats.put("lastServiceStopTime", lastServiceStopTime);
            stats.put("maxRestartAttempts", MAX_RESTART_ATTEMPTS);
            
            if (lastServiceStopTime > 0) {
                long downtime = System.currentTimeMillis() - lastServiceStopTime;
                stats.put("currentDowntime", downtime);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取重启统计信息异常", e);
        }
        
        return stats;
    }
    
    /**
     * 服务重启广播接收器
     */
    public static class ServiceRestartReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Log.d(TAG, "收到重启广播: " + action);
                
                if (ACTION_RESTART_SERVICE.equals(action)) {
                    // 执行服务重启
                    ServiceRestartManager restartManager = new ServiceRestartManager(context);
                    restartManager.restartService();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "处理重启广播异常", e);
            }
        }
    }
}
