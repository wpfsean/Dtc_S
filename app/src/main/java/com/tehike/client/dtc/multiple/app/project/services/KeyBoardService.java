package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.phone.SipManager;
import com.tehike.client.dtc.multiple.app.project.phone.SipUtils;
import com.tehike.client.dtc.multiple.app.project.thread.SendAlarmToServerThread;
import com.tehike.client.dtc.multiple.app.project.ui.display.SecondDisplayActivity;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.IntercomCallFragment;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.G711Utils;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.RemoteVoiceRequestUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SharedPreferencesUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述：键盘操作服务
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/20 10:29
 */


public class KeyBoardService extends Service {

    /**
     * 键盘单条指令
     */
    String singleCommand = "";

    /**
     * 键盘串口对象
     */
    Device keyBoardSerialPortDevice = null;

    /**
     * 串口管理
     */
    SerialPortManager mSerialPortManager;

    /**
     * 盛放键值的集合
     */
    List<Integer> keyList = new ArrayList<>();

    /**
     * 用于远程喊话请求的Socket
     */
    Socket tcpClientSocket = null;

    /**
     * 用于发送喊话的udp消息的端口
     */
    int remoterSpeakingPort = -1;

    /**
     * 是否正在自主喊话的标识
     */
    boolean isRemoteSpeaking = false;

    /**
     * 录音时声音缓存大小
     */
    private int rBufferSize;

    /**
     * 录音对象
     */
    private AudioRecord recorder;

    /**
     * 声音采样率
     */
    public int frequency = 16000;

    /**
     * 停止标识
     */
    private boolean stopRecordingFlag = false;

    /**
     * 发送声音数据的Udp
     */
    DatagramSocket udpSocket = null;

    /**
     * 自主喊话对象的Ip
     */
    String remoteIp = "";

    /**
     * 是否正在通话
     */
    boolean isCalling = false;

    /**
     * 录音线程
     */
    private RecordingVoiceThread mRecordingVoiceThread;

