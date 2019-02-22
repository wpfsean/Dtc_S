package com.tehike.client.dtc.multiple.app.project.utils;

import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.global.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Root on 2018/8/31.
 */

public class RemoteVoiceRequestUtils extends Thread {

    //用于请求的Socket
    Socket socket = null;
    //警告和鸣枪回调
    RemoteCallbck mRemoteCallbck;
    //操作对象的Ip
    String ip;
    // 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
    int type;

    //构造函数
    public RemoteVoiceRequestUtils(int type, String ip, RemoteCallbck mRemoteCallbck) {
        this.type = type;
        this.ip = ip;
        this.mRemoteCallbck = mRemoteCallbck;
    }

    @Override
    public void run() {
        try {
            //子线程去请求
            socket = new Socket(ip, AppConfig.REMOTE_PORT);
            //超时
            socket.setSoTimeout(4000);
            // 需要发送的数据 flag+action+paramater+reserved
            byte[] requestData = new byte[4 + 4 + 4 + 4];
            // flag
            byte[] flag = new byte[4];
            flag = "RVRD".getBytes();
            System.arraycopy(flag, 0, requestData, 0, flag.length);

            // action
            byte[] action = new byte[4];
            action[0] = (byte) type;// 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
            action[1] = 0;
            action[2] = 0;
            action[3] = 0;
            System.arraycopy(action, 0, requestData, 4, action.length);

            // 接受喊话时=接收语音数据包的 UDP端口(测试)
            byte[] parameter = new byte[4];
            System.arraycopy(parameter, 0, requestData, 8, parameter.length);
            // // 向服务器发消息
            OutputStream os = socket.getOutputStream();// 字节输出流
            os.write(requestData);
            socket.shutdownOutput();// 关闭输出流
            // 读取服务器返回的消息
            InputStream in = socket.getInputStream();
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
            String r_questMess = getMessage(r_questCode);

            // 返回的状态
            byte[] r_status = new byte[4];
            for (int i = 0; i < 4; i++) {
                r_status[i] = data[i + 8];
            }
            int r_statusCode = r_status[0];
            String r_statusMess = getStatusMessage(r_statusCode);

            // 返回参数
            byte[] r_paramater = new byte[4];
            for (int i = 0; i < 4; i++) {
                r_paramater[i] = data[i + 12];
            }
            if (!TextUtils.isEmpty(r_DataFlag)) {
                if (mRemoteCallbck != null) {
                    mRemoteCallbck.remoteStatus(r_statusMess);
                }
            }
        } catch (Exception e) {
            if (mRemoteCallbck != null) {
                mRemoteCallbck.remoteStatus("error:" + e.getMessage());
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    if (mRemoteCallbck != null) {
                        mRemoteCallbck.remoteStatus("error:socket" + e.getMessage());
                    }
                }
            }
        }
    }

    public void start() {
        new Thread(this).start();
    }

    //接口回调
    public interface RemoteCallbck {
        public void remoteStatus(String status);
    }

    public static String getStatusMessage(int r_statusCode) {
        String r_statusMess = "";
        if (r_statusCode == 0) {
            r_statusMess = "Accept";
        } else if (r_statusCode == 1) {
            r_statusMess = "Reject";
        } else if (r_statusCode == 2) {
            r_statusMess = " Unknown";
        } else if (r_statusCode == 3) {
            r_statusMess = "  Busy";
        } else if (r_statusCode == 4) {
            r_statusMess = "Done";
        } else if (r_statusCode == 5) {
            r_statusMess = " BadFormat";
        }
        return r_statusMess;
    }

    public static String getMessage(int r_questCode) {
        String r_questMess = "";
        if (r_questCode == 0) {
            r_questMess = "無操作";
        } else if (r_questCode == 1) {
            r_questMess = "遠程喊話";
        } else if (r_questCode == 2) {
            r_questMess = "播放語音警告";
        } else if (r_questCode == 3) {
            r_questMess = "播放鳴槍警告";
        } else if (r_questCode == 4) {
            r_questMess = "遠程監聽";
        } else if (r_questCode == 5) {
            r_questMess = "單向廣播";
        }
        return r_questMess;
    }


}
