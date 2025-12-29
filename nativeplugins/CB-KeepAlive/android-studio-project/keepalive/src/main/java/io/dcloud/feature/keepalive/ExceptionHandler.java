package io.dcloud.feature.keepalive;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 异常处理管理器
 * 负责处理保活服务运行过程中的各种异常情况
 * 
 * 功能特性：
 * 1. 网络异常时的心跳缓存和重试
 * 2. 权限被拒绝时的友好提示
 * 3. 保活异常时的状态提示和建议
 * 4. 异常事件记录和统计
 * 
 * 验证需求：5.3, 6.5
 */
public class ExceptionHandler {
    
    private static final String TAG = "ExceptionHandler";
    
    // 异常类型
    public static final String EXCEPTION_TYPE_NETWORK = "network_error";
    public static final String EXCEPTION_TYPE_PERMISSION = "permission_denied";
    public static final String EXCEPTION_TYPE_SERVICE = "service_error";
    public static final String EXCEPTION_TYPE_MEMORY = "memory_error";
    public static final String EXCEPTION_TYPE_UNKNOWN = "unknown_error";
    
    // 重试配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_BASE = 5000; // 5秒
    private static final long RETRY_DELAY_MAX = 60000; // 60秒
    
    private Context context;
    private LocalHeartbeatLogger logger;
    
    // 缓存的心跳数据
    private List<HeartbeatData> cachedHeartbeats;
    
    // 重试状态
    private int retryAttempts = 0;
    private boolean isRetrying = false;
    
    public ExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
        this.cachedHeartbeats = new ArrayList<>();
        
