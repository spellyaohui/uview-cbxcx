package io.dcloud.feature.keepalive;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Debug;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 内存压力处理器
 * 负责监控系统内存状态并在内存压力下保持服务优先级
 * 
 * 功能特性：
 * 1. 监控系统内存状态
 * 2. 检测内存压力级别
 * 3. 在内存压力下保持服务优先级
 * 4. 实现降级保活方案
 * 5. 内存优化和清理
 * 
 * 验证需求：1.4, 5.5
 */
public class MemoryPressureHandler implements ComponentCallbacks2 {
    
    private static final String TAG = "MemoryPressureHandler";
    
    // 内存压力级别
    public static final int PRESSURE_LEVEL_NORMAL = 0;
    public static final int PRESSURE_LEVEL_LOW = 1;
    public static final int PRESSURE_LEVEL_MODERATE = 2;
    public static final int PRESSURE_LEVEL_CRITICAL = 3;
    
    // 内存阈值（MB）
    private static final long MEMORY_THRESHOLD_LOW = 100;
    private static final long MEMORY_THRESHOLD_MODERATE = 50;
    private static final long MEMORY_THRESHOLD_CRITICAL = 20;
    
    private Context context;
    private ActivityManager activityManager;
    private int currentPressureLevel = PRESSURE_LEVEL_NORMAL;
    private MemoryPressureListener listener;
    
    // 降级策略状态
    private boolean isDegraded = false;
    private int originalHeartbeatInterval = 30000;
    
    public MemoryPressureHandler(Context context) {
        this.context = context.getApplicationContext();
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        
        // 注册内存回调
        context.registerComponentCallbacks(this);
    }
    
    /**
     * 设置内存压力监听器
     * @param listener 监听器
     */
    public void setMemoryPressureListener(MemoryPressureListener listener) {
        this.listener = listener;
    }
    
    /**
     * 检查当前内存状态
     * @return 内存状态信息
     */
    public JSONObject checkMemoryStatus() {
        JSONObject status = new JSONObject();
        
        try {
            // 获取系统内存信息
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            // 可用内存（MB）
            long availableMemoryMB = memoryInfo.availMem / (1024 * 1024);
            long totalMemoryMB = memoryInfo.totalMem / (1024 * 1024);
            long usedMemoryMB = totalMemoryMB - availableMemoryMB;
            
            // 内存使用百分比
            int memoryUsagePercent = (int) ((usedMemoryMB * 100) / totalMemoryMB);
            
            // 是否处于低内存状态
            boolean isLowMemory = memoryInfo.lowMemory;
            
            // 低内存阈值
            long thresholdMB = memoryInfo.threshold / (1024 * 1024);
            
            status.put("availableMemoryMB", availableMemoryMB);
            status.put("totalMemoryMB", totalMemoryMB);
            status.put("usedMemoryMB", usedMemoryMB);
            status.put("memoryUsagePercent", memoryUsagePercent);
            status.put("isLowMemory", isLowMemory);
            status.put("thresholdMB", thresholdMB);
            
            // 获取应用内存使用情况
            Debug.MemoryInfo[] processMemoryInfo = activityManager.getProcessMemoryInfo(
                new int[]{android.os.Process.myPid()}
            );
            
            if (processMemoryInfo != null && processMemoryInfo.length > 0) {
                Debug.MemoryInfo appMemoryInfo = processMemoryInfo[0];
                int appMemoryKB = appMemoryInfo.getTotalPss();
                long appMemoryMB = appMemoryKB / 1024;
                
                status.put("appMemoryMB", appMemoryMB);
                status.put("appMemoryKB", appMemoryKB);
            }
            
            // 确定内存压力级别
            int pressureLevel = determinePressureLevel(availableMemoryMB, isLowMemory);
            status.put("pressureLevel", pressureLevel);
            status.put("pressureLevelName", getPressureLevelName(pressureLevel));
            
            Log.d(TAG, "内存状态: 可用=" + availableMemoryMB + "MB, 使用率=" + memoryUsagePercent + "%, 压力级别=" + getPressureLevelName(pressureLevel));
            
        } catch (Exception e) {
            Log.e(TAG, "检查内存状态异常", e);
        }
        
        return status;
    }
    
