package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.update.AppUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 描述：定时的发送本机的信息到服务器
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/3 10:46
 */

public class TimingSendNativeInfoService extends Service {

    //定时任务线程池
    ScheduledExecutorService mScheduledExecutorService = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //启动线程池服务让子线程去处理
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            mScheduledExecutorService.scheduleWithFixedDelay(new RequestAlarmTypeThread(), 0L, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭线程池
        if (mScheduledExecutorService != null && !mScheduledExecutorService.isShutdown())
            mScheduledExecutorService.shutdown();
    }

    /**
     * 向cms索要sip数据
     */
    class RequestAlarmTypeThread extends Thread {
        @Override
        public void run() {
            //获取终端所有的信息
            String currentDeviceName = "";
            Map<String, String> allInfor = AppUtils.collectDeviceInfo(App.getApplication());
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : allInfor.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("DEVICE")){
                    currentDeviceName = value;
                }
                sb.append(key).append("=").append(value).append("%");
            }
           // Logutil.d(sb.toString());
            Logutil.d("当前设备名:"+currentDeviceName);
            try {
                String commitInfoUrl = AppConfig.COMMIT_NATIVE_INFO_PATH + sb.toString();
                HttpURLConnection connection = (HttpURLConnection) new URL(commitInfoUrl).openConnection();
                connection.connect();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(4000);
                connection.setConnectTimeout(4000);
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    String result = StringUtils.readTxt(inputStream);
                    if (TextUtils.isEmpty(result)){
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString("status");
                        if (!TextUtils.isEmpty(status)) {
                            if (status.equals("ok")) {
                                Logutil.d("提交信息完成");
                            } else {
                                Logutil.d("提交信息缺失参数-->>" + status);
                            }
                        } else {
                            Logutil.e("提交信息缺失");
                        }
                    } catch (Exception e) {
                    }
                } else {
                    Logutil.e("提交本机信息时异常" + connection.getResponseCode());
                }
                connection.disconnect();
            } catch (IOException e) {
                Logutil.e("提交本机信息时异常--->>"+e.getMessage());
            }
        }
    }

}
