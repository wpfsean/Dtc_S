package com.tehike.client.dtc.multiple.app.project;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ZysjSystemManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import com.tehike.client.dtc.multiple.app.project.execption.Cockroach;
import com.tehike.client.dtc.multiple.app.project.execption.CrashLog;
import com.tehike.client.dtc.multiple.app.project.execption.ExceptionHandler;
import com.tehike.client.dtc.multiple.app.project.services.CpuAndRamUtils;
import com.tehike.client.dtc.multiple.app.project.services.InitSystemSettingService;
import com.tehike.client.dtc.multiple.app.project.services.RemoteVoiceOperatService;
import com.tehike.client.dtc.multiple.app.project.services.TerminalUpdateIpService;
import com.tehike.client.dtc.multiple.app.project.services.TimingRefreshNetworkStatus;
import com.tehike.client.dtc.multiple.app.project.thread.SendAlarmToServerThread;
import com.tehike.client.dtc.multiple.app.project.update.InstallUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.ServiceUtil;
import com.tehike.client.dtc.multiple.app.project.utils.SharedPreferencesUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;
import java.io.File;
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

    /**
     * 讯飞合成对象
     */
    private static SpeechSynthesizer mTts;


    @Override
    public void onCreate() {
        super.onCreate();

        boolean deviceIsRoot = InstallUtils.isRoot();
        WriteLogToFile.info("当前设备是否root--->>" + deviceIsRoot);

        //加载配置参数
        loadConfigInfo();

        //初始化
        init();

        //用于捕获异常
        installUncaughtExceptionHandler();

        //启动服务
        startServices();

        //初始化语音播放参数
        initializeParamater();

        //语音提示设备初始化成功
        startSpeaking("设备启动成功");


        initKeyBoard();

    }

    /**
     * 键盘单条指令
     */
    String singleCommand = "";

    /**
     * 键盘串口对象
     */
    Device keyBoardSerialPortDevice = null;

    /**
     * 初始化键盘（测试）
     * ttyACM2
     */
    private void initKeyBoard() {

        //串口管理类
        SerialPortManager mSerialPortManager = new SerialPortManager();
        //取出本地保存的串口标识
        String keyboardSelected = (String) SharedPreferencesUtils.getObject(App.getApplication(), "keyboardserialport", "");
        if (TextUtils.isEmpty(keyboardSelected)) {
            keyBoardSerialPortDevice = new Device("ttyACM2", "", new File("/dev/ttyACM2"));
            SharedPreferencesUtils.putObject(App.getApplication(), "keyboardserialport", GsonUtils.GsonString(keyBoardSerialPortDevice));
        } else {
            keyBoardSerialPortDevice = GsonUtils.GsonToBean(keyboardSelected, Device.class);
        }

        //打开com1（测试）
        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(new OnOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {
                Logutil.d("1设备成功" + device.getName());
            }

            @Override
            public void onFail(File device, Status status) {
                Logutil.d("1设备失败" + status);
            }
        })
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        String header = ByteUtil.ByteArrToHex(bytes).trim();
                        singleCommand += header.trim().replace(" ", "");
                        //判断拼加的字符是否是单条指令
                        if (singleCommand.startsWith("FF") && singleCommand.length() == 8) {
                            //清除屏保
                            if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                                ActivityUtils.getTopActivity().finish();
                            }
                            //Log指令
                            Log.d("TAG", singleCommand);
                            if (singleCommand.equals("FF204A01")) {
                                App.startSpeaking("对讲挂断");
                            } else if (singleCommand.equals("FF205101")) {
                                App.startSpeaking("勤务上哨");
                            } else if (singleCommand.equals("FF205201")) {
                                App.startSpeaking("勤务下哨");
                            } else if (singleCommand.equals("FF203101")) {
                                App.startSpeaking("上级察勤");
                            } else if (singleCommand.equals("FF205301")) {
                                App.startSpeaking("本级察勤");
                            } else if (singleCommand.equals("FF203301")) {
                                App.startSpeaking("检查子弹");
                            } else if (singleCommand.equals("FF203401")) {
                                App.startSpeaking("申请供弹");
                            } else if (singleCommand.equals("FF208301")) {
                                App.startSpeaking("取消");
                            } else if (singleCommand.equals("FF204B01")) {
                                App.startSpeaking("呼叫上级");
                            } else if (singleCommand.equals("FF201001")) {
                                App.startSpeaking("应急报警");
                                sendAlarmToServer("应急");
                            } else if (singleCommand.equals("FF201101")) {
                                App.startSpeaking("脱逃报警");
                                sendAlarmToServer("脱逃");
                            } else if (singleCommand.equals("FF201201")) {
                                App.startSpeaking("暴狱报警");
                                sendAlarmToServer("暴狱");
                            } else if (singleCommand.equals("FF201301")) {
                                App.startSpeaking("袭击报警");
                                sendAlarmToServer("袭击");
                            } else if (singleCommand.equals("FF201401")) {
                                App.startSpeaking("自然报警");
                                sendAlarmToServer("自然");
                            } else if (singleCommand.equals("FF201501")) {
                                App.startSpeaking("挟持报警");
                                sendAlarmToServer("挟持");
                            } else if (singleCommand.equals("FF201601")) {
                                App.startSpeaking("突发报警");
                                sendAlarmToServer("突发");
                            }
                            singleCommand = "";
                        }
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                    }
                }).openSerialPort(keyBoardSerialPortDevice.getFile(), 9600);

        Logutil.d("键盘：" + keyBoardSerialPortDevice.toString() + "初始化--->>>" + openSerialPort);
        WriteLogToFile.info("键盘：" + keyBoardSerialPortDevice.toString() + "初始化--->>>" + openSerialPort);
    }


    private void sendAlarmToServer(String type) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendAlarmToServerThread thread = new SendAlarmToServerThread(type, new SendAlarmToServerThread.SendAlarmCallback() {
            @Override
            public void getCallbackData(String result) {
                Logutil.d("result-->>" + result);
                if (!TextUtils.isEmpty(result) && result.contains("100")) {
                    App.startSpeaking("报警发送成功");
                } else {
                    App.startSpeaking("报警发送异常");
                }
            }
        });
        new Thread(thread).start();
    }

    /**
     * 初始化语音播放参数
     */
    private void initializeParamater() {
        SpeechUtility.createUtility(this, "appid=5c2db3fb");
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        if (mTts == null) {
            Log.e("TAG", "实例化");
            return;
        }

        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, "purextts");
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "yifeng");


        mTts.setParameter(SpeechConstant.SPEED, "70");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "80");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }

    /**
     * 加载语音播放时的配置参数
     */
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/pureXtts_common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + "yifeng" + ".jet"));
        return tempBuffer.toString();
    }

    /**
     * 返回语音播放对象
     */
    public static SpeechSynthesizer getmTts() {
        return mTts;
    }

    /**
     * 语音播放
     */
    public static void startSpeaking(String str) {
        String content = "";

        for (int i = 0; i < str.length(); i++) {
            if (StringUtils.isChineseChar(str.charAt(i))) {
                content += str.charAt(i);
            } else {
                content += str.charAt(i) + " ";
            }
        }

        mTts.startSpeaking(content, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                Log.e("TAG", "开始播放");
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {
                Log.e("TAG", "");
            }

            @Override
            public void onSpeakPaused() {
                Log.e("TAG", "暂停播放");
            }

            @Override
            public void onSpeakResumed() {
                Log.e("TAG", "继续播放");
            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {
                Log.e("TAG", "");
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                Log.e("TAG", "播放完成");
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
                Log.e("TAG", "");
            }
        });
    }

    /**
     * 加载配置信息
     */
    private void loadConfigInfo() {

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

    /**
     * 初始化
     */
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

    /**
     * 启动服务
     */
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
        if (!ServiceUtil.isServiceRunning(InitSystemSettingService.class)) {
            ServiceUtil.startService(InitSystemSettingService.class);
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