    /**
     * 确定内存压力级别
     * @param availableMemoryMB 可用内存（MB）
     * @param isLowMemory 是否低内存
     * @return 压力级别
     */
    private int determinePressureLevel(long availableMemoryMB, boolean isLowMemory) {
        if (isLowMemory || availableMemoryMB < MEMORY_THRESHOLD_CRITICAL) {
            return PRESSURE_LEVEL_CRITICAL;
        } else if (availableMemoryMB < MEMORY_THRESHOLD_MODERATE) {
            return PRESSURE_LEVEL_MODERATE;
        } else if (availableMemoryMB < MEMORY_THRESHOLD_LOW) {
            return PRESSURE_LEVEL_LOW;
        } else {
            return PRESSURE_LEVEL_NORMAL;
        }
    }
    
    /**
     * 获取压力级别名称
     * @param level 压力级别
     * @return 级别名称
     */
    private String getPressureLevelName(int level) {
        switch (level) {
            case PRESSURE_LEVEL_NORMAL:
                return "正常";
            case PRESSURE_LEVEL_LOW:
                return "轻度压力";
            case PRESSURE_LEVEL_MODERATE:
                return "中度压力";
            case PRESSURE_LEVEL_CRITICAL:
                return "严重压力";
            default:
                return "未知";
        }
    }
    
