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
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：加载video资源 和sip资源
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/5 14:39
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
     * 记录已关联的Sip资源数量
     */
    int num = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化
        initialize();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //关闭请求video资源的定时服务
        if (videoScheduledExecutorService != null && !videoScheduledExecutorService.isShutdown())
            videoScheduledExecutorService.shutdown();
        //关闭请求Sip资源的定时服务
        if (sipScheduledExecutorService != null && !sipScheduledExecutorService.isShutdown()) {
            sipScheduledExecutorService.shutdown();
        }
        //移除Handler监听
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    /**
     * 初始化数据
     */
    private void initialize() {

        //加载所有的Video资源
        initializeWebApiVideoSource();
    }

    /**
     * 根据webapi获取所有的Video资源数据
     */
    private void initializeWebApiVideoSource() {
        //拼加的请求videos资源的Url
        String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._WEBAPI_VIDEO_SOURCE;
        //定时线程池任务去执行
        if (videoScheduledExecutorService == null) {
            videoScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            videoScheduledExecutorService.scheduleWithFixedDelay(new RequestWebApiVideoSourceThread(requestUrl), 0L, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
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
                        String result = StringUtils.readTxt(in);
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
                    WriteLogToFile.info("请求videoSources资源异常-->>>"+e.getMessage());
                    Logutil.e("请求videoSources资源异常-->>" + e.getMessage());
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
                        VideoBean mVideo = new VideoBean(jsonItem.getString("channel"), jsonItem.getString("devicetype"), jsonItem.getString("id"), jsonItem.getString("ipaddress"), jsonItem.getString("name"), jsonItem.getString("location"), jsonItem.getString("password"), jsonItem.getInt("port"), jsonItem.getString("username"), "", "", "", "", "", "");
                        webapiVideoSourceList.add(mVideo);
                    }
                    handler.sendEmptyMessage(3);
                }
            } else {
                Logutil.e("请求数据异常--->>>" + jsonObject.getString("reason"));
                WriteLogToFile.info("请求数据异常--->>>" + jsonObject.getString("reason"));
            }
        } catch (Exception e) {
            WriteLogToFile.info("解析webapi获取Video资源时为为空-->>" + e.getMessage());
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

        Logutil.d("webapiVideoSourceList--->>" + webapiVideoSourceList.size());
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
        //接收对象
        Bundle dbundle = msg.getData();
        VideoBean device = (VideoBean) dbundle.getSerializable("device");
        //添加集合
        resolveWebapiVideoSouceList.add(device);
        //判断是否已全部添加
        if (resolveWebapiVideoSouceList.size() == webapiVideoSourceList.size()) {
            //通过gson把集合转成字符串
            String str = GsonUtils.GsonToString(resolveWebapiVideoSouceList);
            if (TextUtils.isEmpty(str)) {
                Logutil.e("Gson转字符串失败");
                return;
            }
            //写入文件
            FileUtil.writeFile(CryptoUtil.encodeBASE64(str), AppConfig.SOURCES_VIDEO);
            Logutil.d("Video数据写入完成");

            //发送广播
            Intent intent = new Intent();
            intent.setAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
            App.getApplication().sendBroadcast(intent);

            //再加载Sip资源
            initializeWebApiSipSource();
        }
    }

    /**
     * 根据webapi获取所有的Sip资源数据
     */
    private void initializeWebApiSipSource() {

        //拼加请求Sips资源的Url
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
                        String result = StringUtils.readTxt(in);
                        Message message = new Message();
                        message.what = 6;
                        message.obj = result;
                        handler.sendMessage(message);
                    } else {
                        Logutil.d("!200-->>" + con.getResponseCode());
                        WriteLogToFile.info("请求sip资源非200-->>"+ con.getResponseCode());
                        handler.sendEmptyMessage(5);
                    }
                    con.disconnect();
                } catch (Exception e) {
                    Logutil.d("er-->>" + e.getMessage());
                    WriteLogToFile.info("请求sip资源异常-->>"+ e.getMessage());
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
                        webapiSipSourceList.add(sipBean);
                    }
                    handler.sendEmptyMessage(7);
                }
            } else {
                Logutil.e("请求数据异常--->>>" + jsonObject.getString("reason"));
                WriteLogToFile.info("请求数据异常--->>>" + jsonObject.getString("reason"));
            }
        } catch (Exception e) {
            WriteLogToFile.info("解析sip资源异常--->>>"+e.getMessage());
            Logutil.e("异常-->>" + e.getMessage());
        }
    }

    /**
     * 关联sip中的哨位视频，面部视频，和弹箱视频
     */
    private void handlerSipVideoSoucesData() {
        if (webapiSipSourceList != null && webapiSipSourceList.size() > 0) {
            for (int i = 0; i < webapiSipSourceList.size(); i++) {
                num = i;
                handlerLinkedData(webapiSipSourceList.get(i));
            }
        } else {
            Logutil.w("无数据");
        }
    }

    /**
     * 处理关联数据
     */
    private void handlerLinkedData(final SipBean sipBean) {
        HttpBasicRequest httpBasicRequest = new HttpBasicRequest(AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._LINKED_VIDEO + sipBean.getId(), new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                //判断关联数据是否存在
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("无关联视频数据");
                    return;
                }
                try {
                    String faceKey = "";
                    String faceVideoId = "";
                    String sentryKey = "";
                    String sentryVideoId = "";
                    String ammoKey = "";
                    String ammoVideoId = "";
                    String key = "";

                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        key = jsonObject.getString("Key");
                        if (!jsonObject.isNull("Value")) {
                            JSONObject jsonItem = jsonObject.getJSONObject("Value");
                            String id = jsonItem.getString("id");
                            if (key.equals("face")) {
                                faceKey = key;
                                faceVideoId = id;
                                for (VideoBean v : resolveWebapiVideoSouceList) {
                                    if (v.getId().equals(faceVideoId)) {
                                        sipBean.setVideoBean(v);
                                    }
                                }
                            }
                            if (key.equals("sentry")) {
                                sentryKey = key;
                                sentryVideoId = id;
                                for (VideoBean v : resolveWebapiVideoSouceList) {
                                    if (v.getId().equals(sentryVideoId)) {
                                        sipBean.setSetryBean(v);
                                    }
                                }
                            }
                            if (key.equals("ammo")) {
                                ammoKey = key;
                                ammoVideoId = id;
                                for (VideoBean v : resolveWebapiVideoSouceList) {
                                    if (v.getId().equals(sentryVideoId)) {
                                        sipBean.setAmmoBean(v);
                                    }
                                }
                            }
                        } else {
                            if (key.equals("face")) {
                                faceKey = key;
                                faceVideoId = "";
                            }
                            if (key.equals("sentry")) {
                                sentryKey = key;
                                sentryVideoId = "";
                            }
                            if (key.equals("ammo")) {
                                ammoKey = key;
                                ammoVideoId = "";
                            }
                        }
                    }
                } catch (Exception ex) {
                    WriteLogToFile.info("解析关联视频数据error-->>" + ex.getMessage());
                    Logutil.e("解析关联视频数据error-->>" + ex.getMessage());
                }
            }
        });
        new Thread(httpBasicRequest).start();

        if (num == webapiSipSourceList.size() - 1) {

            String str = GsonUtils.GsonToString(webapiSipSourceList);
            //判断是否转换成功
            if (TextUtils.isEmpty(str)) {
                Logutil.e("Gson转字符串失败");
                return;
            }
            //把字符串写入本地File(注意，若加密，用gson转换后，sip中的VideoBean是null,只能后期再多测试一下)
            FileUtil.writeFile(CryptoUtil.encodeBASE64(str), AppConfig.SOURCES_SIP);

            Logutil.d("Sip数据写入完成");
            //日志记录sip缓存完成
            WriteLogToFile.info("Sip资源数据缓存完成" + TimeUtils.getCurrentTime());
            webapiSipSourceList.clear();
            resolveWebapiVideoSouceList.clear();
            //发送广播广播Sip缓存完成
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
                    handlerSipVideoSoucesData();
                    break;

            }
        }
    };

}


