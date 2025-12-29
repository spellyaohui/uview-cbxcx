package io.dcloud.feature.keepalive;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 保活策略管理器
 * 负责根据应用状态和系统资源动态调整保活策略
 * 
 * 功能特性：
 * 1. 根据应用活跃状态调整保活强度
 * 2. 基于系统资源状况自适应调整
 * 3. 实现节能模式下的保活优化
 * 
 * 验证需求：3.3, 3.4, 3.5
 */
public class KeepAliveStrategy {
    
    private static final String TAG = "KeepAliveStrategy";
    
    // 策略级别
    public static final int STRATEGY_LEVEL_HIGH = 3;      // 高强度保活
    public static final int STRATEGY_LEVEL_NORMAL = 2;    // 正常保活
    public static final int STRATEGY_LEVEL_LOW = 1;       // 低强度保活
    public static final int STRATEGY_LEVEL_MINIMAL = 0;   // 最小保活
    
    // 心跳间隔（毫秒）
    private static final int HEARTBEAT_INTERVAL_HIGH = 30000;      // 30秒
    private static final int HEARTBEAT_INTERVAL_NORMAL = 60000;    // 60秒
    private static final int HEARTBEAT_INTERVAL_LOW = 120000;      // 2分钟
    private static final int HEARTBEAT_INTERVAL_MINIMAL = 300000;  // 5分钟
    
    // 应用活跃状态判断阈值（毫秒）
    private static final long APP_ACTIVE_THRESHOLD = 5 * 60 * 1000;  // 5分钟
    private static final long APP_INACTIVE_THRESHOLD = 30 * 60 * 1000; // 30分钟
    
    // 电池电量阈值
    private static final int BATTERY_LOW_THRESHOLD = 20;    // 低电量阈值
    private static final int BATTERY_CRITICAL_THRESHOLD = 10; // 极低电量阈值
    
    // 内存压力阈值（MB）
    private static final long MEMORY_LOW_THRESHOLD = 200;   // 可用内存低于200MB
    private static final long MEMORY_CRITICAL_THRESHOLD = 100; // 可用内存低于100MB
    
    private Context context;
    private int currentStrategyLevel;
    private long lastStrategyUpdateTime;
    private long lastAppActiveTime;
    