    /**
     * 处理内存压力
     * @param level 压力级别
     */
    public void handleMemoryPressure(int level) {
        try {
            Log.i(TAG, "处理内存压力: " + getPressureLevelName(level));
            
            currentPressureLevel = level;
            
            switch (level) {
                case PRESSURE_LEVEL_NORMAL:
                    // 正常状态，恢复正常策略
                    restoreNormalStrategy();
                    break;
                    
                case PRESSURE_LEVEL_LOW:
                    // 轻度压力，轻微优化
                    applyLightOptimization();
                    break;
                    
                case PRESSURE_LEVEL_MODERATE:
                    // 中度压力，应用降级策略
                    applyDegradedStrategy();
                    break;
                    
                case PRESSURE_LEVEL_CRITICAL:
                    // 严重压力，应用最小化策略
                    applyMinimalStrategy();
                    break;
            }
            
            // 通知监听器
            if (listener != null) {
                listener.onMemoryPressureChanged(level);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理内存压力异常", e);
        }
    }
    
    /**
     * 恢复正常策略
     */
    private void restoreNormalStrategy() {
        try {
            if (!isDegraded) {
                Log.d(TAG, "已处于正常策略");
                return;
            }
            
            Log.i(TAG, "恢复正常保活策略");
            
            // 恢复正常心跳间隔
            updateHeartbeatInterval(originalHeartbeatInterval);
            
            isDegraded = false;
            
            Log.d(TAG, "正常策略已恢复");
            
        } catch (Exception e) {
            Log.e(TAG, "恢复正常策略异常", e);
        }
    }
    
    /**
     * 应用轻度优化
     */
    private void applyLightOptimization() {
        try {
            Log.i(TAG, "应用轻度内存优化");
            
            // 清理不必要的缓存
            System.gc();
            
            Log.d(TAG, "轻度优化完成");
            
        } catch (Exception e) {
            Log.e(TAG, "应用轻度优化异常", e);
        }
    }
    
    /**
     * 应用降级策略
     */
    private void applyDegradedStrategy() {
        try {
            Log.i(TAG, "应用降级保活策略");
            
            if (!isDegraded) {
                // 保存原始心跳间隔
                KeepAliveManager manager = KeepAliveManager.getInstance();
                if (manager.isInitialized()) {
                    KeepAliveConfig config = manager.getConfig();
                    if (config != null) {
                        originalHeartbeatInterval = config.getHeartbeatInterval();
                    }
                }
            }
            
            // 降低心跳频率（延长间隔到2倍）
            int degradedInterval = originalHeartbeatInterval * 2;
            updateHeartbeatInterval(degradedInterval);
            
            // 清理内存
            System.gc();
            
            isDegraded = true;
            
            Log.d(TAG, "降级策略已应用，心跳间隔: " + degradedInterval + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "应用降级策略异常", e);
        }
    }
    
    /**
     * 应用最小化策略
     */
    private void applyMinimalStrategy() {
        try {
            Log.w(TAG, "应用最小化保活策略（严重内存压力）");
            
            if (!isDegraded) {
                // 保存原始心跳间隔
                KeepAliveManager manager = KeepAliveManager.getInstance();
                if (manager.isInitialized()) {
                    KeepAliveConfig config = manager.getConfig();
                    if (config != null) {
                        originalHeartbeatInterval = config.getHeartbeatInterval();
                    }
                }
            }
            
            // 大幅降低心跳频率（延长间隔到4倍）
            int minimalInterval = originalHeartbeatInterval * 4;
            updateHeartbeatInterval(minimalInterval);
            
            // 强制垃圾回收
            System.gc();
            System.runFinalization();
            
            isDegraded = true;
            
            Log.d(TAG, "最小化策略已应用，心跳间隔: " + minimalInterval + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "应用最小化策略异常", e);
        }
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
     * 获取当前压力级别
     * @return 压力级别
     */
    public int getCurrentPressureLevel() {
        return currentPressureLevel;
    }
    
    /**
     * 是否处于降级状态
     * @return 是否降级
     */
    public boolean isDegraded() {
        return isDegraded;
    }
    
    /**
     * 启动内存监控
     */
    public void startMonitoring() {
        try {
            Log.d(TAG, "启动内存监控");
            
            // 立即检查一次内存状态
            JSONObject status = checkMemoryStatus();
            int pressureLevel = status.getIntValue("pressureLevel");
            handleMemoryPressure(pressureLevel);
            
            // 定期检查内存状态（每分钟）
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable monitorRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject status = checkMemoryStatus();
                        int pressureLevel = status.getIntValue("pressureLevel");
                        
                        // 只有压力级别变化时才处理
                        if (pressureLevel != currentPressureLevel) {
                            handleMemoryPressure(pressureLevel);
                        }
                        
                        // 继续监控
                        handler.postDelayed(this, 60000); // 每分钟检查一次
                        
                    } catch (Exception e) {
                        Log.e(TAG, "内存监控异常", e);
                    }
                }
            };
            
            handler.postDelayed(monitorRunnable, 60000);
            
            Log.d(TAG, "内存监控已启动");
            
        } catch (Exception e) {
            Log.e(TAG, "启动内存监控异常", e);
        }
    }
    
    /**
     * 停止内存监控
     */
    public void stopMonitoring() {
        try {
            Log.d(TAG, "停止内存监控");
            
            // 恢复正常策略
            if (isDegraded) {
                restoreNormalStrategy();
            }
            
            // 注销内存回调
            context.unregisterComponentCallbacks(this);
            
            Log.d(TAG, "内存监控已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "停止内存监控异常", e);
        }
    }
    
    // ComponentCallbacks2 接口实现
    
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "收到内存修剪通知: " + level);
        
        try {
            // 根据系统内存修剪级别处理
            switch (level) {
                case TRIM_MEMORY_RUNNING_MODERATE:
                case TRIM_MEMORY_RUNNING_LOW:
                    // 应用在前台运行，但系统内存不足
                    handleMemoryPressure(PRESSURE_LEVEL_LOW);
                    break;
                    
                case TRIM_MEMORY_RUNNING_CRITICAL:
                    // 应用在前台运行，但系统内存严重不足
                    handleMemoryPressure(PRESSURE_LEVEL_MODERATE);
                    break;
                    
                case TRIM_MEMORY_UI_HIDDEN:
                    // UI不可见，可以释放UI资源
                    applyLightOptimization();
                    break;
                    
                case TRIM_MEMORY_BACKGROUND:
                case TRIM_MEMORY_MODERATE:
                    // 应用在后台，系统内存不足
                    handleMemoryPressure(PRESSURE_LEVEL_MODERATE);
                    break;
                    
                case TRIM_MEMORY_COMPLETE:
                    // 应用在后台，系统内存严重不足，可能被杀死
                    handleMemoryPressure(PRESSURE_LEVEL_CRITICAL);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理内存修剪通知异常", e);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 配置变化时不需要特殊处理
    }
    
    @Override
    public void onLowMemory() {
        Log.w(TAG, "收到低内存警告");
        
        try {
            // 系统内存严重不足
            handleMemoryPressure(PRESSURE_LEVEL_CRITICAL);
            
        } catch (Exception e) {
            Log.e(TAG, "处理低内存警告异常", e);
        }
    }
    
    /**
     * 内存压力监听器接口
     */
    public interface MemoryPressureListener {
        /**
         * 内存压力级别变化
         * @param level 新的压力级别
         */
        void onMemoryPressureChanged(int level);
    }
}
