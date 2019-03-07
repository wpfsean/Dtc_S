package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述：用于修改系统时间
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2019/3/4 15:21
 * @version V1.0
 */

public class UpdateSystemTimeService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 初始化
     */
    private void initialize() {
        //修改系统时间
        updateSystemTime();
    }

    /**
     * 修改系统时间
     */
    private void updateSystemTime() {
        String serverIp = (String) SharedPreferencesUtils.getObject(App.getApplication(),"serverIp","");
        String getServerTimeUrl = AppConfig.WEB_HOST+serverIp+AppConfig.SERVER_TIME;
        Logutil.d("更新时间---Url"+getServerTimeUrl);

        HttpBasicRequest httpBasicRequest = new HttpBasicRequest(getServerTimeUrl, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                Message message = new Message();
                message.what = 1;
                message.obj = result;
                handler.sendMessage(message);
            }
        });

        new Thread(httpBasicRequest).start();
    }

    /**
     * 获取系统的时间（分割成数组）
     */
    private String[] getSystemTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd|HH:mm");
        Date date = new Date();
        String format = dateFormat.format(date);
        String[] splits = format.split("\\|");
        return splits;
    }

    /**
     * 设置系统时间
     */
    private void setTime(String reuslt) {
        String[] system_times = getSystemTime();
        //系统的时分
        String[] hour_minute = system_times[1].split(":");
        //系统时
        final int systemHour = Integer.parseInt(hour_minute[0]);
        //系统分
        final int systenMinute = Integer.parseInt(hour_minute[1]);
        //解析服务器时间
        try {
            JSONObject jsonObject = new JSONObject(reuslt);
            String dateTime = jsonObject.getString("datetime");
            String serverDate = dateTime.split(" ")[0];
            String serverTime = dateTime.split(" ")[1];
            //先判断日期
            if (system_times[0].equals(serverDate)) {
                //限时服务器的时分秒
                String[] serverD = serverTime.split(":");
                //服务时
                int serverhour = Integer.parseInt(serverD[0]);
                //如果小时相等
                if (systemHour == serverhour) {
                    int serverMinute = Integer.parseInt(serverD[1]);
                    //上下错一分钟
                    int minServerMinue = serverMinute - 1;
                    int maxServerMinue = serverMinute + 1;
                    if (systenMinute > minServerMinue && systenMinute < maxServerMinue) {
                        Logutil.d("上下错一分钟，不设置");
                    } else {
                        int r = App.getSystemManager().ZYsetSysTime(serverDate, serverTime);
                        Logutil.i("返回信息" + r);
                    }
                } else {
                    int r = App.getSystemManager().ZYsetSysTime(serverDate, serverTime);
                    Logutil.i("返回信息" + r);
                }
            } else {
                int r = App.getSystemManager().ZYsetSysTime(serverDate, serverTime);
                Logutil.i("返回信息" + r);
            }

        } catch (Exception e) {
        }
    }

    /**
     * handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String reuslt = (String) msg.obj;
                    setTime(reuslt);
                    break;
            }
        }
    };
}
