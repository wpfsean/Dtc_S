package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.OpenBoxParamater;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * 描述：接收开启子弹箱请求的服务
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/2/27 15:59
 */
public class ReceiveOpenBoxRequestService extends Service {

    /**
     * 接收tcp消息的子线程
     */
    ReceivingAmmoBoxThread  mReceivingAmmoBoxThread = null;

    /**
     * TcpSocketServer
     */
    ServerSocket serverSocket = null;

    /**
     * 所有sip字典集合
     */
    List<SipBean> allSipList;

    /**
     * 服务是否正在运行
     */
    boolean serviceIsStop = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceIsStop = true;

        //启动子线程执行socket服务
        if (mReceivingAmmoBoxThread == null)
            mReceivingAmmoBoxThread = new ReceivingAmmoBoxThread();
        new Thread(mReceivingAmmoBoxThread).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceIsStop = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    class ReceivingAmmoBoxThread extends Thread {
        @Override
        public void run() {
            try {
                //启动tcp服务
                if (serverSocket == null)
                    serverSocket = new ServerSocket(2000, 3);
                Logutil.e("服务开了");
                InputStream in = null;
                while (serviceIsStop) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        in = socket.getInputStream();
                        byte[] header = new byte[72];
                        int read = in.read(header);
                        //协议头
                        byte[] flagByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            flagByte[i] = header[i];
                        }
                        String flag = new String(flagByte, "utf-8");

                        //获取申请动作
                        byte[] actionByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            actionByte[i] = header[i + 8];
                        }
                        String action = actionByte[0] + "";
                        //获取发送者Ip
                        byte[] sendIpByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            sendIpByte[i] = header[i + 24];
                        }
                        String sendIp = sendIpByte[0] + "." + sendIpByte[1] + "." + sendIpByte[2] + "." + sendIpByte[3];
                        //获取弹箱的Guid
                        byte[] boxGuidByte = new byte[40];
                        for (int i = 0; i < 40; i++) {
                            boxGuidByte[i] = header[i + 28];
                        }
                        String boxGuid = new String(boxGuidByte, 0, ByteUtil.getPosiotion(boxGuidByte), "gb2312");
                        //设置参数
                        OpenBoxParamater openBoxParamater = new OpenBoxParamater();

                        openBoxParamater.setFalg(flag);
                        openBoxParamater.setVer("0001");
                        openBoxParamater.setAction(action);
                        openBoxParamater.setSendIp(sendIp);
                        openBoxParamater.setBoxId(boxGuid);

                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("openBoxParamater", openBoxParamater);
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);

                    } catch (IOException e) {
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logutil.e("接收申请供弹socket异常:" + e.getMessage());
            }
        }
    }

    /**
     * 处理供弹申请
     */
    private void handlerAmmoBox(OpenBoxParamater mOpenBoxParamater) {
        //获取本地的所有的sip字典
        try {
            if (allSipList == null)
                allSipList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
        } catch (Exception e) {
            //异常后注册广播用来接收sip缓存完成的通知
            allSipList = null;
        }
        //遍历设备名称
        String deviceName = "";
        if (allSipList != null && allSipList.size() > 0) {
            for (int i = 0; i < allSipList.size(); i++) {
                SipBean mSipBean = allSipList.get(i);
                if (mSipBean.getIpAddress().equals(mOpenBoxParamater.getSendIp())) {
                    deviceName = mSipBean.getName();
                }
            }
        }
        //判断是否比对到设备名称
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = mOpenBoxParamater.getSendIp() + "设备申请开启子弹箱！";
        } else {
            deviceName += "申请供弹";
        }
        //语音广播
        App.startSpeaking(deviceName);
        //广播，通知此报警
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(AppConfig.BOX_ACTION);
        alarmIntent.putExtra("box", mOpenBoxParamater);
        App.getApplication().sendBroadcast(alarmIntent);
        //清除屏保
        if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
            ActivityUtils.getTopActivity().finish();
        }
    }

    /**
     * Handler处理子线程发送过来 的数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    //接收对象
                    Bundle bundle = msg.getData();
                    OpenBoxParamater mOpenBoxParamater = (OpenBoxParamater) bundle.getSerializable("openBoxParamater");
                    //处理供弹信息
                    handlerAmmoBox(mOpenBoxParamater);
                    break;
            }
        }
    };
}