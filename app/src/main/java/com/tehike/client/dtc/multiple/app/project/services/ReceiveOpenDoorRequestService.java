package com.tehike.client.dtc.multiple.app.project.services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.OpenBoxParamater;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.ui.display.SecondDisplayActivity;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.ScreenUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import cn.nodemedia.NodePlayer;

/**
 * 描述：用于接收开门申请的服务
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/21 14:03
 */

public class ReceiveOpenDoorRequestService extends Service {

    /**
     * 接收tcp消息的子线程
     */
    ReceivingOpenDoorThread mReceivingOpenDoorThread = null;

    /**
     * TcpSocketServer
     */
    ServerSocket serverSocket = null;

    /**
     * 服务是否正在运行
     */
    boolean serviceIsStop = false;

    /**
     * 广播，关闭弹窗
     */
    ReceiveCloseDialogBroadcast mReceiveCloseDialogBroadcast;

    /**
     * 弹窗对象
     */
    Dialog dialog;

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
        if (mReceivingOpenDoorThread == null)
            mReceivingOpenDoorThread = new ReceivingOpenDoorThread();
        new Thread(mReceivingOpenDoorThread).start();

        //注册广播，接收开门消息
        registerDismissDialogBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止服务标识
        serviceIsStop = false;
        //关闭tcp服务
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
        //销毁广播
        if (mReceiveCloseDialogBroadcast != null)
            App.getApplication().unregisterReceiver(mReceiveCloseDialogBroadcast);
        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    /**
     * 子线程接收申请开门消息服务
     */
    class ReceivingOpenDoorThread extends Thread {
        @Override
        public void run() {
            try {
                //启动tcp服务
                if (serverSocket == null)
                    serverSocket = new ServerSocket(2001, 3);
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
                        String flag = new String(flagByte, "gb2312");

                        if (!TextUtils.isEmpty(flag)) {
                            if (flag.equals("ReqD")) {
                                handler.sendEmptyMessage(1);
                            } else {
                                Logutil.e("开门申请不匹配");
                            }
                        } else {
                            Logutil.e("开门申请不匹配");
                        }

                        Logutil.d(Arrays.toString(flagByte));
                        Logutil.d(flag);

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
                Logutil.e("接收开门申请socket异常:" + e.getMessage());
            }
        }
    }

    /**
     * 显示申请开门信息
     */
    private void disPlayRequestOpenDoorInfor() {

        AppConfig.IS_REQUEST_DOOR = true;
        //用dialog显示
        AlertDialog.Builder builder = new AlertDialog.Builder(App.getApplication());
        builder.setCancelable(false);

        final View view = View.inflate(App.getApplication(), R.layout.dialig_display_request_open_door_infor_item, null);
        builder.setView(view);
        //显示dialog
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        //通过当前的dialog获取window对象
        Window window = dialog.getWindow();
        //设置背景，防止变形
        window.setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ScreenUtils.getInstance(App.getApplication()).getWidth() - 44;//两边设置的间隙相当于margin
        lp.alpha = 0.9f;
        window.setDimAmount(0.5f);//使用时设置窗口后面的暗淡量
        window.setAttributes(lp);
        //同意开门申请
        view.findViewById(R.id.accpet_open_door_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    App.startSpeaking("同意开门");
                    AppConfig.IS_REQUEST_DOOR = false;
                }
            }
        });
        //拒绝开门申请
        view.findViewById(R.id.reject_open_door_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    App.startSpeaking("拒绝开门");
                    AppConfig.IS_REQUEST_DOOR = false;
                }
            }
        });
    }

    /**
     * 注册接收消失弹窗的广播
     */
    private void registerDismissDialogBroadcast() {
        mReceiveCloseDialogBroadcast = new ReceiveCloseDialogBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.REQUEST_DOOR_CLOSE_DIALOG_ACTION);
        App.getApplication().registerReceiver(mReceiveCloseDialogBroadcast, intentFilter);
    }

    /**
     * 广播接收申请开箱信息
     */
    class ReceiveCloseDialogBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            handler.sendEmptyMessage(2);
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
                    //清除屏保
                    if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                        ActivityUtils.getTopActivity().finish();
                    }
                    //语音播报
                    App.startSpeaking("有人申请开门");
                    //写入数据库记录
                    KeyBoardService.eventRecord("有人申请开门");
                    //发送广播刷新副屏的事件列表和已处理的报警列表
                    App.getApplication().sendBroadcast(new Intent(AppConfig.REFRESH_ACTION));
                    //展示弹窗
                    disPlayRequestOpenDoorInfor();
                    break;
                case 2:
                    //消除弹窗
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
            }
        }
    };

}