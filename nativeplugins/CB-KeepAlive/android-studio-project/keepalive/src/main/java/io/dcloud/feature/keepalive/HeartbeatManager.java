package io.dcloud.feature.keepalive;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.UUID;

/**
 * 心跳监控管理器
 * 
 * 功能说明：
 * - 定期发送心跳信号监控保活状态
 * - 收集设备信息和运行状态
 * - 本地存储心跳记录（最多100条）
 * - 支持心跳异常检测和标记
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class HeartbeatManager {
    
    private static final String TAG = "HeartbeatManager";
    
    // 单例实例
    private static volatile HeartbeatManager instance;
    
    // 上下文
    private Context context;
    
    // 心跳处理器
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;
    
    // 心跳配置
    private int heartbeatInterval = 30000; // 默认30秒
    private boolean isRunning = false;
    
    // 心跳统计
    private long heartbeatCount = 0;
    private long lastHeartbeatTime = 0;
    private int errorCount = 0;
    
    // 本地存储
    private static final String PREFS_NAME = "cb_heartbeat_prefs";
    private static final String KEY_HEARTBEAT_LOGS = "heartbeat_logs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final int MAX_LOG_COUNT = 100;
    
    // 心跳回调接口
    public interface HeartbeatCallback {
        void onHeartbeat(JSONObject heartbeatData);
        void onError(String error);
    }
    
    private HeartbeatCallback callback;
    
    /**
     * 获取单例实例
     */
    public static HeartbeatManager getInstance() {
        if (instance == null) {
            synchronized (HeartbeatManager.class) {
                if (instance == null) {
                    instance = new HeartbeatManager();
                }
            }
        }
        return instance;
    }
    
    private HeartbeatManager() {
        heartbeatHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 初始化心跳管理器
     */
    public void init(Context context, int interval) {
        this.context = context.getApplicationContext();
        if (interval > 0) {
            this.heartbeatInterval = interval;
        }
        Log.d(TAG, "心跳管理器初始化，间隔: " + heartbeatInterval + "ms");
    }
    
    /**
     * 设置心跳回调
     */
    public void setCallback(HeartbeatCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 启动心跳监控
     */
    public void start() {
        if (isRunning) {
            Log.d(TAG, "心跳监控已在运行");
            return;
        }
        
        Log.d(TAG, "启动心跳监控");
        isRunning = true;
        
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    sendHeartbeat();
                    heartbeatHandler.postDelayed(this, heartbeatInterval);
                }
            }
        };
        
        // 立即发送第一次心跳
        heartbeatHandler.post(heartbeatRunnable);
    }
    
    /**
     * 停止心跳监控
     */
    public void stop() {
        Log.d(TAG, "停止心跳监控");
        isRunning = false;
        
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
        }
    }
    
    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        try {
            JSONObject heartbeatData = collectHeartbeatData();
            
            // 更新统计
            heartbeatCount++;
            lastHeartbeatTime = System.currentTimeMillis();
            
            // 保存到本地
            saveHeartbeatLog(heartbeatData);
            
            // 回调通知
            if (callback != null) {
                callback.onHeartbeat(heartbeatData);
            }
            
            Log.d(TAG, "心跳发送成功，第 " + heartbeatCount + " 次");
            
        } catch (Exception e) {
            errorCount++;
            Log.e(TAG, "心跳发送失败: " + e.getMessage(), e);
            
            if (callback != null) {
                callback.onError("心跳发送失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 收集心跳数据
     */
    public JSONObject collectHeartbeatData() {
        JSONObject data = new JSONObject();
        
        try {
            // 设备唯一标识
            data.put("deviceId", getDeviceId());
            
            // 应用版本
            data.put("appVersion", getAppVersion());
            
            // 系统版本
            data.put("systemVersion", Build.VERSION.RELEASE);
            
            // 设备型号
            data.put("deviceModel", Build.MODEL);
            
            // 设备品牌
            data.put("deviceBrand", Build.BRAND);
            
            // 保活状态
            data.put("keepAliveStatus", KeepAliveService.isRunning() ? "running" : "stopped");
            
            // 时间戳
            data.put("timestamp", System.currentTimeMillis());
            
            // 电池电量
            data.put("batteryLevel", getBatteryLevel());
            
            // 网络类型
            data.put("networkType", getNetworkType());
            
            // 屏幕尺寸
            int[] screenSize = getScreenSize();
            data.put("screenWidth", screenSize[0]);
            data.put("screenHeight", screenSize[1]);
            
            // 心跳序号
            data.put("heartbeatIndex", heartbeatCount + 1);
            
            // Android版本号
            data.put("sdkVersion", Build.VERSION.SDK_INT);
            
        } catch (Exception e) {
            Log.e(TAG, "收集心跳数据失败: " + e.getMessage(), e);
        }
        
        return data;
    }
    
    /**
     * 获取设备唯一标识
     */
    private String getDeviceId() {
        if (context == null) {
            return "unknown";
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);
        
        if (deviceId == null || deviceId.isEmpty()) {
            // 生成新的设备ID
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        
        return deviceId;
    }
    
    /**
     * 获取应用版本
     */
    private String getAppVersion() {
        if (context == null) {
            return "unknown";
        }
        
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * 获取电池电量
     */
    private int getBatteryLevel() {
        if (context == null) {
            return -1;
        }
        
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level >= 0 && scale > 0) {
                    return (int) ((level / (float) scale) * 100);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取电池电量失败: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * 获取网络类型
     */
    private String getNetworkType() {
        if (context == null) {
            return "unknown";
        }
        
        try {
            ConnectivityManager cm = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                
                if (activeNetwork == null || !activeNetwork.isConnected()) {
                    return "none";
                }
                
                int type = activeNetwork.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    return "mobile";
                } else if (type == ConnectivityManager.TYPE_ETHERNET) {
                    return "ethernet";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取网络类型失败: " + e.getMessage());
        }
        
        return "unknown";
    }
    
    /**
     * 获取屏幕尺寸
     */
    private int[] getScreenSize() {
        int[] size = new int[]{0, 0};
        
        if (context == null) {
            return size;
        }
        
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                size[0] = metrics.widthPixels;
                size[1] = metrics.heightPixels;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取屏幕尺寸失败: " + e.getMessage());
        }
        
        return size;
    }
    
    /**
     * 保存心跳日志到本地
     */
    private void saveHeartbeatLog(JSONObject heartbeatData) {
        if (context == null) {
            return;
        }
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String logsJson = prefs.getString(KEY_HEARTBEAT_LOGS, "[]");
            
            JSONArray logs = JSONArray.parseArray(logsJson);
            if (logs == null) {
                logs = new JSONArray();
            }
            
            // 添加新日志
            logs.add(heartbeatData);
            
            // 限制日志数量
            while (logs.size() > MAX_LOG_COUNT) {
                logs.remove(0);
            }
            
            // 保存
            prefs.edit().putString(KEY_HEARTBEAT_LOGS, logs.toJSONString()).apply();
            
        } catch (Exception e) {
            Log.e(TAG, "保存心跳日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取本地心跳日志
     */
    public JSONArray getHeartbeatLogs() {
        if (context == null) {
            return new JSONArray();
        }
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String logsJson = prefs.getString(KEY_HEARTBEAT_LOGS, "[]");
            
            JSONArray logs = JSONArray.parseArray(logsJson);
            return logs != null ? logs : new JSONArray();
            
        } catch (Exception e) {
            Log.e(TAG, "获取心跳日志失败: " + e.getMessage());
            return new JSONArray();
        }
    }
    
    /**
     * 获取本地心跳日志（带数量限制）
     */
    public JSONArray getHeartbeatLogs(int limit) {
        JSONArray allLogs = getHeartbeatLogs();
        
        if (limit <= 0 || limit >= allLogs.size()) {
            return allLogs;
        }
        
        // 返回最近的limit条记录
        JSONArray result = new JSONArray();
        int startIndex = allLogs.size() - limit;
        for (int i = startIndex; i < allLogs.size(); i++) {
            result.add(allLogs.get(i));
        }
        
        return result;
    }
    
    /**
     * 手动触发心跳
     */
    public JSONObject triggerHeartbeat() {
        JSONObject heartbeatData = collectHeartbeatData();
        
        // 更新统计
        heartbeatCount++;
        lastHeartbeatTime = System.currentTimeMillis();
        
        // 保存到本地
        saveHeartbeatLog(heartbeatData);
        
        // 回调通知
        if (callback != null) {
            callback.onHeartbeat(heartbeatData);
        }
        
        Log.d(TAG, "手动触发心跳成功，第 " + heartbeatCount + " 次");
        
        return heartbeatData;
    }
    
    /**
     * 清除日志（别名方法）
     */
    public void clearLogs() {
        clearHeartbeatLogs();
    }
    
    /**
     * 清除心跳日志
     */
    public void clearHeartbeatLogs() {
        if (context == null) {
            return;
        }
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_HEARTBEAT_LOGS, "[]").apply();
            Log.d(TAG, "心跳日志已清除");
        } catch (Exception e) {
            Log.e(TAG, "清除心跳日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录异常事件
     */
    public void recordAnomalyEvent(String eventType, String description) {
        try {
            JSONObject anomalyData = collectHeartbeatData();
            anomalyData.put("eventType", eventType);
            anomalyData.put("description", description);
            anomalyData.put("isAnomaly", true);
            
            saveHeartbeatLog(anomalyData);
            
            Log.w(TAG, "记录异常事件: " + eventType + " - " + description);
            
        } catch (Exception e) {
            Log.e(TAG, "记录异常事件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取心跳统计信息
     */
    public JSONObject getStatistics() {
        JSONObject stats = new JSONObject();
        
        stats.put("heartbeatCount", heartbeatCount);
        stats.put("lastHeartbeatTime", lastHeartbeatTime);
        stats.put("errorCount", errorCount);
        stats.put("isRunning", isRunning);
        stats.put("heartbeatInterval", heartbeatInterval);
        
        return stats;
    }
    
    /**
     * 检查心跳是否正常
     */
    public boolean isHeartbeatHealthy() {
        if (!isRunning) {
            return false;
        }
        
        // 如果超过2个心跳周期没有心跳，认为不健康
        long now = System.currentTimeMillis();
        long threshold = heartbeatInterval * 2;
        
        return (now - lastHeartbeatTime) < threshold;
    }
    
    /**
     * 更新心跳间隔
     */
    public void updateInterval(int interval) {
        if (interval > 0) {
            this.heartbeatInterval = interval;
            Log.d(TAG, "心跳间隔更新为: " + interval + "ms");
            
            // 如果正在运行，重启心跳
            if (isRunning) {
                stop();
                start();
            }
        }
    }
    
    /**
     * 获取心跳计数
     */
    public long getHeartbeatCount() {
        return heartbeatCount;
    }
    
    /**
     * 获取最后心跳时间
     */
    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }
    
    /**
     * 获取错误计数
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
}
