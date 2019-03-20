package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.HbLogToFile;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：定时发送心跳任务
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/19 14:11
 */

public class TimingSendHbService extends Service {

    /**
     * 线程池
     */
    ScheduledExecutorService mThreadPoolServer = null;

    /**
     * 服务器端口
     */
    int sendPort = -1;

    /**
     * 服务器Ip
     */
    String sendIp = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //实例定时发送心跳任务线程池
        if (mThreadPoolServer == null) {
            mThreadPoolServer = Executors.newSingleThreadScheduledExecutor();
        }
        mThreadPoolServer.scheduleWithFixedDelay(new RequestVideoSourcesThread(), 0L, 15000, TimeUnit.MILLISECONDS);

        Logutil.i("启动心跳服务");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThreadPoolServer != null && !mThreadPoolServer.isShutdown()) {
            mThreadPoolServer.shutdown();
            Logutil.d("停止定时发送心跳功能");
        }
    }


    /**
     * 子线程向服务器发送消息
     */
    class RequestVideoSourcesThread extends Thread {
        @Override
        public void run() {
            sendHbData();
        }

        /**
         * 发送心跳数据
         */
        private void sendHbData() {

            //获取心中的服务器地址
            sendIp = SysinfoUtils.getSysinfo().getHeartbeatServer();
            //获取心中服务器的端口
            sendPort = SysinfoUtils.getSysinfo().getHeartbeatPort();

            //判断服务器地址或端口是否正确
            if (TextUtils.isEmpty(sendIp) || sendPort == -1) {
                Logutil.e("心跳服务器地址或端口未知");
                WriteLogToFile.info("心跳服务器地址或端口未知");
                return;
            }
            //判断网络是否正确
            if (!NetworkUtils.isConnected()) {
                Logutil.e("发送心跳时网络异常");
                WriteLogToFile.info("发送心跳时网络异常");
                return;
            }

            //udp发送的总数据
            byte[] requestBytes = new byte[96];

            //数据头
            byte[] flag = "ZDHB".getBytes();
            System.arraycopy(flag, 0, requestBytes, 0, 4);

            //手机唯一标识
            byte[] id = new byte[52];
            String senderGuid = SysinfoUtils.getSysinfo().getDeviceGuid();

            byte[] idKey = senderGuid.getBytes();//Guid
            System.arraycopy(idKey, 0, id, 0, idKey.length);
            System.arraycopy(id, 0, requestBytes, 4, 52);

            //时间戳
            long timeStemp = TimeUtils.dateStamp();
            byte[] timeByte = ByteUtil.longToBytes(timeStemp);
            byte[] stamp = new byte[8];
            byte[] timeT = timeByte;
            System.arraycopy(timeT, 0, stamp, 0, timeT.length);
            System.arraycopy(stamp, 0, requestBytes, 56, stamp.length);

            // 纬度
            double lat = AppConfig.LOCATION_LAT;
            byte[] latB = ByteUtil.getBytes(lat);
            System.arraycopy(latB, 0, requestBytes, 64, 8);

            // 经度
            double lon = AppConfig.LOCATION_LOG;
            byte[] lonB = ByteUtil.getBytes(lon);
            System.arraycopy(lonB, 0, requestBytes, 72, 8);

            //剩余电量
            byte[] power = new byte[1];
            power[0] = 100;
            System.arraycopy(power, 0, requestBytes, 80, 1);

            //内存使用
            byte[] mem = new byte[1];
            mem[0] = (byte) Double.parseDouble(AppConfig.DEVICE_RAM + "");
            System.arraycopy(mem, 0, requestBytes, 81, 1);

            //cpu使用
            byte[] cpu = new byte[1];
            cpu[0] = (byte) Double.parseDouble(AppConfig.DEVICE_CPU + "");
            System.arraycopy(cpu, 0, requestBytes, 82, 1);

            //信号强度
            byte[] signal = new byte[1];

            //默认信号满格
            signal[0] = 100;
            System.arraycopy(signal, 0, requestBytes, 83, 1);

            //蓝牙连接状态， 0-未连接，1-已连接
            byte[] bluetooth = new byte[1];
            bluetooth[0] = 0;
            System.arraycopy(bluetooth, 0, requestBytes, 84, 1);

            //弹箱锁闭状态， 0-未锁闭，1-已锁闭
            byte[] ammobox = new byte[1];
            ammobox[0] = 0;
            System.arraycopy(ammobox, 0, requestBytes, 85, 1);

            //保留
            byte[] save = new byte[11];
            System.arraycopy(save, 0, requestBytes, 86, 10);

            //把第56到64位（时间戳）的四个字节反转
            byte[] temp = new byte[8];
            System.arraycopy(requestBytes, 56, temp, 0, 8);
            byte[] reversalByte = new byte[8];
            reversalByte[0] = temp[7];
            reversalByte[1] = temp[6];
            reversalByte[2] = temp[5];
            reversalByte[3] = temp[4];
            reversalByte[4] = temp[3];
            reversalByte[5] = temp[2];
            reversalByte[6] = temp[1];
            reversalByte[7] = temp[0];
            System.arraycopy(reversalByte, 0, requestBytes, 56, 8);

            //建立UDP请求
            DatagramSocket socketUdp = null;
            DatagramPacket datagramPacket = null;

            try {
                socketUdp = new DatagramSocket(sendPort);
                datagramPacket = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName(sendIp), sendPort);
                socketUdp.send(datagramPacket);
                socketUdp.close();
                HbLogToFile.info("心跳发送成功-->>"+ Arrays.toString(requestBytes));
            } catch (Exception e) {
                Logutil.e("发送心跳异常:" + e.getMessage());
                HbLogToFile.info("发送心跳异常:" + e.getMessage());
            }
        }
    }
}
