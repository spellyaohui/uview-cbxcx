package io.dcloud.feature.keepalive;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 设备适配器
 * 负责检测设备信息和提供厂商特定的适配方案
 * 
 * 功能特性：
 * 1. 检测设备品牌和ROM版本
 * 2. 提供厂商特定的白名单引导
 * 3. 实现自动跳转到系统设置页面
 * 4. 支持主流厂商ROM适配（小米、华为、OPPO、VIVO、三星等）
 * 
 * 验证需求：2.1, 2.2, 2.3, 2.4, 2.5
 */
public class DeviceAdapter {
    
    private static final String TAG = "DeviceAdapter";
    
    private Context context;
    
    public DeviceAdapter(Context context) {
        this.context = context;
    }
    
    /**
     * 获取设备信息
     * 验证需求：2.1 - 检测设备品牌并显示相应的白名单设置引导
     * @return JSONObject包含设备信息
     */
    public JSONObject getDeviceInfo() {
        JSONObject deviceInfo = new JSONObject();
        
        try {
            String manufacturer = getManufacturer();
            String model = getModel();
            String androidVersion = getAndroidVersion();
            int apiLevel = getApiLevel();
            String romVersion = getROMVersion();
            String brand = getBrand();
            
            deviceInfo.put("manufacturer", manufacturer);
            deviceInfo.put("model", model);
            deviceInfo.put("androidVersion", androidVersion);
            deviceInfo.put("apiLevel", apiLevel);
            deviceInfo.put("romVersion", romVersion);
            deviceInfo.put("brand", brand);
            deviceInfo.put("isMainlandChina", isMainlandChinaDevice());
            deviceInfo.put("needsSpecialAdaptation", needsSpecialAdaptation());
            
            Log.d(TAG, "设备信息: " + deviceInfo.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "获取设备信息异常", e);
        }
        
        return deviceInfo;
    }
    
