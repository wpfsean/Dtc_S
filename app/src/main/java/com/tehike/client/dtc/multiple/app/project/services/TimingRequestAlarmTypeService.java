package com.tehike.client.dtc.multiple.app.project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.entity.AlarmTypeBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：每隔15分钟向cms要一次SipResources数据
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/10/16 14:29
 */
public class TimingRequestAlarmTypeService extends Service {

    //定时任务线程池
    ScheduledExecutorService mScheduledExecutorService = null;

    List<AlarmTypeBean> mList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //启动线程池服务让子线程去处理
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            mScheduledExecutorService.scheduleWithFixedDelay(new RequestAlarmTypeThread(), 0L, AppConfig.REFRESH_DATA_TIME, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭线程池
        mScheduledExecutorService.shutdown();

        //移除handler监听
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    /**
     * 向cms索要sip数据
     */
    class RequestAlarmTypeThread extends Thread {
        @Override
        public void run() {
            mList = new ArrayList<>();

            //先清空集合数据
            if (mList != null && mList.size() > 0) {
                mList.clear();
            }

            //子线程去请求数据
            String url = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._ALARM_COLOR;
            HttpBasicRequest httpBasicRequest = new HttpBasicRequest(url, new HttpBasicRequest.GetHttpData() {
                @Override
                public void httpData(String result) {
                    if (TextUtils.isEmpty(result)) {
                        Logutil.e("未获取到数据");
                        return;
                    }
                    if (result.contains("Execption")) {
                        Logutil.e("未获取到数据");
                    }
                    Message message = new Message();
                    message.what = 1;
                    message.obj = result;
                    handler.sendMessage(message);
                }
            });
            new Thread(httpBasicRequest).start();
        }
    }

    /**
     * 处理报警类型数据
     */
    private void handlerAlertTypeData(String result) {

        if (TextUtils.isEmpty(result) || result.contains("Execption")) {
            Logutil.e("处理报警类型数据--->>>无数据!");
            return;
        }
        Logutil.d("AlarmType-->>>"+result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    AlarmTypeBean alarmTypeBean = new AlarmTypeBean(jsonObject.getString("TypeColor"), jsonObject.getString("TypeName"));
                    mList.add(alarmTypeBean);
                }
            }
            handler.sendEmptyMessage(2);
        } catch (Exception e) {
            Logutil.e("报警类型解析异常---" + e.getMessage());
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    handlerAlertTypeData(result);
                    break;
                case 2:
                    if (mList != null) {
                        String str = GsonUtils.GsonString(mList);
                        if (!TextUtils.isEmpty(str)) {
                            FileUtil.writeFile(CryptoUtil.encodeBASE64(str), AppConfig.ALARM_COLOR);
                        }
                    }
                    break;
            }
        }
    };
}
