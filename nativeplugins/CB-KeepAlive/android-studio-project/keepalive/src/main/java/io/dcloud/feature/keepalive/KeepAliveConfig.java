package io.dcloud.feature.keepalive;

import com.alibaba.fastjson.JSONObject;

/**
 * 保活配置类
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class KeepAliveConfig {
    
    // 是否启用保活
    private boolean enabled = true;
    
    // 心跳间隔（毫秒）
    private int heartbeatInterval = 30000;
    
    // 最大重试次数
    private int maxRetryCount = 3;
    
    // 通知配置
    private JSONObject notificationConfig;
    
    // 适配配置
    private JSONObject adaptationConfig;
    
    public KeepAliveConfig() {
        // 默认通知配置
        notificationConfig = new JSONObject();
        notificationConfig.put("title", "崔博小程序正在后台运行");
        notificationConfig.put("content", "确保及时接收健康报告通知");
        notificationConfig.put("icon", "ic_notification");
        notificationConfig.put("showProgress", false);
        
        // 默认适配配置
        adaptationConfig = new JSONObject();
        adaptationConfig.put("enableManufacturerOptimization", true);
        adaptationConfig.put("enableBatteryWhitelist", true);
        adaptationConfig.put("enableAutoStart", true);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public void setHeartbeatInterval(int heartbeatInterval) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }
    
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
    
    public void setMaxRetryCount(int maxRetryCount) {
        if (maxRetryCount > 0) {
            this.maxRetryCount = maxRetryCount;
        }
    }
    
    public JSONObject getNotificationConfig() {
        return notificationConfig;
    }
    
    public void setNotificationConfig(JSONObject notificationConfig) {
        if (notificationConfig != null) {
            // 合并配置，保留默认值
            if (notificationConfig.containsKey("title")) {
                this.notificationConfig.put("title", notificationConfig.getString("title"));
            }
            if (notificationConfig.containsKey("content")) {
                this.notificationConfig.put("content", notificationConfig.getString("content"));
            }
            if (notificationConfig.containsKey("icon")) {
                this.notificationConfig.put("icon", notificationConfig.getString("icon"));
            }
            if (notificationConfig.containsKey("showProgress")) {
                this.notificationConfig.put("showProgress", notificationConfig.getBooleanValue("showProgress"));
            }
        }
    }
    
    public JSONObject getAdaptationConfig() {
        return adaptationConfig;
    }
    
    public void setAdaptationConfig(JSONObject adaptationConfig) {
        if (adaptationConfig != null) {
            this.adaptationConfig = adaptationConfig;
        }
    }
    
    public String getNotificationTitle() {
        return notificationConfig.getString("title");
    }
    
    public String getNotificationContent() {
        return notificationConfig.getString("content");
    }
    
    public String getNotificationIcon() {
        return notificationConfig.getString("icon");
    }
    
    public boolean isShowProgress() {
        return notificationConfig.getBooleanValue("showProgress");
    }
    
    /**
     * 设置通知标题
     * @param title 标题
     */
    public void setNotificationTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            notificationConfig.put("title", title);
        }
    }
    
    /**
     * 设置通知内容
     * @param content 内容
     */
    public void setNotificationContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            notificationConfig.put("content", content);
        }
    }
    
    /**
     * 设置是否显示进度
     * @param showProgress 是否显示进度
     */
    public void setShowProgress(boolean showProgress) {
        notificationConfig.put("showProgress", showProgress);
    }
    
    /**
     * 设置是否启用厂商优化
     * @param enable 是否启用
     */
    public void setEnableManufacturerOptimization(boolean enable) {
        adaptationConfig.put("enableManufacturerOptimization", enable);
    }
    
    /**
     * 设置是否启用电池白名单
     * @param enable 是否启用
     */
    public void setEnableBatteryWhitelist(boolean enable) {
        adaptationConfig.put("enableBatteryWhitelist", enable);
    }
    
    /**
     * 设置是否启用自启动
     * @param enable 是否启用
     */
    public void setEnableAutoStart(boolean enable) {
        adaptationConfig.put("enableAutoStart", enable);
    }
    
    /**
     * 获取是否启用厂商优化
     * @return 是否启用
     */
    public boolean isEnableManufacturerOptimization() {
        return adaptationConfig.getBooleanValue("enableManufacturerOptimization");
    }
    
    /**
     * 获取是否启用电池白名单
     * @return 是否启用
     */
    public boolean isEnableBatteryWhitelist() {
        return adaptationConfig.getBooleanValue("enableBatteryWhitelist");
    }
    
    /**
     * 获取是否启用自启动
     * @return 是否启用
     */
    public boolean isEnableAutoStart() {
        return adaptationConfig.getBooleanValue("enableAutoStart");
    }
    
    /**
     * 转换为JSON对象
     * @return JSONObject
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("enabled", enabled);
        json.put("heartbeatInterval", heartbeatInterval);
        json.put("maxRetryCount", maxRetryCount);
        json.put("notificationConfig", notificationConfig);
        json.put("adaptationConfig", adaptationConfig);
        return json;
    }
    
    /**
     * 从JSON对象解析配置
     * @param json JSON对象
     * @return KeepAliveConfig
     */
    public static KeepAliveConfig fromJson(JSONObject json) {
        KeepAliveConfig config = new KeepAliveConfig();
        
        if (json == null) {
            return config;
        }
        
        if (json.containsKey("enabled")) {
            config.setEnabled(json.getBooleanValue("enabled"));
        }
        if (json.containsKey("heartbeatInterval")) {
            config.setHeartbeatInterval(json.getIntValue("heartbeatInterval"));
        }
        if (json.containsKey("maxRetryCount")) {
            config.setMaxRetryCount(json.getIntValue("maxRetryCount"));
        }
        if (json.containsKey("notificationConfig")) {
            config.setNotificationConfig(json.getJSONObject("notificationConfig"));
        }
        if (json.containsKey("adaptationConfig")) {
            config.setAdaptationConfig(json.getJSONObject("adaptationConfig"));
        }
        
        return config;
    }
    
    /**
     * 从JSON字符串解析配置
     * @param jsonStr JSON字符串
     * @return KeepAliveConfig
     */
    public static KeepAliveConfig fromJsonString(String jsonStr) {
        try {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return new KeepAliveConfig();
            }
            JSONObject json = JSONObject.parseObject(jsonStr);
            return fromJson(json);
        } catch (Exception e) {
            return new KeepAliveConfig();
        }
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
}