    @Override
    public void onCreate() {
        Logutil.d("KeyBoardService start");
        //串口管理类
        mSerialPortManager = new SerialPortManager();
        //初始化键盘
        initKeyBoard();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //关闭键盘串口
        if (mSerialPortManager != null)
            mSerialPortManager.closeSerialPort();
        //清空键值集合
        if (keyList != null)
            keyList.clear();
        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化键盘（测试）
     * ttyACM2
     */
    private void initKeyBoard() {
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
                //Logutil.d("1设备成功" + device.getName());
            }

            @Override
            public void onFail(File device, OnOpenSerialPortListener.Status status) {
                Logutil.d("1设备失败" + status);
            }
        })
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
                                //功能取消
                                functionCancel();
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
                            } else if (singleCommand.equals("FF202401")) {
                                App.startSpeaking("远程喊话");
                            } else if (singleCommand.equals("FF202301")) {
                                //鸣枪警告
                                gunshootWarring();
                            } else if (singleCommand.equals("FF202201")) {
                                //语音警告
                                voiceWarring();
                            } else if (singleCommand.equals("FF204101")) {
                                //1
                                keyList.add(1);
                            } else if (singleCommand.equals("FF204201")) {
                                //2
                                keyList.add(2);
                            } else if (singleCommand.equals("FF204301")) {
                                //3
                                keyList.add(3);
                            } else if (singleCommand.equals("FF204401")) {
                                //4
                                keyList.add(4);
                            } else if (singleCommand.equals("FF204501")) {
                                //5
                                keyList.add(5);
                            } else if (singleCommand.equals("FF204601")) {
                                //6
                                keyList.add(6);
                            } else if (singleCommand.equals("FF204701")) {
                                //7
                                keyList.add(7);
                            } else if (singleCommand.equals("FF204801")) {
                                //8
                                keyList.add(8);
                            } else if (singleCommand.equals("FF204901")) {
                                //9
                                keyList.add(9);
                            } else if (singleCommand.equals("FF204001")) {
                                //0
                                keyList.add(0);
                            } else if (singleCommand.equals("FF208301")) {
                                App.startSpeaking("取消");
                                clearKeyList();

                                LinkedList<AlarmVideoSource> a = SecondDisplayActivity.alarmQueueList;

                                Logutil.d(a.toString());


                            } else if (singleCommand.equals("FF204B01")) {
                                //呼叫上级
                                sureMakeCall();
                            } else if (singleCommand.equals("FF202101")) {
                                //自主喊话
                                remoteSpeaking();
                            }
                            //重置当前指令
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

    /**
     * 功能取消
     */
    private void functionCancel() {
        //判断是否正在自主喊话
        if (isRemoteSpeaking) {
            App.startSpeaking("自主喊话已挂断");
            isRemoteSpeaking = false;
            try {
                stopRecord();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isCalling) {
            //判断是否正在通话
            App.startSpeaking("呼叫已挂断");
            isCalling = false;
            SipManager.getLc().terminateAllCalls();
        } else {
            //未操作
            App.startSpeaking("取消");
        }
        //清空指令集合
        clearKeyList();
    }

    /**
     * 呼叫上级
     */
    private void sureMakeCall() {

        //判断当前是否正在通话中（挂断）
        if (isCalling) {
            isCalling = false;
            SipManager.getLc().terminateAllCalls();
        }
        //延时一秒后执行下一步
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Logutil.w("Key:--->>>" + returnCurrentKey());
        //判断是否指定对象
        if (!TextUtils.isEmpty(returnCurrentKey())) {

            SipBean mSipBean = returnCurrentSipBean(returnCurrentKey());
            if (mSipBean == null) {
                App.startSpeaking("未找到操作对象");
                //清空键值集合
                clearKeyList();
            } else {
                Logutil.d("AA-->>" + mSipBean.toString());
                //判断网络是否正常
                if (!NetworkUtils.isConnected()) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                //得到当前的sip号码
                String sipNumber = mSipBean.getNumber();
                //判断是否为空
                if (TextUtils.isEmpty(sipNumber)) {
                    Logutil.e("SipNumber is :" + sipNumber);
                    return;
                }
                Logutil.d("SipNumber--->>" + sipNumber);
                App.startSpeaking("正在呼叫" + mSipBean.getSentryId() + "号哨");


                isCalling = true;

                Linphone.callTo(sipNumber, false);

                //清空键值集合
                clearKeyList();
            }

        } else {
            App.startSpeaking("呼叫上级");
            //清空键值集合
            clearKeyList();
        }

    }

    /**
     * 鸣枪警告
     */
    private void gunshootWarring() {
        Logutil.w("Key:--->>>" + returnCurrentKey());
        //判断是否指定对象
        if (!TextUtils.isEmpty(returnCurrentKey())) {

            SipBean mSipBean = returnCurrentSipBean(returnCurrentKey());
            if (mSipBean == null) {
                App.startSpeaking("未找到操作对象");
                //清空键值集合
                clearKeyList();
            } else {
                Logutil.d("AA-->>" + mSipBean.toString());
                //判断网络是否正常
                if (!NetworkUtils.isConnected()) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                String remoteIp = mSipBean.getIpAddress();
                if (TextUtils.isEmpty(remoteIp)) {
                    Logutil.e("RemoteIp is null");
                    return;
                }
                //子线程远程警告
                RemoteVoiceRequestUtils remoteVoiceRequestUtils = new RemoteVoiceRequestUtils(3, remoteIp, new RemoteVoiceRequestUtils.RemoteCallbck() {
                    @Override
                    public void remoteStatus(String status) {
                        if (TextUtils.isEmpty(status) || status.contains("error")) {
                            handler.sendEmptyMessage(2);
                            return;
                        }
                        Message message = new Message();
                        message.what = 3;
                        message.obj = status;
                        handler.sendMessage(message);
                    }
                });
                new Thread(remoteVoiceRequestUtils).start();
                //清空键值集合
                clearKeyList();
            }

        } else {
            //本身操作
            App.startSpeaking("鸣枪警告");
            //延时
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //本身鸣枪
            RemoteVoiceOperatService.playVoice(R.raw.gunshoot);
            //清空键值集合
            clearKeyList();
        }
    }

    /**
     * 语音警告
     */
    private void voiceWarring() {
        Logutil.w("Key:--->>>" + returnCurrentKey());
        //判断是否指定对象
        if (!TextUtils.isEmpty(returnCurrentKey())) {

            SipBean mSipBean = returnCurrentSipBean(returnCurrentKey());
            if (mSipBean == null) {
                App.startSpeaking("未找到操作对象");
                //清空键值集合
                clearKeyList();
            } else {
                Logutil.d("AA-->>" + mSipBean.toString());
                //判断网络是否正常
                if (!NetworkUtils.isConnected()) {
                    Logutil.e("鸣枪警告时网络异常");
                    handler.sendEmptyMessage(1);
                    return;
                }
                String remoteIp = mSipBean.getIpAddress();
                if (TextUtils.isEmpty(remoteIp)) {
                    Logutil.e("RemoteIp is null");
                    return;
                }
                //子线程远程警告
                RemoteVoiceRequestUtils remoteVoiceRequestUtils = new RemoteVoiceRequestUtils(2, remoteIp, new RemoteVoiceRequestUtils.RemoteCallbck() {
                    @Override
                    public void remoteStatus(String status) {
                        if (TextUtils.isEmpty(status) || status.contains("error")) {
                            handler.sendEmptyMessage(5);
                            return;
                        }
                        Message message = new Message();
                        message.what = 6;
                        message.obj = status;
                        handler.sendMessage(message);
                    }
                });
                new Thread(remoteVoiceRequestUtils).start();
                //清空键值集合
                clearKeyList();
            }

        } else {
            //本身操作
            App.startSpeaking("鸣枪警告");
            //延时
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //本身鸣枪
            RemoteVoiceOperatService.playVoice(R.raw.warning);
            //清空键值集合
            clearKeyList();
        }
    }

    /**
     * 自主喊话
     */
    private void remoteSpeaking() {
        if (!TextUtils.isEmpty(returnCurrentKey())) {

            SipBean mSipBean = returnCurrentSipBean(returnCurrentKey());
            if (mSipBean == null) {
                App.startSpeaking("未找到操作对象");
                //清空键值集合
                clearKeyList();
            } else {
                Logutil.d("AA-->>" + mSipBean.toString());
                //判断网络是否正常
                if (!NetworkUtils.isConnected()) {
                    Logutil.e("自主喊话时网络异常");
                    handler.sendEmptyMessage(1);
                    return;
                }
                //喊话对象的Ip
                remoteIp = mSipBean.getIpAddress();
                if (TextUtils.isEmpty(remoteIp)) {
                    Logutil.e("RemoteIp is null");
                    return;
                }
                //子线云建立喊话tcp连接
                RequestSpeakingSocket sendSoundData = new RequestSpeakingSocket(remoteIp);
                new Thread(sendSoundData).start();
                //清空键值集合
                clearKeyList();
            }

        } else {
            //本身操作
            App.startSpeaking("自主喊话");
            //清空键值集合
            clearKeyList();
        }
    }

    /**
     * 发送报警
     */
    private void sendAlarmToServer(final String type) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendAlarmToServerThread thread = new SendAlarmToServerThread(type, new SendAlarmToServerThread.SendAlarmCallback() {
            @Override
            public void getCallbackData(String result) {
                Logutil.d("result-->>" + result);
                WriteLogToFile.info("报警结果：类型--->>" + type + "<<<" + result);
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
     * 得到当前的指令集合
     */
    private String returnCurrentKey() {
        String currnetKey = "";
        if (keyList != null && keyList.size() > 0) {
            for (int a : keyList) {
                currnetKey += a;
            }
        }
        return currnetKey;
    }

    /**
     * 获取当前的操作对象
     */
    private SipBean returnCurrentSipBean(String str) {
        SipBean mSipBean = null;
        try {
            List<SipBean> allSipList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            if (allSipList != null && allSipList.size() > 0) {
                for (SipBean bean : allSipList) {
                    if (bean.getSentryId().equals(str)) {
                        mSipBean = bean;
                        return mSipBean;
                    }
                }
            } else {
                return mSipBean;
            }
        } catch (Exception e) {
            return mSipBean;
        }
        return mSipBean;
    }

    /**
     * 清除指令集合
     */
    private void clearKeyList() {
        if (keyList != null) {
            keyList.clear();
        }
    }

    /**
     * 用于远程喊话请求的子线程
     */
    class RequestSpeakingSocket extends Thread {
        //远程喊话对象的Ip
        String remoteIp;

        //构造方法
        public RequestSpeakingSocket(String remoteIp) {
            this.remoteIp = remoteIp;
        }

        @Override
        public void run() {
            try {
                if (tcpClientSocket == null) {
                    //创建tcp请求
                    tcpClientSocket = new Socket(remoteIp, AppConfig.REMOTE_PORT);
                    //设置请求超时
                    tcpClientSocket.setSoTimeout(3 * 1000);
                    //请求的总数据
                    byte[] requestData = new byte[4 + 4 + 4 + 4];
                    // flag
                    byte[] flag = new byte[4];
                    flag = "RVRD".getBytes();
                    System.arraycopy(flag, 0, requestData, 0, flag.length);

                    // action
                    byte[] action = new byte[4];
                    action[0] = 1;// 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
                    action[1] = 0;
                    action[2] = 0;
                    action[3] = 0;
                    System.arraycopy(action, 0, requestData, 4, action.length);

                    // 接受喊话时=接收语音数据包的 UDP端口(测试)
                    byte[] parameter = new byte[4];
                    System.arraycopy(parameter, 0, requestData, 8, parameter.length);
                    // // 向服务器发消息
                    OutputStream os = tcpClientSocket.getOutputStream();// 字节输出流
                    os.write(requestData);
                    //   tcpSocket.shutdownOutput();// 关闭输出流
                    // 读取服务器返回的消息
                    InputStream in = tcpClientSocket.getInputStream();
                    byte[] data = new byte[20];
                    int read = in.read(data);
                    //   System.out.println("返回的數據" + Arrays.toString(data));
                    // 解析数据头
                    byte[] r_flag = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_flag[i] = data[i];
                    }
                    String r_DataFlag = new String(r_flag, "gb2312");
                    //     System.out.println("數據頭:" + new String(r_flag, "gb2312"));
                    // 解析返回的請求
                    byte[] r_quest = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_quest[i] = data[i + 4];
                    }
                    // 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
                    int r_questCode = r_quest[0];
                    String r_questMess = RemoteVoiceRequestUtils.getMessage(r_questCode);

                    // 返回的状态
                    byte[] r_status = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_status[i] = data[i + 8];
                    }
                    int r_statusCode = r_status[0];
                    String r_statusMess = RemoteVoiceRequestUtils.getStatusMessage(r_statusCode);
                    Logutil.i("应答状态:" + r_statusCode + "\t" + r_statusMess);

                    // 返回参数
                    byte[] r_paramater = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_paramater[i] = data[i + 12];
                    }
                    Logutil.i(Arrays.toString(r_paramater));
                    int port = ByteUtil.bytesToInt(r_paramater, 0);

                    if (r_statusMess.equals("Accept")) {
                        Message message = new Message();
                        message.arg1 = port;
                        message.what = 7;
                        handler.sendMessage(message);
                        Logutil.d("喊话请求同意");
                    } else {
                        handler.sendEmptyMessage(8);
                        Logutil.d("喊话请求拒绝");
                        if (tcpClientSocket != null) {
                            tcpClientSocket.close();
                            tcpClientSocket = null;
                        }
                    }
                }
            } catch (Exception e) {
                if (tcpClientSocket != null) {
                    try {
                        tcpClientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    tcpClientSocket = null;
                    isRemoteSpeaking = false;
                }
                handler.sendEmptyMessage(8);
                Logutil.e("error:" + e.getMessage());
            }
        }
    }

    /**
     * 初始化录音 参数
     */
    public void initializeRecordParamater() {
        try {
            //设置录音缓冲区大小
            rBufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //获取录音机对象
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, rBufferSize);
        } catch (Exception e) {
            String msg = "ERROR init: " + e.getStackTrace();
            Logutil.e("error:" + msg);
        }
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        //更改停止录音标识
        stopRecordingFlag = false;
        //开启录音线程
        mRecordingVoiceThread = new RecordingVoiceThread();
        mRecordingVoiceThread.start();
    }

    /**
     * 结束录音
     */
    public void stopRecord() throws IOException {

        //Tcp断开连接
        if (tcpClientSocket != null) {
            tcpClientSocket.close();
            tcpClientSocket = null;
        }
        //Udp断开连接
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        //更改停止标识
        stopRecordingFlag = true;
    }

    /**
     * 录音线程
     */
    class RecordingVoiceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                byte[] tempBuffer, readBuffer = new byte[rBufferSize];
                int bufResult = 0;
                recorder.startRecording();
                while (!stopRecordingFlag) {
                    bufResult = recorder.read(readBuffer, 0, rBufferSize);
                    if (bufResult > 0 && bufResult % 2 == 0) {
                        tempBuffer = new byte[bufResult];
                        System.arraycopy(readBuffer, 0, tempBuffer, 0, rBufferSize);
                        G711EncodeVoice(tempBuffer);
                    }
                }
                recorder.stop();
                Looper.prepare();
                Looper.loop();
            } catch (Exception e) {
                String msg = "ERROR AudioRecord: " + e.getMessage();
                Logutil.e(msg);
                Looper.prepare();
                Looper.loop();
            }
        }
    }

    /**
     * G711a声音压缩
     */
    private void G711EncodeVoice(byte[] tempBuffer) {
        DatagramPacket dp = null;
        try {
            dp = new DatagramPacket(G711Utils.encode(tempBuffer), G711Utils.encode(tempBuffer).length, InetAddress.getByName(remoteIp), remoterSpeakingPort);
            try {
                if (udpSocket == null)
                    udpSocket = new DatagramSocket();
                udpSocket.send(dp);
                Logutil.i("正在发送...." + Arrays.toString(G711Utils.encode(tempBuffer)) + "\n长度" + G711Utils.encode(tempBuffer).length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启自主喊话
     */
    private void startRemoteSpeaking() {
        Logutil.d("remoterSpeakingPort---->>>" + remoterSpeakingPort);
        isRemoteSpeaking = true;
        App.startSpeaking("自主喊话已开启");
        initializeRecordParamater();
        startRecord();
    }

    /**
     * handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //播告网络异常
                    App.startSpeaking("网络异常");
                    break;
                case 2:
                    //Log鸣枪警告时操作异常
                    Logutil.e("鸣枪警告时操作异常");
                    break;
                case 3:
                    //提示鸣枪警告结果
                    String gunshootResult = (String) msg.obj;
                    Logutil.d("gunshootResult--->>" + gunshootResult);
                    if (gunshootResult.equals("Done")) {
                        App.startSpeaking("鸣枪警告成功");
                    } else if (gunshootResult.equals("Reject")) {
                        App.startSpeaking("鸣枪警告被拒绝");
                    } else if (gunshootResult.equals("Busy")) {
                        App.startSpeaking("对方忙");
                    }
                    break;
                case 5:
                    //Log远程警告时操作异常
                    Logutil.e("远程警告时操作异常");
                    break;
                case 6:
                    //提示远程警告结果
                    String warringResult = (String) msg.obj;
                    Logutil.d("warringResult--->>" + warringResult);
                    if (warringResult.equals("Done")) {
                        App.startSpeaking("远程警告成功");
                    } else if (warringResult.equals("Reject")) {
                        App.startSpeaking("远程警告被拒绝");
                    } else if (warringResult.equals("Busy")) {
                        App.startSpeaking("对方忙");
                    }
                    break;
                case 7:
                    //自主喊话同意
                    remoterSpeakingPort = msg.arg1;
                    startRemoteSpeaking();
                    break;
                case 8:
                    //自主喊话拒绝
                    isRemoteSpeaking = false;
                    break;
            }
        }
    };
}
