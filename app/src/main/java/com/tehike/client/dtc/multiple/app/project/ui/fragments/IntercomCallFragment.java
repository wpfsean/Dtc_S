package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupItemInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.phone.PhoneCallback;
import com.tehike.client.dtc.multiple.app.project.phone.RegistrationCallback;
import com.tehike.client.dtc.multiple.app.project.phone.SipManager;
import com.tehike.client.dtc.multiple.app.project.phone.SipService;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.ui.views.CustomViewPagerSlide;
import com.tehike.client.dtc.multiple.app.project.ui.views.VerticalSeekBar;
import com.tehike.client.dtc.multiple.app.project.utils.ByteUtil;
import com.tehike.client.dtc.multiple.app.project.utils.ContextUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.G711Utils;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.RemoteVoiceRequestUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述：对讲呼叫页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/4 14:06
 */
public class IntercomCallFragment extends BaseFragment {

    /**
     * 切换通话1
     */
    @BindView(R.id.swap_call1_btn_layout)
    Button swapCall1Btn;

    /**
     * 切换通话2
     */
    @BindView(R.id.swap_call2_btn_layout)
    Button swapCall2Btn;

    /**
     * 接通电话
     */
    @BindView(R.id.accept_btn_layout)
    Button acceptCallBtn;


    /**
     * 展示sip组的展开列表
     */
    @BindView(R.id.intercom_group_item_layout)
    public ListView groupItemListView;

    /**
     * 显示sipItem的view
     */
    @BindView(R.id.sipitem_gridview_layout)
    public GridView sipGroupItemGridView;

    /**
     * 显示通话的主界面
     */
    @BindView(R.id.phone_status_layout)
    public RelativeLayout phoneLayout;

    /**
     * 显示sip状态的主界面
     */
    @BindView(R.id.sip_status_layout)
    public RelativeLayout sipLayout;

    /**
     * 对方视频源
     */
    @BindView(R.id.remote_video_layout)
    NodePlayerView remoteVideoLayout;

    /**
     * 本地视频源
     */
    @BindView(R.id.native_video_layout)
    NodePlayerView nativeVideoLayout;

    /**
     * 挂断和拒接按钮
     */
    @BindView(R.id.sip_hangup_btn_layout)
    Button hangUpBtnLayout;

    /**
     * 正在与谁通话
     */
    @BindView(R.id.current_call_number_info_layout)
    public TextView currentCallNumLayout;

    /**
     * 远端加载的进度条
     */
    @BindView(R.id.remote_prbar_layout)
    ProgressBar remotePrLayout;

    /**
     * 远端加载提示
     */
    @BindView(R.id.remote_display_tv_layout)
    TextView remoteTvLayout;

    /**
     * 本地加载进度条
     */
    @BindView(R.id.native_prbar_layout)
    ProgressBar nativePrLayout;

    /**
     * 本地加载提示
     */
    @BindView(R.id.native_display_tv_layout)
    TextView nativeTvLayout;

    /**
     * 远程加载提示父布局
     */
    @BindView(R.id.remote_display_layout)
    RelativeLayout remoteParentLayout;

    /**
     * 本地加载提示父布局
     */
    @BindView(R.id.native_display_layout)
    RelativeLayout nativeParentLayout;

    /**
     * 显示通话时间
     */
    @BindView(R.id.display_phone_time_tv_layout)
    public TextView displayPhoneTimeLayout;

    /**
     * 远端底图的父布局
     */
    @BindView(R.id.remote_video_parent_layout)
    public FrameLayout remoteVideoParentLayout;

    /**
     * 本地底图的父布局
     */
    @BindView(R.id.native_video_parent_layout)
    public FrameLayout nativeVideoParentLayout;

    /**
     * 切换底图的父布局
     */
    @BindView(R.id.phone_parent_layout)
    public LinearLayout phoneParentLayout;

    /**
     * 静音按键
     */
    @BindView(R.id.mute_btn_layout)
    public RadioButton muteRadioButton;

    /**
     * 声音拖动条
     */
    @BindView(R.id.verticalseekbar_external_sound_layout)
    public VerticalSeekBar voiceSeekbar;

    /**
     * 通话录音
     */
    @BindView(R.id.call_recording_btn_layout)
    public RadioButton callRecordingButton;

    /**
     * item选中标识
     */
    int sipItemSelected = -1;

    /**
     * 电话接通标识
     */
    boolean isCallConnected = false;

    /**
     * 语音电话标识（tue语音电话，false视频电话）
     */
    boolean isVoiceCall = true;

    /**
     * 是否来电标识
     */
    boolean isCommingCall = false;

    /**
     * 是否打电话的标识
     */
    boolean isOutCall = false;

    /**
     * 播放对方视频 的播放器
     */
    NodePlayer remotePlayer = null;

    /**
     * 播放本地视频的播放器
     */
    NodePlayer nativePlayer = null;

    /**
     * 盛放sip组数据的集合
     */
    List<SipGroupInfoBean> sipGroupItemList = new ArrayList<>();

    /**
     * 盛放展示sip某个组内数据的集合
     */
    List<SipGroupItemInfoBean> sipItemList = new ArrayList<>();

    /**
     * 当前页面是是否可见
     */
    boolean currentPageVisible = false;

    /**
     * 中间展示sip状态的Adapter
     */
    SipItemAdapter sipItemAdapter;

    /**
     * 时间显示线程是否正在远行
     */
    boolean isTimingThreadWork = false;

    /**
     * 计时的子线程
     */
    Thread timingThread = null;

    /**
     * 计时
     */
    int timingNumber = 0;

    /**
     * 本地视频源的播放地址
     */
    String nativePlayRtspUrl = "";

    /**
     * 显示中心状态
     */
    TextView currentServerCenterTv;

    /**
     * 声音控制对象
     */
    AudioManager mAudioManager = null;

    /**
     * 本机的sip号码
     */
    String currentNativeSipNum = "";

    /**
     * 用于远程喊话请求的Socket
     */
    Socket tcpClientSocket = null;

    /**
     * 声音采样率
     */
    public int frequency = 16000;

    /**
     * 录音时声音缓存大小
     */
    private int rBufferSize;

    /**
     * 录音对象
     */
    private AudioRecord recorder;

    /**
     * 停止标识
     */
    private boolean stopRecordingFlag = false;

    /**
     * 用udp发送声音数据的端口
     */
    int port = -1;

    /**
     * 发送声音数据的Udp
     */
    DatagramSocket udpSocket = null;

    /**
     * 用于显示远程喊话时间（布局）
     */
    TextView speaking_time = null;

    /**
     * 显示喊话时间的线程
     */
    SpeakingTimeThread thread = null;

    /**
     * 记录喊话时间
     */
    int speakingTime = 0;

    /**
     * 选中对象的远程Ip
     */
    String remoteIp = "";

    /**
     * Sip组数据列表
     */
    IntercomSipGroupListViewAdapter mIntercomSipGroupListViewAdapter;

    /**
     * 定时的线程池任务
     */
    private ScheduledExecutorService timingPoolTaskService;

    /**
     * 监听sip资源缓存完成的广播
     */
    SipDataCacheBroadcast mSipDataCacheBroadcast;

    /**
     * 本地缓存的所有的Sip数据
     */
    List<SipBean> allCacheList = null;

    /**
     * 广播监听打电话
     */
    MakeCallBroadcast mMakeCallBroadcast;

    /**
     * 接收到的要拨打电话的sip对象
     */
    SipGroupItemInfoBean mSipGroupItemInfoBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_intercom_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化必要参数
        initializeParamaters();

