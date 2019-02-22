package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.onvif.ResolveVideoSourceRtsp;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class RequestWebApiDataService extends Service {

    /**
     * video定时线程池任务
     */
    ScheduledExecutorService videoScheduledExecutorService = null;

    /**
     * sip定时线程池任务
     */
    ScheduledExecutorService sipScheduledExecutorService = null;

    /**
     * 存放获取webapi上的video资源的集合
     */
    List<VideoBean> webapiVideoSourceList = new ArrayList<>();

    /**
     * 盛放已解析的video资源的集合
     */
    List<VideoBean> resolveWebapiVideoSouceList = new ArrayList<>();

    /**
     * 存放获取webapi上的sip资源的集合
     */
    List<SipBean> webapiSipSourceList = new ArrayList<>();

    /**
     * 盛放已解析的sip资源的集合
     */
    List<SipBean> resolveWebapiSipSouceList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        if (videoScheduledExecutorService != null && !videoScheduledExecutorService.isShutdown())
            videoScheduledExecutorService.shutdown();
        if (sipScheduledExecutorService != null && !sipScheduledExecutorService.isShutdown()) {
            sipScheduledExecutorService.shutdown();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }

    /**
     * 初始化数据
     */
    private void initialize() {

        initializeWebApiVideoSource();

        initializeWebApiSipSource();
    }


    /**
     * 根据webapi获取所有的Video资源数据
     */
    private void initializeWebApiVideoSource() {

        String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._WEBAPI_VIDEO_SOURCE;
        //定时线程池任务去执行
        if (videoScheduledExecutorService == null) {
            videoScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            videoScheduledExecutorService.scheduleWithFixedDelay(new RequestWebApiVideoSourceThread(requestUrl), 0L, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 流转字符串
     */
    public String readTxt(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }


    /**
     * 子线程请求webapi的video资源
     */
    class RequestWebApiVideoSourceThread extends Thread {
        //请求Url
        String url;

        //构造函数
        public RequestWebApiVideoSourceThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            synchronized (this) {
                //取出本地的用戶名
                String uName = SysinfoUtils.getUserName();
                //取出本地的密碼
                String uPwd = SysinfoUtils.getUserPwd();
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    String authString = uName + ":" + uPwd;
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = readTxt(in);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = result;
                        handler.sendMessage(message);
                    } else {
                        Logutil.d("!200-->>" + con.getResponseCode());
                        handler.sendEmptyMessage(2);
                    }
                    con.disconnect();
                } catch (Exception e) {
                    Logutil.d("er-->>" + e.getMessage());
                    handler.sendEmptyMessage(2);
                }
            }
        }
    }

    /**
     * 处理video资源
     */
    private void handlerVideoSouces(String result) {

        //先清空盛放数据的集合（防止重复数据）
        if (webapiVideoSourceList != null && webapiVideoSourceList.size() > 0) {
            webapiVideoSourceList.clear();
        }
        //判断数据是否存放阿卡异常
        if (TextUtils.isEmpty(result) || result.contains("Execption")) {
            Logutil.e("webapi获取Video资源时为为空");
            return;
        }
        //解析数据
        try {
            JSONObject jsonObject = new JSONObject(result);
            //数据无异常
            if (jsonObject.isNull("errorCode")) {
                int count = jsonObject.getInt("count");
                if (count > 0) {
                    JSONArray jsonArray = jsonObject.getJSONArray("sources");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonItem = jsonArray.getJSONObject(i);
                        VideoBean mVideo = new VideoBean(jsonItem.getString("channel"), jsonItem.getString("devicetype"), jsonItem.getString("id"), jsonItem.getString("ipaddress"), jsonItem.getString("location"),jsonItem.getString("name"), jsonItem.getString("password"), jsonItem.getInt("port"), jsonItem.getString("username"), "", "", "", "", "", "");
                        webapiVideoSourceList.add(mVideo);
                    }
                    handler.sendEmptyMessage(3);
                }
            } else {
                Logutil.e("请求数据异常--->>>" + jsonObject.getString("reason"));
                Logutil.e("result--->>>" + result);
            }
        } catch (Exception e) {
            Logutil.e("解析webapi获取Video资源时为为空-->>" + e.getMessage());
        }
    }

    /**
     * 解析videoSource资源中的Rtsp地址
     */
    private void resolveVideoSourceRtsp() {

        //判断待解析的集合是否为空
        if (webapiVideoSourceList == null || webapiVideoSourceList.size() == 0) {
            return;
        }
        //遍历解析
        for (int i = 0; i < webapiVideoSourceList.size(); i++) {

            VideoBean videoBean = webapiVideoSourceList.get(i);
            String deviceType = videoBean.getDevicetype();
            String ip = videoBean.getIpaddress();
            //先判断设备类型和ip是否为空
            if (!TextUtils.isEmpty(deviceType) && !TextUtils.isEmpty(ip)) {
                if (deviceType.toUpperCase().equals("ONVIF")) {
                    videoBean.setServiceUrl("http://" + videoBean.getIpaddress() + "/onvif/device_service");
                    ResolveVideoSourceRtsp onvif = new ResolveVideoSourceRtsp(videoBean, new ResolveVideoSourceRtsp.GetRtspCallback() {
                        @Override
                        public void getDeviceInfoResult(String rtsp, boolean isSuccess, VideoBean mVideoBean) {
                            //handler处理解析返回的设备对象
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("device", mVideoBean);
                            message.setData(bundle);
                            message.what = 4;
                            handler.sendMessage(message);
                        }
                    });
                    //执行线程
                    App.getExecutorService().execute(onvif);
                } else if (deviceType.toUpperCase().equals("RTSP")) {
                    //若设备类型是RTSP类型，拼加成rtsp
                    String mRtsp = "rtsp://" + videoBean.getUsername() + ":" + videoBean.getPassword() + "@" + videoBean.getIpaddress() + "/" + videoBean.getChannel();
                    //同样用handler处理这个设备对象
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    videoBean.setRtsp(mRtsp);
                    bundle.putSerializable("device", videoBean);
                    message.setData(bundle);
                    message.what = 4;
                    handler.sendMessage(message);
                } else if (deviceType.toUpperCase().equals("RTMP")) {
                    //若设备类型是RTSP类型，拼加成rtsp
                    String mRtsp = videoBean.getChannel();
                    //同样用handler处理这个设备对象
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    videoBean.setRtsp(mRtsp);
                    bundle.putSerializable("device", videoBean);
                    message.setData(bundle);
                    message.what = 4;
                    handler.sendMessage(message);
                }
            } else {
                //如果为空说明没面部视频
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", videoBean);
                message.setData(bundle);
                message.what = 4;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 保存video资源到本地
     */
    private void saveVideoSource(Message msg) {
        Bundle dbundle = msg.getData();
        VideoBean device = (VideoBean) dbundle.getSerializable("device");
        resolveWebapiVideoSouceList.add(device);
        //通过gson把集合转成字符串
        if (resolveWebapiVideoSouceList.size() == webapiVideoSourceList.size()) {
            String str = GsonUtils.GsonToString(resolveWebapiVideoSouceList);
            if (TextUtils.isEmpty(str)) {
                Logutil.e("Gson转字符串失败");
                return;
            }
            FileUtil.writeFile(CryptoUtil.encodeBASE64(str), AppConfig.SOURCES_VIDEO);
            Logutil.d("resolveWebapiVideoSouceList" + resolveWebapiVideoSouceList.size());
            Logutil.d("Video数据写入完成");
            resolveWebapiVideoSouceList.clear();
            Intent intent = new Intent();
            intent.setAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
            App.getApplication().sendBroadcast(intent);
        }
    }

    /**
     * 根据webapi获取所有的Sip资源数据
     */
    private void initializeWebApiSipSource() {

        String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._WEBAPI_SIP_SOURCE;

        //定时线程池任务去执行
        if (sipScheduledExecutorService == null) {
            sipScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            sipScheduledExecutorService.scheduleWithFixedDelay(new RequestWebApiSipSourceThread(requestUrl), 0L, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 子线程请求webapi的Sip资源
     */
    class RequestWebApiSipSourceThread extends Thread {

        String url;

        //构造函数
        public RequestWebApiSipSourceThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            synchronized (this) {
                //取出本地的用戶名
                String uName = SysinfoUtils.getUserName();
                //取出本地的密碼
                String uPwd = SysinfoUtils.getUserPwd();
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    String authString = uName + ":" + uPwd;
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = readTxt(in);
                        Message message = new Message();
                        message.what = 6;
                        message.obj = result;
                        handler.sendMessage(message);
                    } else {
                        Logutil.d("!200-->>" + con.getResponseCode());
                        handler.sendEmptyMessage(5);
                    }
                    con.disconnect();
                } catch (Exception e) {
                    Logutil.d("er-->>" + e.getMessage());
                    handler.sendEmptyMessage(5);
                }
            }
        }
    }

    /**
     * 处理Sip资源
     */
    private void handlerSipSouces(String sipSourceResult) {
        //先清空盛放数据的集合（防止重复数据）
        if (webapiSipSourceList != null && webapiSipSourceList.size() > 0) {
            webapiSipSourceList.clear();
        }
        //判断数据是否存放阿卡异常
        if (TextUtils.isEmpty(sipSourceResult)) {
            return;
        }
        //解析数据
        try {
            JSONObject jsonObject = new JSONObject(sipSourceResult);
            //数据无异常
            if (jsonObject.isNull("errorCode")) {
                int count = jsonObject.getInt("count");
                if (count > 0) {
                    JSONArray jsonArray = jsonObject.getJSONArray("resources");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonItem = jsonArray.getJSONObject(i);
                        SipBean sipBean = new SipBean();
                        sipBean.setDeviceType(jsonItem.getString("deviceType"));
                        sipBean.setId(jsonItem.getString("id"));
                        sipBean.setIpAddress(jsonItem.getString("ipAddress"));
                        sipBean.setName(jsonItem.getString("name"));
                        sipBean.setNumber(jsonItem.getString("number"));
                        sipBean.setSentryId(jsonItem.getInt("sentryId") + "");
                        //判断是否有面部视频
                        if (!jsonItem.isNull("videosource")) {
                            //解析面部视频
                            JSONObject jsonItemVideo = jsonItem.getJSONObject("videosource");
                            VideoBean mVideo = new VideoBean(jsonItemVideo.getString("channel"), jsonItemVideo.getString("devicetype"), jsonItemVideo.getString("id"), jsonItemVideo.getString("ipaddress"),jsonItem.getString("location"), jsonItemVideo.getString("name"), jsonItemVideo.getString("password"), jsonItemVideo.getInt("port"), jsonItemVideo.getString("username"), "", "", "", "", "", "");
                            sipBean.setVideoBean(mVideo);
                        } else {
                            sipBean.setVideoBean(null);
                        }
                        webapiSipSourceList.add(sipBean);
                    }
                    handler.sendEmptyMessage(7);
                }
            } else {
                Logutil.e("请求数据异常--->>>" + jsonObject.getString("reason"));
            }
        } catch (Exception e) {
            Logutil.e("异常-->>" + e.getMessage());
        }

    }

    /**
     * 解析Sip的面部视频的rtsp
     */
    private void resolveSipSourceRtsp() {
        //判断待解析的集合是否为空
        if (webapiSipSourceList == null || webapiSipSourceList.size() == 0) {
            return;
        }
        //遍历解析
        for (int i = 0; i < webapiSipSourceList.size(); i++) {
            final SipBean sipBean = webapiSipSourceList.get(i);
            VideoBean videoBean = sipBean.getVideoBean();
            if (videoBean != null) {
                String deviceType = videoBean.getDevicetype();
                String ip = videoBean.getIpaddress();
                //先判断设备类型和ip是否为空
                if (!TextUtils.isEmpty(deviceType) && !TextUtils.isEmpty(ip)) {
                    if (deviceType.toUpperCase().equals("ONVIF")) {
                        videoBean.setServiceUrl("http://" + videoBean.getIpaddress() + "/onvif/device_service");
                        ResolveVideoSourceRtsp onvif = new ResolveVideoSourceRtsp(videoBean, new ResolveVideoSourceRtsp.GetRtspCallback() {
                            @Override
                            public void getDeviceInfoResult(String rtsp, boolean isSuccess, VideoBean mVideoBean) {
                                //handler处理解析返回的设备对象
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                sipBean.setVideoBean(mVideoBean);
                                bundle.putSerializable("device", sipBean);
                                message.setData(bundle);
                                message.what = 8;
                                handler.sendMessage(message);
                            }
                        });
                        //执行线程
                        App.getExecutorService().execute(onvif);
                    } else if (deviceType.toUpperCase().equals("RTSP")) {
                        //若设备类型是RTSP类型，拼加成rtsp
                        String mRtsp = "rtsp://" + videoBean.getUsername() + ":" + videoBean.getPassword() + "@" + videoBean.getIpaddress() + "/" + videoBean.getChannel();
                        //同样用handler处理这个设备对象
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        videoBean.setRtsp(mRtsp);
                        sipBean.setVideoBean(videoBean);
                        bundle.putSerializable("device", sipBean);
                        message.setData(bundle);
                        message.what = 8;
                        handler.sendMessage(message);
                    } else if (deviceType.toUpperCase().equals("RTMP")) {
                        //若设备类型是RTSP类型，拼加成rtsp
                        String mRtsp = videoBean.getChannel();
                        //同样用handler处理这个设备对象
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        videoBean.setRtsp(mRtsp);
                        sipBean.setVideoBean(videoBean);
                        bundle.putSerializable("device", sipBean);
                        message.setData(bundle);
                        message.what = 8;
                        handler.sendMessage(message);
                    }
                }
            } else {
                //如果为空说明没面部视频
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", sipBean);
                message.setData(bundle);
                message.what = 8;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 保存sip资源数据
     */
    private void saveSipSource(Message msg) {
        Bundle dbundle = msg.getData();
        SipBean device = (SipBean) dbundle.getSerializable("device");
        resolveWebapiSipSouceList.add(device);
        //通过gson把集合转成字符串
        if (resolveWebapiSipSouceList.size() == webapiSipSourceList.size()) {
            String str = GsonUtils.GsonToString(resolveWebapiSipSouceList);
            if (TextUtils.isEmpty(str)) {
                Logutil.e("Gson转字符串失败");
                return;
            }
            Logutil.d("resolveWebapiSipSouceList" + resolveWebapiSipSouceList.size());
            FileUtil.writeFile(CryptoUtil.encodeBASE64(str), AppConfig.SOURCES_SIP);
            Logutil.d("Sip数据写入完成");
            resolveWebapiSipSouceList.clear();

            App.getApplication().sendBroadcast(new Intent("SipDone"));
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //处理video资源数据
                    String videoSourceResult = (String) msg.obj;
                    handlerVideoSouces(videoSourceResult);
                    break;
                case 2:
                    //提示获取video资源数据异常或失败
                    Logutil.d("请求webapi的video资源失败");
                    break;
                case 3:
                    //解析video资源
                    resolveVideoSourceRtsp();
                    break;
                case 4:
                    //保存video资源到本地
                    saveVideoSource(msg);
                    break;
                case 5:
                    //提示获取Sip资源数据异常或失败
                    Logutil.d("请求webapi的Sip资源失败");
                    break;
                case 6:
                    //处理Sip资源数据
                    String sipSourceResult = (String) msg.obj;
                    handlerSipSouces(sipSourceResult);
                    break;
                case 7:
                    //解析sip数据
                    resolveSipSourceRtsp();
                    break;
                case 8:
                    saveSipSource(msg);
                    break;
            }
        }
    };
}