    /**
     * 获取设备制造商
     * @return 制造商名称
     */
    public String getManufacturer() {
        try {
            return Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "获取制造商信息异常", e);
            return "Unknown";
        }
    }
    
    /**
     * 获取设备型号
     * @return 设备型号
     */
    public String getModel() {
        try {
            return Build.MODEL != null ? Build.MODEL : "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "获取设备型号异常", e);
            return "Unknown";
        }
    }
    
    /**
     * 获取Android版本
     * @return Android版本号
     */
    public String getAndroidVersion() {
        try {
            return Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "获取Android版本异常", e);
            return "Unknown";
        }
    }
    
    /**
     * 获取API级别
     * @return API级别
     */
    public int getApiLevel() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
            Log.e(TAG, "获取API级别异常", e);
            return 0;
        }
    }
    
    /**
     * 获取ROM版本
     * @return ROM版本信息
     */
    public String getROMVersion() {
        try {
            String manufacturer = getManufacturer().toLowerCase();
            
            if (manufacturer.contains("xiaomi")) {
                return getMIUIVersion();
            } else if (manufacturer.contains("huawei")) {
                return getEMUIVersion();
            } else if (manufacturer.contains("oppo")) {
                return getColorOSVersion();
            } else if (manufacturer.contains("vivo")) {
                return getFuntouchOSVersion();
            } else if (manufacturer.contains("samsung")) {
                return getOneUIVersion();
            } else if (manufacturer.contains("meizu")) {
                return getFlymeVersion();
            }
            
            return "Android " + getAndroidVersion();
            
        } catch (Exception e) {
            Log.e(TAG, "获取ROM版本异常", e);
            return "Unknown";
        }
    }
    
    /**
     * 获取品牌信息
     * @return 品牌名称
     */
    public String getBrand() {
        try {
            return Build.BRAND != null ? Build.BRAND : "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "获取品牌信息异常", e);
            return "Unknown";
        }
    }
    
    /**
     * 获取适配策略
     * 验证需求：2.3 - 根据不同厂商提供具体的设置路径
     * @return 适配策略信息
     */
    public JSONObject getAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        
        try {
            String manufacturer = getManufacturer().toLowerCase();
            
            Log.d(TAG, "获取适配策略，设备厂商: " + manufacturer);
            
            if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
                strategy = getMiuiAdaptationStrategy();
            } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                strategy = getHuaweiAdaptationStrategy();
            } else if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
                strategy = getOppoAdaptationStrategy();
            } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
                strategy = getVivoAdaptationStrategy();
            } else if (manufacturer.contains("samsung")) {
                strategy = getSamsungAdaptationStrategy();
            } else if (manufacturer.contains("meizu")) {
                strategy = getMeizuAdaptationStrategy();
            } else if (manufacturer.contains("lenovo") || manufacturer.contains("zuk")) {
                strategy = getLenovoAdaptationStrategy();
            } else if (manufacturer.contains("asus")) {
                strategy = getAsusAdaptationStrategy();
            } else if (manufacturer.contains("letv") || manufacturer.contains("leeco")) {
                strategy = getLetvAdaptationStrategy();
            } else {
                strategy = getDefaultAdaptationStrategy();
            }
            
            Log.d(TAG, "适配策略: " + strategy.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "获取适配策略异常", e);
            strategy = getDefaultAdaptationStrategy();
        }
        
        return strategy;
    }
    
    /**
     * 获取白名单引导信息
     * 验证需求：2.1, 2.2, 2.3 - 显示相应的白名单设置引导和具体设置路径
     * @return 白名单引导信息
     */
    public JSONObject getWhitelistGuide() {
        JSONObject guide = new JSONObject();
        
        try {
            JSONObject strategy = getAdaptationStrategy();
            String manufacturer = strategy.getString("manufacturer");
            
            guide.put("manufacturer", manufacturer);
            guide.put("needAutoStart", strategy.getBooleanValue("needAutoStartPermission"));
            guide.put("needBatteryWhitelist", strategy.getBooleanValue("needBatteryWhitelist"));
            
            // 自启动引导
            if (strategy.containsKey("autoStartSettingPath")) {
                guide.put("autoStartGuide", "请在【" + strategy.getString("autoStartSettingPath") + "】中允许应用自启动");
            }
            
            // 电池优化引导
            if (strategy.containsKey("batterySettingPath")) {
                guide.put("batteryGuide", "请在【" + strategy.getString("batterySettingPath") + "】中将应用添加到白名单");
            }
            
            // 后台运行引导
            if (strategy.containsKey("backgroundSettingPath")) {
                guide.put("backgroundGuide", "请在【" + strategy.getString("backgroundSettingPath") + "】中允许应用后台运行");
            }
            
            // 锁定后台引导
            if (strategy.containsKey("lockBackgroundGuide")) {
                guide.put("lockBackgroundGuide", strategy.getString("lockBackgroundGuide"));
            }
            
            Log.d(TAG, "白名单引导: " + guide.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "获取白名单引导异常", e);
        }
        
        return guide;
    }
    
    /**
     * 打开厂商特定的设置页面
     * 验证需求：2.2, 2.4 - 跳转到对应的系统设置页面，记录状态不再重复提示
     * @param settingType 设置类型（autoStart, battery, background）
     * @return 是否成功打开
     */
    public boolean openManufacturerSettings(String settingType) {
        try {
            Log.d(TAG, "打开厂商设置页面，类型: " + settingType);
            
            String manufacturer = getManufacturer().toLowerCase();
            Intent intent = null;
            
            switch (settingType) {
                case "autoStart":
                    intent = getAutoStartIntent(manufacturer);
                    break;
                case "battery":
                    intent = getBatterySettingsIntent(manufacturer);
                    break;
                case "background":
                    intent = getBackgroundSettingsIntent(manufacturer);
                    break;
                default:
                    Log.w(TAG, "未知的设置类型: " + settingType);
                    return false;
            }
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.d(TAG, "成功打开" + manufacturer + "的" + settingType + "设置页面");
                return true;
            } else {
                Log.w(TAG, "未找到" + manufacturer + "的" + settingType + "设置页面");
                // 降级方案：打开应用详情页面
                openApplicationDetailsSettings();
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "打开厂商设置页面异常", e);
            // 降级方案：打开应用详情页面
            openApplicationDetailsSettings();
            return false;
        }
    }
    
    /**
     * 打开应用详情设置页面
     */
    private void openApplicationDetailsSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "已打开应用详情设置页面");
        } catch (Exception e) {
            Log.e(TAG, "打开应用详情设置页面异常", e);
        }
    }
    
    /**
     * 应用厂商优化
     * 验证需求：2.5 - 支持手动触发，用户可在设置页面重新查看
     */
    public void applyManufacturerOptimizations() {
        try {
            Log.d(TAG, "应用厂商优化");
            
            JSONObject strategy = getAdaptationStrategy();
            
            // 如果需要自启动权限，提示用户设置
            if (strategy.getBooleanValue("needAutoStartPermission")) {
                Log.i(TAG, "建议设置自启动权限");
            }
            
            // 如果需要电池白名单，提示用户设置
            if (strategy.getBooleanValue("needBatteryWhitelist")) {
                Log.i(TAG, "建议添加到电池优化白名单");
            }
            
            // 如果需要后台运行权限，提示用户设置
            if (strategy.getBooleanValue("needBackgroundPermission")) {
                Log.i(TAG, "建议允许后台运行");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "应用厂商优化异常", e);
        }
    }
    
    /**
     * 判断是否需要特殊适配
     * @return 是否需要特殊适配
     */
    private boolean needsSpecialAdaptation() {
        String manufacturer = getManufacturer().toLowerCase();
        return manufacturer.contains("xiaomi") ||
               manufacturer.contains("huawei") ||
               manufacturer.contains("oppo") ||
               manufacturer.contains("vivo") ||
               manufacturer.contains("meizu") ||
               manufacturer.contains("samsung");
    }
    
    /**
     * 判断是否为中国大陆设备
     * @return 是否为中国大陆设备
     */
    private boolean isMainlandChinaDevice() {
        String manufacturer = getManufacturer().toLowerCase();
        return manufacturer.contains("xiaomi") ||
               manufacturer.contains("huawei") ||
               manufacturer.contains("oppo") ||
               manufacturer.contains("vivo") ||
               manufacturer.contains("meizu") ||
               manufacturer.contains("oneplus") ||
               manufacturer.contains("realme") ||
               manufacturer.contains("iqoo");
    }
    
    /**
     * 获取MIUI版本
     * @return MIUI版本
     */
    private String getMIUIVersion() {
        try {
            String version = getSystemProperty("ro.miui.ui.version.name");
            if (version != null && !version.isEmpty()) {
                return "MIUI " + version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取MIUI版本异常", e);
        }
        return "MIUI";
    }
    
    /**
     * 获取EMUI版本
     * @return EMUI版本
     */
    private String getEMUIVersion() {
        try {
            String version = getSystemProperty("ro.build.version.emui");
            if (version != null && !version.isEmpty()) {
                return "EMUI " + version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取EMUI版本异常", e);
        }
        return "EMUI";
    }
    
    /**
     * 获取ColorOS版本
     * @return ColorOS版本
     */
    private String getColorOSVersion() {
        try {
            String version = getSystemProperty("ro.build.version.opporom");
            if (version != null && !version.isEmpty()) {
                return "ColorOS " + version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取ColorOS版本异常", e);
        }
        return "ColorOS";
    }
    
    /**
     * 获取FuntouchOS版本
     * @return FuntouchOS版本
     */
    private String getFuntouchOSVersion() {
        try {
            String version = getSystemProperty("ro.vivo.os.version");
            if (version != null && !version.isEmpty()) {
                return "FuntouchOS " + version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取FuntouchOS版本异常", e);
        }
        return "FuntouchOS";
    }
    
    /**
     * 获取OneUI版本
     * @return OneUI版本
     */
    private String getOneUIVersion() {
        try {
            String version = getSystemProperty("ro.build.PDA");
            if (version != null && !version.isEmpty()) {
                return "OneUI " + version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取OneUI版本异常", e);
        }
        return "OneUI";
    }
    
    /**
     * 获取Flyme版本
     * @return Flyme版本
     */
    private String getFlymeVersion() {
        try {
            String version = getSystemProperty("ro.build.display.id");
            if (version != null && version.toLowerCase().contains("flyme")) {
                return version;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取Flyme版本异常", e);
        }
        return "Flyme";
    }
    
    /**
     * 获取系统属性
     * @param key 属性键
     * @return 属性值
     */
    private String getSystemProperty(String key) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            return (String) systemProperties.getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            Log.e(TAG, "获取系统属性异常: " + key, e);
            return null;
        }
    }
    
    /**
     * 获取小米适配策略
     * @return 适配策略
     */
    private JSONObject getMiuiAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "xiaomi");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("autoStartSettingPath", "设置 > 应用设置 > 授权管理 > 自启动管理");
        strategy.put("batterySettingPath", "设置 > 应用设置 > 省电策略 > 无限制");
        strategy.put("backgroundSettingPath", "设置 > 应用设置 > 应用管理 > 省电策略");
        strategy.put("lockBackgroundGuide", "在最近任务中下拉应用卡片，点击锁定图标");
        return strategy;
    }
    
    /**
     * 获取华为适配策略
     * @return 适配策略
     */
    private JSONObject getHuaweiAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "huawei");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("autoStartSettingPath", "设置 > 应用 > 应用启动管理");
        strategy.put("batterySettingPath", "设置 > 电池 > 启动管理 > 手动管理");
        strategy.put("backgroundSettingPath", "设置 > 应用 > 应用启动管理 > 手动管理");
        strategy.put("lockBackgroundGuide", "在最近任务中下拉应用卡片，点击锁定图标");
        return strategy;
    }
    
    /**
     * 获取OPPO适配策略
     * @return 适配策略
     */
    private JSONObject getOppoAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "oppo");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("autoStartSettingPath", "设置 > 电池 > 应用耗电管理 > 睡眠待机优化");
        strategy.put("batterySettingPath", "设置 > 电池 > 应用耗电管理 > 允许后台运行");
        strategy.put("backgroundSettingPath", "设置 > 电池 > 应用耗电管理");
        strategy.put("lockBackgroundGuide", "在最近任务中下拉应用卡片，点击锁定图标");
        return strategy;
    }
    
    /**
     * 获取VIVO适配策略
     * @return 适配策略
     */
    private JSONObject getVivoAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "vivo");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("autoStartSettingPath", "设置 > 电池 > 后台应用管理 > 允许自启动");
        strategy.put("batterySettingPath", "设置 > 电池 > 后台高耗电 > 允许后台高耗电");
        strategy.put("backgroundSettingPath", "设置 > 电池 > 后台应用管理");
        strategy.put("lockBackgroundGuide", "在最近任务中下拉应用卡片，点击锁定图标");
        return strategy;
    }
    
    /**
     * 获取三星适配策略
     * @return 适配策略
     */
    private JSONObject getSamsungAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "samsung");
        strategy.put("needAutoStartPermission", false);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("batterySettingPath", "设置 > 设备保养 > 电池 > 应用电源管理 > 未监视的应用");
        strategy.put("backgroundSettingPath", "设置 > 设备保养 > 电池 > 后台使用限制");
        strategy.put("lockBackgroundGuide", "在最近任务中点击应用图标，选择锁定此应用");
        return strategy;
    }
    
    /**
     * 获取魅族适配策略
     * @return 适配策略
     */
    private JSONObject getMeizuAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "meizu");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", true);
        strategy.put("autoStartSettingPath", "设置 > 应用管理 > 应用列表 > 自启动管理");
        strategy.put("batterySettingPath", "设置 > 电量管理 > 待机耗电管理");
        strategy.put("backgroundSettingPath", "设置 > 应用管理 > 应用列表 > 后台管理");
        return strategy;
    }
    
    /**
     * 获取联想适配策略
     * @return 适配策略
     */
    private JSONObject getLenovoAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "lenovo");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", false);
        strategy.put("autoStartSettingPath", "设置 > 应用 > 自启动管理");
        strategy.put("batterySettingPath", "设置 > 电池 > 省电模式");
        return strategy;
    }
    
    /**
     * 获取华硕适配策略
     * @return 适配策略
     */
    private JSONObject getAsusAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "asus");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", false);
        strategy.put("autoStartSettingPath", "设置 > 电源管理 > 自启动管理器");
        strategy.put("batterySettingPath", "设置 > 电源管理 > 省电模式");
        return strategy;
    }
    
    /**
     * 获取乐视适配策略
     * @return 适配策略
     */
    private JSONObject getLetvAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "letv");
        strategy.put("needAutoStartPermission", true);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", false);
        strategy.put("autoStartSettingPath", "设置 > 应用管理 > 自启动管理");
        strategy.put("batterySettingPath", "设置 > 省电管理");
        return strategy;
    }
    
    /**
     * 获取默认适配策略
     * @return 默认适配策略
     */
    private JSONObject getDefaultAdaptationStrategy() {
        JSONObject strategy = new JSONObject();
        strategy.put("manufacturer", "default");
        strategy.put("needAutoStartPermission", false);
        strategy.put("needBatteryWhitelist", true);
        strategy.put("needBackgroundPermission", false);
        strategy.put("batterySettingPath", "设置 > 电池 > 电池优化");
        return strategy;
    }
    
    // ========== 获取各厂商设置Intent的方法 ==========
    
    /**
     * 获取自启动设置Intent
     */
    private Intent getAutoStartIntent(String manufacturer) {
        try {
            if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
                return getXiaomiAutoStartIntent();
            } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                return getHuaweiAutoStartIntent();
            } else if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
                return getOppoAutoStartIntent();
            } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
                return getVivoAutoStartIntent();
            } else if (manufacturer.contains("meizu")) {
                return getMeizuAutoStartIntent();
            } else if (manufacturer.contains("samsung")) {
                return getSamsungAutoStartIntent();
            } else if (manufacturer.contains("lenovo") || manufacturer.contains("zuk")) {
                return getLenovoAutoStartIntent();
            } else if (manufacturer.contains("asus")) {
                return getAsusAutoStartIntent();
            } else if (manufacturer.contains("letv") || manufacturer.contains("leeco")) {
                return getLetvAutoStartIntent();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取自启动Intent异常", e);
        }
        return null;
    }
    
    /**
     * 获取电池设置Intent
     */
    private Intent getBatterySettingsIntent(String manufacturer) {
        try {
            if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
                return getXiaomiBatteryIntent();
            } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                return getHuaweiBatteryIntent();
            } else if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
                return getOppoBatteryIntent();
            } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
                return getVivoBatteryIntent();
            } else if (manufacturer.contains("samsung")) {
                return getSamsungBatteryIntent();
            } else if (manufacturer.contains("meizu")) {
                return getMeizuBatteryIntent();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取电池设置Intent异常", e);
        }
        
        // 默认跳转到系统电池优化设置
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        return intent;
    }
    
    /**
     * 获取后台运行设置Intent
     */
    private Intent getBackgroundSettingsIntent(String manufacturer) {
        try {
            if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
                return getXiaomiBackgroundIntent();
            } else if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
                return getHuaweiBackgroundIntent();
            } else if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
                return getOppoBackgroundIntent();
            } else if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
                return getVivoBackgroundIntent();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取后台运行设置Intent异常", e);
        }
        return null;
    }
    
    // ========== 小米相关Intent ==========
    
    private Intent getXiaomiAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        return intent;
    }
    
    private Intent getXiaomiBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.powerkeeper",
            "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
        intent.putExtra("package_name", context.getPackageName());
        intent.putExtra("package_label", getAppName());
        return intent;
    }
    
    private Intent getXiaomiBackgroundIntent() {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        return intent;
    }
    
    // ========== 华为相关Intent ==========
    
    private Intent getHuaweiAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
        return intent;
    }
    
    private Intent getHuaweiBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        return intent;
    }
    
    private Intent getHuaweiBackgroundIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"));
        return intent;
    }
    
    // ========== OPPO相关Intent ==========
    
    private Intent getOppoAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
        return intent;
    }
    
    private Intent getOppoBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.coloros.oppoguardelf",
            "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"));
        return intent;
    }
    
    private Intent getOppoBackgroundIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
        return intent;
    }
    
    // ========== VIVO相关Intent ==========
    
    private Intent getVivoAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
        return intent;
    }
    
    private Intent getVivoBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.vivo.abe",
            "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
        return intent;
    }
    
    private Intent getVivoBackgroundIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
        return intent;
    }
    
    // ========== 三星相关Intent ==========
    
    private Intent getSamsungAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.samsung.android.lool",
            "com.samsung.android.sm.ui.battery.BatteryActivity"));
        return intent;
    }
    
    private Intent getSamsungBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.samsung.android.lool",
            "com.samsung.android.sm.ui.battery.BatteryActivity"));
        return intent;
    }
    
    // ========== 魅族相关Intent ==========
    
    private Intent getMeizuAutoStartIntent() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", context.getPackageName());
        return intent;
    }
    
    private Intent getMeizuBatteryIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.meizu.safe",
            "com.meizu.safe.powerui.PowerAppPermissionActivity"));
        return intent;
    }
    
    // ========== 联想相关Intent ==========
    
    private Intent getLenovoAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.lenovo.security",
            "com.lenovo.security.purebackground.PureBackgroundActivity"));
        return intent;
    }
    
    // ========== 华硕相关Intent ==========
    
    private Intent getAsusAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.asus.mobilemanager",
            "com.asus.mobilemanager.autostart.AutoStartActivity"));
        return intent;
    }
    
    // ========== 乐视相关Intent ==========
    
    private Intent getLetvAutoStartIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.letv.android.letvsafe",
            "com.letv.android.letvsafe.AutobootManageActivity"));
        return intent;
    }
    
    /**
     * 获取应用名称
     */
    private String getAppName() {
        try {
            return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        } catch (Exception e) {
            Log.e(TAG, "获取应用名称异常", e);
            return context.getPackageName();
        }
    }
}