        //初始化本页面数据
        initializeSipGroupsData();

        //静音监听
        initializeMuteRadioBotton();

        //通话录音功能
        initializeRecordingRadioBotton();

        //seekBar拖动事件
        initializeVoiceSeekbar();

        //初始化视频资源
        initializeVideoData();

        registerMakeCallBroadcast();
    }

    private void initializeVideoData() {
        //取出本地缓存的所有Sip资源（如果为空或异常时，就注册广播，用来监听sip数据是否已缓存完成）
        try {
            allCacheList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            if (allCacheList == null || allCacheList.isEmpty()) {
                registerSipDataDoneBroadcast();
            } else {
                initSipCacheData();
            }
        } catch (Exception e) {
            registerSipDataDoneBroadcast();
        }
    }

    /**
     * 注册广播监听sip数据是否缓存完成
     */
    private void registerSipDataDoneBroadcast() {
        mSipDataCacheBroadcast = new SipDataCacheBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SipDone");
        getActivity().registerReceiver(mSipDataCacheBroadcast, intentFilter);
    }

    /**
     * 显示cpu和rom使用率的广播
     */
    class SipDataCacheBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            allCacheList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            initSipCacheData();
        }

    }

    /**
     * 注册广播监听拨打电话
     */
    private void registerMakeCallBroadcast() {
        mMakeCallBroadcast = new MakeCallBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("makeCall");
        getActivity().registerReceiver(mMakeCallBroadcast, intentFilter);
    }

    /**
     * 打电话广播
     */
    class MakeCallBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean isVideoCall = intent.getBooleanExtra("call", false);
            Bundle bundle = intent.getBundleExtra("bundle");
            mSipGroupItemInfoBean = (SipGroupItemInfoBean) bundle.getSerializable("bean");
            if (mSipGroupItemInfoBean != null) {
                Logutil.d("AA" + mSipGroupItemInfoBean.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RadioGroup bottomRadioGroupLayout = getActivity().findViewById(R.id.bottom_radio_group_layout);
                        CustomViewPagerSlide customViewPagerLayout = getActivity().findViewById(R.id.main_viewpager_layout);
                        bottomRadioGroupLayout.check(bottomRadioGroupLayout.getChildAt(0).getId());
                        customViewPagerLayout.setCurrentItem(0);
                        App.startSpeaking("正在呼叫" + mSipGroupItemInfoBean.getName());

                        if (!isVideoCall){
                            App.startSpeaking("正在呼叫"+mSipGroupItemInfoBean.getName());
                            //视频电话标识
                            isVoiceCall = false;
                            //向外拨打视频电话
                            isOutCall = true;

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //拨号
                            Linphone.callTo(mSipGroupItemInfoBean.getNumber(), false);
                            currentCallNumLayout.setVisibility(View.VISIBLE);
                            currentCallNumLayout.setText(mSipGroupItemInfoBean.getName());
                            handler.sendEmptyMessage(12);
                        }else {
                            App.startSpeaking("正在呼叫"+mSipGroupItemInfoBean.getName());
                            //视频电话标识
                            isVoiceCall = false;
                            //向外拨打视频电话
                            isOutCall = true;

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //拨号
                            Linphone.callTo(mSipGroupItemInfoBean.getNumber(), false);
                            currentCallNumLayout.setVisibility(View.VISIBLE);
                            currentCallNumLayout.setText(mSipGroupItemInfoBean.getName());
                            handler.sendEmptyMessage(12);

                        }
                    }
                });
            }
        }
    }


    /**
     * 初始化本地的sip数据
     */
    private void initSipCacheData() {

        //获取本机的rtsp播放地址
        if (allCacheList != null && !allCacheList.isEmpty() && !TextUtils.isEmpty(currentNativeSipNum)) {
            for (int i = 0; i < allCacheList.size(); i++) {
                SipBean mSipbean = allCacheList.get(i);
                if (mSipbean != null) {
                    if (mSipbean.getNumber().equals(currentNativeSipNum)) {
                        if (mSipbean.getVideoBean() != null) {
                            String rtsp = mSipbean.getVideoBean().getRtsp();
                            if (!TextUtils.isEmpty(rtsp)) {
                                nativePlayRtspUrl = rtsp;
                                break;
                            } else {
                                nativePlayRtspUrl = "";
                            }
                        } else {
                            nativePlayRtspUrl = "";
                        }
                    } else {
                        nativePlayRtspUrl = "";
                    }
                } else {
                    nativePlayRtspUrl = "";
                }
            }
        }
    }

    //接口
    CallBackValue callBackValue;

    /**
     * 绑定
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBackValue = (CallBackValue) getActivity();
    }

    /**
     * 定义接口向宿 主Activity传递数据
     */
    public interface CallBackValue {
        void SendMessageValue(String strValue);
    }

    /**
     * 初始化参数
     */
    private void initializeParamaters() {

        //本地取出sip号码
        if (SysinfoUtils.getSysinfo() != null) {
            currentNativeSipNum = SysinfoUtils.getSysinfo().getSipUsername();
        }

        //音频处理对象(控制通话声音大小)
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        //找到父Activity中的控件（显示当前中心状态<Sip是否注册>）
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        currentServerCenterTv = (TextView) activity.findViewById(R.id.current_server_center_status_layout);
    }

    /**
     * 初始化数据
     */
    private void initializeSipGroupsData() {

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
                    Logutil.e("请求sip组数据异常" + result);
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
     * 声音拖动处理
     */
    private void initializeVoiceSeekbar() {

        //判断声音处理对象是否为空
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        //拖动事件
        voiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voiceSeekbar.setProgress(progress);
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
     * 静音按键监听
     */
    private void initializeMuteRadioBotton() {
        final MuteRadioButtonValue globalValue = new MuteRadioButtonValue();
        muteRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = globalValue.isCheck();
                if (isCheck) {
                    if (v == muteRadioButton) muteRadioButton.setChecked(false);
                    Linphone.toggleMicro(false);
                } else {
                    if (v == muteRadioButton) muteRadioButton.setChecked(true);
                    Linphone.toggleMicro(true);
                }
                globalValue.setCheck(!isCheck);
            }
        });
    }

    /**
     * 通话录音键盘监听
     */
    private void initializeRecordingRadioBotton() {
        final RecordingRadioButtonValue gValue = new RecordingRadioButtonValue();
        callRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = gValue.isCheck();
                if (isCheck) {
                    if (v == callRecordingButton) callRecordingButton.setChecked(false);
                    Logutil.d("正在录音");
                    callRecordingButton.setText("正在录音");
                    callRecordingButton.setTextColor(0xffff00ff);

                } else {
                    if (v == callRecordingButton) callRecordingButton.setChecked(true);
                    Logutil.d("停止录音");
                    callRecordingButton.setText("停止录音");
                    callRecordingButton.setTextColor(0xffffffff);
                }
                gValue.setCheck(!isCheck);
            }
        });
    }

    /**
     * 处理sip分组数据
     */
    private void handlerSipGroupData(String result) {

        //先清空集合防止
        if (sipGroupItemList != null && sipGroupItemList.size() > 0) {
            sipGroupItemList.clear();
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("errorCode")) {
                Logutil.w("请求不到数据信息");
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
                    sipGroupItemList.add(sipGroupInfoBean);
                }
            }
            handler.sendEmptyMessage(4);
        } catch (Exception e) {
            WriteLogToFile.info("解析Sip分组数据异常" + e.getMessage()+"--->>>"+result);
            Logutil.e("解析Sip分组数据异常" + e.getMessage());
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 上部List适配数据
     */
    private void disPlayListViewAdapter() {
        //判断是否有要适配的数据
        if (sipGroupItemList == null || sipGroupItemList.size() == 0) {
            handler.sendEmptyMessage(1);
            Logutil.e("适配的数据时无数据");
            return;
        }
        mIntercomSipGroupListViewAdapter = new IntercomSipGroupListViewAdapter();
        //显示左侧的sip分组页面
        groupItemListView.setAdapter(mIntercomSipGroupListViewAdapter);
        mIntercomSipGroupListViewAdapter.setSelectedItem(0);
        mIntercomSipGroupListViewAdapter.notifyDataSetChanged();

        //默认加载第一组的数据
        Message handlerMess = new Message();
        handlerMess.arg1 = sipGroupItemList.get(0).getId();
        handlerMess.what = 5;
        handler.sendMessage(handlerMess);

        //点击事件
        groupItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIntercomSipGroupListViewAdapter.setSelectedItem(position);
                mIntercomSipGroupListViewAdapter.notifyDataSetChanged();
                SipGroupInfoBean mSipGroupInfoBean = sipGroupItemList.get(position);
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
        if (sipItemAdapter != null) {
            sipItemAdapter = null;
            sipItemList.clear();
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

                if (sipItemList != null && sipItemList.size() > 0) {
                    sipItemList.clear();
                }

                Logutil.d("组数据" + result);

                //解析sip资源
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.isNull("errorCode")) {
                        Logutil.w("请求勤务通信组数据数据信息异常"+result);
                        WriteLogToFile.info("请求勤务通信组数据数据信息异常"+result);
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
                            sipItemList.add(groupItemInfoBean);
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

                    if (!NetworkUtils.isConnected()) {
                        Logutil.e("刷新状态时网络异常");
                        handler.sendEmptyMessage(2);
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

                //获取本机的Sip状态
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(currentNativeSipNum)) {
                    if (currentNativeSipNum.equals(name)) {
                        int nativeStatus = state;
                        if (nativeStatus == 0) {
                            AppConfig.SIP_STATUS = false;
                            handler.sendEmptyMessage(18);
                        } else if (nativeStatus == 1) {
                            AppConfig.SIP_STATUS = true;
                            handler.sendEmptyMessage(17);
                        } else {
                            AppConfig.SIP_STATUS = false;
                            handler.sendEmptyMessage(18);
                        }
                    }
                }
                SipStatusInfoBean mSipStatusInfoBean = new SipStatusInfoBean();
                mSipStatusInfoBean.setName(name);
                mSipStatusInfoBean.setState(state);
                sipStatusList.add(mSipStatusInfoBean);
            }

            for (int n = 0; n < sipStatusList.size(); n++) {
                for (int k = 0; k < sipItemList.size(); k++) {
                    if (sipStatusList.get(n).getName().equals(sipItemList.get(k).getNumber())) {
                        sipItemList.get(k).setState(sipStatusList.get(n).getState());
                    }
                }
            }
            if (sipItemAdapter != null)
                sipItemAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            WriteLogToFile.info("解析SIp状态时异常:-->>" + e.getMessage()+"---->>>"+sisStatusResult);
            Logutil.e("解析SIp状态时异常:-->>" + e.getMessage());
        }
    }

    /**
     * Sip状态类（用于刷新状态时实体类封闭）
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
        if (sipItemAdapter == null)
            sipItemAdapter = new SipItemAdapter(getActivity());
        if (currentPageVisible) {
            sipGroupItemGridView.setAdapter(sipItemAdapter);
            sipItemAdapter.notifyDataSetChanged();
        }

        //item点击事件
        sipGroupItemGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (sipItemAdapter != null) {
                    if (sipItemList != null && sipItemList.size() > 0) {
                        //判断选中的是否是在线状态对象
                        if (sipItemList.get(position).getState() == 1 || sipItemList.get(position).getState() == 3) {
                            sipItemSelected = position;
                        } else {
                            sipItemSelected = -1;
                        }
                        if (sipItemSelected != -1) {
                            Logutil.d("sipItemSelected--->>>" + sipItemSelected);
                            Logutil.d("sipItemSelected---->>>>" + sipItemList.get(sipItemSelected).toString());
                        }
                        sipItemAdapter.setSeclection(position);
                        sipItemAdapter.notifyDataSetChanged();
                    }
                }
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
    class SipItemAdapter extends BaseAdapter {
        //选中对象的标识
        private int clickTemp = -1;
        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public SipItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return sipItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return sipItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSeclection(int position) {
            clickTemp = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_sipstatus_item, null);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.mRelativeLayout = (FrameLayout) convertView.findViewById(R.id.item_layout);
                viewHolder.mainLayout = convertView.findViewById(R.id.sipstatus_main_layout);
                viewHolder.deviceType = convertView.findViewById(R.id.device_type_layout);
                viewHolder.StatusIcon = convertView.findViewById(R.id.sip_status_icon_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupItemInfoBean mSipClient = sipItemList.get(position);
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
                int status = mSipClient.getState();
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

            if (clickTemp == position) {
                //默认只有在线状态对能被选中
                if (sipItemList.get(position).getState() == 1 || sipItemList.get(position).getState() == 3) {
                    viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_selected);
                    // viewHolder.mainLayout.setBackgroundResource(R.drawable.sip_selected_bg);

                }
            } else {
                viewHolder.mainLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
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


        }
    }

    /**
     * 勤务通信页面左则listview展示数据的adapter
     */
    class IntercomSipGroupListViewAdapter extends BaseAdapter {

        private int selectedItem = -1;

        @Override
        public int getCount() {
            return sipGroupItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return sipGroupItemList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_intercom_sipgroup_listview_layout, null);
                viewHolder.sipItemNameLayout = convertView.findViewById(R.id.sipgroup_item_name_layout);
                viewHolder.sipParentLayout = convertView.findViewById(R.id.intercom_sip_group_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //显示组名称
            SipGroupInfoBean itemBean = sipGroupItemList.get(position);
            viewHolder.sipItemNameLayout.setText(itemBean.getName());
            //是否选中
            if (position == selectedItem) {
                viewHolder.sipParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
                viewHolder.sipItemNameLayout.setTextColor(0xffffe034);
            } else {
                viewHolder.sipParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
                viewHolder.sipItemNameLayout.setTextColor(0xffffffff);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            //Sip组名称
            TextView sipItemNameLayout;
            //Sip组所在的父布局
            RelativeLayout sipParentLayout;
        }
    }

    /**
     * 静音的radio值保存（来源网络，可用sharedpreference代替）
     */
    class MuteRadioButtonValue {
        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }

        private boolean isCheck;
    }

    /**
     * 通话录音的radio值保存（来源网络，可用sharedpreference代替）
     */
    class RecordingRadioButtonValue {
        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }

        private boolean isCheck;
    }

    /**
     * 按键点击事件
     */
    @OnClick({R.id.swap_call1_btn_layout, R.id.swap_call2_btn_layout, R.id.accept_btn_layout, R.id.intercom_voice_btn_layout, R.id.intercom_video_btn_layout, R.id.sip_hangup_btn_layout, R.id.voice_lose_btn_layout, R.id.call_demolition_btn_layout, R.id.remote_warring_btn_layout, R.id.remote_gunshot_btn_layout, R.id.remote_speak_btn_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.swap_call1_btn_layout:
                swapFirstCall();
                break;
            case R.id.swap_call2_btn_layout:
                swapSecondCall();
                break;
            case R.id.accept_btn_layout:
                //判断来电，并且是非接听状态下
                acceptIncomingCall();
                break;
            case R.id.intercom_voice_btn_layout:
                //拨打语音电话
                makeVoiceCall();
                break;
            case R.id.intercom_video_btn_layout:
                //向外打视频电话
                makeVideoCall();
                break;
            case R.id.sip_hangup_btn_layout:
                //挂电话
                handupCall();
                break;
            case R.id.voice_lose_btn_layout:
                //音量减
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
            case R.id.call_demolition_btn_layout:
                //强拆通话
                forcedCall();
                break;
            case R.id.remote_warring_btn_layout:
                //远程警告
                remoteWarring();
                break;
            case R.id.remote_speak_btn_layout:
                //远程喊话
                remoteSpeaking();
                break;
            case R.id.remote_gunshot_btn_layout:
                remoteGunshotWarring();
                //远程鸣枪
                break;
        }
    }

    /**
     * 远程语音警告
     */
    private void remoteWarring() {
        String remoteIp = "";
        //判断远程操作对象是否选中
        if (sipItemSelected == -1) {
            showProgressFail("请选择操作对象");
            return;
        }
        //获取远程操作对象的Ip
        if (sipItemList.get(sipItemSelected) != null) {
            remoteIp = sipItemList.get(sipItemSelected).getIpAddress();
            if (TextUtils.isEmpty(remoteIp)) {
                showProgressFail("无号码");
                return;
            }
        }
        //判断网络是否正常
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //子线程远程警告
        RemoteVoiceRequestUtils remoteVoiceRequestUtils = new RemoteVoiceRequestUtils(2, remoteIp, new RemoteVoiceRequestUtils.RemoteCallbck() {
            @Override
            public void remoteStatus(String status) {
                Logutil.i(status);
                if (TextUtils.isEmpty(status) || status.contains("error")) {
                    handler.sendEmptyMessage(20);
                    return;
                }
                Message message = new Message();
                message.what = 21;
                message.obj = status;
                handler.sendMessage(message);
            }
        });
        new Thread(remoteVoiceRequestUtils).start();
    }

    /**
     * 远程鸣枪警告
     */
    private void remoteGunshotWarring() {
        String remoteIp = "";
        //判断远程操作对象是否选中
        if (sipItemSelected == -1) {
            showProgressFail("请选择操作对象");
            return;
        }
        //获取远程操作对象的Ip
        if (sipItemList.get(sipItemSelected) != null) {
            remoteIp = sipItemList.get(sipItemSelected).getIpAddress();
            if (TextUtils.isEmpty(remoteIp)) {
                showProgressFail("无号码");
                return;
            }
        }
        //判断网络是否正常
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //子线程远程警告
        RemoteVoiceRequestUtils remoteVoiceRequestUtils = new RemoteVoiceRequestUtils(3, remoteIp, new RemoteVoiceRequestUtils.RemoteCallbck() {
            @Override
            public void remoteStatus(String status) {
                if (TextUtils.isEmpty(status) || status.contains("error")) {
                    handler.sendEmptyMessage(20);
                    return;
                }
                Message message = new Message();
                message.what = 21;
                message.obj = status;
                handler.sendMessage(message);
            }
        });
        new Thread(remoteVoiceRequestUtils).start();
    }

    /**
     * 远程喊话
     */
    private void remoteSpeaking() {
        if (sipItemSelected == -1) {
            showProgressFail("请选择操作对象");
            return;
        }
        //获取远程操作对象的Ip
        if (sipItemList.get(sipItemSelected) != null) {
            remoteIp = sipItemList.get(sipItemSelected).getIpAddress();
            if (TextUtils.isEmpty(remoteIp)) {
                showProgressFail("无号码");
                return;
            }
        }
        //判断网络是否正常
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        //获取此对象的设备类型
        String deviceType = sipItemList.get(sipItemSelected).getDeviceType();
        if (!TextUtils.isEmpty(deviceType)) {
            if (deviceType.equals("TH-C6000")) {
                handler.sendEmptyMessage(22);
                return;
            }
        }
        //先去请求喊话的操作(见协议)
        requestSpeakingSocket sendSoundData = new requestSpeakingSocket(remoteIp);
        new Thread(sendSoundData).start();

    }

    /**
     * 进行强拆通话
     */
    private void forcedCall() {
        String number = "";
        if (sipItemSelected == -1) {
            showProgressFail("请选择");
            return;
        }
        //获取当前的拨号号码
        if (sipItemList.get(sipItemSelected) != null) {
            number = sipItemList.get(sipItemSelected).getNumber();
            if (TextUtils.isEmpty(number)) {
                showProgressFail("无号码");
                return;
            }
        }
        String url = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._SIP_RELEASE + number;
        Logutil.d(number);
        Logutil.d(url);
        HttpBasicRequest mHttpBasicRequest = new HttpBasicRequest(url, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                Logutil.d(result + "AAAAAAAAAAAA");
                //判断返回数据是否为空
                if (TextUtils.isEmpty(result)) {
                    Logutil.e("强拆通话失败!");
                    handler.sendEmptyMessage(19);
                    return;
                }
                //判断返回数据是否异常
                if (result.contains("Execption")) {
                    Logutil.e("强拆通话失败!");
                    handler.sendEmptyMessage(19);
                    return;
                }
                //解析数据
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean isSuccess = jsonObject.getBoolean("success");
                    if (isSuccess) {
                        //强拆成功
                        Logutil.d("强拆成功!");
                        makeVoiceCall();
                    } else {
                        //强拆通话失败
                        handler.sendEmptyMessage(19);
                    }

                } catch (Exception e) {
                    //异常时提示强拆失败
                    handler.sendEmptyMessage(19);
                }
            }
        });
        new Thread(mHttpBasicRequest).start();
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
                            handler.sendEmptyMessage(15);
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
     * 初始化播放器
     */
    private void initializePlayer() {
        //远程（对方）
        if (remotePlayer == null) {
            remotePlayer = new NodePlayer(getActivity());
            remotePlayer.setPlayerView(remoteVideoLayout);
        }
        //本地（自己）
        if (nativePlayer == null) {
            nativePlayer = new NodePlayer(getActivity());
            nativePlayer.setPlayerView(nativeVideoLayout);
        }
    }

    /**
     * 播放对方的视频源视频
     */
    private void playRemoteVideo() {

        LinphoneCall c = SipManager.getLc().getCurrentCall();
        if (c == null) {
            return;
        }
        if (c.getRemoteAddress() == null) {
            return;
        }
        SipBean sipbean = querySipBeanFromSipNumber(c.getRemoteAddress().getUserName());
        if (sipbean == null || sipbean.getVideoBean() == null) {
            remoteVideoParentLayout.setVisibility(View.VISIBLE);
            remoteTvLayout.setVisibility(View.VISIBLE);
            remoteTvLayout.setTextColor(0xff00ff00);
            remoteTvLayout.setText("未加载到对方视频源!");
        } else {
            //判断对方是否有面部视频
            if (sipbean.getVideoBean() == null) {
                return;
            }
            //判断是否在播放地址
            String rtsp = sipbean.getVideoBean().getRtsp();
            Logutil.d("remotePlayer--->>>" + rtsp);
            if (TextUtils.isEmpty(rtsp)) {
                remoteTvLayout.setVisibility(View.VISIBLE);
                remoteTvLayout.setTextColor(0xff00ff00);
                remotePrLayout.setVisibility(View.INVISIBLE);
                remoteTvLayout.setText("未加载到对方的视频源!");
            } else {
                //判断播放器是否已实例化
                if (remotePlayer == null) {
                    initializePlayer();
                }
                //判断播放器是否正在播放
                if (remotePlayer != null && remotePlayer.isPlaying()) {
                    remotePlayer.stop();
                }
                //开始播放
                remotePlayer.setInputUrl(rtsp);
                remotePlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
                remotePlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                    @Override
                    public void onEventCallback(NodePlayer player, int event, String msg) {
                        if (player == remotePlayer) {
                            if (getActivity() != null) {
                                Logutil.d("remotePlayer--->>>" + event);

                                if (event == 1001 || event == 1102 || event == 1104) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            remotePrLayout.setVisibility(View.GONE);
                                            remoteTvLayout.setVisibility(View.GONE);
                                        }
                                    });
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            remotePrLayout.setVisibility(View.INVISIBLE);
                                            remoteTvLayout.setVisibility(View.VISIBLE);
                                            remoteTvLayout.setText("重新连接...");
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                remotePlayer.setVideoEnable(true);
                remotePlayer.start();
            }
        }
    }

    /**
     * 播放自己的视频源
     */
    private void playNativeVideo(final String rtsp1) {

        //判断视频源地址是否为空
        if (TextUtils.isEmpty(rtsp1)) {
            nativeTvLayout.setVisibility(View.VISIBLE);
            nativeTvLayout.setTextColor(0xff00ff00);
            nativePrLayout.setVisibility(View.INVISIBLE);
            nativeTvLayout.setText("未配置本机视频源!");
        } else {
            //判断播放器是为空
            if (nativePlayer == null) {
                initializePlayer();
            }
            //判断播放器是否正在播放
            if (nativePlayer != null && nativePlayer.isPlaying()) {
                nativePlayer.stop();
            }
            //开始播放
            nativePlayer.setInputUrl(rtsp1);
            nativePlayer.setAudioEnable(AppConfig.ISVIDEOSOUNDS);
            nativePlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                @Override
                public void onEventCallback(NodePlayer player, int event, String msg) {
                    if (getActivity() != null) {
                        Logutil.d("nativePlayer--->>>" + event);
                        Logutil.d("nativePlayer--->>>" + rtsp1);

                        if (event == 1000) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nativePrLayout.setVisibility(View.INVISIBLE);
                                    nativeTvLayout.setVisibility(View.VISIBLE);
                                    nativeTvLayout.setText("正在连接...");
                                }
                            });
                        } else if (event == 1001 || event == 1102 || event == 1104) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nativePrLayout.setVisibility(View.GONE);
                                    nativeTvLayout.setVisibility(View.GONE);
                                }
                            });
                        } else if (event == 1002 || event == 1003) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nativePrLayout.setVisibility(View.INVISIBLE);
                                    nativeTvLayout.setVisibility(View.VISIBLE);
                                    nativeTvLayout.setText("重新连接...");
                                }
                            });
                        } else if (event == 1005 || event == 1006) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nativePrLayout.setVisibility(View.INVISIBLE);
                                    nativeTvLayout.setVisibility(View.VISIBLE);
                                    nativeTvLayout.setText("网络异常...");
                                }
                            });
                        }


