package io.dcloud.feature.keepalive;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 设备适配策略选择器
 * 根据Android版本和厂商ROM选择最优的保活策略
 * 
 * 功能特性：
 * 1. Android版本适配（8.0+前台服务优先）
 * 2. 厂商ROM特定优化策略
 * 3. 不同设备性能的保活策略
 * 
 * 验证需求：3.1, 3.2
 */
public class DeviceAdaptationStrategy {
    
    private static final String TAG = "DeviceAdaptationStrategy";
    
    // Android版本常量
    private static final int ANDROID_8_0 = Build.VERSION_CODES.O;  // API 26
    private static final int ANDROID_9_0 = Build.VERSION_CODES.P;  // API 28
    private static final int ANDROID_10 = Build.VERSION_CODES.Q;   // API 29
    private static final int ANDROID_11 = Build.VERSION_CODES.R;   // API 30
    private static final int ANDROID_12 = Build.VERSION_CODES.S;   // API 31
    
    // 策略类型
    public static final String STRATEGY_FOREGROUND_SERVICE = "foreground_service";  // 前台服务策略
    public static final String STRATEGY_MANUFACTURER_OPTIMIZED = "manufacturer_optimized";  // 厂商优化策略
    public static final String STRATEGY_STANDARD = "standard";  // 标准策略
    public static final String STRATEGY_MINIMAL = "minimal";  // 最小策略
    
    private Context context;
    private DeviceAdapter deviceAdapter;
    
    public DeviceAdaptationStrategy(Context context) {
        this.context = context;
        this.deviceAdapter = new DeviceAdapter(context);
    }
    
    /**
     * 选择最优保活策略
     * 验证需求：3.1, 3.2 - 根据Android版本和厂商ROM选择策略
     * @return 策略信息
     */
    public JSONObject selectOptimalStrategy() {
        try {
            Log.d(TAG, "开始选择最优保活策略");
            
            JSONObject strategy = new JSONObject();
            
            // 获取设备信息
            int apiLevel = deviceAdapter.getApiLevel();
            String manufacturer = deviceAdapter.getManufacturer().toLowerCase();
            String androidVersion = deviceAdapter.getAndroidVersion();
            
            Log.d(TAG, String.format("设备信息 - API级别:%d, 厂商:%s, Android版本:%s", 
                apiLevel, manufacturer, androidVersion));
            
            // 需求3.1：Android 8.0及以上优先使用前台服务保活策略
            if (apiLevel >= ANDROID_8_0) {
                strategy.put("primaryStrategy", STRATEGY_FOREGROUND_SERVICE);
                strategy.put("useForegroundService", true);
                strategy.put("reason", "Android " + androidVersion + " 支持前台服务");
                Log.i(TAG, "选择前台服务策略（Android " + androidVersion + "）");
            } else {
                strategy.put("primaryStrategy", STRATEGY_STANDARD);
                strategy.put("useForegroundService", false);
                strategy.put("reason", "Android版本低于8.0，使用标准策略");
                Log.i(TAG, "选择标准策略（Android " + androidVersion + "）");
            }
            
            // 需求3.2：检测特定厂商ROM，采用对应的优化保活方案
            boolean needsManufacturerOptimization = needsManufacturerOptimization(manufacturer);
            strategy.put("needsManufacturerOptimization", needsManufacturerOptimization);
            
            if (needsManufacturerOptimization) {
                JSONObject manufacturerStrategy = getManufacturerStrategy(manufacturer, apiLevel);
                strategy.put("manufacturerStrategy", manufacturerStrategy);
                Log.i(TAG, "应用厂商优化策略: " + manufacturer);
            }
            
            // 添加设备性能相关的策略调整
            JSONObject performanceStrategy = getPerformanceBasedStrategy();
            strategy.put("performanceStrategy", performanceStrategy);
            
            // 添加版本特定的优化建议
            JSONObject versionOptimizations = getVersionSpecificOptimizations(apiLevel);
            strategy.put("versionOptimizations", versionOptimizations);
            
            // 添加设备信息
            strategy.put("deviceInfo", deviceAdapter.getDeviceInfo());
            
            Log.d(TAG, "策略选择完成: " + strategy.toString());
            return strategy;
            
        } catch (Exception e) {
            Log.e(TAG, "选择保活策略异常", e);
            return getDefaultStrategy();
        }
    }
    
