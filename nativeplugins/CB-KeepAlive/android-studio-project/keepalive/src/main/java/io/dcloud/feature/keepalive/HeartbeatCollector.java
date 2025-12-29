package io.dcloud.feature.keepalive;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 心跳数据收集器
 * 负责收集设备信息、系统状态和保活状态
 * 
 * 功能特性：
 * 1. 收集设备基本信息（型号、厂商、系统版本）
 * 2. 收集应用信息（版本号）
 * 3. 收集系统状态（电池、网络、内存、屏幕）
 * 4. 收集保活状态
 * 
 * 验证需求：4.2
 */
public class HeartbeatCollector {
    
    private static final String TAG = "HeartbeatCollector";
    
    private Context context;
    private String deviceId;
    
    public HeartbeatCollector(Context context) {
        this.context = context;
        this.deviceId = getOrCreateDeviceId();
    }
    
    /**
     * 收集完整的心跳数据
     * @param keepAliveStatus 保活状态
     * @return HeartbeatData对象
     */
    public HeartbeatData collectHeartbeatData(String keepAliveStatus) {
        HeartbeatData data = new HeartbeatData();
        
        try {
            // 设备信息
            data.setDeviceId(deviceId);
            data.setDeviceModel(Build.MODEL);
            data.setManufacturer(Build.MANUFACTURER);
            data.setSystemVersion(Build.VERSION.RELEASE);
            
            // 应用信息
            data.setAppVersion(getAppVersion());
            
            // 保活状态
            data.setKeepAliveStatus(keepAliveStatus);
            
            // 时间戳
            data.setTimestamp(System.currentTimeMillis());
            
            // 电池信息
            data.setBatteryLevel(getBatteryLevel());
            
            // 网络信息
            data.setNetworkType(getNetworkType());
            
            // 屏幕信息
            DisplayMetrics metrics = getScreenMetrics();
            if (metrics != null) {
                data.setScreenWidth(metrics.widthPixels);
                data.setScreenHeight(metrics.heightPixels);
            }
            
            // 屏幕状态
            data.setScreenOn(isScreenOn());
            
            // 内存信息
            ActivityManager.MemoryInfo memoryInfo = getMemoryInfo();
            if (memoryInfo != null) {
                data.setFreeMemory(memoryInfo.availMem);
                data.setTotalMemory(memoryInfo.totalMem);
            }
            
            Log.d(TAG, "心跳数据收集完成: " + data.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "收集心跳数据失败", e);
        }
        
        return data;
    }
    
    /**
     * 获取或创建设备ID
     * @return 设备唯一标识
     */
    private String getOrCreateDeviceId() {
        try {
            // 尝试从SharedPreferences获取
            String savedDeviceId = context.getSharedPreferences("keep_alive_prefs", Context.MODE_PRIVATE)
                .getString("device_id", null);
            
            if (savedDeviceId != null && !savedDeviceId.isEmpty()) {
                return savedDeviceId;
            }
            
            // 尝试使用Android ID
            String androidId = Settings.Secure.getString(
                context.getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
            
            if (androidId != null && !androidId.isEmpty() && !"9774d56d682e549c".equals(androidId)) {
                // 保存到SharedPreferences
                context.getSharedPreferences("keep_alive_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("device_id", androidId)
                    .apply();
                return androidId;
            }
            
            // 生成随机ID
            String randomId = "device_" + System.currentTimeMillis() + "_" + 
                (int)(Math.random() * 100000);
            
            // 保存到SharedPreferences
            context.getSharedPreferences("keep_alive_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("device_id", randomId)
                .apply();
            
            return randomId;
            
        } catch (Exception e) {
            Log.e(TAG, "获取设备ID失败", e);
            return "unknown_device";
        }
    }
    
    /**
     * 获取应用版本号
     * @return 版本号字符串
     */
    private String getAppVersion() {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            Log.e(TAG, "获取应用版本失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取电池电量
     * @return 电量百分比 (0-100)，失败返回-1
     */
    private int getBatteryLevel() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level >= 0 && scale > 0) {
                    return (int)((level / (float)scale) * 100);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取电池电量失败", e);
        }
        
        return -1;
    }
    
    /**
     * 获取网络类型
     * @return 网络类型字符串
     */
    private String getNetworkType() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return "unknown";
            }
            
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                return "none";
            }
            
            int type = networkInfo.getType();
            switch (type) {
                case ConnectivityManager.TYPE_WIFI:
                    return "wifi";
                case ConnectivityManager.TYPE_MOBILE:
                    int subtype = networkInfo.getSubtype();
                    return getMobileNetworkType(subtype);
                case ConnectivityManager.TYPE_ETHERNET:
                    return "ethernet";
                default:
                    return "other";
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取网络类型失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取移动网络类型
     * @param subtype 网络子类型
     * @return 网络类型字符串
     */
    private String getMobileNetworkType(int subtype) {
        switch (subtype) {
            case android.telephony.TelephonyManager.NETWORK_TYPE_GPRS:
            case android.telephony.TelephonyManager.NETWORK_TYPE_EDGE:
            case android.telephony.TelephonyManager.NETWORK_TYPE_CDMA:
            case android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT:
            case android.telephony.TelephonyManager.NETWORK_TYPE_IDEN:
                return "2g";
                
            case android.telephony.TelephonyManager.NETWORK_TYPE_UMTS:
            case android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0:
            case android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A:
            case android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA:
            case android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA:
            case android.telephony.TelephonyManager.NETWORK_TYPE_HSPA:
            case android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B:
            case android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD:
            case android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3g";
                
            case android.telephony.TelephonyManager.NETWORK_TYPE_LTE:
                return "4g";
                
            case android.telephony.TelephonyManager.NETWORK_TYPE_NR:
                return "5g";
                
            default:
                return "mobile";
        }
    }
    
    /**
     * 获取屏幕尺寸信息
     * @return DisplayMetrics对象
     */
    private DisplayMetrics getScreenMetrics() {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                return metrics;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取屏幕信息失败", e);
        }
        
        return null;
    }
    
    /**
     * 检查屏幕是否开启
     * @return 是否开启
     */
    private boolean isScreenOn() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    return pm.isInteractive();
                } else {
                    return pm.isScreenOn();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查屏幕状态失败", e);
        }
        
        return false;
    }
    
    /**
     * 获取内存信息
     * @return MemoryInfo对象
     */
    private ActivityManager.MemoryInfo getMemoryInfo() {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(memoryInfo);
                return memoryInfo;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取内存信息失败", e);
        }
        
        return null;
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
}
