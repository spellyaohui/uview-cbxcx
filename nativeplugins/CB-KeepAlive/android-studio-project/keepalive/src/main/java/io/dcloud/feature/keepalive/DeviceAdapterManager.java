package io.dcloud.feature.keepalive;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 设备适配管理器
 * 
 * 功能说明：
 * - 检测设备品牌和ROM版本
 * - 提供厂商特定的白名单引导
 * - 实现自动跳转到系统设置页面
 * 
 * 支持厂商：
 * - 小米 (MIUI)
 * - 华为 (EMUI/HarmonyOS)
 * - OPPO (ColorOS)
 * - VIVO (OriginOS/FuntouchOS)
 * - 三星 (One UI)
 * - 魅族 (Flyme)
 * - 一加 (OxygenOS/ColorOS)
 * - 联想/ZUK
 * - 乐视
 * - 酷派
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class DeviceAdapterManager {
    
    private static final String TAG = "DeviceAdapterManager";
    
    // 单例实例
    private static volatile DeviceAdapterManager instance;
    
    // 上下文
    private Context context;
    
    // 设备信息
    private String manufacturer;
    private String brand;
    private String model;
    private String romVersion;
    
    private DeviceAdapterManager() {
    }
    
    /**
     * 获取单例实例
     */
    public static DeviceAdapterManager getInstance() {
        if (instance == null) {
            synchronized (DeviceAdapterManager.class) {
                if (instance == null) {
                    instance = new DeviceAdapterManager();
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
        
        // 获取设备信息
        this.manufacturer = Build.MANUFACTURER;
        this.brand = Build.BRAND;
        this.model = Build.MODEL;
        this.romVersion = Build.DISPLAY;
        
        Log.d(TAG, "设备适配管理器初始化完成");
        Log.d(TAG, "厂商: " + manufacturer + ", 品牌: " + brand + ", 型号: " + model);
    }
    
    /**
     * 获取设备信息JSON
     */
    public JSONObject getDeviceInfoJson() {
        JSONObject info = new JSONObject();
        info.put("manufacturer", manufacturer);
        info.put("brand", brand);
        info.put("model", model);
        info.put("romVersion", romVersion);
        info.put("androidVersion", Build.VERSION.RELEASE);
        info.put("sdkVersion", Build.VERSION.SDK_INT);
        info.put("deviceType", getDeviceType());
        return info;
    }
    
    /**
     * 获取设备类型
     */
    public String getDeviceType() {
        String brandLower = brand.toLowerCase();
        
        if (brandLower.contains("xiaomi") || brandLower.contains("redmi") || brandLower.contains("poco")) {
            return "xiaomi";
        } else if (brandLower.contains("huawei") || brandLower.contains("honor")) {
            return "huawei";
        } else if (brandLower.contains("oppo") || brandLower.contains("realme")) {
            return "oppo";
        } else if (brandLower.contains("vivo") || brandLower.contains("iqoo")) {
            return "vivo";
        } else if (brandLower.contains("samsung")) {
            return "samsung";
        } else if (brandLower.contains("meizu")) {
            return "meizu";
        } else if (brandLower.contains("oneplus")) {
            return "oneplus";
        } else if (brandLower.contains("lenovo") || brandLower.contains("zuk")) {
            return "lenovo";
        } else if (brandLower.contains("letv") || brandLower.contains("leeco")) {
            return "letv";
        } else if (brandLower.contains("coolpad")) {
            return "coolpad";
        } else {
            return "other";
        }
    }
    
    /**
     * 获取白名单引导信息
     */
    public JSONObject getWhitelistGuide() {
        JSONObject guide = new JSONObject();
        String deviceType = getDeviceType();
        
        guide.put("deviceType", deviceType);
        guide.put("manufacturer", manufacturer);
        guide.put("brand", brand);
        
        switch (deviceType) {
            case "xiaomi":
                guide.put("title", "小米手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用设置」→「应用管理」",
                    "找到「崔博小程序」→「省电策略」→选择「无限制」",
                    "返回应用详情→「自启动」→开启",
                    "「设置」→「电池与性能」→「应用智能省电」→找到应用→选择「无限制」"
                });
                guide.put("autoStartPath", "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
                
            case "huawei":
                guide.put("title", "华为手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用」→「应用启动管理」",
                    "找到「崔博小程序」→关闭「自动管理」",
                    "手动开启「自启动」「关联启动」「后台活动」",
                    "「设置」→「电池」→「更多电池设置」→关闭「休眠时始终保持网络连接」"
                });
                guide.put("autoStartPath", "com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                break;
                
            case "oppo":
                guide.put("title", "OPPO手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用管理」→「应用列表」",
                    "找到「崔博小程序」→「耗电保护」→选择「允许后台运行」",
                    "返回应用详情→「自启动」→开启",
                    "「设置」→「电池」→「自定义耗电保护」→找到应用→选择「允许后台运行」"
                });
                guide.put("autoStartPath", "com.coloros.safecenter/com.coloros.safecenter.startupapp.StartupAppListActivity");
                break;
                
            case "vivo":
                guide.put("title", "VIVO手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用与权限」→「应用管理」",
                    "找到「崔博小程序」→「权限」→「后台弹出界面」→开启",
                    "返回→「电池」→「后台耗电管理」→找到应用→选择「允许后台高耗电」",
                    "「设置」→「电池」→「后台耗电管理」→找到应用→开启"
                });
                guide.put("autoStartPath", "com.iqoo.secure/com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
                
            case "samsung":
                guide.put("title", "三星手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用程序」→「崔博小程序」",
                    "点击「电池」→选择「不受限制」",
                    "「设置」→「设备维护」→「电池」→「应用程序电源管理」",
                    "找到应用→关闭「将应用置于休眠状态」"
                });
                guide.put("autoStartPath", "");
                break;
                
            case "meizu":
                guide.put("title", "魅族手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用管理」→「崔博小程序」",
                    "点击「权限管理」→「后台管理」→选择「允许后台运行」",
                    "「手机管家」→「权限管理」→「后台管理」→找到应用→允许"
                });
                guide.put("autoStartPath", "com.meizu.safe/com.meizu.safe.permission.SmartBGActivity");
                break;
                
            case "oneplus":
                guide.put("title", "一加手机设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「电池」→「电池优化」",
                    "点击右上角菜单→「所有应用」",
                    "找到「崔博小程序」→选择「不优化」",
                    "「设置」→「应用」→「应用管理」→找到应用→「电池」→「不优化」"
                });
                guide.put("autoStartPath", "");
                break;
                
            default:
                guide.put("title", "通用设置指南");
                guide.put("steps", new String[]{
                    "打开「设置」→「应用」或「应用管理」",
                    "找到「崔博小程序」",
                    "查找「电池优化」或「省电策略」相关选项",
                    "选择「不优化」或「允许后台运行」",
                    "如有「自启动」选项，请开启"
                });
                guide.put("autoStartPath", "");
                break;
        }
        
        return guide;
    }
    
    /**
     * 打开自启动设置
     */
    public boolean openAutoStartSettings(Context activityContext) {
        String deviceType = getDeviceType();
        
        try {
            Intent intent = null;
            
            switch (deviceType) {
                case "xiaomi":
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    break;
                    
                case "huawei":
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
                    break;
                    
                case "oppo":
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.coloros.safecenter",
                        "com.coloros.safecenter.startupapp.StartupAppListActivity"));
                    if (!isIntentAvailable(intent)) {
                        intent.setComponent(new ComponentName("com.oppo.safe",
                            "com.oppo.safe.permission.startup.StartupAppListActivity"));
                    }
                    break;
                    
                case "vivo":
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                    if (!isIntentAvailable(intent)) {
                        intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                    }
                    break;
                    
                case "meizu":
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.meizu.safe",
                        "com.meizu.safe.permission.SmartBGActivity"));
                    break;
                    
                case "samsung":
                    // 三星没有统一的自启动管理页面
                    return PermissionManager.getInstance().openAppSettings(activityContext);
                    
                case "oneplus":
                    // 一加使用OPPO的设置
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.coloros.safecenter",
                        "com.coloros.safecenter.startupapp.StartupAppListActivity"));
                    break;
                    
                default:
                    // 其他品牌打开应用详情页
                    return PermissionManager.getInstance().openAppSettings(activityContext);
            }
            
            if (intent != null && isIntentAvailable(intent)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityContext.startActivity(intent);
                Log.d(TAG, "已打开自启动设置: " + deviceType);
                return true;
            } else {
                // 如果特定页面不可用，打开应用详情页
                Log.d(TAG, "特定自启动页面不可用，打开应用详情页");
                return PermissionManager.getInstance().openAppSettings(activityContext);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "打开自启动设置失败: " + e.getMessage(), e);
            // 失败时打开应用详情页
            return PermissionManager.getInstance().openAppSettings(activityContext);
        }
    }
    
    /**
     * 检查Intent是否可用
     */
    private boolean isIntentAvailable(Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }
    
    /**
     * 获取厂商名称
     */
    public String getManufacturer() {
        return manufacturer;
    }
    
    /**
     * 获取品牌名称
     */
    public String getBrand() {
        return brand;
    }
    
    /**
     * 获取设备型号
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 获取ROM版本
     */
    public String getRomVersion() {
        return romVersion;
    }
}
