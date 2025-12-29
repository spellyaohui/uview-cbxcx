package io.dcloud.feature.keepalive;

import com.alibaba.fastjson.JSONObject;

/**
 * 心跳数据模型
 * 包含设备信息、应用状态、保活状态等关键信息
 * 
 * 验证需求：4.2
 */
public class HeartbeatData {
    
    private String deviceId;           // 设备唯一标识
    private String appVersion;         // 应用版本
    private String systemVersion;      // 系统版本
    private String deviceModel;        // 设备型号
    private String manufacturer;       // 设备厂商
    private String keepAliveStatus;    // 保活状态
    private long timestamp;            // 时间戳
    private int batteryLevel;          // 电池电量
    private String networkType;        // 网络类型
    private int screenWidth;           // 屏幕宽度
    private int screenHeight;          // 屏幕高度
    private boolean isScreenOn;        // 屏幕是否开启
    private long freeMemory;           // 可用内存
    private long totalMemory;          // 总内存
    
    public HeartbeatData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    public String getSystemVersion() {
        return systemVersion;
    }
    
    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }
    
    public String getDeviceModel() {
        return deviceModel;
    }
    
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getKeepAliveStatus() {
        return keepAliveStatus;
    }
    
    public void setKeepAliveStatus(String keepAliveStatus) {
        this.keepAliveStatus = keepAliveStatus;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public String getNetworkType() {
        return networkType;
    }
    
    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }
    
    public int getScreenWidth() {
        return screenWidth;
    }
    
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
    
    public int getScreenHeight() {
        return screenHeight;
    }
    
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
    
    public boolean isScreenOn() {
        return isScreenOn;
    }
    
    public void setScreenOn(boolean screenOn) {
        isScreenOn = screenOn;
    }
    
    public long getFreeMemory() {
        return freeMemory;
    }
    
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }
    
    /**
     * 转换为JSON对象
     * @return JSONObject
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("deviceId", deviceId);
        json.put("appVersion", appVersion);
        json.put("systemVersion", systemVersion);
        json.put("deviceModel", deviceModel);
        json.put("manufacturer", manufacturer);
        json.put("keepAliveStatus", keepAliveStatus);
        json.put("timestamp", timestamp);
        json.put("batteryLevel", batteryLevel);
        json.put("networkType", networkType);
        json.put("screenWidth", screenWidth);
        json.put("screenHeight", screenHeight);
        json.put("isScreenOn", isScreenOn);
        json.put("freeMemory", freeMemory);
        json.put("totalMemory", totalMemory);
        return json;
    }
    
    /**
     * 从JSON对象创建
     * @param json JSON对象
     * @return HeartbeatData
     */
    public static HeartbeatData fromJson(JSONObject json) {
        HeartbeatData data = new HeartbeatData();
        data.setDeviceId(json.getString("deviceId"));
        data.setAppVersion(json.getString("appVersion"));
        data.setSystemVersion(json.getString("systemVersion"));
        data.setDeviceModel(json.getString("deviceModel"));
        data.setManufacturer(json.getString("manufacturer"));
        data.setKeepAliveStatus(json.getString("keepAliveStatus"));
        data.setTimestamp(json.getLongValue("timestamp"));
        data.setBatteryLevel(json.getIntValue("batteryLevel"));
        data.setNetworkType(json.getString("networkType"));
        data.setScreenWidth(json.getIntValue("screenWidth"));
        data.setScreenHeight(json.getIntValue("screenHeight"));
        data.setScreenOn(json.getBooleanValue("isScreenOn"));
        data.setFreeMemory(json.getLongValue("freeMemory"));
        data.setTotalMemory(json.getLongValue("totalMemory"));
        return data;
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
}
