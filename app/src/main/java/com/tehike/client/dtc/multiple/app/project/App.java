package com.tehike.client.dtc.multiple.app.project;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ZysjSystemManager;
import android.os.Looper;
import android.util.Log;
import com.tehike.client.dtc.multiple.app.project.execption.Cockroach;
import com.tehike.client.dtc.multiple.app.project.execption.CrashLog;
import com.tehike.client.dtc.multiple.app.project.execption.ExceptionHandler;
import com.tehike.client.dtc.multiple.app.project.services.CpuAndRamUtils;
import com.tehike.client.dtc.multiple.app.project.services.RemoteVoiceOperatService;
import com.tehike.client.dtc.multiple.app.project.services.TerminalUpdateIpService;
import com.tehike.client.dtc.multiple.app.project.services.TimingRefreshNetworkStatus;
import com.tehike.client.dtc.multiple.app.project.services.UpdateSystemSettingService;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.ServiceUtil;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述：全局配置
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/2 16:25
 */
public class App extends Application {

    /**
     * 提供一个供全局使用的application的Context上下文
     */
    public static App mContext;

    /**
     * 众云（提供接口）
     */
    public static ZysjSystemManager mZysjSystemManager;

    /**
     * 线程池
     */
    public static ExecutorService mExecutorService = null;

    /**
     * 本机的最大的线程
     */
    int threadCount = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        //加载配置参数
        loadConfigInfo();

        //初始化
        init();

        //用于捕获异常
        installUncaughtExceptionHandler();

        //启动服务
        startServices();
    }

    /**
     * 加载配置信息
     */
    private void loadConfigInfo() {

//        AppConfig.IS_CAN_SLIDE = true;
//        Logutil.d("AAAAAAAAAAAAAAAAAAAAAAAAAA-->>"+ AppConfig.IS_CAN_SLIDE);

        /**
         * 配置思路
         *
         * 建表存储：
         * id
         * time     建表日期
         * isCanSlide  是否请允许滑动
         * systemVoice  系统声音
         * callVoice 通话声音
         * ringVoice 振铃声音
         * voideVoice 播放时是否允许声音
         * isRemederPwd 是否记住密码
         * isAutoLogin 是否自动登录
         * loadCacheTime 定时加载数据的间隔时间
         * ipDns  Dns
         * sPort 上位机的监听端口（修改Ip）
         * xPort 下位机的监听端口（修改Ip）
         * remotePort 远程喊话时的端口
         * screenSaverTime 屏保时间
         * meetinger 会议发起人号码
         */


    }

    @SuppressLint("WrongConstant")
    private void init() {
        mContext = this;
        //初始化
        mZysjSystemManager = (ZysjSystemManager) getSystemService("zysj");
        //获取可用的处理器数
        threadCount = Runtime.getRuntime().availableProcessors();
        //线程池内运行程序可执行的最大线程数
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(threadCount);
        }
    }

    private void startServices() {
        //启动cpu和ram监听
        CpuAndRamUtils.getInstance().init(getApplicationContext(), 5 * 1000L);
        CpuAndRamUtils.getInstance().start();

        //启动被动远程操作的服务
        if (!ServiceUtil.isServiceRunning(RemoteVoiceOperatService.class)) {
            ServiceUtil.startService(RemoteVoiceOperatService.class);
        }
        //启动被动修改ip
        if (!ServiceUtil.isServiceRunning(TerminalUpdateIpService.class)) {
            ServiceUtil.startService(TerminalUpdateIpService.class);
        }
        //定时刷新网络
        if (!ServiceUtil.isServiceRunning(TimingRefreshNetworkStatus.class)) {
            ServiceUtil.startService(TimingRefreshNetworkStatus.class);
        }
        //用于修改系统设置
        if (!ServiceUtil.isServiceRunning(UpdateSystemSettingService.class)) {
            ServiceUtil.startService(UpdateSystemSettingService.class);
        }
    }

    /**
     * 替换系统默认的异常处理机制，用于捕获异常
     */
    private void installUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        Cockroach.install(new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, final Throwable throwable) {
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                //把崩溃异常写入文件
                CrashLog.saveCrashLog(mContext, throwable);
                Logutil.e(throwable.getMessage());
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                ToastUtils.showShort("Arrest!");
            }

            @Override
            protected void onEnterSafeMode() {

            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                //黑屏时建议直接杀死app
                sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
            }

        });
    }

    /**
     * 获取application上下文
     */
    public static App getApplication() {
        return mContext;
    }

    /**
     * 众云
     */
    public static ZysjSystemManager getSystemManager() {
        return mZysjSystemManager;
    }

    /**
     * 获取本机可用的最大线程池对象
     */
    public static ExecutorService getExecutorService() {
        return mExecutorService;
    }

}
