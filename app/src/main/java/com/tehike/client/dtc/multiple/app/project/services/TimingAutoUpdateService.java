package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.update.AppUtils;
import com.tehike.client.dtc.multiple.app.project.update.InstallUtils;
import com.tehike.client.dtc.multiple.app.project.update.UpDateInfo;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：定时更新apk
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/13 17:18
 */

public class TimingAutoUpdateService extends Service {

    /**
     * 最新下载apk的路径
     */
    String sdPath = "";

    /**
     * 定时请求更新的线程池任务
     */
    ScheduledExecutorService timingRequestUpdateService = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //启动线程池服务让子线程去处理
        if (timingRequestUpdateService == null) {
            timingRequestUpdateService = Executors.newSingleThreadScheduledExecutor();
            timingRequestUpdateService.scheduleWithFixedDelay(new AutomaticUpdateThread(), 8000, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //停止线程任务
        if (timingRequestUpdateService != null && !timingRequestUpdateService.isShutdown()) {
            timingRequestUpdateService.shutdown();
        }
        //移除Handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 子线程请求更新
     */
    class AutomaticUpdateThread extends Thread {
        @Override
        public void run() {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(AppConfig.WEB_HOST+ SysinfoUtils.getSysinfo().getWebresourceServer()+AppConfig.UPDATE_APK_PATH+AppConfig.UPDATE_APK_FILE).openConnection();
                connection.setReadTimeout(6000);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    String line = "";
                    StringBuilder builder = new StringBuilder();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    inputStream.close();
                    connection.disconnect();
                    UpDateInfo mUpDateInfo = StringUtils.resolveXml(builder.toString());
                    updateApk(mUpDateInfo);

                } else {
                    Logutil.e("请求更新失败-->>"+AppConfig.WEB_HOST+ SysinfoUtils.getSysinfo().getWebresourceServer()+AppConfig.UPDATE_APK_PATH+AppConfig.UPDATE_APK_FILE);
                    WriteLogToFile.info("请求更新失败--->>>"+AppConfig.WEB_HOST+ SysinfoUtils.getSysinfo().getWebresourceServer()+AppConfig.UPDATE_APK_PATH+AppConfig.UPDATE_APK_FILE);
                }
            } catch (Exception e) {
                Logutil.e("请求更新异常"+e.getMessage());
                WriteLogToFile.info("请求更新异常"+e.getMessage());
            }
        }
    }

    /**
     * 本地版本和服务器版本比较
     */
    private void updateApk(UpDateInfo mUpDateInfo) {
        //当前版本
        int currentVersionCode = AppUtils.getVersionCode(App.getApplication());
        //如果本地版本小于服务器版本就执行更新
        if (mUpDateInfo.getVersion() > currentVersionCode) {
            downloadAPK(mUpDateInfo);
        }else {
            Logutil.w("无需更新");
        }
    }

    /*
     * 开启新线程下载文件
	 */
    private void downloadAPK(final UpDateInfo mUpDateInfo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //sd卡上存放新版本apk的路径
                    sdPath = Environment.getExternalStorageDirectory() + "/" + AppConfig.SD_DIR + "/" + AppConfig.SOURCES_DIR;
                    // 下载文件
                    HttpURLConnection conn = (HttpURLConnection) new URL(AppConfig.WEB_HOST+ SysinfoUtils.getSysinfo().getWebresourceServer()+AppConfig.UPDATE_APK_PATH+mUpDateInfo.getName()).openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    //判断本地是否存在此apk
                    File apkFile = new File(sdPath, mUpDateInfo.getName());
                    //若存在就删除
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    //写入sd卡文件夹内
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int count = -1;
                    while ((count = is.read(data, 0, 1024)) != -1)
                        outStream.write(data, 0, count);
                    outStream.flush();
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    fos.write(outStream.toByteArray());
                    outStream.close();
                    fos.flush();
                    fos.close();
                    is.close();
                    Logutil.d("新版本的Apk下载完成---" + mUpDateInfo.getName());
                    Message message = new Message();
                    message.what = 1;
                    message.obj = mUpDateInfo.getName();
                    handler.sendMessage(message);
                } catch (Exception e) {
                    Logutil.e("更新apk下载异常--->>>" + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 安装此apk文件
     */
    protected void installAPK(String appName) {
        //更新apk所在的路径
        String path = sdPath + "/" + appName;
        File apkFile = new File(path);
        if (!apkFile.exists()){
            Logutil.e("Apk文件不存在");
            return;
        }
        Logutil.d("CurrentApkPath--->>>"+sdPath);
        //执行静默更新
        InstallUtils.install(path);
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String appName = (String) msg.obj;
                    //安装apk
                    installAPK(appName);
                    break;
            }
        }
    };
}
