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
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.RecordLog;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 描述：接收报警的服务
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/26 14:11
 */

public class ReceiverAlarmService extends Service {

    /**
     * 接收报警消息的子线程
     */
    ReceivingAlarmThread mReceivingAlarmThread = null;

    /**
     * TcpSocketServer
     */
    ServerSocket serverSocket = null;

    /**
     * 此服务是否正在运行标识
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
        if (mReceivingAlarmThread == null)
            mReceivingAlarmThread = new ReceivingAlarmThread();
        new Thread(mReceivingAlarmThread).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //重置标识
        serviceIsStop = false;
        //关闭tcpServer服务
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
        //移除 Handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    /**
     * 接收友邻哨报警
     */
    class ReceivingAlarmThread extends Thread {
        @Override
        public void run() {
            try {
                //启动tcp服务
                if (serverSocket == null)
                    serverSocket = new ServerSocket(SysinfoUtils.getSysinfo().getNeighborWatchPort(), 3);
                InputStream in = null;
                while (serviceIsStop) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        in = socket.getInputStream();

                        byte[] header = new byte[524];
                        int read = in.read(header);
                        //获取报警报文数据头
                        byte[] flageByte = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            flageByte[i] = header[i];
                        }
                        String flag = new String(flageByte, "gb2312");

                        AlarmVideoSource alarmVideoSource = new AlarmVideoSource();
                        //获取报警发送者
                        byte[] senderByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            senderByte[i] = header[i + 4];
                        }
                        int senderP = ByteUtil.getPosiotion(senderByte);
                        String sender = new String(senderByte, 0, senderP, "gb2312");


                        byte[] videoIdByte = new byte[48];
                        for (int i = 0; i < 48; i++) {
                            videoIdByte[i] = header[i + 40];
                        }
                        int videoIdPosition = ByteUtil.getPosiotion(videoIdByte);

                        alarmVideoSource.setSenderIp(sender);
                        alarmVideoSource.setFaceVideoId(new String(videoIdByte, 0, videoIdPosition, "gb2312"));
                        Logutil.i("sender-->>>" + sender);
                        //视频源名称
                        byte[] videoNameByte = new byte[128];
                        for (int i = 0; i < 128; i++) {
                            videoNameByte[i] = header[i + 88];
                        }
                        int videoNameP = ByteUtil.getPosiotion(videoNameByte);
                        String videoName = new String(videoNameByte, 0, videoNameP, "gb2312");
                        Logutil.i("videoName-->>>" + videoName);
                        alarmVideoSource.setFaceVideoName(videoName);
                        //报警类型
                        byte[] alarmTypeByte = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            alarmTypeByte[i] = header[i + 460];
                        }
                        int alarmTypeP = ByteUtil.getPosiotion(alarmTypeByte);
                        String alarmType = new String(alarmTypeByte, 0, alarmTypeP, "gb2312");
                        Logutil.i("alarmType-->>>" + alarmType);
                        alarmVideoSource.setAlarmType(alarmType);

                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("AlarmVideoSource", alarmVideoSource);
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
                Logutil.e("接收友邻哨报警socket异常:" + e.getMessage());
            }
        }
    }

    /**
     * 处理报警信息
     */
    private void handlerAlarm(AlarmVideoSource mAlarmVideoSource) {
        //Log
        Logutil.d("alarm-->>" + mAlarmVideoSource.toString());
        //此记录写入file供别人参考(SD卡)
        RecordLog.wirteLog(mAlarmVideoSource.toString() + "发生报警");
        //广播，通知此报警
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(AppConfig.ALARM_ACTION);
        alarmIntent.putExtra("alarm", mAlarmVideoSource);
        App.getApplication().sendBroadcast(alarmIntent);
        //判断这个报警是否已处理（参考,需要改）
        if (TextUtils.isEmpty(mAlarmVideoSource.getFaceVideoId())) {
            //App.startSpeaking("值班室关闭报警");
        } else {
            //播报报警
            String speakSomething = mAlarmVideoSource.getFaceVideoName() + "发生" + mAlarmVideoSource.getAlarmType() + "报警";
            App.startSpeaking(speakSomething);
            //报警记录写入数据库，方便记录的历史记录依据
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", TimeUtils.getCurrentTime());
            contentValues.put("senderIp", mAlarmVideoSource.getSenderIp());
            contentValues.put("faceVideoId", mAlarmVideoSource.getFaceVideoId());
            contentValues.put("faceVideoName", mAlarmVideoSource.getFaceVideoName());
            contentValues.put("alarmType", mAlarmVideoSource.getAlarmType());
            contentValues.put("isHandler", "否");
            new DbUtils(App.getApplication()).insert(DbHelper.TAB_NAME, contentValues);
            //清除屏保
            if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                ActivityUtils.getTopActivity().finish();
            }
            //发送广播刷新副屏的事件列表和已处理的报警列表
            App.getApplication().sendBroadcast(new Intent(AppConfig.REFRESH_ACTION));
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
                    //接收到的报警消息（封装实体）
                    Bundle bundle = msg.getData();
                    AlarmVideoSource mAlarmVideoSource = (AlarmVideoSource) bundle.getSerializable("AlarmVideoSource");
                    //处理报警信息
                    handlerAlarm(mAlarmVideoSource);
                    break;
            }
        }
    };
}