package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：定时的判断网络是否正常
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/16 13:48
 */

public class TimingRefreshNetworkStatus extends Service {

    //定时线程任务池
    ScheduledExecutorService timingPoolTaskService = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //启动线程池服务让子线程去处理
        if (timingPoolTaskService == null) {
            timingPoolTaskService = Executors.newSingleThreadScheduledExecutor();
            timingPoolTaskService.scheduleWithFixedDelay(new RefrshNetworkThread(), 0L,3000, TimeUnit.MILLISECONDS);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止线程任务
        if (timingPoolTaskService != null && !timingPoolTaskService.isShutdown()) {
            timingPoolTaskService.shutdown();
        }
    }

    /**
     * 判断网络是否正常
     */
    class RefrshNetworkThread extends Thread{
        @Override
        public void run() {
            boolean networkStatus = NetworkUtils.isConnected();
           // Logutil.d("networkStatus---->>>"+networkStatus);
            Intent intent = new Intent();
            intent.setAction(AppConfig.REFRESH_NETWORK_ACTION);
            intent.putExtra("isNormal",networkStatus);
            App.getApplication().sendBroadcast(intent);
        }
    }
}
