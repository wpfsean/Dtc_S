package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupItemInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.ui.views.VerticalSeekBar;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：广播，监听，会议界面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/9 10:47
 */
public class NetworkBroadcastFragment extends BaseFragment {

    /**
     * 显示操作类型（会议，监听，广播）
     */
    @BindView(R.id.display_current_broadcast_infor_tv_layout)
    TextView displayBroadcastTypeLayout;

    /**
     * 显示操作时间
     */
    @BindView(R.id.display_current_broadcast_time_tv_layout)
    TextView displayBroadcastTimeLayout;

    /**
     * 显示所有的广播对象的父布局
     */
    @BindView(R.id.display_all_broadcast_item_parent_layout)
    RelativeLayout display_all_broadcast_item_parent_layout;

    /**
     * 显示正在广播的父布局
     */
    @BindView(R.id.display_all_broadcasting_item_parent_layout)
    RelativeLayout display_all_broadcasting_item_parent_layout;

    /**
     * 显示正在通话成员的Gridview
     */
    @BindView(R.id.broadcasting_gridview_layout)
    GridView broadcastinGridViewLayout;

    /**
     * 展示所有广播组的ListView
     */
    @BindView(R.id.network_broadcast_group_item_layout)
    public ListView networkBroadcstGroupItemListView;

    /**
     * 展示某个广播组的GridView
     */
    @BindView(R.id.network_broadcast_item_gridview_layout)
    public GridView networkBroadcstItemGridView;

    /**
     * 终止操作按键
     */
    @BindView(R.id.stop_braodcast_btn_layout)
    Button stopOperationBtn;

    /**
     * 网络广播按钮
     */
    @BindView(R.id.network_broadcast_btn_layout)
    Button webcastBtn;

    /**
     * 网络监听按键
     */
    @BindView(R.id.network_monitor_btn_layout)
    Button webListenBtn;

    /**
     * 网络会议按钮
     */
    @BindView(R.id.network_meetting_btn_layout)
    Button webMeetingBtn;

    /**
     * 静音按键
     */
    @BindView(R.id.webcast_mute_btn_layout)
    RadioButton webcastMuteBtn;

    /**
     * 音量拖动条
     */
    @BindView(R.id.webcast_external_sound_layout)
    VerticalSeekBar voiceBar;

    /**
     * item选中标识
     */
    int broadcastItemSelected = -1;

    /**
     * 展示广播组ListView的adapter
     */
    WebcastGroupAdapter mWebcastGroupAdapter;

    /**
     * 盛放广播组数据的集合
     */
    List<SipGroupInfoBean> allWebcastGroupDataList = new ArrayList<>();

    /**
     * 盛放某个组的内广播数据的集合
     */
    List<SipGroupItemInfoBean> webcastDataList = new ArrayList<>();

    /**
     * 盛放选中对象的集合
     */
    List<SipGroupItemInfoBean> itemSelectedList = new ArrayList<>();

    /**
     * 当前页面是是否可见
     */
    boolean currentPageVisible = false;

    /**
     * 中间展示广播组数据的适配器
     */
    WebcastItemAdapter mWebcastItemAdapter;

    /**
     * 显示时间线程是否正在远行
     */
    boolean isTimingThreadWork = false;

    /**
     * 计时的子线程
     */
    Thread timingThread = null;

    /**
     * 广播计时
     */
    int timingNumber = 0;

    /**
     * 电话是否接通
     */
    boolean callIsConnected = false;

    /**
     * 声音控制对象
     */
    AudioManager mAudioManager = null;

    /**
     * 定时的线程池任务
     */
    private ScheduledExecutorService timingPoolTaskService;

    /**
     * 显示正在广播成员的适配器
     */
    WebcastingAdapter mWebcastingAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_network_broadcast_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化本页面数据
        initializeWebcastGroupsData();

