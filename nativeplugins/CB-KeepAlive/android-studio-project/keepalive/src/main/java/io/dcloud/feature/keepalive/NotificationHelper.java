package io.dcloud.feature.keepalive;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

/**
 * 通知管理助手
 * 
 * 功能说明：
 * - 管理前台服务通知的配置
 * - 提供通知内容的动态更新
 * - 管理通知的启用/禁用状态
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    
    // 单例实例
    private static volatile NotificationHelper instance;
    
    // 上下文
    private Context context;
    
    // 通知配置
    private String title = "崔博小程序正在后台运行";
    private String content = "确保及时接收健康报告通知";
    private String icon = "ic_notification";
    private boolean showProgress = false;
    
    // 是否启用
    private boolean enabled = true;
    
    private NotificationHelper() {
    }
    
    /**
     * 获取单例实例
     */
    public static NotificationHelper getInstance() {
        if (instance == null) {
            synchronized (NotificationHelper.class) {
                if (instance == null) {
                    instance = new NotificationHelper();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化
     */
    public void init(Context context, JSONObject config) {
        this.context = context.getApplicationContext();
        
        if (config != null) {
            if (config.containsKey("title")) {
                this.title = config.getString("title");
            }
            if (config.containsKey("content")) {
                this.content = config.getString("content");
            }
            if (config.containsKey("icon")) {
                this.icon = config.getString("icon");
            }
            if (config.containsKey("showProgress")) {
                this.showProgress = config.getBooleanValue("showProgress");
            }
        }
        
        Log.d(TAG, "通知助手初始化完成，标题: " + title);
    }
    
    /**
     * 更新通知内容
     */
    public void updateNotification(String title, String content) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
        if (content != null && !content.isEmpty()) {
            this.content = content;
        }
        
        // 发送更新通知的Intent
        if (context != null && KeepAliveService.isRunning()) {
            Intent intent = new Intent(context, KeepAliveService.class);
            intent.setAction(KeepAliveService.ACTION_UPDATE_NOTIFICATION);
            intent.putExtra("title", this.title);
            intent.putExtra("content", this.content);
            context.startService(intent);
        }
        
        Log.d(TAG, "通知内容已更新: " + this.title + " - " + this.content);
    }
    
    /**
     * 获取通知标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置通知标题
     */
    public void setTitle(String title) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
    }
    
    /**
     * 获取通知内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置通知内容
     */
    public void setContent(String content) {
        if (content != null && !content.isEmpty()) {
            this.content = content;
        }
    }
    
    /**
     * 获取通知图标
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * 设置通知图标
     */
    public void setIcon(String icon) {
        if (icon != null && !icon.isEmpty()) {
            this.icon = icon;
        }
    }
    
    /**
     * 是否显示进度
     */
    public boolean isShowProgress() {
        return showProgress;
    }
    
    /**
     * 设置是否显示进度
     */
    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 取消通知
     */
    public void cancelNotification() {
        if (context != null) {
            NotificationManager manager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(KeepAliveService.NOTIFICATION_ID);
                Log.d(TAG, "通知已取消");
            }
        }
    }
    
    /**
     * 获取通知配置JSON
     */
    public JSONObject getConfigJson() {
        JSONObject config = new JSONObject();
        config.put("title", title);
        config.put("content", content);
        config.put("icon", icon);
        config.put("showProgress", showProgress);
        config.put("enabled", enabled);
        return config;
    }
}