    /**
     * 判断是否需要厂商优化
     * 验证需求：3.2 - 检测特定厂商ROM
     * @param manufacturer 厂商名称
     * @return 是否需要优化
     */
    private boolean needsManufacturerOptimization(String manufacturer) {
        // 中国大陆主流厂商需要特殊优化
        return manufacturer.contains("xiaomi") ||
               manufacturer.contains("redmi") ||
               manufacturer.contains("huawei") ||
               manufacturer.contains("honor") ||
               manufacturer.contains("oppo") ||
               manufacturer.contains("realme") ||
               manufacturer.contains("oneplus") ||
               manufacturer.contains("vivo") ||
               manufacturer.contains("iqoo") ||
               manufacturer.contains("meizu") ||
               manufacturer.contains("lenovo") ||
               manufacturer.contains("asus") ||
               manufacturer.contains("letv") ||
               manufacturer.contains("leeco");
    }
    
    /**
     * 获取厂商特定策略
     * 验证需求：3.2 - 厂商ROM特定优化策略
     * @param manufacturer 厂商名称
     * @param apiLevel API级别
     * @return 厂商策略
     */
    private JSONObject getManufacturerStrategy(String manufacturer, int apiLevel) {
        JSONObject strategy = new JSONObject();
        
        try {
            strategy.put("manufacturer", manufacturer);
            
            if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
                // 小米/红米策略
                strategy.put("name", "小米优化策略");
                strategy.put("requiresAutoStart", true);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", true);
                strategy.put("priority", "high");
                strategy.put("notes", "MIUI系统对后台应用限制严格，需要用户手动设置自启动和电池优化白名单");
                
            } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                // 华为/荣耀策略
                strategy.put("name", "华为优化策略");
                strategy.put("requiresAutoStart", true);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", true);
                strategy.put("priority", "high");
                strategy.put("notes", "EMUI/HarmonyOS系统需要在启动管理中设置为手动管理");
                
            } else if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
                // OPPO/Realme/一加策略
                strategy.put("name", "OPPO优化策略");
                strategy.put("requiresAutoStart", true);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", true);
                strategy.put("priority", "high");
                strategy.put("notes", "ColorOS系统需要在应用耗电管理中允许后台运行");
                
            } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
                // VIVO/iQOO策略
                strategy.put("name", "VIVO优化策略");
                strategy.put("requiresAutoStart", true);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", true);
                strategy.put("priority", "high");
                strategy.put("notes", "FuntouchOS系统需要在后台高耗电中允许应用");
                
            } else if (manufacturer.contains("samsung")) {
                // 三星策略
                strategy.put("name", "三星优化策略");
                strategy.put("requiresAutoStart", false);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", true);
                strategy.put("priority", "medium");
                strategy.put("notes", "OneUI系统需要在设备保养中添加到未监视的应用");
                
            } else if (manufacturer.contains("meizu")) {
                // 魅族策略
                strategy.put("name", "魅族优化策略");
                strategy.put("requiresAutoStart", true);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", true);
                strategy.put("recommendLockInRecents", false);
                strategy.put("priority", "medium");
                strategy.put("notes", "Flyme系统需要在待机耗电管理中设置");
                
            } else {
                // 其他厂商默认策略
                strategy.put("name", "通用优化策略");
                strategy.put("requiresAutoStart", false);
                strategy.put("requiresBatteryWhitelist", true);
                strategy.put("requiresBackgroundPermission", false);
                strategy.put("recommendLockInRecents", false);
                strategy.put("priority", "low");
                strategy.put("notes", "使用标准Android保活机制");
            }
            
            // 根据API级别调整策略
            if (apiLevel >= ANDROID_12) {
                strategy.put("apiLevelNote", "Android 12+需要明确声明前台服务类型");
            } else if (apiLevel >= ANDROID_10) {
                strategy.put("apiLevelNote", "Android 10+后台位置访问受限");
            } else if (apiLevel >= ANDROID_9_0) {
                strategy.put("apiLevelNote", "Android 9+电池优化更严格");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取厂商策略异常", e);
        }
        
        return strategy;
    }
    
    /**
     * 获取基于性能的策略
     * 验证需求：3.2 - 不同设备性能的保活策略
     * @return 性能策略
     */
    private JSONObject getPerformanceBasedStrategy() {
        JSONObject strategy = new JSONObject();
        
        try {
            // 获取设备性能指标
            long availableMemory = getAvailableMemory();
            int cpuCores = Runtime.getRuntime().availableProcessors();
            
            strategy.put("availableMemory", availableMemory);
            strategy.put("cpuCores", cpuCores);
            
            // 根据内存情况调整策略
            if (availableMemory < 512) {
                strategy.put("performanceLevel", "low");
                strategy.put("recommendedHeartbeatInterval", 120000);  // 2分钟
                strategy.put("notes", "低内存设备，建议降低心跳频率");
            } else if (availableMemory < 1024) {
                strategy.put("performanceLevel", "medium");
                strategy.put("recommendedHeartbeatInterval", 60000);  // 1分钟
                strategy.put("notes", "中等内存设备，使用标准心跳频率");
            } else {
                strategy.put("performanceLevel", "high");
                strategy.put("recommendedHeartbeatInterval", 30000);  // 30秒
                strategy.put("notes", "高内存设备，可使用较高心跳频率");
            }
            
            // 根据CPU核心数调整
            if (cpuCores <= 2) {
                strategy.put("cpuLevel", "low");
                strategy.put("recommendReduceBackgroundWork", true);
            } else if (cpuCores <= 4) {
                strategy.put("cpuLevel", "medium");
                strategy.put("recommendReduceBackgroundWork", false);
            } else {
                strategy.put("cpuLevel", "high");
                strategy.put("recommendReduceBackgroundWork", false);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取性能策略异常", e);
        }
        
        return strategy;
    }
    
    /**
     * 获取版本特定的优化建议
     * 验证需求：3.1 - Android版本适配
     * @param apiLevel API级别
     * @return 优化建议
     */
    private JSONObject getVersionSpecificOptimizations(int apiLevel) {
        JSONObject optimizations = new JSONObject();
        
        try {
            optimizations.put("apiLevel", apiLevel);
            optimizations.put("androidVersion", Build.VERSION.RELEASE);
            
            if (apiLevel >= ANDROID_12) {
                // Android 12+ 优化
                optimizations.put("version", "Android 12+");
                optimizations.put("foregroundServiceType", "dataSync");
                optimizations.put("requiresExactAlarm", true);
                optimizations.put("notes", "需要明确声明前台服务类型为dataSync，精确闹钟需要特殊权限");
                
            } else if (apiLevel >= ANDROID_11) {
                // Android 11 优化
                optimizations.put("version", "Android 11");
                optimizations.put("foregroundServiceType", "dataSync");
                optimizations.put("requiresExactAlarm", false);
                optimizations.put("notes", "前台服务需要声明类型，后台位置访问受限");
                
            } else if (apiLevel >= ANDROID_10) {
                // Android 10 优化
                optimizations.put("version", "Android 10");
                optimizations.put("foregroundServiceType", "optional");
                optimizations.put("requiresExactAlarm", false);
                optimizations.put("notes", "后台位置访问需要用户明确授权");
                
            } else if (apiLevel >= ANDROID_9_0) {
                // Android 9 优化
                optimizations.put("version", "Android 9");
                optimizations.put("foregroundServiceType", "optional");
                optimizations.put("requiresExactAlarm", false);
                optimizations.put("notes", "电池优化更严格，建议引导用户添加白名单");
                
            } else if (apiLevel >= ANDROID_8_0) {
                // Android 8.0-8.1 优化
                optimizations.put("version", "Android 8.0-8.1");
                optimizations.put("foregroundServiceType", "optional");
                optimizations.put("requiresExactAlarm", false);
                optimizations.put("notes", "首次支持前台服务，需要显示持久通知");
                
            } else {
                // Android 7.x 及以下
                optimizations.put("version", "Android 7.x及以下");
                optimizations.put("foregroundServiceType", "not_supported");
                optimizations.put("requiresExactAlarm", false);
                optimizations.put("notes", "不支持前台服务，使用传统保活方式");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取版本优化建议异常", e);
        }
        
        return optimizations;
    }
    
    /**
     * 获取可用内存（MB）
     * @return 可用内存大小
     */
    private long getAvailableMemory() {
        try {
            android.app.ActivityManager activityManager = 
                (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return -1;
            }
            
            android.app.ActivityManager.MemoryInfo memoryInfo = 
                new android.app.ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            return memoryInfo.availMem / (1024 * 1024); // 转换为MB
            
        } catch (Exception e) {
            Log.e(TAG, "获取可用内存异常", e);
            return -1;
        }
    }
    
    /**
     * 获取默认策略
     * @return 默认策略
     */
    private JSONObject getDefaultStrategy() {
        JSONObject strategy = new JSONObject();
        
        try {
            strategy.put("primaryStrategy", STRATEGY_STANDARD);
            strategy.put("useForegroundService", Build.VERSION.SDK_INT >= ANDROID_8_0);
            strategy.put("needsManufacturerOptimization", false);
            strategy.put("reason", "使用默认策略");
            
            JSONObject performanceStrategy = new JSONObject();
            performanceStrategy.put("performanceLevel", "medium");
            performanceStrategy.put("recommendedHeartbeatInterval", 60000);
            strategy.put("performanceStrategy", performanceStrategy);
            
        } catch (Exception e) {
            Log.e(TAG, "创建默认策略异常", e);
        }
        
        return strategy;
    }
    
    /**
     * 应用选定的策略
     * @param strategy 策略信息
     * @return 应用后的配置
     */
    public KeepAliveConfig applyStrategy(JSONObject strategy) {
        try {
            Log.d(TAG, "应用保活策略");
            
            KeepAliveConfig config = new KeepAliveConfig();
            
            // 应用性能策略中的心跳间隔建议
            if (strategy.containsKey("performanceStrategy")) {
                JSONObject performanceStrategy = strategy.getJSONObject("performanceStrategy");
                if (performanceStrategy.containsKey("recommendedHeartbeatInterval")) {
                    int interval = performanceStrategy.getIntValue("recommendedHeartbeatInterval");
                    config.setHeartbeatInterval(interval);
                    Log.d(TAG, "应用心跳间隔: " + interval + "ms");
                }
            }
            
            // 应用厂商优化策略
            if (strategy.getBooleanValue("needsManufacturerOptimization")) {
                JSONObject manufacturerStrategy = strategy.getJSONObject("manufacturerStrategy");
                if (manufacturerStrategy != null) {
                    // 根据厂商策略调整适配配置
                    config.setEnableManufacturerOptimization(true);
                    config.setEnableBatteryWhitelist(
                        manufacturerStrategy.getBooleanValue("requiresBatteryWhitelist"));
                    config.setEnableAutoStart(
                        manufacturerStrategy.getBooleanValue("requiresAutoStart"));
                    
                    Log.d(TAG, "应用厂商优化: " + manufacturerStrategy.getString("name"));
                }
            }
            
            Log.d(TAG, "策略应用完成");
            return config;
            
        } catch (Exception e) {
            Log.e(TAG, "应用策略异常", e);
            return new KeepAliveConfig();
        }
    }
    
    /**
     * 获取策略摘要
     * @return 策略摘要信息
     */
    public String getStrategySummary() {
        try {
            JSONObject strategy = selectOptimalStrategy();
            
            StringBuilder summary = new StringBuilder();
            summary.append("保活策略摘要:\n");
            summary.append("主策略: ").append(strategy.getString("primaryStrategy")).append("\n");
            summary.append("原因: ").append(strategy.getString("reason")).append("\n");
            
            if (strategy.getBooleanValue("needsManufacturerOptimization")) {
                JSONObject manufacturerStrategy = strategy.getJSONObject("manufacturerStrategy");
                summary.append("厂商优化: ").append(manufacturerStrategy.getString("name")).append("\n");
            }
            
            JSONObject performanceStrategy = strategy.getJSONObject("performanceStrategy");
            summary.append("性能级别: ").append(performanceStrategy.getString("performanceLevel")).append("\n");
            summary.append("建议心跳间隔: ").append(performanceStrategy.getIntValue("recommendedHeartbeatInterval")).append("ms\n");
            
            return summary.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "获取策略摘要异常", e);
            return "无法获取策略摘要";
        }
    }
}
