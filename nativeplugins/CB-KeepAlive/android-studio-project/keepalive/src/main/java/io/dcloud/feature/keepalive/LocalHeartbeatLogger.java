package io.dcloud.feature.keepalive;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地心跳日志管理器
 * 负责心跳数据的本地存储和管理
 * 
 * 功能特性：
 * 1. 本地存储心跳日志（最多100条）
 * 2. 自动清理旧日志
 * 3. 提供日志查询和统计功能
 * 
 * 验证需求：4.3, 4.4
 */
public class LocalHeartbeatLogger {
    
    private static final String TAG = "LocalHeartbeatLogger";
    private static final String PREFS_NAME = "heartbeat_logs";
    private static final String KEY_LOGS = "logs";
    private static final int MAX_LOGS = 100;
    
    private Context context;
    private SharedPreferences prefs;
    private List<HeartbeatData> logs;
    
    public LocalHeartbeatLogger(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.logs = new ArrayList<>();
        loadLogs();
    }
    
    /**
     * 记录心跳日志
     * @param heartbeatData 心跳数据
     */
    public synchronized void log(HeartbeatData heartbeatData) {
        try {
            Log.d(TAG, "记录心跳日志: " + heartbeatData.getTimestamp());
            
            // 添加到日志列表
            logs.add(heartbeatData);
            
            // 如果超过最大数量，删除最旧的日志
            if (logs.size() > MAX_LOGS) {
                int removeCount = logs.size() - MAX_LOGS;
                logs.subList(0, removeCount).clear();
                Log.d(TAG, "清理了 " + removeCount + " 条旧日志");
            }
            
            // 保存到本地存储
            saveLogs();
            
            Log.d(TAG, "心跳日志记录成功，当前日志数: " + logs.size());
            
        } catch (Exception e) {
            Log.e(TAG, "记录心跳日志失败", e);
        }
    }
    
    /**
     * 获取所有日志
     * @return 日志列表
     */
    public synchronized List<HeartbeatData> getAllLogs() {
        return new ArrayList<>(logs);
    }
    
    /**
     * 获取最近N条日志
     * @param count 日志数量
     * @return 日志列表
     */
    public synchronized List<HeartbeatData> getRecentLogs(int count) {
        int size = logs.size();
        if (size <= count) {
            return new ArrayList<>(logs);
        }
        
        int startIndex = size - count;
        return new ArrayList<>(logs.subList(startIndex, size));
    }
    
    /**
     * 获取指定时间范围内的日志
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 日志列表
     */
    public synchronized List<HeartbeatData> getLogsByTimeRange(long startTime, long endTime) {
        List<HeartbeatData> result = new ArrayList<>();
        
        for (HeartbeatData log : logs) {
            long timestamp = log.getTimestamp();
            if (timestamp >= startTime && timestamp <= endTime) {
                result.add(log);
            }
        }
        
        return result;
    }
    
