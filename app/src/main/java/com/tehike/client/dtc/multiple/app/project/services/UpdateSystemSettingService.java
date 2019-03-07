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
}
