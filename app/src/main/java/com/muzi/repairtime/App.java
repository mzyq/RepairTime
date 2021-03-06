package com.muzi.repairtime;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.muzi.repairtime.data.DataProxy;
import com.muzi.repairtime.data.MmkvModel;
import com.muzi.repairtime.http.RxUtils;
import com.muzi.repairtime.manager.AppManager;
import com.muzi.repairtime.utils.FileUtils;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.IOException;

/**
 * 作者: lipeng
 * 时间: 2019/3/4
 * 邮箱: lipeng@moyi365.com
 * 功能:
 */
public class App extends Application {

    private static App instance;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RxUtils.initErrorHandler();
        initRegisterActivityLifecycle();
        DataProxy.getInstance().init(new MmkvModel(this));
        initCloudChannel(this);
        if (AndPermission.hasPermissions(instance, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            copyRing();
        }
        initAlbum();
    }

    public static App getInstance() {
        return instance;
    }

    private void initRegisterActivityLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                AppManager.getAppManager().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                AppManager.getAppManager().removeActivity(activity);
            }
        });
    }

    /**
     * 初始化云推送通道
     *
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        createNotificationChannel();

        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.setNotificationSoundFilePath(Constans.RING);
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("MyMessageReceiver", "onSuccess:" + response);
                Log.d("MyMessageReceiver", pushService.getDeviceId());
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("MyMessageReceiver", " errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = "1";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "RepairTimeChannel";
            // 用户可以看到的通知渠道的描述
            String description = "RepairTimePushMessage";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    public void copyRing() {
        final File ringFile = new File(Constans.RING);
        if (!ringFile.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.createRootDir(Constans.PATH_ROOT);
                        FileUtils.createRootDir(Constans.PATH_IMAGE);
                        FileUtils.copy(instance.getApplicationContext(), "ring.mp3", Constans.RING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void initAlbum() {
        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new MediaLoader())
                .build());
    }

}