//                        if (event == 1001 || event == 1102 || event == 1104) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    nativePrLayout.setVisibility(View.GONE);
//                                    nativeTvLayout.setVisibility(View.GONE);
//                                }
//                            });
//                        } else {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    nativePrLayout.setVisibility(View.INVISIBLE);
//                                    nativeTvLayout.setVisibility(View.VISIBLE);
//                                    nativeTvLayout.setText("重新连接...");
//                                }
//                            });
//                        }
                    }
                }
            });
            nativePlayer.setVideoEnable(true);
            nativePlayer.start();
        }
    }

    /**
     * 释放播放器
     */
    private void releasePlayer() {

        if (remotePlayer != null) {
            remotePlayer.stop();
        }
        if (nativePlayer != null) {
            nativePlayer.stop();
        }
    }

    /**
     * 用于远程喊话请求的子线程
     */
    class requestSpeakingSocket extends Thread {
        //远程喊话对象的Ip
        String remoteIp;

        //构造方法
        public requestSpeakingSocket(String remoteIp) {
            this.remoteIp = remoteIp;
        }

        @Override
        public void run() {
            try {
                if (tcpClientSocket == null) {
                    //创建tcp请求
                    tcpClientSocket = new Socket(remoteIp, AppConfig.REMOTE_PORT);
                    //设置请求超时
                    tcpClientSocket.setSoTimeout(3 * 1000);
                    //请求的总数据
                    byte[] requestData = new byte[4 + 4 + 4 + 4];
                    // flag
                    byte[] flag = new byte[4];
                    flag = "RVRD".getBytes();
                    System.arraycopy(flag, 0, requestData, 0, flag.length);

                    // action
                    byte[] action = new byte[4];
                    action[0] = 1;// 0無操作，1遠程喊話，2播放語音警告，3播放鳴槍警告，4遠程監聽，5單向廣播
                    action[1] = 0;
                    action[2] = 0;
                    action[3] = 0;
                    System.arraycopy(action, 0, requestData, 4, action.length);

                    // 接受喊话时=接收语音数据包的 UDP端口(测试)
                    byte[] parameter = new byte[4];
                    System.arraycopy(parameter, 0, requestData, 8, parameter.length);
                    // // 向服务器发消息
                    OutputStream os = tcpClientSocket.getOutputStream();// 字节输出流
                    os.write(requestData);
                    //   tcpSocket.shutdownOutput();// 关闭输出流
                    // 读取服务器返回的消息
                    InputStream in = tcpClientSocket.getInputStream();
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
                    String r_questMess = RemoteVoiceRequestUtils.getMessage(r_questCode);

                    // 返回的状态
                    byte[] r_status = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_status[i] = data[i + 8];
                    }
                    int r_statusCode = r_status[0];
                    String r_statusMess = RemoteVoiceRequestUtils.getStatusMessage(r_statusCode);
                    Logutil.i("应答状态:" + r_statusCode + "\t" + r_statusMess);

                    // 返回参数
                    byte[] r_paramater = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        r_paramater[i] = data[i + 12];
                    }
                    Logutil.i(Arrays.toString(r_paramater));
                    int port = ByteUtil.bytesToInt(r_paramater, 0);

                    if (r_statusMess.equals("Accept")) {
                        Message message = new Message();
                        message.arg1 = port;
                        message.what = 24;
                        handler.sendMessage(message);
                        Logutil.d("喊话请求同意");
                    } else {
                        handler.sendEmptyMessage(23);
                        Logutil.d("喊话请求拒绝");
                        if (tcpClientSocket != null) {
                            tcpClientSocket.close();
                            tcpClientSocket = null;
                        }
                    }
                }
            } catch (Exception e) {
                if (tcpClientSocket != null) {
                    try {
                        tcpClientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    tcpClientSocket = null;
                }
                handler.sendEmptyMessage(23);
                Logutil.e("error:" + e.getMessage());
            }
        }
    }

    /**
     * 显示通话界面
     */
    private void disPlayCallView() {
        sipLayout.setVisibility(View.GONE);
        phoneLayout.setVisibility(View.VISIBLE);
        remoteParentLayout.setVisibility(View.VISIBLE);
        nativeParentLayout.setVisibility(View.VISIBLE);
        remoteVideoLayout.setVisibility(View.VISIBLE);
        nativeVideoLayout.setVisibility(View.VISIBLE);
        remotePrLayout.setVisibility(View.VISIBLE);
        remoteTvLayout.setVisibility(View.VISIBLE);
        nativePrLayout.setVisibility(View.VISIBLE);
        nativeTvLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示Sip状态页面
     */
    private void disPlaySipView() {

        sipLayout.setVisibility(View.VISIBLE);
        phoneLayout.setVisibility(View.GONE);

        remotePrLayout.setVisibility(View.GONE);
        remoteTvLayout.setVisibility(View.GONE);
        nativePrLayout.setVisibility(View.GONE);
        nativeTvLayout.setVisibility(View.GONE);

        remoteParentLayout.setVisibility(View.INVISIBLE);
        nativeParentLayout.setVisibility(View.INVISIBLE);
        remoteVideoLayout.setVisibility(View.INVISIBLE);
        nativeVideoLayout.setVisibility(View.INVISIBLE);

        remoteVideoParentLayout.setVisibility(View.INVISIBLE);
        nativeVideoParentLayout.setVisibility(View.INVISIBLE);
        displayPhoneTimeLayout.setText("00:00");
        phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);
    }

    /**
     * 根据 sip号码通过字典查询rtsp地址
     */
    public SipBean querySipBeanFromSipNumber(String from) {

        SipBean mSipBean = null;

        //获取本机的rtsp播放地址
        if (allCacheList != null && !allCacheList.isEmpty() && !TextUtils.isEmpty(from)) {
            for (int i = 0; i < allCacheList.size(); i++) {
                if (from.equals(allCacheList.get(i).getNumber())) {
                    mSipBean = allCacheList.get(i);
                    break;
                }
            }
        }
        return mSipBean;
    }

    /**
     * 初始化录音 参数
     */
    public void initializeRecordParamater() {
        try {
            //设置录音缓冲区大小
            rBufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //获取录音机对象
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, rBufferSize);
        } catch (Exception e) {
            String msg = "ERROR init: " + e.getStackTrace();
            Logutil.e("error:" + msg);
        }
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        //更改停止录音标识
        stopRecordingFlag = false;
        //开启录音线程
        mRecordingVoiceThread = new RecordingVoiceThread();
        mRecordingVoiceThread.start();
    }

    /**
     * 结束录音
     */
    public void stopRecord() throws IOException {

        //Tcp断开连接
        if (tcpClientSocket != null) {
            tcpClientSocket.close();
            tcpClientSocket = null;
        }
        //Udp断开连接
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        //更改停止标识
        stopRecordingFlag = true;
    }

    /**
     * 录音线程
     */
    class RecordingVoiceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                byte[] tempBuffer, readBuffer = new byte[rBufferSize];
                int bufResult = 0;
                recorder.startRecording();
                while (!stopRecordingFlag) {
                    bufResult = recorder.read(readBuffer, 0, rBufferSize);
                    if (bufResult > 0 && bufResult % 2 == 0) {
                        tempBuffer = new byte[bufResult];
                        System.arraycopy(readBuffer, 0, tempBuffer, 0, rBufferSize);
                        G711EncodeVoice(tempBuffer);
                    }
                }
                recorder.stop();
                Looper.prepare();
                Looper.loop();
            } catch (Exception e) {
                String msg = "ERROR AudioRecord: " + e.getMessage();
                Logutil.e(msg);
                Looper.prepare();
                Looper.loop();
            }
        }
    }

    /**
     * G711a声音压缩
     */
    private void G711EncodeVoice(byte[] tempBuffer) {
        DatagramPacket dp = null;
        try {
            dp = new DatagramPacket(G711Utils.encode(tempBuffer), G711Utils.encode(tempBuffer).length, InetAddress.getByName(remoteIp), port);
            try {
                if (udpSocket == null)
                    udpSocket = new DatagramSocket();
                udpSocket.send(dp);
                Logutil.i("正在发送...." + Arrays.toString(G711Utils.encode(tempBuffer)) + "\n长度" + G711Utils.encode(tempBuffer).length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 录音线程
     */
    private RecordingVoiceThread mRecordingVoiceThread;

    /**
     * 显示喊话的提示框
     */
    private void showSpeakingDialog() {
        //加载dialog布局
        View view = View.inflate(getActivity(), R.layout.activity_port_prompt_dialog, null);
        speaking_time = view.findViewById(R.id.speaking_time_layout);
        //正在向谁喊话（布局）
        TextView speaking_name = view.findViewById(R.id.speaking_name_layout);
        //关闭（布局）
        TextView close_dialog = view.findViewById(R.id.seaking_close_dialog_layout);
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        //点击外面使不消失
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();
        //此处设置位置窗体大小
        dialog.getWindow().setLayout(ContextUtils.dip2px(getActivity(), 280), ContextUtils.dip2px(getActivity(), 280));

        //判断dialog是否正在显示
        if (dialog.isShowing()) {
            //运行喊话计时线程
            if (thread == null)
                thread = new SpeakingTimeThread();
            thread.start();

            String deviceName = "";
            //根据Sip号码查询设备名称
            List<SipBean> mList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getIpAddress().equals(remoteIp)) {
                    deviceName = mList.get(i).getName();
                    break;
                }
            }
            //显示
            String str = "正在向\t<<< <b><font color=#ff0000>" + deviceName + "</b><font/> >>>喊话";
            speaking_name.setText(Html.fromHtml(str));
        }
        //关闭按钮的点击事件
        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                speakingTime = 0;
                try {
                    stopRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 显示喊话的时间
     */
    private void displaySpeakingTime() {
        speakingTime += 1;
        String speakTime = TimeUtils.getTime(speakingTime);
        if (speaking_time != null)
            speaking_time.setText(speakTime);
    }

    /**
     * 喊话计时
     */
    class SpeakingTimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logutil.i("Thread error:" + e.getMessage());
                }
                handler.sendEmptyMessage(25);
            } while (!stopRecordingFlag);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        currentPageVisible = isVisibleToUser;
        Logutil.w("Intercom--->>" + isVisibleToUser);
        if (isVisibleToUser) {
            //可见时刷新GridView的Adapter
            if (sipItemAdapter != null) {
                sipItemAdapter.notifyDataSetChanged();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }


    @Override
    public void onResume() {

        //更改sip的状态标识
        if (AppConfig.SIP_STATUS) {
            handler.sendEmptyMessage(17);
        } else {
            handler.sendEmptyMessage(18);
        }
        //启动Sip服务
        if (!SipService.isReady() || !SipManager.isInstanceiated()) {
            Linphone.startService(App.getApplication());

        }

        //SIp注册状态和来电状态监听回调
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationOk() {
                handler.sendEmptyMessage(17);
            }

            @Override
            public void registrationFailed() {
                handler.sendEmptyMessage(18);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {
                Logutil.i("super.incomingCall(linphoneCall);" + SipManager.getLc().getCalls().length);

                if (SipManager.getLc().getCalls().length >= 2) {
                    handler.sendEmptyMessage(8);
                }

                //来电标识
                isCommingCall = true;
                //视频电话标识
                isVoiceCall = false;

                //第一个来电
                if (!firstInCommingCallConteted) {
                    handler.sendEmptyMessage(26);
                }

                //第二个来电
                if (isCallConnected) {
                    handler.sendEmptyMessage(29);
                }


            }

            @Override
            public void outgoingInit() {
                AppConfig.IS_CALLING = true;
                Logutil.i("super.outgoingInit();");
            }

            @Override
            public void callConnected() {
                AppConfig.IS_CALLING = true;
                Logutil.i("super.callConnected();");
                //fragment向宿主Activity传递数据
                callBackValue.SendMessageValue("true");

                //向外拨打的语音电话接通
                if (isOutCall && isVoiceCall) {
                    handler.sendEmptyMessage(10);
                }
                //向外拨打的视频电话接通
                if (isOutCall && !isVoiceCall) {
                    handler.sendEmptyMessage(13);
                }

                //第一个来电视频电话接通
                if (isCommingCall && !isVoiceCall) {
                    handler.sendEmptyMessage(27);
                }

                //第二个来电视频电话接通
                if (isCommingCall && !isVoiceCall && isCallConnected && firstInCommingCallConteted) {
                    handler.sendEmptyMessage(30);
                }


            }

            @Override
            public void callEnd() {
                AppConfig.IS_CALLING = false;
                Logutil.i("super.callEnd();");
                //fragment向宿主Activity传递数据
                callBackValue.SendMessageValue("false");
            }

            @Override
            public void callReleased() {
                AppConfig.IS_CALLING = false;
                Logutil.i("super.callReleased();");
                //fragment向宿主Activity传递数据
                callBackValue.SendMessageValue("false");

                //语音电话释放
                if (isOutCall && isVoiceCall) {
                    handler.sendEmptyMessage(11);
                }
                //向个拨打的视频电话释放
                if (isOutCall && !isVoiceCall) {
                    handler.sendEmptyMessage(14);
                }
                //来电视频电话释放
                if (isCommingCall && !isVoiceCall) {
                    handler.sendEmptyMessage(28);
                }

            }

            @Override
            public void error() {
                AppConfig.IS_CALLING = false;
                Logutil.i("     super.error();");
            }
        });
        super.onResume();
    }

    /**
     * 第一个电话接通
     */
    boolean firstInCommingCallConteted = false;

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
        //音频管理对象
        if (mAudioManager != null) {
            mAudioManager = null;
        }
        //停止录音
        stopRecordingFlag = false;

        if (mSipDataCacheBroadcast != null)
            getActivity().unregisterReceiver(mSipDataCacheBroadcast);

        if (mMakeCallBroadcast != null)
            getActivity().unregisterReceiver(mMakeCallBroadcast);

        super.onDestroyView();
    }

    private void swapSecondCall() {

        LinphoneCall[] calls = SipManager.getLc().getCalls();
        if (calls.length >= 2) {
            SipManager.getLc().resumeCall(calls[1]);
            SipManager.getLc().pauseCall(calls[0]);
            playRemoteVideo();
            currentCallNumLayout.setText(calls[1].getRemoteAddress().getUserName());
            swapCall1Btn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
            swapCall2Btn.setBackgroundResource(R.mipmap.dtc_btn2_bg_pressed);

        } else {
            SipManager.getLc().resumeCall(SipManager.getLc().getCurrentCall());
        }

    }

    private void swapFirstCall() {
        LinphoneCall[] calls = SipManager.getLc().getCalls();
        if (calls.length >= 2) {
            SipManager.getLc().resumeCall(calls[0]);
            SipManager.getLc().pauseCall(calls[1]);
            playRemoteVideo();
            currentCallNumLayout.setText(calls[0].getRemoteAddress().getUserName());
            swapCall1Btn.setBackgroundResource(R.mipmap.dtc_btn2_bg_pressed);
            swapCall2Btn.setBackgroundResource(R.drawable.btn_pressed_select_bg);

        } else {
            SipManager.getLc().resumeCall(SipManager.getLc().getCurrentCall());
        }

    }

    /**
     * 挂断电话
     */
    private void handupCall() {

        //结束当前向外拨打的语音电话
        if (isOutCall && isVoiceCall) {
            SipManager.getLc().terminateCall(SipManager.getLc().getCurrentCall());
        }

        //结束当前向外拨打的视频电话
        if (isOutCall && !isVoiceCall) {
            SipManager.getLc().terminateCall(SipManager.getLc().getCurrentCall());
        }

        //结束当前来电的第一个电话

        LinphoneCall[] calls = SipManager.getLc().getCalls();
        if (calls.length >= 2) {
            SipManager.getLc().terminateCall(SipManager.getLc().getCurrentCall());
            LinphoneCall[] call = SipManager.getLc().getCalls();
            if (call.length == 1) {
                SipManager.getLc().resumeCall(call[0]);
                playRemoteVideo();
                currentCallNumLayout.setText(call[0].getRemoteAddress().getUserName());
                swapCall2Btn.setVisibility(View.GONE);
                swapCall1Btn.setVisibility(View.GONE);
            }
        }
        if (calls.length == 1) {
            SipManager.getLc().terminateAllCalls();
            disPlaySipView();
            threadStop();
            phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);
            isCommingCall = false;
            isVoiceCall = true;
        }
    }

    /**
     * 接听来电
     */
    private void acceptIncomingCall() {

        //接第一个电话
        if (isCommingCall && !isVoiceCall && !firstInCommingCallConteted) {
            try {
                SipManager.getLc().acceptCall(SipManager.getLc().getCurrentCall());
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }

        //接第二个电话
        if (isCommingCall && !isVoiceCall && firstInCommingCallConteted && isCallConnected) {
            LinphoneCall[] calls = SipManager.getLc().getCalls();
            if (calls.length >= 2) {
                try {
                    SipManager.getLc().acceptCall(calls[1]);
                    SipManager.getLc().pauseCall(calls[0]);
                    swapCall1Btn.setBackgroundResource(R.drawable.btn_pressed_select_bg);
                    swapCall2Btn.setBackgroundResource(R.mipmap.dtc_btn2_bg_pressed);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向外拨打视频电话
     */
    private void makeVideoCall() {
        if (sipItemSelected == -1) {
            showProgressFail("请选择");
            return;
        }
        SipGroupItemInfoBean mSipGroupItemInfoBean = sipItemList.get(sipItemSelected);
        if (mSipGroupItemInfoBean != null) {
            String number = mSipGroupItemInfoBean.getNumber();
            if (TextUtils.isEmpty(number)) {
                showProgressFail("无号码");
                return;
            }

            App.startSpeaking("正在呼叫"+mSipGroupItemInfoBean.getName());
            //视频电话标识
            isVoiceCall = false;
            //向外拨打视频电话
            isOutCall = true;

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //拨号
            Linphone.callTo(mSipGroupItemInfoBean.getNumber(), false);
            currentCallNumLayout.setVisibility(View.VISIBLE);
            currentCallNumLayout.setText(mSipGroupItemInfoBean.getName());
            handler.sendEmptyMessage(12);
        }
    }

    /**
     * 向外拨打语音电话
     */
    private void makeVoiceCall() {
        if (sipItemSelected == -1) {
            showProgressFail("请选择拨号对象");
            return;
        }
        //获取当前的拨号号码
        SipGroupItemInfoBean sSipGroupItemInfoBean = sipItemList.get(sipItemSelected);
        if (sSipGroupItemInfoBean != null) {
            String number = sSipGroupItemInfoBean.getNumber();
            if (TextUtils.isEmpty(number)) {
                showProgressFail("无号码");
                return;
            }

            App.startSpeaking("正在呼叫"+sSipGroupItemInfoBean.getName());

            //语音电话标识
            isVoiceCall = true;

            isOutCall = true;

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //拨号
            Linphone.callTo(sSipGroupItemInfoBean.getNumber(), false);
            //显示向谁拨号
            currentCallNumLayout.setVisibility(View.VISIBLE);
            currentCallNumLayout.setText(sSipGroupItemInfoBean.getName());
            handler.sendEmptyMessage(9);
        }
    }


    /**
     * 向外拨打的语音电话释放
     */
    private void outVoiceCallReleased() {
        //停止定时
        threadStop();
        //显示Sip界面
        disPlaySipView();

        //背景恢复
        phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);

        isOutCall = false;
        isVoiceCall = true;
        acceptCallBtn.setVisibility(View.VISIBLE);
        swapCall2Btn.setVisibility(View.GONE);
        swapCall1Btn.setVisibility(View.GONE);

        isCallConnected = false;

    }

    /**
     * 释放向外拨打的视频电话
     */
    private void outVideoCallReleased() {

        threadStop();

        releasePlayer();

        disPlaySipView();

        phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);

        isOutCall = false;
        isVoiceCall = true;
        swapCall2Btn.setVisibility(View.GONE);
        swapCall1Btn.setVisibility(View.GONE);

        isCallConnected = false;

    }

    /**
     * 第一个来电视频电话释放
     */
    private void firstInComingCallReleased() {


        LinphoneCall[] calls = SipManager.getLc().getCalls();

        Logutil.d("calls-->>" + calls.length);
        if (calls.length == 0) {

            threadStop();

            releasePlayer();

            disPlaySipView();

            phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);

            isCommingCall = false;

            isVoiceCall = true;

            isCallConnected = false;

            firstInCommingCallConteted = false;

            swapCall1Btn.setVisibility(View.GONE);
            swapCall2Btn.setVisibility(View.GONE);

        } else {
            if (SipManager.getLc().getCalls().length == 1) {
                currentCallNumLayout.setText(querySipBeanFromSipNumber(calls[0].getRemoteAddress().getUserName()).getName());
                playRemoteVideo();
                swapCall1Btn.setVisibility(View.GONE);
                swapCall2Btn.setVisibility(View.GONE);
                firstInCommingCallConteted = false;
                isCallConnected = false;
            }
        }
//
// else if (calls.length == 1){
//          //  currentCallNumLayout.setText(querySipBeanFromSipNumber(SipManager.getLc().getCurrentCall().getRemoteAddress().getUserName()).getName());
//            playRemoteVideo();
//            swapCall1Btn.setVisibility(View.GONE);
//            swapCall2Btn.setVisibility(View.GONE);
//        }
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
                    handlerSipGroupData(result);
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
                    //第N下电话来的时候显示的状态
                    swapCall1Btn.setVisibility(View.VISIBLE);
                    swapCall2Btn.setVisibility(View.VISIBLE);
                    acceptCallBtn.setText("接听通话2");
                    break;
                case 9:
                    //拨打语音电话的界面
                    disPlayCallView();
                    if (isOutCall) {
                        acceptCallBtn.setVisibility(View.INVISIBLE);
                    }
                    phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice1);
                    break;
                case 10:
                    //语音电话接通

                    //开始计时
                    threadStart();
                    //设置语音电话的背景
                    phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_voice);
                    break;
                case 11:
                    //语音电话释放

                    //向外拨打的语音电话释放
                    outVoiceCallReleased();
                    break;

                case 12:
                    //向外拨打视频电话

                    //展现电话而已
                    disPlayCallView();
                    if (isOutCall) {
                        acceptCallBtn.setVisibility(View.INVISIBLE);
                    }
                    //实例播放器
                    initializePlayer();
                    //更改视频电话的背景
                    phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_video1);
                    //播放本机的视频源
                    nativeVideoParentLayout.setVisibility(View.VISIBLE);
                    playNativeVideo(nativePlayRtspUrl);
                    break;

                case 13:
                    //向外拨打的视频电话接通

                    //开始计时
                    threadStart();
                    //播放对方的视频
                    remoteVideoParentLayout.setVisibility(View.VISIBLE);
                    playRemoteVideo();
                    break;

                case 14:
                    //释放向外拨打的视频电话
                    outVideoCallReleased();
                    break;

                case 26:
                    //来电

                    disPlayCallView();
                    //实例播放器
                    initializePlayer();
                    //更改视频电话的背景
                    phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_video1);
                    //播放本机的视频源
                    nativeVideoParentLayout.setVisibility(View.VISIBLE);
                    playNativeVideo(nativePlayRtspUrl);
                    break;

                case 27:
                    //来电接通
                    isCallConnected = true;
                    if (SipManager.getLc().getCalls().length == 1) {
                        firstInCommingCallConteted = true;
                    }

                    threadStart();
                    currentCallNumLayout.setText(querySipBeanFromSipNumber(SipManager.getLc().getCurrentCall().getRemoteAddress().getUserName()).getName());
                    //播放对方的视频
                    remoteVideoParentLayout.setVisibility(View.VISIBLE);
                    playRemoteVideo();
                    break;

                case 28:
                    //来电释放
                    firstInComingCallReleased();
                    break;

                case 29:
                    //第二个来电
                    swapCall1Btn.setVisibility(View.VISIBLE);
                    swapCall2Btn.setVisibility(View.VISIBLE);
                    acceptCallBtn.setText("接听通话2");
                    break;
                case 30:
                    //第二个电话接通
                    currentCallNumLayout.setText(querySipBeanFromSipNumber(SipManager.getLc().getCurrentCall().getRemoteAddress().getUserName()).getName());
                    playRemoteVideo();
                    break;


                case 15://通话计时
                    timingNumber++;
                    if (currentPageVisible) {
                        displayPhoneTimeLayout.setText(TimeUtils.getTime(timingNumber) + "");
                    }
                    break;
                case 16: // 来电标识
                    //显示通话界面
                    disPlayCallView();
                    //初始化播放器
                    initializePlayer();
                    //显示来电者信息
                    currentCallNumLayout.setText(SipManager.getLc().getCurrentCall().getRemoteAddress().getUserName());
                    //拒接标识
                    hangUpBtnLayout.setText("拒接");
                    //默认是视频电话的背景
                    if (!isVoiceCall)
                        phoneParentLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_video1);
                    break;
                case 17:
                    //提示中心连接状态正常
                    if (getActivity() != null) {
                        currentServerCenterTv.setTextColor(0xff6adeff);
                        currentServerCenterTv.setText("中心状态:连接正常");
                    }
                    break;
                case 18:
                    //提示中心连接状态断开
                    if (getActivity() != null) {
                        currentServerCenterTv.setTextColor(0xffff0000);
                        currentServerCenterTv.setText("中心状态:已断开");
                    }
                    break;
                case 19:
                    //提示强拆失败
                    showProgressFail("强拆失败!");
                    break;
                case 20:
                    //提示无损操作无法连接
                    showProgressFail(getString(R.string.str_resource_no_connected));
                    break;
                case 21:
                    //提示远程操作结果
                    String status = (String) msg.obj;
                    showProgressSuccess(status);
                    break;
                case 22:
                    //提示此设备不支持
                    showProgressFail(getString(R.string.str_resource_no_support));
                    break;
                case 23:
                    //提示远程喊话失败
                    showProgressFail("喊话请求失败");
                    break;
                case 24:
                    //请求喊话
                    port = msg.arg1;
                    //初始化录音参数
                    initializeRecordParamater();
                    //弹出对话框提示
                    showSpeakingDialog();
                    //开始录音
                    startRecord();
                    break;
                case 25:
                    //显示喊话时间
                    displaySpeakingTime();
                    break;

            }
        }
    };


    /**
     * 更改可见的Ui状态
     */
    private void updateUiStatus() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        CustomViewPagerSlide mViewPage = (CustomViewPagerSlide) activity.findViewById(R.id.main_viewpager_layout);
        RadioGroup bottomRadioGroup = activity.findViewById(R.id.bottom_radio_group_layout);
        bottomRadioGroup.check(bottomRadioGroup.getChildAt(0).getId());
        mViewPage.setCurrentItem(3);

    }
}