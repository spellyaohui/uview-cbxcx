package io.dcloud.feature.keepalive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * 保活前台服务
 * 
 * 功能说明：
 * - 创建和管理前台服务
 * - 显示持久通知
 * - 保持应用进程活跃
 * - 支持服务自动重启
 * 
 * @author 崔博小程序开发团队
 * @version 1.0.0
 */
public class KeepAliveService extends Service {
    
    private static final String TAG = "KeepAliveService";
    
    // 通知相关常量
    public static final int NOTIFICATION_ID = 10001;
    public static final String CHANNEL_ID = "cb_keep_alive_channel";
    public static final String CHANNEL_NAME = "保活服务";
    
    // 服务动作
    public static final String ACTION_START = "io.dcloud.feature.keepalive.ACTION_START";
    public static final String ACTION_STOP = "io.dcloud.feature.keepalive.ACTION_STOP";
    public static final String ACTION_UPDATE_NOTIFICATION = "io.dcloud.feature.keepalive.ACTION_UPDATE_NOTIFICATION";
    
    // 服务运行状态
    private static volatile boolean isRunning = false;
    
    // 唤醒锁
    private PowerManager.WakeLock wakeLock;
    
    /**
     * 检查服务是否正在运行
     */
    public static boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "保活服务创建");
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 获取唤醒锁
        acquireWakeLock();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "保活服务启动命令，action: " + (intent != null ? intent.getAction() : "null"));
        
        if (intent != null) {
            String action = intent.getAction();
            
            if (ACTION_STOP.equals(action)) {
                // 停止服务
                stopForegroundService();
                return START_NOT_STICKY;
            } else if (ACTION_UPDATE_NOTIFICATION.equals(action)) {
                // 更新通知
                String title = intent.getStringExtra("title");
                String content = intent.getStringExtra("content");
                updateNotification(title, content);
                return START_STICKY;
            }
        }
        
        // 启动前台服务
        startForegroundService();
        
        // 返回START_STICKY，服务被杀死后自动重启
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "保活服务销毁");
        
        isRunning = false;
        
        // 释放唤醒锁
        releaseWakeLock();
        
        // 尝试重启服务
        if (NotificationHelper.getInstance().isEnabled()) {
            Log.d(TAG, "尝试重启保活服务");
            Intent restartIntent = new Intent(this, KeepAliveService.class);
            restartIntent.setAction(ACTION_START);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent);
            } else {
                startService(restartIntent);
            }
        }
        
        super.onDestroy();
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "任务被移除，尝试重启服务");
        
        // 任务被移除时尝试重启服务
        if (NotificationHelper.getInstance().isEnabled()) {
            Intent restartIntent = new Intent(this, KeepAliveService.class);
            restartIntent.setAction(ACTION_START);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent);
            } else {
                startService(restartIntent);
            }
        }
        
        super.onTaskRemoved(rootIntent);
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                // 提高重要级别，确保通知在状态栏可见
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("崔博小程序后台保活服务通知");
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "通知渠道创建成功");
            }
        }
    }
    
    /**
     * 启动前台服务
     */
    private void startForegroundService() {
        Log.d(TAG, "启动前台服务");
        
        try {
            Notification notification = createNotification();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 需要指定前台服务类型
                startForeground(NOTIFICATION_ID, notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
            
            isRunning = true;
            Log.d(TAG, "前台服务启动成功");
            
        } catch (Exception e) {
            Log.e(TAG, "启动前台服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 停止前台服务
     */
    private void stopForegroundService() {
        Log.d(TAG, "停止前台服务");
        
        isRunning = false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        
        stopSelf();
    }
    
    /**
     * 创建通知
     */
    private Notification createNotification() {
        String title = NotificationHelper.getInstance().getTitle();
        String content = NotificationHelper.getInstance().getContent();
        
        if (title == null || title.isEmpty()) {
            title = "崔博小程序正在后台运行";
        }
        if (content == null || content.isEmpty()) {
            content = "确保及时接收健康报告通知";
        }
        
        // 创建点击通知时打开应用的Intent
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pendingIntent = null;
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, flags);
        }
        
        // 获取应用图标
        int iconResId = getApplicationInfo().icon;
        if (iconResId == 0) {
            iconResId = android.R.drawable.ic_dialog_info;
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(iconResId)
            .setOngoing(true)
            .setAutoCancel(false)
            // 提高优先级，增强可见性
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        
        return builder.build();
    }
    
    /**
     * 更新通知内容
     */
    private void updateNotification(String title, String content) {
        Log.d(TAG, "更新通知内容: " + title + " - " + content);
        
        NotificationHelper.getInstance().setTitle(title);
        NotificationHelper.getInstance().setContent(content);
        
        if (isRunning) {
            Notification notification = createNotification();
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, notification);
            }
        }
    }
    
    /**
     * 获取唤醒锁
     */
    private void acquireWakeLock() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "CB-KeepAlive:WakeLock"
                );
                wakeLock.acquire(10 * 60 * 1000L); // 10分钟超时
                Log.d(TAG, "唤醒锁获取成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "获取唤醒锁失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 释放唤醒锁
     */
    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "唤醒锁已释放");
            }
        } catch (Exception e) {
            Log.e(TAG, "释放唤醒锁失败: " + e.getMessage(), e);
        }
    }
}
