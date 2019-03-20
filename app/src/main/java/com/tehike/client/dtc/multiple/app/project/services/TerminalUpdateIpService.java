package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述：修改Ip
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/26 16:57
 */
public class TerminalUpdateIpService extends Service {

    /**
     * 网络类型（1.有线 2.无线）
     */
    static int netWorkType;

    //本机的Ip
    String nativeIp = "";
    //本机的mac
    String nativeMac = "";
    //本机的子网掩码
    String nativeNetmask = "";
    //本机的网关
    String nativeGateway = "";
    //上位机发出Mac信息
    String serverMac;

    boolean serviceIsStop = false;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceIsStop = true;
        //启动子线程去修改
        UpdateIpThread thread = new UpdateIpThread();
        new Thread(thread).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        serviceIsStop = false;
        super.onDestroy();
    }


    /**
     * 子线程修改IP
     */
    class UpdateIpThread extends Thread {
        @Override
        public void run() {
            //获取当前的网络类型
            netWorkType = NetworkUtils.getNetMode(App.getApplication());
            try {
                //启动udp服务监听（用于接收上位机的发出的消息）
                DatagramSocket udpServer = new DatagramSocket(AppConfig.X_PORT);
                while (serviceIsStop) {
                    byte[] buf = new byte[1024];
                    DatagramPacket udpPacket = new DatagramPacket(buf, buf.length);
                    udpServer.receive(udpPacket);
                    // 接收信息
                    String udpFromMess = new String(udpPacket.getData(), 0,
                            udpPacket.getLength(), "gb2312");
                    String udpFromIp = udpPacket.getAddress().getHostAddress();

                    // 接收到hello时回复应答消息
                    if (udpFromMess.contains("Hello")) {
                        Logutil.w("谁在修改Ip--->>>" + udpFromIp);
                        // 子线去向上位机返回本机的信息
                        SendReplyMessUdpThread thread = new SendReplyMessUdpThread(
                                udpFromMess, udpFromIp);
                        thread.start();
                    }
                    // 收到要修改的内容
                    if (udpFromMess.contains("SETIP")) {
                        UpdateNativeIpThread thread = new UpdateNativeIpThread(udpFromMess);
                        thread.start();
                    }
                    //收到消息要重启系统
                    if (udpFromMess.contains("REBOOT")) {
                        RebootThread thread = new RebootThread(udpFromMess);
                        thread.start();
                    }
                }
            } catch (IOException e) {
                System.err.println("UdpServer异常-->>" + e.getMessage());
            }
        }
    }


    /**
     * Udp线程用来回复应答消息
     * <p>
     * 消息（WHOAMI|下位机MAC地址|下位机IP地址/子网掩码/缺省网关[|下位机型号] 示例：
     * WHOAMI|00-06-bf-12-34-56|
     * 192.168.0.202/255.255.255.0/192.168.0.1|NV-D2008HD ）
     *
     * @author wpfse
     */
    class SendReplyMessUdpThread extends Thread {

        //上位机发出的消息
        String str;
        //上位机的Ip地址
        String tempIp;

        //构造函数
        public SendReplyMessUdpThread(String str, String ip) {
            this.str = str;
            this.tempIp = ip;
        }

        @Override
        public void run() {
            try {
                //先判断网络类型
                if (netWorkType == 0) {
                    Logutil.e("无网络");
                    return;
                }
                //本机ip
                if (netWorkType == 1) {
                    nativeIp = App.getSystemManager().ZYgetEthIp();
                } else {
                    nativeIp = App.getSystemManager().ZYgetWifiIp();
                }
                //本机mac
                if (netWorkType == 1) {
                    nativeMac = App.getSystemManager().ZYgetEthMacAddress();
                } else {
                    nativeMac = App.getSystemManager().ZYgetWifiMacAddress();
                }
                //本机子网掩码
                if (netWorkType == 1) {
                    nativeNetmask = App.getSystemManager().ZYgetEthNetMask();
                    ;
                } else {
                    nativeNetmask = App.getSystemManager().ZYgetWifiNetMask();
                }
                //本机网关
                if (netWorkType == 1) {
                    nativeGateway = App.getSystemManager().ZYgetEthGatWay();
                } else {
                    nativeGateway = App.getSystemManager().ZYgetWifiGatWay();
                }
                // udp客户端
                DatagramSocket udpClient = new DatagramSocket();

                // 要发送的信息
                String data = "WHOAMI|" + nativeMac.toUpperCase().replace(":", "-") + "|" + nativeIp
                        + "/" + nativeNetmask + "/" + nativeGateway + "|NV-Dtc_S";
                Logutil.i("本机向上位机发的消息-->>>" + data);
                DatagramPacket d = new DatagramPacket(data.getBytes(),
                        data.getBytes().length, InetAddress.getByName(tempIp),
                        AppConfig.S_PORT);
                // 发送后关闭udp
                udpClient.send(d);
                udpClient.close();
                System.err.print("已发送信息");
            } catch (Exception e) {
                System.err.println("UdpClient异常-->>" + e.getMessage());
            }
        }
    }


    /**
     * 子线程去修改本机的信息
     *
     * @author wpfse
     */
    class UpdateNativeIpThread extends Thread {
        String str;

        public UpdateNativeIpThread(String str) {
            this.str = str;
        }

        @Override
        public void run() {
            //用正则分数据
            String regEx = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            Logutil.i("上位机向本机发的消息--->>>>" + str);
            String[] updateInfo = new String[4];
            int i = 0;
            while (m.find()) {
                i++;
                String result1 = m.group();
                updateInfo[i - 1] = result1;
            }
            //上位机返回的mac信息
            serverMac = (String) str.subSequence(6, 23);
            //上位机返回的Ip
            String serverIp = updateInfo[0];
            //netmask
            String serverNetmask = updateInfo[1];
            //getway
            String serverGateway = updateInfo[2];


            if (serverMac.equals(nativeMac.toUpperCase().replace(":", "-"))) {
                //设置静态ip
                if (netWorkType == 1)
                    App.getSystemManager().ZYsetEthStaticMode(serverIp, serverGateway, serverNetmask, AppConfig.DNS, AppConfig.DNS);
                else
                    App.getSystemManager().ZYsetWifiStaticMode(serverIp, serverGateway, serverNetmask, AppConfig.DNS, AppConfig.DNS);
            }
        }
    }

    /**
     * 子线程去重启系统
     *
     * @author wpfse
     */
    class RebootThread extends Thread {
        String str;

        public RebootThread(String str) {
            this.str = str;
        }

        @Override
        public void run() {
            if (serverMac.equals(nativeMac.toUpperCase().replace(":", "-"))) {
                if (netWorkType == 1)
                    Logutil.w("哈哈，我重启网络了-->>>" + App.getSystemManager().ZYgetEthIp());
                else
                    Logutil.w("哈哈，我重启网络了-->>>" + App.getSystemManager().ZYgetWifiIp());
            }
        }
    }
}
