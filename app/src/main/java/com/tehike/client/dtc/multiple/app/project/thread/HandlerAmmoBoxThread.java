package com.tehike.client.dtc.multiple.app.project.thread;

import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.entity.OpenBoxParamater;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 描述：向服务器转发处理供弹功能
 * 同意供弹
 * 拒绝供弹
 * 全部供弹
 * 全部拒绝
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/22 9:27
 */


public class HandlerAmmoBoxThread extends Thread {

    /**
     * 打开弹箱对象
     */
    OpenBoxParamater mOpenBoxParamater;

    /**
     * 执行的动作
     * 0-请求
     * 1-同意
     * 2-拒绝
     * 3-直接开启
     */
    int action;

    /**
     * 转供弹信息的服务器Ip
     */
    String requestOpenAmmoBoxServiceIp = "";

    /**
     * 转供弹信息的服务器端口
     */
    int requestOpenAmmoBoxServicePort = -1;

    /**
     * 本机Ip
     */
    String nativeIp = "";

    /**
     * 构造方法
     */
    public HandlerAmmoBoxThread(OpenBoxParamater mOpenBoxParamater, int action) {
        this.mOpenBoxParamater = mOpenBoxParamater;
        this.action = action;
    }

    @Override
    public void run() {
        //服务器地址
        requestOpenAmmoBoxServiceIp = SysinfoUtils.getSysinfo().getWebresourceServer();
        //服务器端口
        requestOpenAmmoBoxServicePort = SysinfoUtils.getSysinfo().getAlertPort();
        //判断地址
        if (TextUtils.isEmpty(requestOpenAmmoBoxServiceIp) || requestOpenAmmoBoxServicePort == -1) {
            Logutil.e("处理供弹要求时未知服务器信息");
            WriteLogToFile.info("处理供弹要求时未知服务器信息");
            return;
        }
        //获取本机Ip
        if (NetworkUtils.isConnected())
            nativeIp = NetworkUtils.getIPAddress(true);
        //要发送的数据
        byte[] sendData = new byte[72];
        // 数据头
        byte[] flag = mOpenBoxParamater.getFalg().getBytes();
        System.arraycopy(flag, 0, sendData, 0, 4);
        // 版本号
        byte[] version = new byte[4];
        version[0] = 0;
        version[1] = 0;
        version[2] = 0;
        version[3] = 1;
        System.arraycopy(version, 0, sendData, 4, 4);
        // 动作， 0-请求，1-同意，2-拒绝，3-直接开启
        sendData[9] = (byte) action;
        sendData[10] = 0;
        sendData[11] = 0;
        sendData[12] = 0;

        // 随机申请 码

        // uiAction = 0, 保存设备端随机生成的申请码
        byte[] requestCode = new byte[4];

        // uiAction = 0, 保存设备端的SALT
        byte[] requestSalt = new byte[4];
        // uiAction = 1, 保存服务端根据申请码计算得到的开锁码
        byte[] responseCode = new byte[4];

        System.arraycopy(requestCode, 0, sendData, 12, 4);
        System.arraycopy(requestSalt, 0, sendData, 16, 4);
        System.arraycopy(responseCode, 0, sendData, 20, 4);

        //测试（有问题）
        String[] ip = nativeIp.split("\\.");
        byte[] senderIP = new byte[4];
        senderIP[0] = (byte) Integer.parseInt(ip[0]);
        senderIP[1] = (byte) Integer.parseInt(ip[1]);
        senderIP[2] = (byte) Integer.parseInt(ip[2]);
        senderIP[3] = (byte) Integer.parseInt(ip[3]);
        System.arraycopy(senderIP, 0, sendData, 24, 4);

        //
        byte[] senderID = mOpenBoxParamater.getBoxId().getBytes();
        System.arraycopy(senderID, 0, sendData, 28, senderID.length);

        Socket socket = null;
        OutputStream os = null;
        try {
            // 测试
            socket = new Socket(requestOpenAmmoBoxServiceIp, requestOpenAmmoBoxServicePort);
            os = socket.getOutputStream();
            os.write(sendData);
            os.flush();
            WriteLogToFile.info(mOpenBoxParamater.toString() + "---Action---" + action);
            Logutil.d("处理供弹发送成功");
        } catch (IOException e) {
            Logutil.e("处理供弹发送异常--->>" + e.getMessage());
            WriteLogToFile.info("处理供弹发送异常--->>" + e.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}