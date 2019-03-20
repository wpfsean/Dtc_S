package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.thread.SendAlarmToServerThread;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.SharedPreferencesUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.io.File;
import java.util.ArrayList;
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
                                keyList.clear();
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
                                keyList.clear();
                            } else if (singleCommand.equals("FF204B01")) {
                                //呼叫上级
                                sureMakeCall();
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
     * 呼叫上级
     */
    private void sureMakeCall() {
        String currnetKey = "";
        if (keyList != null && keyList.size() > 0) {
            for (int a : keyList) {
                currnetKey += a;
            }
            Logutil.d("currentKey" + currnetKey);
            if (currnetKey.length() <= 4) {
                App.startSpeaking("呼叫" + StringUtils.voiceConVersion(currnetKey) + "号哨");
            } else {
                App.startSpeaking("长度限制");
            }
            keyList.clear();
        } else {
            App.startSpeaking("呼叫上级");
        }
    }

    /**
     * 鸣枪警告
     */
    private void gunshootWarring() {
        App.startSpeaking("鸣枪警告");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RemoteVoiceOperatService.playVoice(R.raw.gunshoot);
    }

    /**
     * 语音警告
     */
    private void voiceWarring() {
        App.startSpeaking("语音警告");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RemoteVoiceOperatService.playVoice(R.raw.warning);
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
                WriteLogToFile.info("报警结果：类型--->>"+type+"<<<"+result);
                if (!TextUtils.isEmpty(result) && result.contains("100")) {
                    App.startSpeaking("报警发送成功");
                } else {
                    App.startSpeaking("报警发送异常");
                }
            }
        });
        new Thread(thread).start();
    }

}
