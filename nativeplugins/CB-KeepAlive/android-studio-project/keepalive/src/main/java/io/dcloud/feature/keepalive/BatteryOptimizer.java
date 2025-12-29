package io.dcloud.feature.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 电池优化管理器
 * 负责监控电池状态并优化保活策略以减少电池消耗
 * 
 * 功能特性：
 * 1. 监控电池电量和充电状态
 * 2. 网络异常时暂停心跳
 * 3. 屏幕关闭时降低心跳频率
 * 4. 低电量时调整保活策略
 * 5. 充电时恢复正常策略
 * 
 * 性能目标：
 * - 电池影响 < 5%
 * 
 * 验证需求：性能需求
 */
public class BatteryOptimizer {
    
    private static final String TAG = "BatteryOptimizer";
    
    // 电池电量阈值
    private static final int BATTERY_LOW_THRESHOLD = 20;      // 低电量阈值 20%
    private static final int BATTERY_CRITICAL_THRESHOLD = 10; // 严重低电量阈值 10%
    
    // 心跳间隔调整倍数
    private static final float SCREEN_OFF_MULTIPLIER = 2.0f;    // 屏幕关闭时延长2倍
    private static final float LOW_BATTERY_MULTIPLIER = 3.0f;   // 低电量时延长3倍
    private static final float CRITICAL_BATTERY_MULTIPLIER = 5.0f; // 严重低电量时延长5倍
    
    private Context context;
    private Handler optimizationHandler;
    
    // 电池状态
    private int currentBatteryLevel = 100;
    private boolean isCharging = false;
    private boolean isScreenOn = true;
    private boolean isNetworkAvailable = true;
    
    // 优化状态
    private boolean isOptimizationActive = false;
    private int originalHeartbeatInterval = 30000;
    private int currentOptimizedInterval = 30000;
    
    // 广播接收器
    private BatteryReceiver batteryReceiver;
    private ScreenReceiver screenReceiver;
    private NetworkReceiver networkReceiver;
    
    // 统计信息
    private long optimizationStartTime = 0;
    private int optimizationCount = 0;
    private long totalOptimizationDuration = 0;
    
    public BatteryOptimizer(Context context) {
        this.context = context;
        this.optimizationHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 启动电池优化
     */
    public void startOptimization() {
        try {
            Log.d(TAG, "启动电池优化");
            
            // 获取初始电池状态
            updateBatteryStatus();
            
            // 获取初始屏幕状态
            updateScreenStatus();
            
            // 获取初始网络状态
            updateNetworkStatus();
            
            // 注册广播接收器
            registerReceivers();
            
            // 执行初始优化
            performOptimization();
            
            Log.d(TAG, "电池优化已启动");
            
        } catch (Exception e) {
            Log.e(TAG, "启动电池优化异常", e);
        }
    }
    
    /**
     * 停止电池优化
     */
    public void stopOptimization() {
        try {
            Log.d(TAG, "停止电池优化");
            
            // 注销广播接收器
            unregisterReceivers();
            
            // 恢复正常策略
            if (isOptimizationActive) {
                restoreNormalStrategy();
            }
            
            // 清理Handler
            if (optimizationHandler != null) {
                optimizationHandler.removeCallbacksAndMessages(null);
            }
            
            Log.d(TAG, "电池优化已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "停止电池优化异常", e);
        }
    }
    
    /**
     * 注册广播接收器
     */
    private void registerReceivers() {
        try {
            // 注册电池状态接收器
            if (batteryReceiver == null) {
                batteryReceiver = new BatteryReceiver();
            }
            IntentFilter batteryFilter = new IntentFilter();
            batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            context.registerReceiver(batteryReceiver, batteryFilter);
            
            // 注册屏幕状态接收器
            if (screenReceiver == null) {
                screenReceiver = new ScreenReceiver();
            }
            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction(Intent.ACTION_SCREEN_ON);
            screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(screenReceiver, screenFilter);
            
            // 注册网络状态接收器
            if (networkReceiver == null) {
                networkReceiver = new NetworkReceiver();
            }
            IntentFilter networkFilter = new IntentFilter();
            networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkReceiver, networkFilter);
            
            Log.d(TAG, "广播接收器已注册");
            
        } catch (Exception e) {
            Log.e(TAG, "注册广播接收器异常", e);
        }
    }
    
    /**
     * 注销广播接收器
     */
    private void unregisterReceivers() {
        try {
            if (batteryReceiver != null) {
                context.unregisterReceiver(batteryReceiver);
                batteryReceiver = null;
            }
            
            if (screenReceiver != null) {
                context.unregisterReceiver(screenReceiver);
                screenReceiver = null;
            }
            
            if (networkReceiver != null) {
                context.unregisterReceiver(networkReceiver);
                networkReceiver = null;
            }
            
            Log.d(TAG, "广播接收器已注销");
            
        } catch (Exception e) {
            Log.e(TAG, "注销广播接收器异常", e);
        }
    }
    
    /**
     * 更新电池状态
     */
    private void updateBatteryStatus() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            
            if (batteryStatus != null) {
                // 获取电池电量
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level >= 0 && scale > 0) {
                    currentBatteryLevel = (int) ((level / (float) scale) * 100);
                }
                
                // 获取充电状态
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL);
                
