package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述：用于修改系统设置
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/25 10:00
 */
public class UpdateSystemSettingService extends Service {

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

        //修改系统亮度
        updateSystemBrighness();

        //隐藏系统的导航栏
        hideSystembar();

        //修改系统时间
        updateSystemTime();
    }

    /**
     * 修改系统亮度
     */
    private void updateSystemBrighness() {

        //获取当前屏幕背光亮度
        if (App.getSystemManager() == null) {
            Logutil.e("ZysjSystemManager is null!");
            return;
        }
        int result = App.getSystemManager().ZYgetBackLight();
        if (result != -1) {
            if (result < 245) {
                int set_result = App.getSystemManager().ZYsetBackLight(245);
                if (set_result == 0) {
                    Logutil.d("系统亮度设置成功");
                } else {
                    Logutil.e("系统亮度设置失败");
                }
            }
        }
    }

    /**
     * 隐藏系统的导航栏
     */
    private void hideSystembar() {

        if (App.getSystemManager() == null) {
            Logutil.e("App.getSystemManager() is null");
            return;
        }

        int get_state = App.getSystemManager().ZYSystemBar(2);
        if (get_state == 0) {
            //当前状态为显示状态，设置导航栏状态为隐藏
            int set_state = App.getSystemManager().ZYSystemBar(0);
            if (set_state == 0) {
                Logutil.d("导航栏状态设置为隐藏");
            } else {
                Logutil.e("设置导航栏状态失败");
            }
        }

    }

    /**
     * 修改系统时间
     */
    private void updateSystemTime() {
        new Thread(new RequestServerTimeThread()).start();
    }

    /**
     * 请求服务器上的时间
     */
    class RequestServerTimeThread extends Thread {
        @Override
        public void run() {
            try {
                String getServerTimeUrl = AppConfig.SERVER_TIME;
                HttpURLConnection connection = (HttpURLConnection) new URL(getServerTimeUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = "";
                    StringBuilder builder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }

                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();

                    String serverTimeResult = builder.toString();
                    Message message = new Message();
                    message.what = 1;
                    message.obj = serverTimeResult;
                    handler.sendMessage(message);


                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Logutil.e("获取服务器信息失败" + e.getMessage()+"--->>RequestServerTimeThread");
            }
        }
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
     *
     * @param reuslt
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
            String serverDate = jsonObject.getString("date");
            String serverTime = jsonObject.getString("time");
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