    public KeepAliveStrategy(Context context) {
        this.context = context;
        this.currentStrategyLevel = STRATEGY_LEVEL_NORMAL;
        this.lastStrategyUpdateTime = System.currentTimeMillis();
        this.lastAppActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 获取当前策略级别
     * @return 策略级别
     */
    public int getCurrentStrategyLevel() {
        return currentStrategyLevel;
    }
    
    /**
     * 动态调整保活策略
     * 验证需求：3.3, 3.4, 3.5 - 根据应用状态、系统资源和节能模式动态调整
     * @return 新的策略级别
     */
    public int adjustStrategy() {
        try {
            Log.d(TAG, "开始动态调整保活策略");
            
            // 获取各项状态指标
            boolean isAppActive = isAppActive();
            boolean isLowBattery = isLowBattery();
            boolean isPowerSaveMode = isPowerSaveMode();
            boolean isMemoryLow = isMemoryLow();
            boolean isScreenOn = isScreenOn();
            
            Log.d(TAG, String.format("状态指标 - 应用活跃:%b, 低电量:%b, 省电模式:%b, 内存不足:%b, 屏幕开启:%b",
                isAppActive, isLowBattery, isPowerSaveMode, isMemoryLow, isScreenOn));
            
            // 计算新的策略级别
            int newStrategyLevel = calculateStrategyLevel(
                isAppActive, isLowBattery, isPowerSaveMode, isMemoryLow, isScreenOn
            );
            
            // 更新策略级别
            if (newStrategyLevel != currentStrategyLevel) {
                Log.i(TAG, String.format("策略级别变更: %d -> %d", currentStrategyLevel, newStrategyLevel));
                currentStrategyLevel = newStrategyLevel;
                lastStrategyUpdateTime = System.currentTimeMillis();
                
                // 通知策略变更
                notifyStrategyChanged(newStrategyLevel);
            } else {
                Log.d(TAG, "策略级别保持不变: " + currentStrategyLevel);
            }
            
            return currentStrategyLevel;
            
        } catch (Exception e) {
            Log.e(TAG, "调整保活策略异常", e);
            return currentStrategyLevel;
        }
    }
    
    /**
     * 计算策略级别
     * 验证需求：3.3, 3.4, 3.5 - 综合考虑多个因素计算最优策略
     */
    private int calculateStrategyLevel(boolean isAppActive, boolean isLowBattery,
                                      boolean isPowerSaveMode, boolean isMemoryLow,
                                      boolean isScreenOn) {
        
        // 基础策略级别
        int level = STRATEGY_LEVEL_NORMAL;
        
        // 需求3.3：应用处于活跃使用状态时，增强保活策略强度
        if (isAppActive) {
            level = STRATEGY_LEVEL_HIGH;
            Log.d(TAG, "应用活跃，提升到高强度保活");
        }
        
        // 需求3.4：用户长时间未使用应用时，适当降低保活频率以节省电量
        if (!isAppActive && getAppInactiveTime() > APP_INACTIVE_THRESHOLD) {
            level = Math.min(level, STRATEGY_LEVEL_LOW);
            Log.d(TAG, "应用长时间未使用，降低到低强度保活");
        }
        
        // 需求3.5：节能模式下的保活优化
        if (isPowerSaveMode) {
            level = Math.min(level, STRATEGY_LEVEL_LOW);
            Log.d(TAG, "系统处于省电模式，降低保活强度");
        }
        
        // 低电量优化
        if (isLowBattery) {
            int batteryLevel = getBatteryLevel();
            if (batteryLevel < BATTERY_CRITICAL_THRESHOLD) {
                level = STRATEGY_LEVEL_MINIMAL;
                Log.d(TAG, "电量极低(" + batteryLevel + "%)，降至最小保活");
            } else if (batteryLevel < BATTERY_LOW_THRESHOLD) {
                level = Math.min(level, STRATEGY_LEVEL_LOW);
                Log.d(TAG, "电量较低(" + batteryLevel + "%)，降低保活强度");
            }
        }
        
        // 内存压力优化
        if (isMemoryLow) {
            long availableMemory = getAvailableMemory();
            if (availableMemory < MEMORY_CRITICAL_THRESHOLD) {
                level = Math.min(level, STRATEGY_LEVEL_MINIMAL);
                Log.d(TAG, "内存极度不足(" + availableMemory + "MB)，降至最小保活");
            } else if (availableMemory < MEMORY_LOW_THRESHOLD) {
                level = Math.min(level, STRATEGY_LEVEL_LOW);
                Log.d(TAG, "内存不足(" + availableMemory + "MB)，降低保活强度");
            }
        }
        
        // 屏幕关闭时适当降低
        if (!isScreenOn && level > STRATEGY_LEVEL_LOW) {
            level = Math.max(STRATEGY_LEVEL_LOW, level - 1);
            Log.d(TAG, "屏幕关闭，适当降低保活强度");
        }
        
        return level;
    }
    
    /**
     * 判断应用是否活跃
     * 验证需求：3.3 - 检测应用活跃状态
     * @return 是否活跃
     */
    public boolean isAppActive() {
        try {
            long currentTime = System.currentTimeMillis();
            long inactiveTime = currentTime - lastAppActiveTime;
            
            // 如果最近5分钟内有活动，认为是活跃状态
            if (inactiveTime < APP_ACTIVE_THRESHOLD) {
                return true;
            }
            
            // 检查应用是否在前台
            if (isAppInForeground()) {
                lastAppActiveTime = currentTime;
                return true;
            }
            
            // 检查最近使用时间
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                long lastUsedTime = getLastUsedTime();
                if (lastUsedTime > 0 && (currentTime - lastUsedTime) < APP_ACTIVE_THRESHOLD) {
                    lastAppActiveTime = lastUsedTime;
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "判断应用活跃状态异常", e);
            return false;
        }
    }
    
    /**
     * 判断应用是否在前台
     * @return 是否在前台
     */
    private boolean isAppInForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return false;
            }
            
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null || appProcesses.isEmpty()) {
                return false;
            }
            
            String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "判断应用前台状态异常", e);
            return false;
        }
    }
    
    /**
     * 获取应用最后使用时间
     * @return 最后使用时间戳
     */
    private long getLastUsedTime() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                if (usageStatsManager == null) {
                    return 0;
                }
                
                long currentTime = System.currentTimeMillis();
                long startTime = currentTime - 24 * 60 * 60 * 1000; // 查询最近24小时
                
                SortedMap<Long, UsageStats> usageStatsMap = new TreeMap<>();
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, currentTime
                );
                
                if (usageStatsList != null && !usageStatsList.isEmpty()) {
                    String packageName = context.getPackageName();
                    for (UsageStats usageStats : usageStatsList) {
                        if (usageStats.getPackageName().equals(packageName)) {
                            return usageStats.getLastTimeUsed();
                        }
                    }
                }
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "获取应用最后使用时间异常", e);
            return 0;
        }
    }
    
    /**
     * 获取应用未活跃时间
     * @return 未活跃时间（毫秒）
     */
    public long getAppInactiveTime() {
        return System.currentTimeMillis() - lastAppActiveTime;
    }
    
    /**
     * 更新应用活跃时间
     */
    public void updateAppActiveTime() {
        lastAppActiveTime = System.currentTimeMillis();
        Log.d(TAG, "更新应用活跃时间");
    }
    
    /**
     * 判断是否低电量
     * 验证需求：3.5 - 节能模式下的优化
     * @return 是否低电量
     */
    public boolean isLowBattery() {
        try {
            int batteryLevel = getBatteryLevel();
            return batteryLevel > 0 && batteryLevel < BATTERY_LOW_THRESHOLD;
        } catch (Exception e) {
            Log.e(TAG, "判断低电量状态异常", e);
            return false;
        }
    }
    
    /**
     * 获取电池电量
     * @return 电池电量百分比
     */
    public int getBatteryLevel() {
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, intentFilter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level >= 0 && scale > 0) {
                    return (int) ((level / (float) scale) * 100);
                }
            }
            
            return -1;
            
        } catch (Exception e) {
            Log.e(TAG, "获取电池电量异常", e);
            return -1;
        }
    }
    
    /**
     * 判断是否处于省电模式
     * 验证需求：3.5 - 节能模式检测
     * @return 是否省电模式
     */
    public boolean isPowerSaveMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager != null) {
                    return powerManager.isPowerSaveMode();
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断省电模式异常", e);
            return false;
        }
    }
    
    /**
     * 判断内存是否不足
     * 验证需求：3.5 - 系统资源状况自适应
     * @return 是否内存不足
     */
    public boolean isMemoryLow() {
        try {
            long availableMemory = getAvailableMemory();
            return availableMemory > 0 && availableMemory < MEMORY_LOW_THRESHOLD;
        } catch (Exception e) {
            Log.e(TAG, "判断内存状态异常", e);
            return false;
        }
    }
    
    /**
     * 获取可用内存（MB）
     * @return 可用内存大小
     */
    public long getAvailableMemory() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return -1;
            }
            
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            return memoryInfo.availMem / (1024 * 1024); // 转换为MB
            
        } catch (Exception e) {
            Log.e(TAG, "获取可用内存异常", e);
            return -1;
        }
    }
    
    /**
     * 判断屏幕是否开启
     * @return 是否开启
     */
    public boolean isScreenOn() {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    return powerManager.isInteractive();
                } else {
                    return powerManager.isScreenOn();
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "判断屏幕状态异常", e);
            return true;
        }
    }
    
    /**
     * 根据策略级别获取心跳间隔
     * @param strategyLevel 策略级别
     * @return 心跳间隔（毫秒）
     */
    public int getHeartbeatInterval(int strategyLevel) {
        switch (strategyLevel) {
            case STRATEGY_LEVEL_HIGH:
                return HEARTBEAT_INTERVAL_HIGH;
            case STRATEGY_LEVEL_NORMAL:
                return HEARTBEAT_INTERVAL_NORMAL;
            case STRATEGY_LEVEL_LOW:
                return HEARTBEAT_INTERVAL_LOW;
            case STRATEGY_LEVEL_MINIMAL:
                return HEARTBEAT_INTERVAL_MINIMAL;
            default:
                return HEARTBEAT_INTERVAL_NORMAL;
        }
    }
    
    /**
     * 获取当前心跳间隔
     * @return 心跳间隔（毫秒）
     */
    public int getCurrentHeartbeatInterval() {
        return getHeartbeatInterval(currentStrategyLevel);
    }
    
    /**
     * 获取策略级别名称
     * @param level 策略级别
     * @return 级别名称
     */
    public String getStrategyLevelName(int level) {
        switch (level) {
            case STRATEGY_LEVEL_HIGH:
                return "高强度";
            case STRATEGY_LEVEL_NORMAL:
                return "正常";
            case STRATEGY_LEVEL_LOW:
                return "低强度";
            case STRATEGY_LEVEL_MINIMAL:
                return "最小";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取策略信息
     * @return 策略信息JSON
     */
    public JSONObject getStrategyInfo() {
        JSONObject info = new JSONObject();
        
        try {
            info.put("currentLevel", currentStrategyLevel);
            info.put("levelName", getStrategyLevelName(currentStrategyLevel));
            info.put("heartbeatInterval", getCurrentHeartbeatInterval());
            info.put("lastUpdateTime", lastStrategyUpdateTime);
            info.put("lastAppActiveTime", lastAppActiveTime);
            info.put("appInactiveTime", getAppInactiveTime());
            
            // 系统状态
            JSONObject systemStatus = new JSONObject();
            systemStatus.put("isAppActive", isAppActive());
            systemStatus.put("isLowBattery", isLowBattery());
            systemStatus.put("batteryLevel", getBatteryLevel());
            systemStatus.put("isPowerSaveMode", isPowerSaveMode());
            systemStatus.put("isMemoryLow", isMemoryLow());
            systemStatus.put("availableMemory", getAvailableMemory());
            systemStatus.put("isScreenOn", isScreenOn());
            
            info.put("systemStatus", systemStatus);
            
        } catch (Exception e) {
            Log.e(TAG, "获取策略信息异常", e);
        }
        
        return info;
    }
    
    /**
     * 通知策略变更
     * @param newLevel 新的策略级别
     */
    private void notifyStrategyChanged(int newLevel) {
        try {
            Intent intent = new Intent("io.dcloud.feature.keepalive.STRATEGY_CHANGED");
            intent.putExtra("strategyLevel", newLevel);
            intent.putExtra("heartbeatInterval", getHeartbeatInterval(newLevel));
            intent.putExtra("levelName", getStrategyLevelName(newLevel));
            context.sendBroadcast(intent);
            
            Log.d(TAG, "已发送策略变更广播: " + getStrategyLevelName(newLevel));
            
        } catch (Exception e) {
            Log.e(TAG, "发送策略变更广播异常", e);
        }
    }
}