        //声音拖动条的监听
        initializeVoiceSeekbar();
    }

    /**
     * 声音拖动条
     */
    private void initializeVoiceSeekbar() {

        //判断声音处理对象是否为空
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        if (callIsConnected) {
            mAudioManager.setStreamVolume(AudioManager.RINGER_MODE_SILENT, voiceBar.getProgress(), 0);
        }

        //拖动事件
        voiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voiceBar.setProgress(progress);
                //根据拖动大小控制音量
                if (mAudioManager != null) {
                    mAudioManager.setSpeakerphoneOn(true);
                    mAudioManager.setStreamVolume(AudioManager.RINGER_MODE_SILENT, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 初始化广播分组数据
     */
    private void initializeWebcastGroupsData() {
        //判断网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //请求sip分组数据的Url
        String sipGroupUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS;

        //请求sip组数据
        HttpBasicRequest thread = new HttpBasicRequest(sipGroupUrl, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                //无数据
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("请求sip组无数据");
                    handler.sendEmptyMessage(1);
                    return;
                }
                //数据异常
                if (result.contains("Execption")) {
                    Logutil.e("请求sip组数据异常");
                    handler.sendEmptyMessage(1);
                    return;
                }
                Logutil.d("當前數據分組信息--->>>" + result);
                //让handler去处理数据
                Message sipGroupMess = new Message();
                sipGroupMess.what = 3;
                sipGroupMess.obj = result;
                handler.sendMessage(sipGroupMess);
            }
        });
        new Thread(thread).start();
    }

    /**
     * 处理广播分组数据
     */
    private void handlerNertworkBroadcastGroupData(String result) {
        //先清空集合防止
        if (allWebcastGroupDataList != null && allWebcastGroupDataList.size() > 0) {
            allWebcastGroupDataList.clear();
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("errorCode")) {
                Logutil.w("请求不到数据信息--->>>" + result);
                return;
            }
            int sipCount = jsonObject.getInt("count");
            if (sipCount > 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("groups");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonItem = jsonArray.getJSONObject(i);
                    SipGroupInfoBean sipGroupInfoBean = new SipGroupInfoBean();
                    sipGroupInfoBean.setId(jsonItem.getInt("id"));
                    sipGroupInfoBean.setMember_count(jsonItem.getString("member_count"));
                    sipGroupInfoBean.setName(jsonItem.getString("name"));
                    allWebcastGroupDataList.add(sipGroupInfoBean);
                }
            }
            handler.sendEmptyMessage(4);
        } catch (Exception e) {
            Logutil.e("解析Sip分组数据异常" + e.getMessage());
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 上部List适配数据
     */
    private void disPlayListViewAdapter() {
        //判断是否有要适配的数据
        if (allWebcastGroupDataList == null || allWebcastGroupDataList.size() == 0) {
            handler.sendEmptyMessage(1);
            Logutil.e("适配的数据时无数据");
            return;
        }
        mWebcastGroupAdapter = new WebcastGroupAdapter();
        //显示左侧的sip分组页面
        networkBroadcstGroupItemListView.setAdapter(mWebcastGroupAdapter);
        mWebcastGroupAdapter.setSelectedItem(0);
        mWebcastGroupAdapter.notifyDataSetChanged();

        //默认加载第一组的数据
        Message handlerMess = new Message();
        handlerMess.arg1 = allWebcastGroupDataList.get(0).getId();
        handlerMess.what = 5;
        handler.sendMessage(handlerMess);

        //点击事件
        networkBroadcstGroupItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWebcastGroupAdapter.setSelectedItem(position);
                mWebcastGroupAdapter.notifyDataSetChanged();
                SipGroupInfoBean mSipGroupInfoBean = allWebcastGroupDataList.get(position);
                Logutil.i("SipGroupInfoBean-->>" + mSipGroupInfoBean.toString());
                int groupId = mSipGroupInfoBean.getId();
                disPlaySipGroupItemStatus(groupId);
            }
        });
    }

    /**
     * 请求某个组内的sip状态数据
     */
    private void disPlaySipGroupItemStatus(int id) {
        //提示无网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //刷新适配器
        if (mWebcastItemAdapter != null) {
            mWebcastItemAdapter.notifyDataSetChanged();
            webcastDataList.clear();
        }

        Logutil.d("Id-->>>" + id);
        //获取某个组内数据
        String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;

        //子线程根据组Id请求组数据
        HttpBasicRequest httpThread = new HttpBasicRequest(sipGroupItemUrl + id, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                //无数据
                if (TextUtils.isEmpty(result)) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                //数据异常
                if (result.contains("Execption")) {
                    handler.sendEmptyMessage(1);
                    return;
                }

                if (webcastDataList != null && webcastDataList.size() > 0) {
                    webcastDataList.clear();
                }

                Logutil.d("组数据" + result);

                //解析sip资源
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.isNull("errorCode")) {
                        Logutil.w("请求网络广播组数据数据信息异常" + result);
                        WriteLogToFile.info("请求网络广播组数据数据信息异常" + result);
                        return;
                    }

                    int count = jsonObject.getInt("count");
                    if (count > 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("resources");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonItem = jsonArray.getJSONObject(i);
                            //解析
                            SipGroupItemInfoBean groupItemInfoBean = new SipGroupItemInfoBean();
                            groupItemInfoBean.setDeviceType(jsonItem.getString("deviceType"));
                            groupItemInfoBean.setId(jsonItem.getString("id"));
                            groupItemInfoBean.setIpAddress(jsonItem.getString("ipAddress"));
                            groupItemInfoBean.setName(jsonItem.getString("name"));
                            groupItemInfoBean.setNumber(jsonItem.getString("number"));
                            groupItemInfoBean.setSentryId(jsonItem.getInt("sentryId"));
                            //判断是否有面部视频
                            if (!jsonItem.isNull("videosource")) {
                                //解析面部视频
                                JSONObject jsonItemVideo = jsonItem.getJSONObject("videosource");
                                if (jsonItemVideo != null) {
                                    //封闭面部视频
                                    VideoBean videoBean = new VideoBean(
                                            jsonItemVideo.getString("channel"),
                                            jsonItemVideo.getString("devicetype"),
                                            jsonItemVideo.getString("id"),
                                            jsonItemVideo.getString("ipaddress"),
                                            jsonItemVideo.getString("location"),
                                            jsonItemVideo.getString("name"),
                                            jsonItemVideo.getString("password"),
                                            jsonItemVideo.getInt("port"),
                                            jsonItemVideo.getString("username"), "", "", "", "", "", "");
                                    groupItemInfoBean.setBean(videoBean);
                                }
                            }
                            webcastDataList.add(groupItemInfoBean);
                        }
                    }
                    handler.sendEmptyMessage(6);
                } catch (JSONException e) {
                    Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                }
            }
        });
        new Thread(httpThread).start();
    }

    /**
     * 子线程请求数据去刷新状态
     */
    class RequestRefreshStatus implements Runnable {

        //传入的url
        String url;
        String userName;
        String userPwd;

        /**
         * 构造方法
         */
        public RequestRefreshStatus(String s, String p, String url) {
            this.url = url;
            this.userName = s;
            this.userPwd = p;
        }

        @Override
        public void run() {
            //加同步锁
            synchronized (this) {
                try {
                    //当可见时去请求刷新
                    if (isVisible() && currentPageVisible) {
                        if (!NetworkUtils.isConnected()) {
                            Logutil.e("刷新状态时网络异常");
                            handler.sendEmptyMessage(18);
                        } else {
                            //用HttpURLConnection请求
                            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                            con.setRequestMethod("GET");
                            con.setConnectTimeout(3000);
                            String authString = userName + ":" + userPwd;
                            //添加 basic参数
                            con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                            con.connect();
                            Message message = new Message();
                            message.what = 7;
                            if (con.getResponseCode() == 200) {
                                InputStream in = con.getInputStream();
                                String result = StringUtils.readTxt(in);
                                message.obj = result;
                            } else {
                                message.obj = "";
                            }
                            handler.sendMessage(message);
                            con.disconnect();
                        }
                    }
                } catch (Exception e) {
                    Logutil.e("请求刷新状态数据时的异常--->>" + e.getMessage());
                }
            }
        }
    }

    /**
     * 刷新sip状态
     */
    private void handlerSipStatusData(String sisStatusResult) {
        List<SipStatusInfoBean> sipStatusList = new ArrayList<>();
        try {
            if (TextUtils.isEmpty(sisStatusResult)) {
                handler.sendEmptyMessage(1);
                return;
            }
            JSONArray jsonArray = new JSONArray(sisStatusResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                int state = jsonArray.getJSONObject(i).getInt("state");
                String name = jsonArray.getJSONObject(i).getString("usrname");

                SipStatusInfoBean mSipStatusInfoBean = new SipStatusInfoBean();
                mSipStatusInfoBean.setName(name);
                mSipStatusInfoBean.setState(state);
                sipStatusList.add(mSipStatusInfoBean);
            }

            for (int n = 0; n < sipStatusList.size(); n++) {
                for (int k = 0; k < webcastDataList.size(); k++) {
                    if (sipStatusList.get(n).getName().equals(webcastDataList.get(k).getNumber())) {
                        webcastDataList.get(k).setState(sipStatusList.get(n).getState());
                    }
//                    //为正在广播组的成员状态数据赋值
//                    if (itemSelectedList != null && !itemSelectedList.isEmpty()) {
//                        if (sipStatusList.get(n).getName().equals(itemSelectedList.get(k).getNumber())) {
//                            itemSelectedList.get(k).setState(sipStatusList.get(n).getState());
//                        }
//                    }
                }
            }

            //刷新当前选中组的广播成员的状态
            if (mWebcastItemAdapter != null)
                mWebcastItemAdapter.notifyDataSetChanged();

//            //刷新正在广播成员的状态
//            if (mWebcastingAdapter != null) {
//                mWebcastingAdapter.notifyDataSetChanged();
//            }
        } catch (Exception e) {
            Logutil.e("解析SIp状态时异常:-->>" + e.getMessage());
        }
    }

    /**
     * Sip状态类（用来刷新状态）
     */
    class SipStatusInfoBean implements Serializable {
        private String name;
        private int state;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    /**
     * 处理sip数据
     */
    private void diplayGridViewAdaperAndRefreshStatus() {
        //gridView显示数据
        if (mWebcastItemAdapter == null) {
            mWebcastItemAdapter = new WebcastItemAdapter(getActivity());
            networkBroadcstItemGridView.setAdapter(mWebcastItemAdapter);
        }
        mWebcastItemAdapter.notifyDataSetChanged();

        //item点击事件
        networkBroadcstItemGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWebcastItemAdapter.changeItemStatus(position);
                mWebcastItemAdapter.notifyDataSetChanged();
            }
        });

        //定时线程任务池
        if (timingPoolTaskService == null || timingPoolTaskService.isShutdown())
            timingPoolTaskService = Executors.newSingleThreadScheduledExecutor();

        //启动定时线程池任务去刷新sip状态
        String sispStatusUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._SIS_STATUS;
        if (!timingPoolTaskService.isShutdown()) {
            timingPoolTaskService.scheduleWithFixedDelay(new RequestRefreshStatus(SysinfoUtils.getUserName(), SysinfoUtils.getUserPwd(), sispStatusUrl), 0L, 3000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 中間GridView数据展示
     */
    class WebcastItemAdapter extends BaseAdapter {
        //item是否选中标识
        private boolean[] isCheck;

        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public WebcastItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

            //item全部的标识都设为false(未选中状态)
            if (webcastDataList != null) {
                isCheck = new boolean[webcastDataList.size()];
                for (int i = 0; i < webcastDataList.size(); i++) {
                    isCheck[i] = false;
                }
            }
        }

        @Override
        public int getCount() {
            return webcastDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return webcastDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ViewHolder viewHolder = null;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_network_broadcast_item, null);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.mRelativeLayout = (FrameLayout) convertView.findViewById(R.id.item_layout);
                viewHolder.mainLayout = convertView.findViewById(R.id.sipstatus_main_layout);
                viewHolder.deviceType = convertView.findViewById(R.id.device_type_layout);
                viewHolder.StatusIcon = convertView.findViewById(R.id.sip_status_icon_layout);
                viewHolder.mCheckBox = convertView.findViewById(R.id.check_box);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupItemInfoBean mSipClient = webcastDataList.get(position);
            int status = -1;
            if (mSipClient != null) {
                //显示设备名
                String deviceName = mSipClient.getName();
                if (!TextUtils.isEmpty(deviceName)) {
                    viewHolder.itemName.setText(deviceName);
                } else {
                    viewHolder.itemName.setText("");
                }
                //设备类型
                String deviceType = mSipClient.getDeviceType();
                if (!TextUtils.isEmpty(deviceType)) {
                    if (deviceType.equals("TH-C6000")) {
                        viewHolder.deviceType.setText("移动终端");
                    }
                    if (deviceType.equals("TH-S6100")) {
                        viewHolder.deviceType.setText("哨位终端");
                    }
                    if (deviceType.equals("TH-S6200")) {
                        viewHolder.deviceType.setText("值班终端");
                    }
                }
                //显示状态
                status = mSipClient.getState();
                switch (status) {
                    case -1://未知状态
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_offline);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_offline_normal);
                        break;
                    case 1://在线
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_free);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_normal);
                        break;
                    case 2://振铃
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_ringing);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_ringing_normal);
                        break;
                    case 3://通话
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_call);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_call_normal);
                        break;
                }
            }


            //如果选中就设置背景，未选中就设置透明背景
            if (isCheck[position]) {
                //如果在线就选中
                if (status == 1) {
                    viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                    viewHolder.mCheckBox.setImageResource(R.mipmap.ic_checked);
                    viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_selected);
                }

            } else {
                viewHolder.mCheckBox.setVisibility(View.GONE);
                viewHolder.mCheckBox.setImageResource(R.mipmap.ic_uncheck);
                //如果去除选中，就去除背景
                if (status == 1) {
                    viewHolder.mainLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            //遍历时设置已选中的对象的背景
            for (int j = 0; j < itemSelectedList.size(); j++) {
                if (mSipClient.getId().equals(itemSelectedList.get(j).getId())) {
                    if (status == 1) {
                        viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_selected);
                    }
                    isCheck[position] = true;
                }
            }
            return convertView;
        }

        /**
         * 更改选中状态
         */
        public void changeItemStatus(int post) {
            isCheck[post] = isCheck[post] == true ? false : true;
            Logutil.i("post" + post);
            Logutil.i("Post" + isCheck[post]);
            //如果是在线状态就选中添加到集合中
            if (isCheck[post]) {
                Logutil.i("哈哈哈");
                if (webcastDataList.get(post).getState() == 1) {
                    itemSelectedList.add(webcastDataList.get(post));
                    Logutil.i("itemSelectedList-->>" + itemSelectedList.size());
                    for (SipGroupItemInfoBean s : itemSelectedList) {
                        Logutil.d(s.getName());
                    }
                }
            } else {
                //从集合中删除对象
                Logutil.i("咆咆咆");
                itemSelectedList.remove(webcastDataList.get(post));
                Logutil.d("itemSelectedList-->>" + itemSelectedList.size());
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                for (SipGroupItemInfoBean s : itemSelectedList) {
                    Logutil.d(s.getName());
                }
            }
            this.notifyDataSetChanged();
        }

        /**
         * 反选
         */
        public void changeAllItemState() {
            for (int i = 0; i < isCheck.length; i++) {
                //把标识全变成成false
                isCheck[i] = isCheck[i] == true ? false : false;

                viewHolder.mCheckBox.setVisibility(View.GONE);
                viewHolder.mCheckBox.setImageResource(R.mipmap.ic_uncheck);
                //如果去除选中，就去除背景
                viewHolder.mainLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        /**
         * 内部类
         */
        class ViewHolder {
            //显示设备名
            TextView itemName;
            //根布局
            LinearLayout mainLayout;
            //外层父布局
            FrameLayout mRelativeLayout;
            //显示设备类型
            TextView deviceType;
            //状态图标
            ImageView StatusIcon;

            ImageView mCheckBox;


        }
    }

    /**
     * 左则listview展示数据的adapter
     */
    class WebcastGroupAdapter extends BaseAdapter {

        private int selectedItem = -1;

        @Override
        public int getCount() {
            return allWebcastGroupDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return allWebcastGroupDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSelectedItem(int selectedItem) {
            this.selectedItem = selectedItem;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            //复用convertView
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_network_broadcast_sipgroup_listview_layout, null);
                viewHolder.networkBroadcastItemNameLayout = convertView.findViewById(R.id.network_broadcast_item_name_layout);
                viewHolder.networkBroadcastParentLayout = convertView.findViewById(R.id.network_broadcast_list_group_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //设置广播组列表
            SipGroupInfoBean itemBean = allWebcastGroupDataList.get(position);
            viewHolder.networkBroadcastItemNameLayout.setText(itemBean.getName());

            //设置点击选中的背景
            if (position == selectedItem) {
                viewHolder.networkBroadcastParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
            } else {
                viewHolder.networkBroadcastParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            //网络广播组名
            TextView networkBroadcastItemNameLayout;
            //网络广播组父布局
            RelativeLayout networkBroadcastParentLayout;
        }
    }

    /**
     * 获取所有的人员号码
     */
    public String getItemSelectedInfor() {
        if (itemSelectedList == null || itemSelectedList.size() == 0) {
            Logutil.e("未选中广播对象");
            showProgressFail("未选中广播对象");
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < itemSelectedList.size(); i++) {
            builder.append(itemSelectedList.get(i).getNumber() + ",");
        }

        String paramaters = builder.toString();

        return paramaters;
    }

    /**
     * 子线程处理
     */
    class DoSomethingThread extends Thread {
        String requestUrl;

        public DoSomethingThread(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        @Override
        public void run() {
            HttpBasicRequest httpBasicRequest = new HttpBasicRequest(requestUrl, new HttpBasicRequest.GetHttpData() {
                @Override
                public void httpData(String result) {
                    handler.sendEmptyMessage(13);
                    if (TextUtils.isEmpty(result)) {
                        Logutil.e("多人操作失败 is null");
                        return;
                    }
                    Logutil.d("result--->>" + result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject != null) {
                            boolean isSuccess = jsonObject.getBoolean("success");
                            if (isSuccess) {
                                callIsConnected = true;
                                if (requestUrl.contains("sipbroadcast")) {
                                    handler.sendEmptyMessage(8);
                                } else if (requestUrl.contains("siplisten")) {
                                    handler.sendEmptyMessage(9);
                                } else if (requestUrl.contains("sipmeeting")) {
                                    handler.sendEmptyMessage(10);
                                } else {
                                    Logutil.e("错误判断");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logutil.e("HttpBasicRequest is exception-->>" + e.getMessage());
                    }
                }
            });
            new Thread(httpBasicRequest).start();
        }
    }

    /**
     * 显示正在广播的成员的adapter
     */
    class WebcastingAdapter extends BaseAdapter {

        //item选中标识
        private int clickTemp = -1;

        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public WebcastingAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return itemSelectedList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemSelectedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        public void setSeclection(int position) {
            clickTemp = position;
        }

        ViewHolder viewHolder = null;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.broadcasting_gridview_item_layout, null);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.broadcast_item_tv_layout);
                viewHolder.itemParentLayout = (RelativeLayout) convertView.findViewById(R.id.broadcast_parent_layout);
                viewHolder.itemImage = convertView.findViewById(R.id.broadcast_item_imageview_layout);
                viewHolder.parentLayout = convertView.findViewById(R.id.broadcast_item_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupItemInfoBean mSipClient = itemSelectedList.get(position);
            if (mSipClient != null) {
                //显示设备名
                String deviceName = mSipClient.getName();
                if (!TextUtils.isEmpty(deviceName)) {
                    viewHolder.itemName.setText(deviceName);
                } else {
                    viewHolder.itemName.setText("AAAAAAAAAA");
                }
            }

            int status = mSipClient.getState();
            switch (status) {
                case -1://未知状态
                    viewHolder.itemImage.setBackgroundResource(R.mipmap.radio_icon_sentry_offline);
                    viewHolder.itemParentLayout.setBackgroundResource(R.mipmap.radio_bg_offline);
                    break;
                case 1://在线
                    viewHolder.itemImage.setBackgroundResource(R.mipmap.radio_icon_sentry_online);
                    viewHolder.itemParentLayout.setBackgroundResource(R.mipmap.radio_bg_online);
                    break;
                case 2://振铃
                    viewHolder.itemImage.setBackgroundResource(R.mipmap.radio_icon_sentry_ringling);
                    viewHolder.itemParentLayout.setBackgroundResource(R.mipmap.radio_bg_ringing);
                    break;
                case 3://通话
                    viewHolder.itemImage.setBackgroundResource(R.mipmap.radio_icon_sentry_call);
                    viewHolder.itemParentLayout.setBackgroundResource(R.mipmap.radio_bg_call);
                    break;
            }

            //item选中是添加背景
            if (clickTemp == position) {
                viewHolder.parentLayout.setBackgroundResource(R.drawable.sip_selected_bg);
            } else {
                viewHolder.parentLayout.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

        /**
         * 内部类
         */
        class ViewHolder {
            //显示成员图
            ImageView itemImage;
            //显示设备名
            TextView itemName;
            //背景贴图的布局
            RelativeLayout itemParentLayout;
            //根布局（用于item选中布局）
            RelativeLayout parentLayout;

        }
    }

    /**
     * 计时线程开启
     */
    public void threadStart() {
        isTimingThreadWork = true;
        if (timingThread != null && timingThread.isAlive()) {
        } else {
            timingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isTimingThreadWork) {

                        try {
                            Thread.sleep(1 * 1000);
                            handler.sendEmptyMessage(11);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
            timingThread.start();
        }
    }

    /**
     * 计时线程停止
     */
    public void threadStop() {
        if (isTimingThreadWork) {
            if (timingThread != null && timingThread.isAlive()) {
                timingThread.interrupt();
                timingThread = null;
            }
            timingNumber = 0;
            isTimingThreadWork = false;
        }
    }

    /**
     * 静音按钮
     */
    @OnClick(R.id.webcast_mute_btn_layout)
    public void mute(View view) {
        if (callIsConnected) {
            Logutil.d("当前麦克风状态：" + Linphone.getLC().isMicMuted());
            if (!Linphone.getLC().isMicMuted()) {
                Linphone.toggleMicro(true);
                webcastMuteBtn.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
            } else {
                Linphone.toggleMicro(false);
                webcastMuteBtn.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
            }
        }
    }

    /**
     * 解散操作
     */
    @OnClick(R.id.stop_braodcast_btn_layout)
    public void stopBroadcast(View view) {
        //显示或隐藏父布局
        display_all_broadcast_item_parent_layout.setVisibility(View.VISIBLE);
        display_all_broadcasting_item_parent_layout.setVisibility(View.GONE);

        //计时显示修正
        displayBroadcastTimeLayout.setText("");

        //修正按钮的状态
        webcastBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
        webListenBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
        webMeetingBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);

        //销毁显示正在广播成员 的适配器
        if (mWebcastingAdapter != null) {
            mWebcastingAdapter = null;
        }

        //刷新一下显示所有广播成员的适配器
        if (mWebcastItemAdapter != null && getActivity() != null) {
            mWebcastItemAdapter.changeAllItemState();
            mWebcastItemAdapter.notifyDataSetChanged();
            if (itemSelectedList != null && !itemSelectedList.isEmpty()) {
                itemSelectedList.clear();
            }
        }
        //挂断电话
        if (callIsConnected) {
            Linphone.hangUp();
            callIsConnected = false;
        }

        //停止计时
        threadStop();
    }

    /**
     * 踢除正在广播成员的操作
     */
    @OnClick(R.id.kickoff_braodcast_btn_layout)
    public void kickoffBroadcastItem(View view) {
        if (broadcastItemSelected != -1 && itemSelectedList.size() > 0) {
            if (getActivity() != null) {
                if (broadcastItemSelected <= itemSelectedList.size() - 1) {
                    Logutil.d(itemSelectedList.get(broadcastItemSelected).getName());
                    handler.sendEmptyMessage(12);
                    removeBroadcastItem(itemSelectedList.get(broadcastItemSelected).getNumber());
                }
                mWebcastingAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 强拆指定的号码
     */
    public void removeBroadcastItem(final String name) {
        if (TextUtils.isEmpty(name)) {
            Logutil.e("指定拆除号码为空!");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //测试Url
                    String url = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig._RELEASE_URL + name;
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(4000);
                    connection.setConnectTimeout(4000);
                    String authString = SysinfoUtils.getUserName() + ":" + SysinfoUtils.getUserPwd();
                    connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    connection.connect();
                    if (connection.getResponseCode() == 200) {
                        String result = StringUtils.readTxt(connection.getInputStream());
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                boolean isSuccess = jsonObject.getBoolean("success");
                                if (isSuccess) {
                                    handler.sendEmptyMessage(14);
                                } else {
                                    ToastUtils.showShort("强拆失败！");
                                }
                            } catch (Exception e) {
                            }
                        }
                        handler.sendEmptyMessage(13);
                        Logutil.d("拆除成功" + result);
                    } else {
                        Logutil.e("拆除失败" + connection.getResponseCode());
                    }
                } catch (IOException e) {
                    Logutil.e("拆除失败" + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 多人广播
     */
    @OnClick(R.id.network_broadcast_btn_layout)
    public void broadCastFunction(View view) {
        webcastBtn.setBackgroundResource(R.mipmap.dtc_btn2_bg_disable);
        // handler.sendEmptyMessage(8);
        String paramater = getItemSelectedInfor();
        String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig._BROADCAST_URL + paramater + "&moderator=" + SysinfoUtils.getSysinfo().getSipUsername();
        Logutil.d("requestUrl-->>" + requestUrl);
        handler.sendEmptyMessage(12);
        new Thread(new DoSomethingThread(requestUrl)).start();
    }

    /**
     * 多人监听
     */
    @OnClick(R.id.network_monitor_btn_layout)
    public void monitorFunction(View view) {
        if (itemSelectedList != null && itemSelectedList.size() == 1) {
            webListenBtn.setBackgroundResource(R.mipmap.dtc_btn2_bg_disable);
            String paramater = getItemSelectedInfor();
            String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig._LISTEN_URL + paramater + "&moderator=" + SysinfoUtils.getSysinfo().getSipUsername();
            handler.sendEmptyMessage(12);
            new Thread(new DoSomethingThread(requestUrl)).start();
        } else {
            showProgressFail("只限监听一人!");
        }
    }

    /**
     * 多人会议
     */
    @OnClick(R.id.network_meetting_btn_layout)
    public void meettingFunction(View view) {
        webMeetingBtn.setBackgroundResource(R.mipmap.dtc_btn2_bg_disable);
        String paramater = getItemSelectedInfor();
        String requestUrl = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig._MEETING_URL + paramater + "&moderator=" + SysinfoUtils.getSysinfo().getSipUsername();
        handler.sendEmptyMessage(12);
        new Thread(new DoSomethingThread(requestUrl)).start();
    }

    /**
     * 显示下在广播的界面
     */
    private void disPlayBroadcastingUI() {

        display_all_broadcast_item_parent_layout.setVisibility(View.GONE);
        display_all_broadcasting_item_parent_layout.setVisibility(View.VISIBLE);


        mWebcastingAdapter = new WebcastingAdapter(getActivity());
        broadcastinGridViewLayout.setAdapter(mWebcastingAdapter);
        mWebcastingAdapter.notifyDataSetChanged();
        broadcastinGridViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                broadcastItemSelected = position;
                mWebcastingAdapter.setSeclection(position);
                mWebcastingAdapter.notifyDataSetChanged();
                if (itemSelectedList != null && !itemSelectedList.isEmpty()) {
                    if (broadcastItemSelected != -1)
                        Logutil.d("AA" + itemSelectedList.get(broadcastItemSelected).getName());
                }
            }
        });
    }

    /**
     * 踢人操作
     */
    private void kickOffPeople() {
        itemSelectedList.remove(broadcastItemSelected);
        if (itemSelectedList.size() > 0) {
            broadcastItemSelected = 0;
            mWebcastingAdapter.setSeclection(0);
            mWebcastingAdapter.notifyDataSetChanged();
        }
        if (itemSelectedList.size() == 0) {
            //显示或隐藏父布局
            display_all_broadcast_item_parent_layout.setVisibility(View.VISIBLE);
            display_all_broadcasting_item_parent_layout.setVisibility(View.GONE);

            //计时显示修正
            displayBroadcastTimeLayout.setText("");

            //修正按钮的状态
            webcastBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
            webListenBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
            webMeetingBtn.setBackgroundResource(R.drawable.btn_pressed_select_bg);

            //销毁显示正在广播成员 的适配器
            if (mWebcastingAdapter != null) {
                mWebcastingAdapter = null;
            }

            //刷新一下显示所有广播成员的适配器
            if (mWebcastItemAdapter != null && getActivity() != null) {
                mWebcastItemAdapter.changeAllItemState();
                mWebcastItemAdapter.notifyDataSetChanged();
                if (itemSelectedList != null && !itemSelectedList.isEmpty()) {
                    itemSelectedList.clear();
                }
            }
            //挂断电话
            if (callIsConnected) {
                Linphone.hangUp();
                callIsConnected = false;
            }
            //停止计时
            threadStop();

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        currentPageVisible = isVisibleToUser;
        if (isVisibleToUser) {
            //可见时刷新GridView的Adapter
            if (mWebcastItemAdapter != null) {
                mWebcastItemAdapter.notifyDataSetChanged();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {

        //停止定时任务
        if (timingPoolTaskService != null && !timingPoolTaskService.isShutdown()) {
            timingPoolTaskService.shutdown();
            timingPoolTaskService = null;
        }
        //移除handler监听
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroyView();
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    //提示无数据
                    if (currentPageVisible)
                        showProgressFail(getString(R.string.str_resource_no_data));

                    break;
                case 2:
                    //提示无网络
                    if (currentPageVisible)
                        showProgressFail(getString(R.string.str_resource_no_network));
                    break;
                case 3:
                    //处理sip组数据
                    String result = (String) msg.obj;
                    handlerNertworkBroadcastGroupData(result);
                    break;
                case 4:
                    //List适配显示数据
                    disPlayListViewAdapter();
                    break;
                case 5:
                    //默认加载第一组的数据
                    int id = msg.arg1;
                    disPlaySipGroupItemStatus(id);
                    break;
                case 6:
                    //处理某个组内的sip状态数据
                    diplayGridViewAdaperAndRefreshStatus();
                    break;
                case 7:
                    //刷新sip状态
                    String sisStatusResult = (String) msg.obj;
                    handlerSipStatusData(sisStatusResult);
                    break;
                case 8:
                    disPlayBroadcastingUI();
                    displayBroadcastTypeLayout.setText("正在广播");
                    stopOperationBtn.setText("停止广播");
                    threadStart();
                    break;
                case 9:
                    disPlayBroadcastingUI();
                    displayBroadcastTypeLayout.setText("正在监听");
                    stopOperationBtn.setText("停止监听");
                    threadStart();
                    break;
                case 10:
                    disPlayBroadcastingUI();
                    displayBroadcastTypeLayout.setText("正在会议");
                    stopOperationBtn.setText("停止会议");
                    threadStart();
                    break;
                case 11:
                    //广播计时
                    timingNumber++;
                    if (currentPageVisible) {
                        displayBroadcastTimeLayout.setText(TimeUtils.getTime(timingNumber) + "");
                    }
                    break;
                case 12:
                    //显示加载框
                    showProgressDialogWithText("正在连接");
                    break;
                case 13:
                    //取消加载框
                    dismissProgressDialog();
                    break;
                case 14:
                    //踢人成功操作
                    kickOffPeople();
                    break;
            }
        }
    };
}