                Log.d(TAG, "电池状态更新: 电量=" + currentBatteryLevel + "%, 充电=" + isCharging);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "更新电池状态异常", e);
        }
    }
    
    /**
     * 更新屏幕状态
     */
    private void updateScreenStatus() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                    isScreenOn = pm.isInteractive();
                } else {
                    isScreenOn = pm.isScreenOn();
                }
                
                Log.d(TAG, "屏幕状态更新: " + (isScreenOn ? "开启" : "关闭"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "更新屏幕状态异常", e);
        }
    }
    
    /**
     * 更新网络状态
     */
    private void updateNetworkStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                isNetworkAvailable = (networkInfo != null && networkInfo.isConnected());
                
                Log.d(TAG, "网络状态更新: " + (isNetworkAvailable ? "可用" : "不可用"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "更新网络状态异常", e);
        }
    }
    
    /**
     * 执行优化
     */
    private void performOptimization() {
        try {
            Log.d(TAG, "执行电池优化");
            
            // 获取保活管理器
            KeepAliveManager manager = KeepAliveManager.getInstance();
            if (!manager.isInitialized()) {
                Log.w(TAG, "保活管理器未初始化，跳过优化");
                return;
            }
            
            // 获取原始心跳间隔
            KeepAliveConfig config = manager.getConfig();
            if (config != null) {
                originalHeartbeatInterval = config.getHeartbeatInterval();
            }
            
            // 计算最优心跳间隔
            int optimalInterval = calculateOptimalInterval();
            
            // 如果需要调整
            if (optimalInterval != originalHeartbeatInterval) {
                applyOptimizedStrategy(optimalInterval);
            } else if (isOptimizationActive) {
                restoreNormalStrategy();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "执行电池优化异常", e);
        }
    }
    
    /**
     * 计算最优心跳间隔
     * @return 最优间隔（毫秒）
     */
    private int calculateOptimalInterval() {
        // 如果正在充电，使用正常间隔
        if (isCharging) {
            Log.d(TAG, "设备正在充电，使用正常间隔");
            return originalHeartbeatInterval;
        }
        
        // 如果网络不可用，暂停心跳（使用极大值表示）
        if (!isNetworkAvailable) {
            Log.i(TAG, "网络不可用，暂停心跳");
            return Integer.MAX_VALUE;
        }
        
        float multiplier = 1.0f;
        
        // 根据电池电量调整
        if (currentBatteryLevel <= BATTERY_CRITICAL_THRESHOLD) {
            multiplier = CRITICAL_BATTERY_MULTIPLIER;
            Log.i(TAG, "严重低电量(" + currentBatteryLevel + "%)，延长心跳间隔 " + multiplier + " 倍");
        } else if (currentBatteryLevel <= BATTERY_LOW_THRESHOLD) {
            multiplier = LOW_BATTERY_MULTIPLIER;
            Log.i(TAG, "低电量(" + currentBatteryLevel + "%)，延长心跳间隔 " + multiplier + " 倍");
        }
        
        // 根据屏幕状态调整
        if (!isScreenOn) {
            multiplier *= SCREEN_OFF_MULTIPLIER;
            Log.i(TAG, "屏幕关闭，进一步延长心跳间隔");
        }
        
        int optimalInterval = (int) (originalHeartbeatInterval * multiplier);
        
        // 限制最大间隔为5分钟
        int maxInterval = 5 * 60 * 1000;
        if (optimalInterval > maxInterval) {
            optimalInterval = maxInterval;
        }
        
        Log.d(TAG, "计算最优心跳间隔: " + originalHeartbeatInterval + "ms -> " + optimalInterval + "ms");
        
        return optimalInterval;
    }
    
    /**
     * 应用优化策略
     * @param optimizedInterval 优化后的心跳间隔
     */
    private void applyOptimizedStrategy(int optimizedInterval) {
        try {
            Log.i(TAG, "应用电池优化策略，心跳间隔: " + optimizedInterval + "ms");
            
            // 如果网络不可用，暂停心跳
            if (optimizedInterval == Integer.MAX_VALUE) {
                pauseHeartbeat();
                return;
            }
            
            // 更新心跳间隔
            KeepAliveManager manager = KeepAliveManager.getInstance();
            if (manager.isInitialized()) {
                KeepAliveConfig config = manager.getConfig();
                if (config != null) {
                    config.setHeartbeatInterval(optimizedInterval);
                    manager.updateConfig(config);
                    
                    currentOptimizedInterval = optimizedInterval;
                    
                    if (!isOptimizationActive) {
                        isOptimizationActive = true;
                        optimizationStartTime = System.currentTimeMillis();
                        optimizationCount++;
                    }
                    
                    Log.d(TAG, "电池优化策略已应用");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "应用优化策略异常", e);
        }
    }
    
    /**
     * 暂停心跳
     */
    private void pauseHeartbeat() {
        try {
            Log.w(TAG, "暂停心跳（网络不可用）");
            
            // 设置一个极大的心跳间隔来实现暂停效果
            KeepAliveManager manager = KeepAliveManager.getInstance();
            if (manager.isInitialized()) {
                KeepAliveConfig config = manager.getConfig();
                if (config != null) {
                    config.setHeartbeatInterval(Integer.MAX_VALUE);
                    manager.updateConfig(config);
                    
                    if (!isOptimizationActive) {
                        isOptimizationActive = true;
                        optimizationStartTime = System.currentTimeMillis();
                        optimizationCount++;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "暂停心跳异常", e);
        }
    }
    
    /**
     * 恢复正常策略
     */
    private void restoreNormalStrategy() {
        try {
            Log.i(TAG, "恢复正常电池策略");
            
            // 恢复原始心跳间隔
            KeepAliveManager manager = KeepAliveManager.getInstance();
            if (manager.isInitialized()) {
                KeepAliveConfig config = manager.getConfig();
                if (config != null) {
                    config.setHeartbeatInterval(originalHeartbeatInterval);
                    manager.updateConfig(config);
                    
                    if (isOptimizationActive) {
                        long duration = System.currentTimeMillis() - optimizationStartTime;
                        totalOptimizationDuration += duration;
                        isOptimizationActive = false;
                    }
                    
                    currentOptimizedInterval = originalHeartbeatInterval;
                    
                    Log.d(TAG, "正常策略已恢复");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "恢复正常策略异常", e);
        }
    }
    
    /**
     * 获取电池优化统计信息
     * @return 统计信息JSON对象
     */
    public JSONObject getBatteryOptimizationStats() {
        JSONObject stats = new JSONObject();
        
        try {
            // 当前状态
            stats.put("currentBatteryLevel", currentBatteryLevel);
            stats.put("isCharging", isCharging);
            stats.put("isScreenOn", isScreenOn);
            stats.put("isNetworkAvailable", isNetworkAvailable);
            
            // 优化状态
            stats.put("isOptimizationActive", isOptimizationActive);
            stats.put("originalHeartbeatInterval", originalHeartbeatInterval);
            stats.put("currentOptimizedInterval", currentOptimizedInterval);
            
            // 统计信息
            stats.put("optimizationCount", optimizationCount);
            stats.put("totalOptimizationDuration", totalOptimizationDuration);
            
            if (isOptimizationActive) {
                long currentDuration = System.currentTimeMillis() - optimizationStartTime;
                stats.put("currentOptimizationDuration", currentDuration);
            }
            
            // 计算节能效果
            if (currentOptimizedInterval > originalHeartbeatInterval) {
                float savingPercent = ((currentOptimizedInterval - originalHeartbeatInterval) / 
                                      (float) originalHeartbeatInterval) * 100;
                stats.put("estimatedBatterySavingPercent", Math.round(savingPercent));
            } else {
                stats.put("estimatedBatterySavingPercent", 0);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取电池优化统计异常", e);
        }
        
        return stats;
    }
    
    /**
     * 电池状态广播接收器
     */
    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Log.d(TAG, "收到电池广播: " + action);
                
                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    // 电池状态变化
                    updateBatteryStatus();
                    performOptimization();
                    
                } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    // 开始充电
                    isCharging = true;
                    Log.i(TAG, "开始充电，恢复正常策略");
                    performOptimization();
                    
                } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    // 停止充电
                    isCharging = false;
                    Log.i(TAG, "停止充电，应用电池优化");
                    performOptimization();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "处理电池广播异常", e);
            }
        }
    }
    
    /**
     * 屏幕状态广播接收器
     */
    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Log.d(TAG, "收到屏幕广播: " + action);
                
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    // 屏幕开启
                    isScreenOn = true;
                    Log.i(TAG, "屏幕开启，调整心跳频率");
                    performOptimization();
                    
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    // 屏幕关闭
                    isScreenOn = false;
                    Log.i(TAG, "屏幕关闭，降低心跳频率");
                    performOptimization();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "处理屏幕广播异常", e);
            }
        }
    }
    
    /**
     * 网络状态广播接收器
     */
    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "收到网络广播");
                
                // 更新网络状态
                boolean wasAvailable = isNetworkAvailable;
                updateNetworkStatus();
                
                // 如果网络状态发生变化
                if (wasAvailable != isNetworkAvailable) {
                    if (isNetworkAvailable) {
                        Log.i(TAG, "网络恢复，恢复心跳");
                    } else {
                        Log.w(TAG, "网络断开，暂停心跳");
                    }
                    
                    performOptimization();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "处理网络广播异常", e);
            }
        }
    }
}
