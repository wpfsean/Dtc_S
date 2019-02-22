package com.tehike.client.dtc.multiple.app.project.voice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.update.InstallUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：定时的检测语音播报服务是否在运行
 * 如果未运行就运行（后台）
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/8 10:51
 */
public class TimingCheckVoiceIsLiveService extends Service {

    /**
     * 定时线程池
     */
    ScheduledExecutorService timingPoolTaskService = null;

    /**
     * 语音播报的应用的包名
     */
    String vociePackName = "com.tehike.voice.xfapp.client.project";

    /**
     * 应用存放路径
     */
    String mSavePath = "";

    /**
     * 应用播报名称
     */
    String voiceApkName = "voice.apk";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //安装语音播报服务
        initInstallVocieService();

        //初始化检测服务
        initService();
    }

    @Override
    public void onDestroy() {
        //定时任务停止
        if (timingPoolTaskService != null && !timingPoolTaskService.isShutdown()) {
            timingPoolTaskService.shutdown();
        }
        //handler移除监听
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    /**
     * 安装服务
     */
    private void initInstallVocieService() {

        //判断应用是否存在
        if (isAvilible(App.getApplication(), vociePackName)) {
            Logutil.d("应用已安装");
        } else {
            Logutil.d("应用未安装");
            //安装应用
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 初始化服务(三秒执行一次)
     */
    private void initService() {
        if (timingPoolTaskService == null) {
            timingPoolTaskService = Executors.newSingleThreadScheduledExecutor();
            timingPoolTaskService.scheduleWithFixedDelay(new CheckVoiceThread(), 0, 15000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检测语音服务
     */
    class CheckVoiceThread extends Thread {
        @Override
        public void run() {
            int uid = getPackageUid(App.getApplication(), vociePackName);
            if (uid > 0) {
                boolean rstA = isAppRunning(App.getApplication(), vociePackName);
                boolean rstB = isProcessRunning(App.getApplication(), uid);
//                Logutil.d("rstA"+rstA);
//                Logutil.d("rstB"+rstB);
                if (rstA || rstB) {
                    Logutil.d("voice.apk活着呢"+new Date().toString());
                } else {
                    Logutil.e("voice.apk没有");
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(vociePackName);
                    startActivity(LaunchIntent);
                    Logutil.d("把voice.apk启动了"+new Date().toString());
                }
            }
        }
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     * Created by cafeting on 2017/2/4.
     *
     * @param context     上下文
     * @param packageName 应用的包名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    //获取已安装应用的 uid，-1 表示未安装此应用或程序异常
    public static int getPackageUid(Context context, String packageName) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                //Logutil.d(applicationInfo.uid + "");
                return applicationInfo.uid;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    /**
     * 判断某一 uid 的程序是否有正在运行的进程，即是否存活
     * Created by cafeting on 2017/2/4.
     *
     * @param context 上下文
     * @param uid     已安装应用的 uid
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isProcessRunning(Context context, int uid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() > 0) {
            for (ActivityManager.RunningServiceInfo appProcess : runningServiceInfos) {
                if (uid == appProcess.uid) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 下载apk
     */

    private void downloadVoiceApk() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String sdPath = Environment.getExternalStorageDirectory() + "/";
                        mSavePath = sdPath + AppConfig.SD_DIR + "/" + AppConfig.SOURCES_DIR;

                        File dir = new File(mSavePath);
                        if (!dir.exists())
                            dir.mkdir();

                        String downloadFileUrl = "http://19.0.0.20/zkth/voice/Voice.apk";
                        // 下载文件
                        HttpURLConnection conn = (HttpURLConnection) new URL(downloadFileUrl).openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        int length = conn.getContentLength();

                        File apkFile = new File(mSavePath, voiceApkName);
                        FileOutputStream fos = new FileOutputStream(apkFile);

                        int count = 0;
                        boolean mIsCancel = false;
                        byte[] buffer = new byte[1024];
                        while (!mIsCancel) {
                            int numread = is.read(buffer);
                            count += numread;

                            // 下载完成
                            if (numread < 0) {
                                mIsCancel = true;
                                Logutil.d("下载完成");
                                handler.sendEmptyMessage(2);
                                break;
                            }
                            fos.write(buffer, 0, numread);
                        }
                        fos.close();
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 安装语音播报apk
     */
    private void installVoiceApk() {
        String path = mSavePath + "/" + voiceApkName;

        File apkFile = new File(path);
        if (!apkFile.exists()) {
            Logutil.e("Apk不存在");
            return;
        }
        InstallUtils.install(path);
    }

    /**
     * 判断应用是否存在
     * 逻辑（取出本机的所有已安装的应用比对）
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //下载语音播报服务
                    downloadVoiceApk();
                    break;
                case 2:
                    //安装语音播报apk
                    installVoiceApk();
                    break;
            }
        }
    };


}