    /**
     * 获取日志统计信息
     * @return 统计信息JSON对象
     */
    public synchronized JSONObject getStatistics() {
        JSONObject stats = new JSONObject();
        
        try {
            int totalCount = logs.size();
            stats.put("totalCount", totalCount);
            
            if (totalCount == 0) {
                stats.put("firstHeartbeat", 0);
                stats.put("lastHeartbeat", 0);
                stats.put("averageInterval", 0);
                stats.put("activeCount", 0);
                stats.put("inactiveCount", 0);
                return stats;
            }
            
            // 第一次和最后一次心跳时间
            long firstHeartbeat = logs.get(0).getTimestamp();
            long lastHeartbeat = logs.get(totalCount - 1).getTimestamp();
            stats.put("firstHeartbeat", firstHeartbeat);
            stats.put("lastHeartbeat", lastHeartbeat);
            
            // 计算平均心跳间隔
            if (totalCount > 1) {
                long totalDuration = lastHeartbeat - firstHeartbeat;
                long averageInterval = totalDuration / (totalCount - 1);
                stats.put("averageInterval", averageInterval);
            } else {
                stats.put("averageInterval", 0);
            }
            
            // 统计保活状态
            int activeCount = 0;
            int inactiveCount = 0;
            
            for (HeartbeatData log : logs) {
                String status = log.getKeepAliveStatus();
                if ("active".equals(status)) {
                    activeCount++;
                } else if ("inactive".equals(status)) {
                    inactiveCount++;
                }
            }
            
            stats.put("activeCount", activeCount);
            stats.put("inactiveCount", inactiveCount);
            
            // 最近1小时的日志数
            long oneHourAgo = System.currentTimeMillis() - 3600000;
            int recentCount = 0;
            for (HeartbeatData log : logs) {
                if (log.getTimestamp() > oneHourAgo) {
                    recentCount++;
                }
            }
            stats.put("recentHourCount", recentCount);
            
            Log.d(TAG, "统计信息: " + stats.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "获取统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * 清除所有日志
     */
    public synchronized void clearLogs() {
        try {
            logs.clear();
            saveLogs();
            Log.d(TAG, "所有日志已清除");
        } catch (Exception e) {
            Log.e(TAG, "清除日志失败", e);
        }
    }
    
    /**
     * 清除指定时间之前的日志
     * @param beforeTime 时间戳
     */
    public synchronized void clearLogsBefore(long beforeTime) {
        try {
            int originalSize = logs.size();
            logs.removeIf(log -> log.getTimestamp() < beforeTime);
            int removedCount = originalSize - logs.size();
            
            if (removedCount > 0) {
                saveLogs();
                Log.d(TAG, "清除了 " + removedCount + " 条旧日志");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清除旧日志失败", e);
        }
    }
    
    /**
     * 保存日志到本地存储
     */
    private void saveLogs() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (HeartbeatData log : logs) {
                jsonArray.add(log.toJson());
            }
            
            String jsonString = jsonArray.toJSONString();
            prefs.edit().putString(KEY_LOGS, jsonString).apply();
            
            Log.d(TAG, "日志已保存到本地存储，共 " + logs.size() + " 条");
            
        } catch (Exception e) {
            Log.e(TAG, "保存日志失败", e);
        }
    }
    
    /**
     * 从本地存储加载日志
     */
    private void loadLogs() {
        try {
            String jsonString = prefs.getString(KEY_LOGS, null);
            
            if (jsonString == null || jsonString.isEmpty()) {
                Log.d(TAG, "没有本地日志");
                return;
            }
            
            JSONArray jsonArray = JSON.parseArray(jsonString);
            logs.clear();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HeartbeatData data = HeartbeatData.fromJson(jsonObject);
                logs.add(data);
            }
            
            Log.d(TAG, "从本地存储加载了 " + logs.size() + " 条日志");
            
        } catch (Exception e) {
            Log.e(TAG, "加载日志失败", e);
            logs.clear();
        }
    }
    
    /**
     * 获取日志数量
     * @return 日志数量
     */
    public synchronized int getLogCount() {
        return logs.size();
    }
    
    /**
     * 检查是否有异常心跳
     * @return 是否有异常
     */
    public synchronized boolean hasAbnormalHeartbeat() {
        if (logs.isEmpty()) {
            return false;
        }
        
        // 检查最近的心跳是否有异常状态
        for (int i = logs.size() - 1; i >= 0 && i >= logs.size() - 10; i--) {
            HeartbeatData log = logs.get(i);
            String status = log.getKeepAliveStatus();
            if ("error".equals(status) || "abnormal".equals(status)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 导出日志为JSON
     * @return JSON字符串
     */
    public synchronized String exportLogsAsJson() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (HeartbeatData log : logs) {
                jsonArray.add(log.toJson());
            }
            return jsonArray.toJSONString();
        } catch (Exception e) {
            Log.e(TAG, "导出日志失败", e);
            return "[]";
        }
    }
    
    /**
     * 记录事件
     * @param event 事件JSON对象
     */
    public synchronized void logEvent(JSONObject event) {
        try {
            Log.d(TAG, "记录事件: " + event.getString("eventType"));
            
            // 创建一个特殊的心跳数据对象来存储事件
            HeartbeatData eventData = new HeartbeatData();
            eventData.setTimestamp(event.getLongValue("timestamp"));
            eventData.setKeepAliveStatus("event:" + event.getString("eventType"));
            
            // 将事件详情存储在设备ID字段中（临时方案）
            eventData.setDeviceId(event.toJSONString());
            
            // 记录到日志
            log(eventData);
            
            Log.d(TAG, "事件记录成功");
            
        } catch (Exception e) {
            Log.e(TAG, "记录事件失败", e);
        }
    }
}