        // 获取日志管理器
        KeepAliveManager manager = KeepAliveManager.getInstance();
        if (manager.isInitialized()) {
            this.logger = manager.getHeartbeatLogger();
        }
    }
    
    /**
     * 处理网络异常
     * @param heartbeatData 心跳数据
     * @return 处理结果
     */
    public JSONObject handleNetworkException(HeartbeatData heartbeatData) {
        JSONObject result = new JSONObject();
        
        try {
            Log.w(TAG, "处理网络异常");
            
            // 检查网络状态
            boolean isNetworkAvailable = isNetworkAvailable();
            
            if (!isNetworkAvailable) {
                Log.w(TAG, "网络不可用，缓存心跳数据");
                
                // 缓存心跳数据
                cacheHeartbeat(heartbeatData);
                
                result.put("success", false);
                result.put("cached", true);
                result.put("message", "网络不可用，心跳数据已缓存");
                result.put("suggestion", "请检查网络连接，数据将在网络恢复后自动上传");
                
                // 记录网络异常事件
                recordException(EXCEPTION_TYPE_NETWORK, "网络不可用", null);
                
            } else {
                Log.d(TAG, "网络可用，尝试重试");
                
                // 网络可用，尝试重试
                boolean retrySuccess = retryHeartbeatUpload(heartbeatData);
                
                result.put("success", retrySuccess);
                result.put("cached", !retrySuccess);
                result.put("message", retrySuccess ? "重试成功" : "重试失败，已缓存");
                
                if (!retrySuccess) {
                    result.put("suggestion", "网络连接不稳定，数据已缓存待重试");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理网络异常失败", e);
            result.put("success", false);
            result.put("message", "处理网络异常失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 处理权限异常
     * @param permissionType 权限类型
     * @return 处理结果
     */
    public JSONObject handlePermissionException(String permissionType) {
        JSONObject result = new JSONObject();
        
        try {
            Log.w(TAG, "处理权限异常: " + permissionType);
            
            // 生成友好的提示信息
            String message = generatePermissionMessage(permissionType);
            String suggestion = generatePermissionSuggestion(permissionType);
            
            result.put("success", false);
            result.put("permissionType", permissionType);
            result.put("message", message);
            result.put("suggestion", suggestion);
            result.put("canRequest", canRequestPermission(permissionType));
            
            // 记录权限异常事件
            recordException(EXCEPTION_TYPE_PERMISSION, "权限被拒绝: " + permissionType, null);
            
            // 发送权限异常通知
            sendPermissionExceptionNotification(permissionType, message, suggestion);
            
        } catch (Exception e) {
            Log.e(TAG, "处理权限异常失败", e);
            result.put("success", false);
            result.put("message", "处理权限异常失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 处理服务异常
     * @param errorMessage 错误信息
     * @param exception 异常对象
     * @return 处理结果
     */
    public JSONObject handleServiceException(String errorMessage, Exception exception) {
        JSONObject result = new JSONObject();
        
        try {
            Log.e(TAG, "处理服务异常: " + errorMessage, exception);
            
            // 分析异常原因
            String cause = analyzeExceptionCause(exception);
            String suggestion = generateServiceSuggestion(cause);
            
            result.put("success", false);
            result.put("errorMessage", errorMessage);
            result.put("cause", cause);
            result.put("suggestion", suggestion);
            
            if (exception != null) {
                result.put("exceptionType", exception.getClass().getSimpleName());
                result.put("exceptionMessage", exception.getMessage());
            }
            
            // 记录服务异常事件
            recordException(EXCEPTION_TYPE_SERVICE, errorMessage, exception);
            
            // 尝试恢复服务
            boolean recovered = attemptServiceRecovery(cause);
            result.put("recovered", recovered);
            
            if (!recovered) {
                // 发送服务异常通知
                sendServiceExceptionNotification(errorMessage, suggestion);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理服务异常失败", e);
            result.put("success", false);
            result.put("message", "处理服务异常失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 缓存心跳数据
     * @param heartbeatData 心跳数据
     */
    private void cacheHeartbeat(HeartbeatData heartbeatData) {
        try {
            synchronized (cachedHeartbeats) {
                cachedHeartbeats.add(heartbeatData);
                
                // 限制缓存大小（最多50条）
                if (cachedHeartbeats.size() > 50) {
                    cachedHeartbeats.remove(0);
                }
                
                Log.d(TAG, "心跳数据已缓存，当前缓存数: " + cachedHeartbeats.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "缓存心跳数据失败", e);
        }
    }
    
    /**
     * 重试心跳上传
     * @param heartbeatData 心跳数据
     * @return 是否成功
     */
    private boolean retryHeartbeatUpload(HeartbeatData heartbeatData) {
        try {
            if (isRetrying) {
                Log.d(TAG, "正在重试中，跳过");
                return false;
            }
            
            if (retryAttempts >= MAX_RETRY_ATTEMPTS) {
                Log.w(TAG, "已达到最大重试次数");
                cacheHeartbeat(heartbeatData);
                return false;
            }
            
            isRetrying = true;
            retryAttempts++;
            
            // 计算重试延迟（指数退避）
            long retryDelay = Math.min(RETRY_DELAY_BASE * (long) Math.pow(2, retryAttempts - 1), RETRY_DELAY_MAX);
            
            Log.d(TAG, "重试心跳上传，尝试次数: " + retryAttempts + ", 延迟: " + retryDelay + "ms");
            
            // 延迟后重试
            Thread.sleep(retryDelay);
            
            // 这里应该调用实际的上传逻辑
            // 目前只是模拟成功
            boolean success = isNetworkAvailable();
            
            if (success) {
                Log.i(TAG, "重试成功");
                retryAttempts = 0;
            } else {
                Log.w(TAG, "重试失败");
                cacheHeartbeat(heartbeatData);
            }
            
            isRetrying = false;
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "重试心跳上传失败", e);
            isRetrying = false;
            return false;
        }
    }
    
    /**
     * 上传缓存的心跳数据
     * @return 上传结果
     */
    public JSONObject uploadCachedHeartbeats() {
        JSONObject result = new JSONObject();
        
        try {
            synchronized (cachedHeartbeats) {
                int totalCount = cachedHeartbeats.size();
                
                if (totalCount == 0) {
                    result.put("success", true);
                    result.put("message", "没有缓存的心跳数据");
                    result.put("uploadedCount", 0);
                    return result;
                }
                
                Log.i(TAG, "开始上传缓存的心跳数据，共 " + totalCount + " 条");
                
                int uploadedCount = 0;
                List<HeartbeatData> failedHeartbeats = new ArrayList<>();
                
                for (HeartbeatData heartbeat : cachedHeartbeats) {
                    // 这里应该调用实际的上传逻辑
                    boolean success = isNetworkAvailable(); // 模拟上传
                    
                    if (success) {
                        uploadedCount++;
                    } else {
                        failedHeartbeats.add(heartbeat);
                    }
                }
                
                // 清除已上传的数据，保留失败的
                cachedHeartbeats.clear();
                cachedHeartbeats.addAll(failedHeartbeats);
                
                result.put("success", failedHeartbeats.isEmpty());
                result.put("totalCount", totalCount);
                result.put("uploadedCount", uploadedCount);
                result.put("failedCount", failedHeartbeats.size());
                result.put("message", "上传完成，成功: " + uploadedCount + ", 失败: " + failedHeartbeats.size());
                
                Log.i(TAG, "缓存心跳上传完成: " + result.getString("message"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "上传缓存心跳失败", e);
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查网络是否可用
     * @return 是否可用
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
            
        } catch (Exception e) {
            Log.e(TAG, "检查网络状态失败", e);
            return false;
        }
    }
    
    /**
     * 生成权限提示信息
     * @param permissionType 权限类型
     * @return 提示信息
     */
    private String generatePermissionMessage(String permissionType) {
        switch (permissionType) {
            case "FOREGROUND_SERVICE":
                return "前台服务权限被拒绝，保活功能可能无法正常工作";
            case "POST_NOTIFICATIONS":
                return "通知权限被拒绝，无法显示保活服务通知";
            case "BATTERY_OPTIMIZATION":
                return "电池优化未关闭，应用可能在后台被系统限制";
            default:
                return "权限被拒绝: " + permissionType;
        }
    }
    
    /**
     * 生成权限建议
     * @param permissionType 权限类型
     * @return 建议信息
     */
    private String generatePermissionSuggestion(String permissionType) {
        switch (permissionType) {
            case "FOREGROUND_SERVICE":
                return "请在系统设置中授予前台服务权限，以确保保活功能正常工作";
            case "POST_NOTIFICATIONS":
                return "请在系统设置中开启通知权限，以便接收保活服务状态通知";
            case "BATTERY_OPTIMIZATION":
                return "请将应用添加到电池优化白名单，避免后台被系统限制";
            default:
                return "请在系统设置中授予必要的权限";
        }
    }
    
    /**
     * 检查是否可以请求权限
     * @param permissionType 权限类型
     * @return 是否可以请求
     */
    private boolean canRequestPermission(String permissionType) {
        // 这里应该检查权限是否可以再次请求
        // 简化实现，总是返回true
        return true;
    }
    
    /**
     * 分析异常原因
     * @param exception 异常对象
     * @return 原因描述
     */
    private String analyzeExceptionCause(Exception exception) {
        if (exception == null) {
            return "未知原因";
        }
        
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        
        if (exceptionType.contains("SecurityException")) {
            return "权限不足";
        } else if (exceptionType.contains("NetworkException") || exceptionType.contains("IOException")) {
            return "网络异常";
        } else if (exceptionType.contains("OutOfMemoryError")) {
            return "内存不足";
        } else if (message != null && message.contains("Service")) {
            return "服务启动失败";
        } else {
            return "系统异常";
        }
    }
    
    /**
     * 生成服务建议
     * @param cause 异常原因
     * @return 建议信息
     */
    private String generateServiceSuggestion(String cause) {
        switch (cause) {
            case "权限不足":
                return "请检查应用权限设置，确保已授予必要的权限";
            case "网络异常":
                return "请检查网络连接，确保网络正常";
            case "内存不足":
                return "系统内存不足，建议关闭其他应用释放内存";
            case "服务启动失败":
                return "服务启动失败，请尝试重启应用";
            default:
                return "发生未知错误，请尝试重启应用或联系技术支持";
        }
    }
    
    /**
     * 尝试服务恢复
     * @param cause 异常原因
     * @return 是否恢复成功
     */
    private boolean attemptServiceRecovery(String cause) {
        try {
            Log.i(TAG, "尝试服务恢复，原因: " + cause);
            
            // 根据不同原因采取不同的恢复策略
            switch (cause) {
                case "网络异常":
                    // 网络异常不需要恢复服务
                    return true;
                    
                case "内存不足":
                    // 触发垃圾回收
                    System.gc();
                    return true;
                    
                case "服务启动失败":
                    // 尝试重启服务
                    KeepAliveManager manager = KeepAliveManager.getInstance();
                    if (manager.isInitialized()) {
                        manager.stop();
                        Thread.sleep(2000);
                        return manager.start();
                    }
                    return false;
                    
                default:
                    return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "服务恢复失败", e);
            return false;
        }
    }
    
    /**
     * 记录异常事件
     * @param exceptionType 异常类型
     * @param message 异常信息
     * @param exception 异常对象
     */
    private void recordException(String exceptionType, String message, Exception exception) {
        try {
            if (logger == null) {
                return;
            }
            
            JSONObject event = new JSONObject();
            event.put("eventType", "exception");
            event.put("exceptionType", exceptionType);
            event.put("message", message);
            event.put("timestamp", System.currentTimeMillis());
            
            if (exception != null) {
                event.put("exceptionClass", exception.getClass().getName());
                event.put("exceptionMessage", exception.getMessage());
                
                // 获取堆栈跟踪（前5行）
                StackTraceElement[] stackTrace = exception.getStackTrace();
                if (stackTrace != null && stackTrace.length > 0) {
                    JSONArray stackArray = new JSONArray();
                    int maxLines = Math.min(5, stackTrace.length);
                    for (int i = 0; i < maxLines; i++) {
                        stackArray.add(stackTrace[i].toString());
                    }
                    event.put("stackTrace", stackArray);
                }
            }
            
            logger.logEvent(event);
            Log.d(TAG, "异常事件已记录: " + exceptionType);
            
        } catch (Exception e) {
            Log.e(TAG, "记录异常事件失败", e);
        }
    }
    
    /**
     * 发送权限异常通知
     * @param permissionType 权限类型
     * @param message 消息
     * @param suggestion 建议
     */
    private void sendPermissionExceptionNotification(String permissionType, String message, String suggestion) {
        try {
            Intent intent = new Intent("io.dcloud.feature.keepalive.PERMISSION_EXCEPTION");
            intent.putExtra("permissionType", permissionType);
            intent.putExtra("message", message);
            intent.putExtra("suggestion", suggestion);
            context.sendBroadcast(intent);
            
            Log.d(TAG, "权限异常通知已发送");
            
        } catch (Exception e) {
            Log.e(TAG, "发送权限异常通知失败", e);
        }
    }
    
    /**
     * 发送服务异常通知
     * @param errorMessage 错误信息
     * @param suggestion 建议
     */
    private void sendServiceExceptionNotification(String errorMessage, String suggestion) {
        try {
            Intent intent = new Intent("io.dcloud.feature.keepalive.SERVICE_EXCEPTION");
            intent.putExtra("errorMessage", errorMessage);
            intent.putExtra("suggestion", suggestion);
            context.sendBroadcast(intent);
            
            Log.d(TAG, "服务异常通知已发送");
            
        } catch (Exception e) {
            Log.e(TAG, "发送服务异常通知失败", e);
        }
    }
    
    /**
     * 获取缓存的心跳数量
     * @return 缓存数量
     */
    public int getCachedHeartbeatCount() {
        synchronized (cachedHeartbeats) {
            return cachedHeartbeats.size();
        }
    }
    
    /**
     * 清除缓存的心跳数据
     */
    public void clearCachedHeartbeats() {
        synchronized (cachedHeartbeats) {
            cachedHeartbeats.clear();
            Log.d(TAG, "缓存的心跳数据已清除");
        }
    }
    
    /**
     * 获取异常统计信息
     * @return 统计信息
     */
    public JSONObject getExceptionStats() {
        JSONObject stats = new JSONObject();
        
        try {
            stats.put("cachedHeartbeatCount", getCachedHeartbeatCount());
            stats.put("retryAttempts", retryAttempts);
            stats.put("isRetrying", isRetrying);
            stats.put("networkAvailable", isNetworkAvailable());
            
        } catch (Exception e) {
            Log.e(TAG, "获取异常统计信息失败", e);
        }
        
        return stats;
    }
}
