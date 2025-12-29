package io.dcloud.feature.keepalive;

import com.alibaba.fastjson.JSONObject;

/**
 * 保活状态类
 * 包含保活服务的运行状态和统计信息
 */
public class KeepAliveStatus {
    
    // 基础状态
    private boolean initialized = false;
    private boolean running = false;
    private boolean enabled = false;
    private String serviceStatus = "stopped";
    
    // 心跳统计
    private long lastHeartbeat = 0;
    private int heartbeatCount = 0;
    private int errorCount = 0;
    
    // 权限状态
    private JSONObject permissions;
    
    // 设备信息
    private JSONObject deviceInfo;
    
    // 配置信息
    private JSONObject config;
    
    // 时间戳
    private long timestamp = System.currentTimeMillis();
    
    public KeepAliveStatus() {
        this.permissions = new JSONObject();
        this.deviceInfo = new JSONObject();
        this.config = new JSONObject();
    }
    
    // Getter和Setter方法
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void setRunning(boolean running) {
        this.running = running;
        this.serviceStatus = running ? "running" : "stopped";
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getServiceStatus() {
        return serviceStatus;
    }
    
    public void setServiceStatus(String serviceStatus) {
        if (serviceStatus != null) {
            this.serviceStatus = serviceStatus;
        }
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public int getHeartbeatCount() {
        return heartbeatCount;
    }
    
    public void setHeartbeatCount(int heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }
    
    public void incrementHeartbeatCount() {
        this.heartbeatCount++;
        this.lastHeartbeat = System.currentTimeMillis();
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
    
    public void incrementErrorCount() {
        this.errorCount++;
    }
    
    public JSONObject getPermissions() {
        return permissions;
    }
    
    public void setPermissions(JSONObject permissions) {
        if (permissions != null) {
            this.permissions = permissions;
        }
    }
    
    public JSONObject getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(JSONObject deviceInfo) {
        if (deviceInfo != null) {
            this.deviceInfo = deviceInfo;
        }
    }
    
    public JSONObject getConfig() {
        return config;
    }
    
    public void setConfig(JSONObject config) {
        if (config != null) {
            this.config = config;
        }
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 转换为JSON对象
     * @return JSONObject
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        
        // 基础状态
        json.put("initialized", initialized);
        json.put("running", running);
        json.put("enabled", enabled);
        json.put("serviceStatus", serviceStatus);
        
        // 心跳统计
        json.put("lastHeartbeat", lastHeartbeat);
        json.put("heartbeatCount", heartbeatCount);
        json.put("errorCount", errorCount);
        
        // 权限状态
        json.put("permissions", permissions);
        
        // 设备信息
        json.put("deviceInfo", deviceInfo);
        
        // 配置信息
        json.put("config", config);
        
        // 时间戳
        json.put("timestamp", timestamp);
        
        return json;
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
}