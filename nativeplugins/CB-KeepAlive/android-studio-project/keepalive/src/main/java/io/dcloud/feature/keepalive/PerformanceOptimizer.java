package io.dcloud.feature.keepalive;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 性能优化管理器
 * 负责CPU和内存优化，确保保活功能的资源消耗在合理范围内
 * 
 * 功能特性：
 * 1. 动态调整心跳间隔以优化CPU使用
 * 2. 减少不必要的JNI调用
 * 3. 限制本地日志存储大小
 * 4. 监控和优化资源使用
 * 
 * 性能目标：
 * - CPU占用 < 1%
 * - 内存占用 < 10MB
 * 
 * 验证需求：性能需求
 */
public class PerformanceOptimizer {
    
    private static final String TAG = "PerformanceOptimizer";
    
    // 性能目标
    private static final int TARGET_CPU_PERCENT = 1;
    private static final long TARGET_MEMORY_MB = 10;
    
    // 心跳间隔范围（毫秒）
    private static final int MIN_HEARTBEAT_INTERVAL = 15000;  // 最小15秒
    private static final int MAX_HEARTBEAT_INTERVAL = 120000; // 最大2分钟
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 30000; // 默认30秒
    
    // 日志大小限制
    private static final int MAX_LOG_COUNT = 100;
    private static final int LOG_CLEANUP_THRESHOLD = 90; // 达到90条时触发清理
    
    // JNI调用优化
    private static final long JNI_CALL_CACHE_DURATION = 5000; // JNI调用结果缓存5秒
    
    private Context context;
    private Handler optimizationHandler;
    private LocalHeartbeatLogger heartbeatLogger;
    
    // 性能统计
    private long lastOptimizationTime = 0;
    private int optimizationCount = 0;
    
    // 心跳间隔调整状态
    private int currentHeartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    private boolean isIntervalAdjusted = false;
    
    // JNI调用缓存
    private JSONObject cachedDeviceInfo = null;
    private long deviceInfoCacheTime = 0;
    
