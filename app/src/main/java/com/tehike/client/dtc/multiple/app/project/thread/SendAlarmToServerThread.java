package com.tehike.client.dtc.multiple.app.project.thread;

import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;

/**
 * 描述：向服務器發送報警的子線程
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/19 13:32
 */

public class SendAlarmToServerThread extends Thread {

    /**
     * 报警类型
     */
    String type;

    /**
     * 报警结果回调
     */
    SendAlarmCallback callback;

    /**
     * 本机视频源对象
     */
    VideoBean videoBen = null;

    /**
     * 本机Ip
     */
    String nativeIp = "";

    /**
     * Sip字典
     */
    List<SipBean> allCacheList;

    public SendAlarmToServerThread(String type, SendAlarmCallback callback) {
        this.type = type;
        this.callback = callback;
    }

    @Override
    public void run() {

        //网络异常
        if (!NetworkUtils.isConnected()) {
            if (callback != null) {
                callback.getCallbackData("error:" + "网络异常");
            }
            return;
        }
        //获取本机Ip
        nativeIp = NetworkUtils.getIPAddress(true);
        //判断本机IP是否为空
        if (TextUtils.isEmpty(nativeIp)) {
            if (callback != null) {
                callback.getCallbackData("error:" + "未获取本机Ip");
            }
            return;
        }
        //获取本机的Guid
        String nativeDeviceGuid = SysinfoUtils.getSysinfo().getDeviceGuid();
        //判断本机的Guid是否为空
        if (TextUtils.isEmpty(nativeDeviceGuid)) {
            if (callback != null) {
                callback.getCallbackData("error:" + "未获取到本机的Guid");
            }
            return;
        }
        //取出本地的所有sip字典
        try {
            allCacheList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
        } catch (Exception e) {
            if (callback != null) {
                callback.getCallbackData("error:" + "取sip字典异常");
            }
            return;
        }
        //判断sip字典是否为空
        if (allCacheList == null || allCacheList.size()==0){
            if (callback != null) {
                callback.getCallbackData("error:" + "取sip字典异常");
            }
            return;
        }
        //遍历字典查询本机的面部视频
        for (int i=0;i<allCacheList.size();i++){
            if (allCacheList.get(i).getId().equals(nativeDeviceGuid)){
                videoBen = allCacheList.get(i).getVideoBean();
            }
        }
        //判断本机的视频源是否为空
        if (videoBen == null){
            if (callback != null) {
                callback.getCallbackData("error:" + "未获取本机的视频源");
            }
            return;
        }

        //要发送的数据
        byte[] requestBys = new byte[580];

        //数据头
        byte[] zd = "ATIF".getBytes();
        System.arraycopy(zd, 0, requestBys, 0, 4);

        //sender ip
        byte[] id = new byte[32];
        byte[] ipByte = new byte[0];
        try {
            ipByte = nativeIp.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(ipByte, 0, id, 0, ipByte.length);
        System.arraycopy(id, 0, requestBys, 4, 32);

        //报文内id
        byte[] id1 = new byte[48];
        byte[] id2 = videoBen.getId().getBytes();
        System.arraycopy(id2, 0, id1, 0, id2.length);
        System.arraycopy(id1, 0, requestBys, 40, 48);

        //报文内name
        byte[] name = new byte[128];
        byte[] name1 = new byte[0];
        try {
            name1 = videoBen.getName().getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(name1, 0, name, 0, name1.length);
        System.arraycopy(name, 0, requestBys, 88, 128);

        //报文内devicetype
        byte[] deviceType = new byte[16];
        byte[] deviceType1 = videoBen.getDevicetype().getBytes();
        System.arraycopy(deviceType1, 0, deviceType, 0, deviceType1.length);
        System.arraycopy(deviceType, 0, requestBys, 216, 16);

        //报文内iPAddress
        byte[] iPAddress = new byte[32];
        byte[] iPAddress1 = videoBen.getIpaddress().getBytes();
        System.arraycopy(iPAddress1, 0, iPAddress, 0, iPAddress1.length);
        System.arraycopy(iPAddress, 0, requestBys, 232, 32);

        //报文内的port
        byte[] port = new byte[4];
        byte[] port1 = ByteUtil.toByteArray(80);
        System.arraycopy(port1, 0, port, 0, port1.length);
        System.arraycopy(port, 0, requestBys, 264, 4);

        //报文内的port
        byte[] channel = new byte[128];
        byte[] channel1 = videoBen.getChannel().getBytes();
        System.arraycopy(channel1, 0, channel, 0, channel1.length);
        System.arraycopy(channel, 0, requestBys, 268, 128);

        //报文内的username
        byte[] username = new byte[32];
        byte[] username1 = videoBen.getUsername().getBytes();
        System.arraycopy(username1, 0, username, 0, username1.length);
        System.arraycopy(username, 0, requestBys, 396, 32);

        //报文内的password
        byte[] password = new byte[32];
        byte[] password1 = videoBen.getPassword().getBytes();
        System.arraycopy(password1, 0, password, 0, password1.length);
        System.arraycopy(password, 0, requestBys, 428, 32);


        // AlertType
        byte[] alertType = new byte[32];
        byte[] alertS = new byte[32];
        try {
            alertS = type.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(alertS, 0, alertType, 0, alertS.length);
        System.arraycopy(alertType, 0, requestBys, 460, 32);
        //预留字节
        byte[] reserved = new byte[32];
        System.arraycopy(reserved, 0, requestBys, 492, 32);

        Socket socket = null;
        OutputStream os = null;
        try {
            //获取报警服务器ip
            String serverIp = SysinfoUtils.getSysinfo().getWebresourceServer();
            //报警服务器端口
            int sendPort = SysinfoUtils.getSysinfo().getAlertPort();
            socket = new Socket(serverIp, sendPort);
            os = socket.getOutputStream();
            os.write(requestBys);
            os.flush();
            InputStream in = socket.getInputStream();
            byte[] headers = new byte[4];
            int read = in.read(headers);

            byte[] flag = new byte[4];
            for (int i = 0; i < 4; i++) {
                flag[i] = headers[i];
            }
            int status = ByteUtil.bytesToInt(flag, 0);
            Logutil.i(status + "-------------报警信息-----------");

            if (callback != null) {
                callback.getCallbackData("状态:" + status);
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.getCallbackData("error:" + e.getMessage());
            }

            e.printStackTrace();
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

    /**
     * 发送报警回调
     */
    public interface SendAlarmCallback {
        void getCallbackData(String result);
    }
}
