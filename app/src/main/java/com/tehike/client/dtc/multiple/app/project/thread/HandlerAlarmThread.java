package com.tehike.client.dtc.multiple.app.project.thread;

import android.text.TextUtils;
import android.util.Log;

import com.tehike.client.dtc.multiple.app.project.entity.OpenBoxParamater;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 描述：处理报警信息
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/22 9:59
 */


public class HandlerAlarmThread extends Thread {

    /**
     * 转供弹信息的服务器Ip
     */
    String requestOpenAmmoBoxServiceIp = "";

    /**
     * 转供弹信息的服务器端口
     */
    int requestOpenAmmoBoxServicePort = -1;

    /**
     * 触发报警的Ip
     */
    String sendAlarmIp;


    public HandlerAlarmThread(String sendAlarmIp) {
        this.sendAlarmIp = sendAlarmIp;
    }

    @Override
    public void run() {
        //服务器地址
        requestOpenAmmoBoxServiceIp = SysinfoUtils.getSysinfo().getWebresourceServer();
        //服务器端口
        requestOpenAmmoBoxServicePort = SysinfoUtils.getSysinfo().getAlertPort();
        //判断地址
        if (TextUtils.isEmpty(requestOpenAmmoBoxServiceIp) || requestOpenAmmoBoxServicePort == -1) {
            Logutil.e("处理报警功能时未知服务器信息");
            WriteLogToFile.info("处理报警功能时未知服务器信息");
            return;
        }
        //关闭报警的数据
        byte[] closeAlarmData = new byte[80];

        //拼加数据头
        byte[] flag = "cmsg".getBytes();
        System.arraycopy(flag, 0, closeAlarmData, 0, flag.length);
        //消息类型
        byte[] type = new byte[4];
        type[0] = 1;
        type[1] = 0;
        type[2] = 0;
        type[3] = 0;
        System.arraycopy(type, 0, closeAlarmData, 4, type.length);
        //目标
        byte[] tage = new byte[4];
        tage[0] = 1;
        tage[1] = 0;
        tage[2] = 0;
        tage[3] = 0;
        System.arraycopy(tage, 0, closeAlarmData, 8, tage.length);

        //触发报警的Ip
        byte[] iPAddress = new byte[32];
        byte[] iPAddress1 = sendAlarmIp.getBytes();
        System.arraycopy(iPAddress1, 0, iPAddress, 0, iPAddress1.length);
        System.arraycopy(iPAddress, 0, closeAlarmData, 16, 32);

        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getByName(requestOpenAmmoBoxServiceIp), requestOpenAmmoBoxServicePort);
            OutputStream os = socket.getOutputStream();
            os.write(closeAlarmData);
            os.flush();
            Logutil.d("处理" + sendAlarmIp + "发送的报警转发成功");
            WriteLogToFile.info("处理" + sendAlarmIp + "发送的报警转发成功");
        } catch (IOException e) {
            System.err.println("error:" + e.getMessage());
            Logutil.e("处理供弹发送异常--->>" + e.getMessage());
            WriteLogToFile.info("处理" + sendAlarmIp + "发送的报警转发异常-->>" + e.getMessage());
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
}