    public PerformanceOptimizer(Context context, LocalHeartbeatLogger heartbeatLogger) {
        this.context = context;
        this.heartbeatLogger = heartbeatLogger;
        this.optimizationHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 启动性能优化
     */
    public void startOptimization() {
        try {
            Log.d(TAG, "启动性能优化");
            
            // 立即执行一次优化
            performOptimization();
            
            // 定期执行优化（每5分钟）
            schedulePeriodicOptimization();
            
            Log.d(TAG, "性能优化已启动");
            
        } catch (Exception e) {
            Log.e(TAG, "启动性能优化异常", e);
        }
    }
    
    /**
     * 停止性能优化
     */
    public void stopOptimization() {
        try {
            Log.d(TAG, "停止性能优化");
            
            if (optimizationHandler != null) {
                optimizationHandler.removeCallbacksAndMessages(null);
            }
            
            // 清理缓存
            clearCache();
            
            Log.d(TAG, "性能优化已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "停止性能优化异常", e);
        }
    }
    
    /**
     * 执行性能优化
     */
    private void performOptimization() {
        try {
            Log.d(TAG, "执行性能优化");
            
            long startTime = System.currentTimeMillis();
            
            // 1. 优化心跳间隔
            optimizeHeartbeatInterval();
            
            // 2. 清理日志
            cleanupLogs();
            
            // 3. 清理过期缓存
            cleanupExpiredCache();
            
            // 4. 内存优化
            optimizeMemory();
            
            long duration = System.currentTimeMillis() - startTime;
            optimizationCount++;
            lastOptimizationTime = System.currentTimeMillis();
            
            Log.d(TAG, "性能优化完成，耗时: " + duration + "ms, 总次数: " + optimizationCount);
            
        } catch (Exception e) {
            Log.e(TAG, "执行性能优化异常", e);
        }
    }
    
    /**
     * 优化心跳间隔
     * 根据应用状态和系统资源动态调整心跳频率
     */
    private void optimizeHeartbeatInterval() {
        try {
            // 获取当前日志统计
            if (heartbeatLogger == null) {
                Log.w(TAG, "心跳日志管理器未初始化");
                return;
            }
            
            JSONObject stats = heartbeatLogger.getStatistics();
            int totalCount = stats.getIntValue("totalCount");
            
            if (totalCount < 10) {
                // 日志数量太少，不进行调整
                return;
            }
            
            // 计算实际平均心跳间隔
            long averageInterval = stats.getLongValue("averageInterval");
            
            // 获取最近1小时的心跳数
            int recentHourCount = stats.getIntValue("recentHourCount");
            
            // 根据心跳频率调整间隔
            int newInterval = calculateOptimalInterval(averageInterval, recentHourCount);
            
            if (newInterval != currentHeartbeatInterval) {
                Log.i(TAG, "调整心跳间隔: " + currentHeartbeatInterval + "ms -> " + newInterval + "ms");
                
                currentHeartbeatInterval = newInterval;
                isIntervalAdjusted = true;
                
                // 更新配置
                updateHeartbeatInterval(newInterval);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "优化心跳间隔异常", e);
        }
    }
    
    /**
     * 计算最优心跳间隔
     * @param averageInterval 平均间隔
     * @param recentHourCount 最近1小时心跳数
     * @return 最优间隔
     */
    private int calculateOptimalInterval(long averageInterval, int recentHourCount) {
        // 如果最近1小时心跳数过多（超过150次，即平均24秒一次），延长间隔
        if (recentHourCount > 150) {
            int newInterval = (int) (currentHeartbeatInterval * 1.2);
            return Math.min(newInterval, MAX_HEARTBEAT_INTERVAL);
        }
        
        // 如果最近1小时心跳数过少（少于60次，即平均60秒一次），缩短间隔
        if (recentHourCount < 60 && currentHeartbeatInterval > MIN_HEARTBEAT_INTERVAL) {
            int newInterval = (int) (currentHeartbeatInterval * 0.9);
            return Math.max(newInterval, MIN_HEARTBEAT_INTERVAL);
        }
        
        // 保持当前间隔
        return currentHeartbeatInterval;
    }
    
    /**
     * 更新心跳间隔
     * @param newInterval 新的心跳间隔
     */
    private void updateHeartbeatInterval(int newInterval) {
        try {
            KeepAliveManager manager = KeepAliveManager.getInstance();
            if (manager.isInitialized()) {
                KeepAliveConfig config = manager.getConfig();
                if (config != null) {
                    config.setHeartbeatInterval(newInterval);
                    manager.updateConfig(config);
                    
                    Log.d(TAG, "心跳间隔已更新: " + newInterval + "ms");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新心跳间隔异常", e);
        }
    }
    
    /**
     * 清理日志
     * 限制日志数量在合理范围内
     */
    private void cleanupLogs() {
        try {
            if (heartbeatLogger == null) {
                return;
            }
            
            int logCount = heartbeatLogger.getLogCount();
            
            // 如果日志数量超过阈值，清理旧日志
            if (logCount >= LOG_CLEANUP_THRESHOLD) {
                Log.i(TAG, "日志数量达到阈值(" + logCount + "/" + MAX_LOG_COUNT + ")，开始清理");
                
                // 清理7天前的日志
                long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                heartbeatLogger.clearLogsBefore(sevenDaysAgo);
                
                int newLogCount = heartbeatLogger.getLogCount();
                Log.d(TAG, "日志清理完成，剩余: " + newLogCount + " 条");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清理日志异常", e);
        }
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // 清理设备信息缓存
            if (cachedDeviceInfo != null && 
                (currentTime - deviceInfoCacheTime) > JNI_CALL_CACHE_DURATION) {
                cachedDeviceInfo = null;
                deviceInfoCacheTime = 0;
                Log.d(TAG, "已清理过期的设备信息缓存");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清理过期缓存异常", e);
        }
    }
    
    /**
     * 内存优化
     */
    private void optimizeMemory() {
        try {
            // 建议系统进行垃圾回收（不强制）
            System.gc();
            
            Log.d(TAG, "内存优化完成");
            
        } catch (Exception e) {
            Log.e(TAG, "内存优化异常", e);
        }
    }
    
    /**
     * 清理所有缓存
     */
    private void clearCache() {
        try {
            cachedDeviceInfo = null;
            deviceInfoCacheTime = 0;
            
            Log.d(TAG, "缓存已清理");
            
        } catch (Exception e) {
            Log.e(TAG, "清理缓存异常", e);
        }
    }
    
    /**
     * 调度定期优化
     */
    private void schedulePeriodicOptimization() {
        try {
            if (optimizationHandler == null) {
                return;
            }
            
            // 每5分钟执行一次优化
            optimizationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    performOptimization();
                    
                    // 继续调度下一次优化
                    schedulePeriodicOptimization();
                }
            }, 5 * 60 * 1000); // 5分钟
            
        } catch (Exception e) {
            Log.e(TAG, "调度定期优化异常", e);
        }
    }
    
    /**
     * 获取缓存的设备信息
     * 减少频繁的JNI调用
     * @return 设备信息JSON对象
     */
    public JSONObject getCachedDeviceInfo() {
        long currentTime = System.currentTimeMillis();
        
        // 如果缓存有效，直接返回
        if (cachedDeviceInfo != null && 
            (currentTime - deviceInfoCacheTime) < JNI_CALL_CACHE_DURATION) {
            return cachedDeviceInfo;
        }
        
        // 缓存失效，返回null，需要重新获取
        return null;
    }
    
    /**
     * 设置设备信息缓存
     * @param deviceInfo 设备信息
     */
    public void setCachedDeviceInfo(JSONObject deviceInfo) {
        this.cachedDeviceInfo = deviceInfo;
        this.deviceInfoCacheTime = System.currentTimeMillis();
    }
    
    /**
     * 获取性能统计信息
     * @return 统计信息JSON对象
     */
    public JSONObject getPerformanceStats() {
        JSONObject stats = new JSONObject();
        
        try {
            stats.put("optimizationCount", optimizationCount);
            stats.put("lastOptimizationTime", lastOptimizationTime);
            stats.put("currentHeartbeatInterval", currentHeartbeatInterval);
            stats.put("isIntervalAdjusted", isIntervalAdjusted);
            
            // 日志统计
            if (heartbeatLogger != null) {
                stats.put("logCount", heartbeatLogger.getLogCount());
                stats.put("maxLogCount", MAX_LOG_COUNT);
            }
            
            // 缓存状态
            stats.put("hasCachedDeviceInfo", cachedDeviceInfo != null);
            
        } catch (Exception e) {
            Log.e(TAG, "获取性能统计异常", e);
        }
        
        return stats;
    }
    
    /**
     * 重置心跳间隔到默认值
     */
    public void resetHeartbeatInterval() {
        try {
            if (currentHeartbeatInterval != DEFAULT_HEARTBEAT_INTERVAL) {
                Log.i(TAG, "重置心跳间隔到默认值: " + DEFAULT_HEARTBEAT_INTERVAL + "ms");
                
                currentHeartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
                isIntervalAdjusted = false;
                
                updateHeartbeatInterval(DEFAULT_HEARTBEAT_INTERVAL);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "重置心跳间隔异常", e);
        }
    }
    
    /**
     * 获取当前心跳间隔
     * @return 心跳间隔（毫秒）
     */
    public int getCurrentHeartbeatInterval() {
        return currentHeartbeatInterval;
    }
    
    /**
     * 心跳间隔是否已调整
     * @return 是否已调整
     */
    public boolean isHeartbeatIntervalAdjusted() {
        return isIntervalAdjusted;
    }
}
