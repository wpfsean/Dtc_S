package com.tehike.client.dtc.multiple.app.project.ui.display;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.entity.EventSources;
import com.tehike.client.dtc.multiple.app.project.entity.OpenBoxParamater;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupItemInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.phone.PhoneCallback;
import com.tehike.client.dtc.multiple.app.project.phone.SipManager;
import com.tehike.client.dtc.multiple.app.project.phone.SipService;
import com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity;
import com.tehike.client.dtc.multiple.app.project.ui.views.CustomViewPagerSlide;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneCall;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述： 副屏页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/24 12:26
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class SecondDisplayActivity extends Presentation {

    /**
     * 副屏的整个父布局
     */
    @BindView(R.id.secondary_screen_parent_layout)
    RelativeLayout secondaryScreenParentLayout;

    /**
     * 屏保的父布局
     */
    @BindView(R.id.screen_saver_parent_layout)
    RelativeLayout screenSaverParentLayout;

    /**
     * 地图背景布局(用于显示地图)
     */
    @BindView(R.id.backgroup_map_view_layout)
    ImageView backGrooupMapLayou;

    /**
     * 已处理的报警队列
     */
    @BindView(R.id.processed_alarm_list_layout)
    ListView processedAlarmList;

    /**
     * 展示事件信息的ListView
     */
    @BindView(R.id.event_queue_listview_layout)
    ListView eventListViewLayout;

    /**
     * 哨位分组布局
     */
    @BindView(R.id.sentinel_group_listview_layout)
    ListView sentinelListViewLayout;

    /**
     * 哨位资源分组布局
     */
    @BindView(R.id.sentinel_resources_group_listview_layout)
    ListView sentinelResourcesListViewLayout;

    /**
     * 显示报警点哨兵位置的图片
     */
    @BindView(R.id.police_sentinel_image_layout)
    ImageView sentinelPointLayout;

    /**
     * 根布局
     */
    @BindView(R.id.sh_police_image_relative)
    RelativeLayout parentLayout;

    /**
     * 左侧功能区的父布局
     */
    @BindView(R.id.left_function_parent_layout)
    RelativeLayout leftFunctionParentLayout;

    /**
     * 右侧功能区的父布局
     */
    @BindView(R.id.right_function_parent_layout)
    RelativeLayout rightFunctionParentLayout;

    /**
     * 左侧显示隐藏的按键
     */
    @BindView(R.id.left_hide_btn_layout)
    ImageButton leftHideBtn;

    /**
     * 左侧显示隐藏的按键
     */
    @BindView(R.id.right_hide_btn_layout)
    ImageButton rightHideBtn;

    /**
     * 显示当前报警信息的父布局
     */
    @BindView(R.id.display_alarm_parent_layout)
    LinearLayout alarmParentLayout;

    /**
     * 视频加载动画的View
     */
    @BindView(R.id.alarm_video_loading_icon_layout)
    ImageView loadingView;

    /**
     * 视频加载提示的View
     */
    @BindView(R.id.alarm_video_loading_tv_layout)
    TextView loadingTv;

    /**
     * 播放视频源的View
     */
    @BindView(R.id.alarm_video_view_layout)
    NodePlayerView alarmView;

    /**
     * 播放对话时对方的视频源
     */
    @BindView(R.id.alarm_call_video_view_layout)
    NodePlayerView alarmCallViewLayout;

    /**
     * 正在处理哪个哨位的报警信息
     */
    @BindView(R.id.alarm_handler_sentry_name_layout)
    TextView handlerSentryNameLayout;

    /**
     * 处理报警时的时间信息
     */
    @BindView(R.id.alarm_handler_sentry_time_layout)
    TextView handlerSenrtyTimeLayout;

    /**
     * 右侧报警队表
     */
    @BindView(R.id.alarm_queue_listview_layout)
    ListView alarmQueueListViewLayout;

    /**
     * 报警队表
     */
    @BindView(R.id.sentinel_request_queue_layout)
    GridView requestOpenBoxViewLayout;

    /**
     * 点击弹窗父布局
     */
    @BindView(R.id.sentry_click_parent_layout)
    RelativeLayout dialogClickSentryParentLayout;

    /**
     * 点击弹窗时哨位名称
     */
    @BindView(R.id.sentinel_name_layout)
    TextView dialogClickSentryNameLayout;

    /**
     * 哨痊视频预览的父布局
     */
    @BindView(R.id.sentry_preview_parent_layout)
    RelativeLayout sentryVideoPreviewParentnLayout;

    /**
     * 屏保显示时间
     */
    @BindView(R.id.display_screen_saver_tv_layout)
    TextView displayScreenSaverTvLayout;

    /**
     * 报警通话视频加载文字提示
     */
    @BindView(R.id.alarm_call_video_loading_tv_layout)
    TextView alarmCallVideoLoadingTvLayout;

    /**
     * 报警通话视频加载动画提示
     */
    @BindView(R.id.alarm_call_video_loading_icon_layout)
    ImageView alarmCallVideoLoadingIconLayout;

    /**
     * 报警视频源播放器
     */
    NodePlayer alarmPlayer;

    /**
     * 报警通话视频源播放器
     */
    NodePlayer alarmCallPlayer;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 盛放哨位分组的数据
     */
    List<SipGroupInfoBean> sentinelGroupItemList = new ArrayList<>();

    /**
     * d盛放哨位资源分组的适配器
     */
    List<SipGroupItemInfoBean> sentinelResourcesGroupItemList = new ArrayList<>();

    /**
     * 展示哨位分组的适配器
     */
    SentinelGroupAdapter mSentinelGroupAdapter;

    /**
     * 展示哨位资源分组的适配器
     */
    SentinelResourcesGroupItemAdapter mSentinelResourcesGroupItemAdapter;

    /**
     * 展示已处理报警队列 的适配器
     */
    ProcessedAlarmQueueAdapter mProcessedAlarmQueueAdapter;

    /**
     * 展示事件的适配器
     */
    EventQueueAdapter eventQueueAdapter;

    /**
     * 上下文
     */
    Context context;

    /**
     * 事件信息的队列
     */
    LinkedList<EventSources> eventQueueList = new LinkedList<>();

    /**
     * 网络请求到的背景图片
     */
    Bitmap backGroupBitmap = null;

    /**
     * 用来存储所有哨位图标的集合
     */
    List<View> allView = new ArrayList<>();

    /**
     * 本地缓存的所有的Sip数制（SIp字典）
     */
    List<SipBean> allSipList;

    /**
     * 本地缓存的所有的视频数制（视频字典）
     */
    List<VideoBean> allVideoList;


    /**
     * 广播（Sip缓存完成）
     */
    SipSourcesBroadcast mSipSourcesBroadcast;

    /**
     * 点击哨位图标时产生的哨位面部视频对象
     */
    VideoBean setryVideoBean = null;

    /**
     * 左侧功能布局是否隐藏的标识
     */
    boolean leftParentLayotHide = false;

    /**
     * 右侧功能布局是否隐藏的标识
     */
    boolean rightParentLayotHide = false;

    /**
     * 当前的哨位名（与哪个哨位通话）
     */
    String sentryName = "";

    /**
     * 报警源的Sip号码
     */
    String alarmSipNumber = "";

    /**
     * 哪个开启申请被选中
     */
    int whichOpenBoxPosition = -1;

    /**
     * 哪个报警对象被选中
     */
    int whichAlarmPosition = -1;

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
     * 状态报警队列的集合
     */
    LinkedList<AlarmVideoSource> alarmQueueList = new LinkedList<>();

    /**
     * 状态报警队列的集合
     */
    LinkedList<OpenBoxParamater> requestOpenBoxQueueList = new LinkedList<>();

    /**
     * 展示报警信息的适配器
     */
    AlarmQueueAdapter mAlarmQueueAdapter;

    /**
     * 接收报警信息的广播
     */
    public ReceiveAlarmBroadcast mReceiveAlarmBroadcast;

    /**
     * 接收开箱申请的广播
     */
    ReceiveBoxBroadcast mReceiveBoxBroadcast;

    /**
     * 展示申请供弹的适配器
     */
    RequestOpenBoxQueueAdapter requestOpenBoxQueueAdapter;

    /**
     * 接收本地缓存的视频字典广播
     */
    public VideoSourcesBroadcast mVideoSourcesBroadcast;

    /**
     * 用来接收屏保的通知的广播
     */
    ReceiveScreenSaverBroadcast mReceiveScreenSaverBroadcast;

    /**
     * 取消屏保的广播
     */
    ReceiveCancelScreenSaverBroadcast mReceiveCancelScreenSaverBroadcast;

    /**
     * 用于标识是否正在屏保
     */
    boolean isScreenSaving = false;

    /**
     * 定时器（定时的变换屏保文字）
     */
    Timer timer;

    /**
     * 屏保提示文字
     */
    String screenTvContent[] = {"像狮子一样高傲,像少女一样温柔。",
            "我骄傲孤独怎敌她温言软语你不忍辜负 ,你和她余生共度而我显得突兀就此退出",
            "承诺如同珍珠，它的莹润是蚌痛苦的代价，也是蚌的荣耀。",
            "希望有一天，你能遇到那个愿为你弯腰的人。从此以后，其他人不过就是匆匆浮云。",
            "偶尔会想念童年时光，不知愁滋味，不用担责备，不会为别人心碎，不知人间苦累。",
            "岁月永远年轻，我们慢慢老去，你会发现，童心未泯，是一件值得骄傲的事情。",
            "如果我们都是孩子，就可以留在时光的原地，坐在一起一边听那些永不老去的故事一边慢慢皓首。",
            "我们像是表面上的针，不停的转动，一面转，一面看着时间匆匆离去，却无能为力。",
            "记忆想是倒在掌心的水，不论你摊开还是紧握，终究还是会从指缝中一滴一滴流淌干净。",
            "一个人的自愈的能力越强，才越有可能接近幸福。做一个寡言，却心有一片海的人，不伤人害己，于淡泊中，平和自在。",
            "那一世，你在天涯，我在海角，组合在一起，便是一场唯美的爱情宣言；那一世，你渡沧海，我醉桑田，组合在一起，便是一笔动人的多情诗篇。",
            "静静的躺在岁月的长河中，回忆着有你的一幕幕，泪湿眼眸。曾经的幸福像在放纪录片一样一张张地倒映在眼前。",
            "不曾爱过，亦不曾痛过，所有的心思和生命，在遇到他的时刻苏醒，汹涌如潮。只是他却，视而不见。",
            "自由是这么来的可奴隶也是这么来的。《勇敢的心》",
            "当我站在瀑布面前，觉得非常难过，应该是两个人站在这里。"
    };

    public SecondDisplayActivity(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
        hideTitleBar();
        //加载本地的Sip资源
        initSipSources();
        //加载本地的所有的视频资源
        initVideoSources();
        //加载布局
        setContentView(R.layout.activity_seconddisplay_layout);
        //框架
        ButterKnife.bind(this);
        //初始化VIew
        initView();
        //加载背景地图
        initBackgroupBitmap(AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig.BACKGROUP_MAP_URL);
        //加载已处理的报警信息
        initProcessedAlarmData();
        //加载事件信息
        initEventData();
        //初始化哨位分组数据
        initSentinelGroupData();
        //注册广播接收报警信息
        registerReceiveAlarmBroadcast();
        //注册广播监听申请供弹
        registerReceiveBoxBroadcast();
        //加载所有的报警队列数据
        initlizeAlarmQueueAdapterData();
        //注册广播监听是否要屏保
        registerReceiveScreenSaverBroadcast();
        // 注册广播用于接收取消屏保通知
        registerCancelReceiveScreenSaverBroadcast();
    }

    /**
     * 隐藏状态栏
     */
    private void hideTitleBar() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 初始化View
     */
    private void initView() {
        timer = new Timer();
        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(context, R.anim.loading);
        //报警视频播放器
        alarmPlayer = new NodePlayer(context);
        alarmPlayer.setPlayerView(alarmView);
        alarmPlayer.setVideoEnable(true);
        alarmPlayer.setAudioEnable(false);
        //接收到报警后的通话视频
        alarmCallPlayer = new NodePlayer(context);
        alarmCallPlayer.setPlayerView(alarmCallViewLayout);
        alarmCallPlayer.setVideoEnable(true);
        alarmCallPlayer.setAudioEnable(false);

    }

    /**
     * 加载本地的已缓存完成的Sip
     */
    private void initSipSources() {
        try {
            allSipList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
        } catch (Exception e) {
            //异常后注册广播用来接收sip缓存完成的通知
            registerAllSipSourceDoneBroadcast();
        }
    }

    /**
     * 注册广播监听Sip资源缓存完成
     */
    private void registerAllSipSourceDoneBroadcast() {
        mSipSourcesBroadcast = new SipSourcesBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SipDone");
        context.registerReceiver(mSipSourcesBroadcast, intentFilter);
    }

    /**
     * Sip字典广播
     */
    class SipSourcesBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                allSipList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            } catch (Exception e) {
                Logutil.e("取allSipList字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 加载背景地图
     */
    private void initBackgroupBitmap(final String s) {

        //开始子线程去请求背景地图
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL iconUrl = new URL(s);
                    URLConnection conn = iconUrl.openConnection();
                    HttpURLConnection http = (HttpURLConnection) conn;
                    int length = http.getContentLength();
                    conn.connect();
                    //获得图像的字符流
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is, length);
                    Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                    if (bm != null) {
                        Message message = new Message();
                        message.what = 1;
                        message.obj = bm;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "请求图片异常--" + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 显示地图背景
     */
    private void disPlayBackGroupBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            backGrooupMapLayou.setImageBitmap(bitmap);
            backGroupBitmap = bitmap;
        } else {
            Logutil.e("请求到的背景图片为空---");
        }
    }

    /**
     * 初始化所有的已处理的报警信息数据
     */
    private void initProcessedAlarmData() {

        LinkedList<AlarmVideoSource> mlist = new LinkedList<>();
        mlist.clear();

        Cursor c = new DbUtils(App.getApplication()).query(DbHelper.TAB_NAME, null, null, null, null, null, null, null);
        if (c == null) {
            Logutil.e("c is null");
            return;
        }
        if (c.moveToFirst()) {
            do {
                AlarmVideoSource alarmVideoSource = new AlarmVideoSource();
                String time = c.getString(c.getColumnIndex("time"));
                String senderIp = c.getString(c.getColumnIndex("senderIp"));
                String faceVideoId = c.getString(c.getColumnIndex("faceVideoId"));
                String faceVideoName = c.getString(c.getColumnIndex("faceVideoName"));
                String alarmType = c.getString(c.getColumnIndex("alarmType"));
                String isHandler = c.getString(c.getColumnIndex("isHandler"));
                // Logutil.d(time + "\t" + senderIp + "\t" + faceVideoId + "\t" + faceVideoName + "\t" + alarmType + "\t" + isHandler);
                alarmVideoSource.setSenderIp(senderIp);
                alarmVideoSource.setFaceVideoId(faceVideoId);
                alarmVideoSource.setAlarmType(alarmType);
                alarmVideoSource.setFaceVideoName(faceVideoName);
                alarmVideoSource.setTime(time);
                mlist.add(alarmVideoSource);
            } while (c.moveToNext());
        }

        mlist = reverseLinkedList(mlist);
        if (mProcessedAlarmQueueAdapter == null) {
            mProcessedAlarmQueueAdapter = new ProcessedAlarmQueueAdapter(mlist);
            processedAlarmList.setAdapter(mProcessedAlarmQueueAdapter);
        }
        mProcessedAlarmQueueAdapter.notifyDataSetChanged();


    }

    /**
     * 已处理的的报警队列的适配器
     */
    class ProcessedAlarmQueueAdapter extends BaseAdapter {

        LinkedList<AlarmVideoSource> mlist;


        public ProcessedAlarmQueueAdapter(LinkedList<AlarmVideoSource> mlist) {
            this.mlist = mlist;
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return mlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.fragment_alarm_processed_event_item_layout, null);
                viewHolder.alarmEventName = convertView.findViewById(R.id.alarm_processed_event_name_layout);
                viewHolder.alarmType = convertView.findViewById(R.id.alarm_processed_event_type_layout);
                viewHolder.alarmTime = convertView.findViewById(R.id.alarm_processed_event_time_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.alarmEventName.setText(mlist.get(position).getFaceVideoName());
            viewHolder.alarmType.setText(mlist.get(position).getAlarmType());
            viewHolder.alarmTime.setText(mlist.get(position).getTime());
            return convertView;
        }

        //内部类
        class ViewHolder {
            //报警地点
            TextView alarmEventName;
            //报警类型
            TextView alarmType;
            //报警发生时间
            TextView alarmTime;

        }
    }

    /**
     * 加载事件信息
     */
    private void initEventData() {

        //清空事件队列
        if (eventQueueList != null && eventQueueList.size() > 0) {
            eventQueueList.clear();
        }
        //查询数据库
        Cursor c = new DbUtils(App.getApplication()).query(DbHelper.EVENT_TAB_NAME, null, null, null, null, null, null, null);
        if (c == null) {
            Logutil.e("c is null");
            return;
        }
        //遍历Cursor
        if (c.moveToFirst()) {
            do {
                EventSources mEventSources = new EventSources();
                String time = c.getString(c.getColumnIndex("time"));
                String event = c.getString(c.getColumnIndex("event"));
                mEventSources.setEvent(event);
                mEventSources.setTime(time);
                eventQueueList.add(mEventSources);
            } while (c.moveToNext());
        }
        //把事件队列反转一下，最新的放在上面
        eventQueueList = reverseLinkedList(eventQueueList);
        //适配器展示
        if (eventQueueAdapter == null) {
            eventQueueAdapter = new EventQueueAdapter();
            eventListViewLayout.setAdapter(eventQueueAdapter);
        }
        eventQueueAdapter.notifyDataSetChanged();

    }

    /**
     * 反转linkedlist
     */
    private LinkedList reverseLinkedList(LinkedList linkedList) {
        LinkedList<Object> newLinkedList = new LinkedList<>();
        for (Object object : linkedList) {
            newLinkedList.add(0, object);
        }
        return newLinkedList;
    }

    /**
     * 展示事件信息的适配器
     */
    class EventQueueAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return eventQueueList.size();
        }

        @Override
        public Object getItem(int position) {
            return eventQueueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.fragment_processed_event_item_layout, null);
                viewHolder.eventName = convertView.findViewById(R.id.processed_event_name_layout);
                viewHolder.eventTime = convertView.findViewById(R.id.processed_event_time_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.eventName.setText(eventQueueList.get(position).getEvent());
            viewHolder.eventTime.setText(eventQueueList.get(position).getTime());

            return convertView;
        }

        //内部类
        class ViewHolder {

            //事件名称
            TextView eventName;
            //事件发生时间
            TextView eventTime;
        }
    }

    /**
     * 加载哨位分组数据
     */
    private void initSentinelGroupData() {

        //判断网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(11);
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
                    handler.sendEmptyMessage(12);
                    return;
                }
                //数据异常
                if (result.contains("Execption")) {
                    Logutil.e("请求sip组数据异常" + result);
                    handler.sendEmptyMessage(12);
                    return;
                }
                Logutil.d("當前數據分組信息--->>>" + result);
                //让handler去处理数据
                Message sipGroupMess = new Message();
                sipGroupMess.what = 13;
                sipGroupMess.obj = result;
                handler.sendMessage(sipGroupMess);
            }
        });
        new Thread(thread).start();
    }

    /**
     * 处理哨位分组数据
     */
    private void handlerSentinelGroupData(String sentinelGroupDataResult) {
        //先清空集合防止
        if (sentinelGroupItemList != null && sentinelGroupItemList.size() > 0) {
            sentinelGroupItemList.clear();
        }

        try {
            JSONObject jsonObject = new JSONObject(sentinelGroupDataResult);
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
                    sentinelGroupItemList.add(sipGroupInfoBean);
                }
            }
            handler.sendEmptyMessage(14);
        } catch (Exception e) {
            Logutil.e("解析Sip分组数据异常" + e.getMessage());
            handler.sendEmptyMessage(12);
        }
    }

    /**
     * 展示哨位分组
     */
    private void displaySentinelListAdapter() {
        //判断是否有要适配的数据
        if (sentinelGroupItemList == null || sentinelGroupItemList.size() == 0) {
            handler.sendEmptyMessage(12);
            Logutil.e("适配的数据时无数据");
            return;
        }
        mSentinelGroupAdapter = new SentinelGroupAdapter(context);
        //显示左侧的sip分组页面
        sentinelListViewLayout.setAdapter(mSentinelGroupAdapter);
        mSentinelGroupAdapter.setSeclection(0);
        mSentinelGroupAdapter.notifyDataSetChanged();

        //默认加载第一组的数据

        String groupId = sentinelGroupItemList.get(0).getId() + "";
        loadVideoGroupItemData(groupId);

        //点击事件
        sentinelListViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSentinelGroupAdapter.setSeclection(position);
                mSentinelGroupAdapter.notifyDataSetChanged();
                SipGroupInfoBean mSipGroupInfoBean = sentinelGroupItemList.get(position);
                Logutil.i("SipGroupInfoBean-->>" + mSipGroupInfoBean.toString());
                int groupId = mSipGroupInfoBean.getId();
                loadVideoGroupItemData(groupId + "");

            }
        });
    }

    /**
     * 加载哨位资源分组数据
     */
    private void loadVideoGroupItemData(final String id) {
        //判断组Id是否为空
        if (TextUtils.isEmpty(id)) {
            return;
        }
        String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;

        //子线程根据组Id请求组数据
        HttpBasicRequest httpThread = new HttpBasicRequest(sipGroupItemUrl + id, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                //无数据
                if (TextUtils.isEmpty(result)) {
                    handler.sendEmptyMessage(12);
                    return;
                }
                //数据异常
                if (result.contains("Execption")) {
                    handler.sendEmptyMessage(12);
                    return;
                }

                if (sentinelResourcesGroupItemList != null && sentinelResourcesGroupItemList.size() > 0) {
                    sentinelResourcesGroupItemList.clear();
                }
                Logutil.d("组数据" + result);

                //解析sip资源
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.isNull("errorCode")) {
                        Logutil.w("请求不到数据信息");
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
                            groupItemInfoBean.setLocation(jsonItem.getString("location"));
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
                                            jsonItemVideo.getString("name"),
                                            jsonItemVideo.getString("location"),
                                            jsonItemVideo.getString("password"),
                                            jsonItemVideo.getInt("port"),
                                            jsonItemVideo.getString("username"), "", "", "", "", "", "");
                                    groupItemInfoBean.setBean(videoBean);
                                }
                            }
                            sentinelResourcesGroupItemList.add(groupItemInfoBean);
                        }
                    }

                    //  Logutil.d(allSipList.size() + "\t" + allSipList.toString());
                    //     Logutil.d("sentinelResourcesGroupItemList--->>" + sentinelResourcesGroupItemList.size() + "\t" + sentinelResourcesGroupItemList.toString());
//


                    handler.sendEmptyMessage(15);
                } catch (JSONException e) {
                    Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                }
            }
        });
        new Thread(httpThread).start();
    }

    /**
     * 哨位分组适配器
     */
    class SentinelGroupAdapter extends BaseAdapter {
        //选中对象的标识
        private int clickTemp = -1;
        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public SentinelGroupAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return sentinelGroupItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return sentinelGroupItemList.get(position);
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
                convertView = layoutInflater.inflate(R.layout.item_video_group_monifor_layout, null);
                viewHolder.videoGroupName = (TextView) convertView.findViewById(R.id.video_group_name_layout);
                viewHolder.videoGroupParentLayout = (RelativeLayout) convertView.findViewById(R.id.video_group_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupInfoBean videoGroupInfoBean = sentinelGroupItemList.get(position);

            if (videoGroupInfoBean != null)
                viewHolder.videoGroupName.setText(videoGroupInfoBean.getName());

            //选中状态
            if (clickTemp == position) {
                viewHolder.videoGroupName.setTextColor(0xffffffff);
                viewHolder.videoGroupParentLayout.setBackgroundResource(R.mipmap.dtc_bg_list_group_selected);
            } else {
                viewHolder.videoGroupName.setTextColor(0xff6adeff);
                viewHolder.videoGroupParentLayout.setBackgroundResource(R.mipmap.dtc_bg_list_group_normal);
            }
            return convertView;
        }

        /**
         * 内部类
         */
        class ViewHolder {
            //显示分组名
            TextView videoGroupName;
            //分组item的父布局
            RelativeLayout videoGroupParentLayout;
        }
    }

    /**
     * 展示哨位资源分组
     */
    private void disPlaySentinelResourcesGroupItemAdapter() {
        if (mSentinelResourcesGroupItemAdapter == null) {
            mSentinelResourcesGroupItemAdapter = new SentinelResourcesGroupItemAdapter();
            sentinelResourcesListViewLayout.setAdapter(mSentinelResourcesGroupItemAdapter);
        }
        mSentinelResourcesGroupItemAdapter.notifyDataSetChanged();
    }

    /**
     * 展示哨位资源分组的适配器
     */
    class SentinelResourcesGroupItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return sentinelResourcesGroupItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return sentinelResourcesGroupItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_video_group_item_monifor_layout, null);
                viewHolder.sipItemName = convertView.findViewById(R.id.video_group_item_name_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupItemInfoBean mDevice = sentinelResourcesGroupItemList.get(position);
            viewHolder.sipItemName.setText(mDevice.getName());
            return convertView;
        }

        class ViewHolder {
            TextView sipItemName;
        }
    }

    /**
     * 计算所有布防点的位置
     */
    private void disPlayAllSentinelPoints() {
        if (backGroupBitmap == null) {
            Logutil.e("backGroupBitmap  is null");
            return;
        }
        //计算网络加载的背景图片的宽高
        int netBitmapWidth = backGroupBitmap.getWidth();
        int netBitmapHeight = backGroupBitmap.getHeight();
        //计算本身背景布局的宽高
        int nativeLayoutwidth = backGrooupMapLayou.getWidth();
        int nativeLayoutHeight = backGrooupMapLayou.getHeight();
        //算出宽高比例
        float percent_width = (float) netBitmapWidth / nativeLayoutwidth;
        float percent_height = (float) netBitmapHeight / nativeLayoutHeight;

        //宽高比例保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String width_format = decimalFormat.format(percent_width);
        String height_format = decimalFormat.format(percent_height);

        //最终的宽高比例
        float final_format_width = Float.parseFloat(width_format);
        float final_format_height = Float.parseFloat(height_format);


        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(sentinelPointLayout.getLayoutParams());

        //清除上次的所有的图标
        if (parentLayout != null) {
            if (allView != null && allView.size() > 0) {
                ViewGroup viewGroup = (ViewGroup) allView.get(0).getParent();
                for (int n = 0; n < allView.size(); n++) {
                    View view = allView.get(n);
                    if (view != null) {
                        viewGroup.removeView(view);
                    }
                }
                viewGroup.invalidate();
                allView.clear();
            }
        }
        //遍历显示所有的哨位图标
        if (sentinelResourcesGroupItemList != null && sentinelResourcesGroupItemList.size() > 0) {
            for (int i = 0; i < sentinelResourcesGroupItemList.size(); i++) {
                String location = sentinelResourcesGroupItemList.get(i).getLocation();
                if (!TextUtils.isEmpty(location)) {
                    String locationArry[] = location.split(",");
                    int x = Integer.parseInt(locationArry[0]);
                    int y = Integer.parseInt(locationArry[1]);
                    float sentinel_width = Float.parseFloat(decimalFormat.format(x / final_format_width)) - 15;
                    float sentinel_height = Float.parseFloat(decimalFormat.format(y / final_format_height)) - 48;
                    //定义显示其他哨兵的ImageView
                    ImageView other_image = new ImageView(App.getApplication());
                    allView.add(other_image);
                    displaySentinel(other_image, layoutParams, (int) sentinel_width, (int) sentinel_height);
                }
            }
        }
        //遍历加监听
        if (allView != null && allView.size() > 0) {
            for (int k = 0; k < allView.size(); k++) {

                final int finalK = k;
                allView.get(k).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String location = sentinelResourcesGroupItemList.get(finalK).getLocation();
                        final int x = Integer.parseInt(location.split(",")[0]);
                        String currentClickBeanId = sentinelResourcesGroupItemList.get(finalK).getId();
                        if (TextUtils.isEmpty(currentClickBeanId)) {
                            Logutil.d("为空了-->>" + currentClickBeanId);
                            return;
                        }
                        //查询当前点击对象的面部视频
                        if (allSipList != null && allSipList.size() > 0) {
                            for (int i = 0; i < allSipList.size(); i++) {
                                String deviceId = allSipList.get(i).getId();
                                if (deviceId.equals(currentClickBeanId)) {
                                    setryVideoBean = allSipList.get(i).getSetryBean();
                                }
                            }
                        }
                        if (setryVideoBean == null) {
                            Logutil.d("哨们视频为空");
                            return;
                        }
                        Logutil.d("setryVideoBean--->>" + setryVideoBean.toString());
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("sentry", sentinelResourcesGroupItemList.get(finalK));
                        message.setData(bundle);
                        message.what = 2;
                        handler.sendMessage(message);

                    }
                });
            }
        }
    }

    /**
     * 展示所有的布防点
     */
    private void displaySentinel(ImageView imageView, final ViewGroup.MarginLayoutParams layoutParams, final int sentinel_width, final int sentinel_height) {
        if (layoutParams != null) {
            imageView.setImageResource(R.mipmap.sentinel);

            //设置其他哨兵哨位点的位置
            layoutParams.setMargins(sentinel_width, sentinel_height, 0, 0);
            //将哨位点位置设置到RelativeLayout.LayoutParams
            RelativeLayout.LayoutParams rllps = new RelativeLayout.LayoutParams(layoutParams);
            //设置显示其他哨兵位置图片的宽高
            rllps.width = 30;
            rllps.height = 48;
            //显示图片
            parentLayout.addView(imageView, rllps);

        }
    }

    /**
     * 点击哨位名时显示
     */
    private void showClickSentryPopuWindow(SipGroupItemInfoBean sipGroupItemInfoBean) {
        currentSentinelBean = sipGroupItemInfoBean;
        dialogClickSentryParentLayout.setVisibility(View.VISIBLE);
        dialogClickSentryNameLayout.setText(sipGroupItemInfoBean.getName());
    }

    /**
     * 当前点击对象
     */
    SipGroupItemInfoBean currentSentinelBean;

    /**
     * 哨位操作
     */
    @OnClick({R.id.make_sentinel_voice_call_layout, R.id.make_sentinel_video_call_layout, R.id.sentinel_video_layout, R.id.popu_prompt_close_btn_layout})
    public void sentryOperating(View view) {
        switch (view.getId()) {
            case R.id.make_sentinel_voice_call_layout:
                if (currentSentinelBean != null) {
                    Intent intent = new Intent();
                    intent.putExtra("call", false);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bean", currentSentinelBean);
                    intent.setAction("makeCall");
                    intent.putExtra("bundle", bundle);
                    App.getApplication().sendBroadcast(intent);
                }
                Logutil.d("语音电话" + currentSentinelBean.toString());
                break;
            case R.id.make_sentinel_video_call_layout:
                if (currentSentinelBean != null) {
                    Intent intent = new Intent();
                    intent.putExtra("call", true);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bean", currentSentinelBean);
                    intent.setAction("makeCall");
                    intent.putExtra("bundle", bundle);
                    App.getApplication().sendBroadcast(intent);
                }
                Logutil.d("视频电话" + currentSentinelBean.toString());
                break;
            case R.id.sentinel_video_layout:
                Logutil.w("面部视频");
                displaySentryPreviewVideo();
                break;
            case R.id.popu_prompt_close_btn_layout:
                sentryVideoPreviewLoadingParentLayout.setVisibility(View.VISIBLE);
                sentryVideoPreviewParentnLayout.setVisibility(View.GONE);
                setryVideoBean = null;
                if (preViewNodePlayer != null) {
                    preViewNodePlayer.stop();
                    preViewNodePlayer.release();
                }
                break;
        }
    }

    /**
     * 预览哨位面部视频的播放器
     */
    NodePlayer preViewNodePlayer;

    /**
     * 视频预览时播放器的View
     */
    @BindView(R.id.popu_prompt_loading_video_view_layout)
    NodePlayerView nodePlayerView;

    /**
     * 预览视频加载时View的父布局
     */
    @BindView(R.id.popu_prompt_loading_parent_layout)
    RelativeLayout sentryVideoPreviewLoadingParentLayout;

    /**
     * 加载预览视频的动画
     */
    @BindView(R.id.popu_sentinel_loading_icon_layout)
    ImageView preViewVideoloadingView;

    /**
     * 哨位视频预览
     */
    private void displaySentryPreviewVideo() {
        if (setryVideoBean != null) {
            String rtsp = setryVideoBean.getRtsp();
            if (!TextUtils.isEmpty(rtsp)) {
                dialogClickSentryParentLayout.setVisibility(View.GONE);
                sentryVideoPreviewParentnLayout.setVisibility(View.VISIBLE);
                preViewVideoloadingView.startAnimation(mLoadingAnim);
                preViewNodePlayer = new NodePlayer(App.getApplication());
                preViewNodePlayer.setPlayerView(nodePlayerView);
                preViewNodePlayer.setAudioEnable(false);
                preViewNodePlayer.setVideoEnable(true);
                preViewNodePlayer.setInputUrl(rtsp);
                preViewNodePlayer.start();
                preViewNodePlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                    @Override
                    public void onEventCallback(NodePlayer player, int event, String msg) {
                        Logutil.d("event---" + event);
                        if (event == 1001) {
                            handler.sendEmptyMessage(18);
                        }
                    }
                });
            } else {
                Logutil.e("rtsp is null");
            }
        } else {
            Logutil.e("v is null");
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        isScreenSaving = false;
        timer.cancel();
        //隐藏哨位操作的弹窗布局
        dialogClickSentryParentLayout.setVisibility(View.GONE);
        //隐藏屏保页面
        screenSaverParentLayout.setVisibility(View.GONE);
        //显示主页面
        secondaryScreenParentLayout.setVisibility(View.VISIBLE);
        //结束屏保页面
        if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
            ActivityUtils.getTopActivity().finish();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 侧边显示或隐藏
     */
    @OnClick({R.id.left_hide_btn_layout, R.id.right_hide_btn_layout})
    public void slideHideOrShow(View v) {
        switch (v.getId()) {
            case R.id.left_hide_btn_layout:
                //隐藏或显示左侧的功能布局
                hideLeftParentLayout();
                break;
            case R.id.right_hide_btn_layout:
                //隐藏或显示右侧的功能布局
                hideRightParentLayout();
                break;
        }
    }

    /**
     * 隐藏或显示右侧的功能布局
     */
    private void hideRightParentLayout() {
        if (!rightParentLayotHide) {
            rightParentLayotHide = true;
            rightFunctionParentLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rightHideBtn.getLayoutParams();
            layoutParams.setMargins(200, 0, 0, 0);
            rightHideBtn.setLayoutParams(layoutParams);
        } else {
            rightParentLayotHide = false;
            rightFunctionParentLayout.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rightHideBtn.getLayoutParams();
            layoutParams.setMargins(0, 0, 290, 0);
            rightHideBtn.setLayoutParams(layoutParams);
        }
    }

    /**
     * 隐藏或显示左侧的功能布局
     */
    private void hideLeftParentLayout() {
        if (!leftParentLayotHide) {
            leftParentLayotHide = true;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leftHideBtn.getLayoutParams();
            layoutParams.leftMargin = leftHideBtn.getLeft() - 290;
            leftHideBtn.setLayoutParams(layoutParams);
            TranslateAnimation animation = new TranslateAnimation(290, 0, 0, 0);
            animation.setDuration(2000);
            animation.setFillAfter(false);
            leftFunctionParentLayout.startAnimation(animation);
            leftFunctionParentLayout.clearAnimation();
            leftFunctionParentLayout.setVisibility(View.GONE);
        } else {
            leftParentLayotHide = false;
            leftFunctionParentLayout.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leftHideBtn.getLayoutParams();
            layoutParams.leftMargin = leftHideBtn.getLeft() + 290;
            leftHideBtn.setLayoutParams(layoutParams);
        }
    }

    /**
     * 注册接收申请供弹信息广播
     */
    private void registerReceiveBoxBroadcast() {
        mReceiveBoxBroadcast = new ReceiveBoxBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.BOX_ACTION);
        context.registerReceiver(mReceiveBoxBroadcast, intentFilter);
    }

    /**
     * 广播接收申请开箱信息
     */
    class ReceiveBoxBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            initProcessedAlarmData();

            initEventData();

            OpenBoxParamater boxbean = (OpenBoxParamater) intent.getSerializableExtra("box");
            Message message = new Message();
            message.what = 17;
            message.obj = boxbean;
            handler.sendMessage(message);

        }
    }

    /**
     * 注册接收报警信息广播
     */
    private void registerReceiveAlarmBroadcast() {
        mReceiveAlarmBroadcast = new ReceiveAlarmBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.ALARM_ACTION);
        context.registerReceiver(mReceiveAlarmBroadcast, intentFilter);
    }

    /**
     * 广播接收报警信息
     */
    class ReceiveAlarmBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //接收报警对象
            AlarmVideoSource alarm = (AlarmVideoSource) intent.getSerializableExtra("alarm");
            Logutil.i("Alarm-->>" + alarm);
            //判断报警对象是否为空
            if (TextUtils.isEmpty(alarm.getFaceVideoName()) && TextUtils.isEmpty(alarm.getAlarmType())) {
                Logutil.e("alarm--- is null");
                return;
            }
            //添加到集合
            alarmQueueList.add(alarm);
            //刷新适配器
            handler.sendEmptyMessage(3);
            //加载已处理的报警数据
            initProcessedAlarmData();
            //加载所有的事件信息数据
            initEventData();


            //遍历查获此报警来源的sip号码
            for (int i = 0; i < allSipList.size(); i++) {
                SipBean mSipBean = allSipList.get(i);
                if (mSipBean.getIpAddress().equals(alarm.getSenderIp())) {
                    alarmSipNumber = mSipBean.getNumber();
                    sentryName = mSipBean.getName();
                }
            }
            Logutil.d("alarmSipNum--->>" + alarmSipNumber);
            Logutil.d("isCalling" + AppConfig.IS_CALLING);
            //如果非通话中，就直接拨打电话电话并
            if (!AppConfig.IS_CALLING) {
                if (!TextUtils.isEmpty(alarmSipNumber)) {
                    try {
                        //延时三秒后执行，为了让语音把报警内存播报完成
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //播报
                    App.startSpeaking("正在呼叫" + sentryName);
                    //电话连接
                    Linphone.callTo(alarmSipNumber, false);

                    threadStart();
                }
                //播放报警视频源
                playAlarmVideo(alarm);

                //播放报警时的视频源
                playAlarmCallVideo(alarm);
            }
        }
    }

    /**
     * 展示报警队列的适配器
     */
    class AlarmQueueAdapter extends BaseAdapter {

        private int selectedItem = -1;

        @Override
        public int getCount() {
            return alarmQueueList.size();
        }

        @Override
        public Object getItem(int position) {
            return alarmQueueList.get(position);
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
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_alarm_listview_layout, null);
                viewHolder.alarmName = convertView.findViewById(R.id.alarm_list_item_name_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.alarmName.setText(alarmQueueList.get(position).getFaceVideoName() + "\t" + alarmQueueList.get(position).getAlarmType());

            if (position == selectedItem) {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
                viewHolder.alarmName.setTextColor(0xffffffff);
            } else {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);

                viewHolder.alarmName.setTextColor(0xffff0000);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            TextView alarmName;
        }
    }

    /**
     * 加载所有的本地视频资源
     */
    private void initVideoSources() {
        try {
            allVideoList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
            Logutil.d("我获取到数据了" + allVideoList.toString());
        } catch (Exception e) {
            Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            registerAllVideoSourceDoneBroadcast();
        }
    }

    /**
     * 注册广播监听所有的视频数据是否解析完成
     */
    private void registerAllVideoSourceDoneBroadcast() {
        mVideoSourcesBroadcast = new VideoSourcesBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        context.registerReceiver(mVideoSourcesBroadcast, intentFilter);
    }

    /**
     * Video字典广播
     */
    class VideoSourcesBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //取出本地缓存的所有的Video数据
                allVideoList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
            } catch (Exception e) {
                Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 展示报警队列的适配器
     */
    class RequestOpenBoxQueueAdapter extends BaseAdapter {

        //item选中标识
        private int selectedItem = -1;

        @Override
        public int getCount() {
            return requestOpenBoxQueueList.size();
        }

        @Override
        public Object getItem(int position) {
            return requestOpenBoxQueueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //item选中的方法
        public void setSelectedItem(int selectedItem) {
            this.selectedItem = selectedItem;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_open_box_listview_layout, null);
                viewHolder.requestOpenBoxSentryNameLayout = convertView.findViewById(R.id.request_open_ammo_box_sentry_name_layout);
                viewHolder.requestOpenAmmoBoxParentLayout = convertView.findViewById(R.id.request_open_ammo_box_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //显示申请供弹的名称
            for (int i = 0; i < allSipList.size(); i++) {
                SipBean mSipBean = allSipList.get(i);
                if (mSipBean.getIpAddress().equals(requestOpenBoxQueueList.get(position).getSendIp())) {
                    viewHolder.requestOpenBoxSentryNameLayout.setText(mSipBean.getName());
                    break;
                }
            }
            //item选中时背景更改
            if (position == selectedItem) {
                viewHolder.requestOpenAmmoBoxParentLayout.setBackgroundResource(R.mipmap.dtc_bg_danxiang_yes_selected);
                viewHolder.requestOpenBoxSentryNameLayout.setTextColor(0xffff00ff);
            } else {
                viewHolder.requestOpenAmmoBoxParentLayout.setBackgroundResource(R.drawable.request_open_ammo_box_item_bg);
                viewHolder.requestOpenBoxSentryNameLayout.setTextColor(0xffffffff);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            //申请供弹item父布局
            RelativeLayout requestOpenAmmoBoxParentLayout;
            //申请打开弹箱的哨位名称
            TextView requestOpenBoxSentryNameLayout;
        }
    }

    /**
     * 播放报警源的视频
     */
    private void playAlarmVideo(AlarmVideoSource mAlarmVideoSource) {
        loadingView.startAnimation(mLoadingAnim);
        if (alarmPlayer != null && alarmPlayer.isPlaying()) {
            alarmPlayer.stop();
        }
        //查询报警源的视频信息
        if (allVideoList != null && allVideoList.size() > 0) {
            for (VideoBean device : allVideoList) {
                if (device != null) {
                    if (device.getId().equals(mAlarmVideoSource.getFaceVideoId())) {
                        String rtsp = device.getRtsp();
                        if (!TextUtils.isEmpty(rtsp)) {
                            Logutil.d("Rtsp-->>>" + rtsp);
                            alarmPlayer.setInputUrl(rtsp);
                            alarmPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                                @Override
                                public void onEventCallback(NodePlayer player, int event, String msg) {
                                    if (event == 1001 || event == 1102 || event == 1104) {
                                        handler.sendEmptyMessage(7);
                                    } else {
                                        handler.sendEmptyMessage(6);
                                    }
                                }
                            });
                            alarmPlayer.start();
                        } else {
                            handler.sendEmptyMessage(8);
                        }
                    } else {
                        handler.sendEmptyMessage(8);
                    }
                } else {
                    handler.sendEmptyMessage(8);
                }
            }
        }
    }

    /**
     * 初始化数据
     */
    private void initlizeAlarmQueueAdapterData() {
        //显示报警列表的适配器
        mAlarmQueueAdapter = new AlarmQueueAdapter();
        alarmQueueListViewLayout.setAdapter(mAlarmQueueAdapter);
        //默认第一个选中
        mAlarmQueueAdapter.setSelectedItem(0);
        whichAlarmPosition = 0;
        mAlarmQueueAdapter.notifyDataSetChanged();


        alarmQueueListViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAlarmQueueAdapter.setSelectedItem(position);
                whichAlarmPosition = position;
                mAlarmQueueAdapter.notifyDataSetChanged();
                Logutil.d("positon" + position);
                // playAlarmVideo(alarmQueueList.get(whichAlarmPosition));
            }
        });

        //展示事件队列
        eventQueueAdapter = new EventQueueAdapter();
        eventListViewLayout.setAdapter(eventQueueAdapter);
    }

    /**
     * 处理申请供弹请求
     */
    private void handlerOpenBoxInfo(OpenBoxParamater boxbean) {

        //判断当前是否正在屏保
        if (isScreenSaving) {
            //隐藏屏保布局
            screenSaverParentLayout.setVisibility(View.GONE);
            //显示副屏主布局
            secondaryScreenParentLayout.setVisibility(View.VISIBLE);
            //同时把主屏的屏保activty杀列
            if (ActivityUtils.getTopActivity().getClass().toString().equals("class com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                ActivityUtils.getTopActivity().finish();
            }
            //取消定时器
            timer.cancel();
            isScreenSaving = false;
        }
        //显示报警弹窗
        alarmParentLayout.setVisibility(View.VISIBLE);
        //申请对象添加到集合
        requestOpenBoxQueueList.add(boxbean);
        //谁发起的申请
        String requestIp = boxbean.getSendIp();
        //把打开的哪个弹箱的ID
        String requestOpenBoxId = boxbean.getBoxId();

        String requestDeviceName = "";

        String requestOpenBoxName = "";

        //要播报的内容
        String voiceContent = "";

        //判断字典是否存在
        if (allSipList == null || allSipList.size() == 0) {
            return;
        }
        //遍历查询
        for (int i = 0; i < allSipList.size(); i++) {
            SipBean mSipBean = allSipList.get(i);
            if (mSipBean.getIpAddress().equals(requestIp)) {
                requestDeviceName = mSipBean.getSentryId();
            }
            if (mSipBean.getId().equals(requestOpenBoxId)) {
                requestOpenBoxName = mSipBean.getName();
            }
        }
        //  voiceContent = requestDeviceName + "申请打开" + requestOpenBoxName + "的子弹箱！";
        if (!TextUtils.isEmpty(requestDeviceName))
            App.startSpeaking(requestDeviceName + "号哨申请开启弹箱");

        Logutil.d("requestIp" + requestIp);
        Logutil.d("requestOpenBoxId" + requestOpenBoxId);
        Logutil.d("requestDeviceName" + requestDeviceName);
        Logutil.d("requestOpenBoxName" + requestOpenBoxName);

        //保存事件到数据库
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put("time", TimeUtils.getCurrentTime());
        if (!TextUtils.isEmpty(requestDeviceName))
            contentValues1.put("event", requestDeviceName + "号哨申请开启" + requestOpenBoxName + "子弹箱");
        else
            contentValues1.put("event", boxbean.getSendIp() + "申请开启" + boxbean.getBoxId() + "子弹箱");
        new DbUtils(App.getApplication()).insert(DbHelper.EVENT_TAB_NAME, contentValues1);
        Logutil.d("数据库写入成功");

        //展示供弹申请队列
        if (requestOpenBoxQueueAdapter == null) {
            requestOpenBoxQueueAdapter = new RequestOpenBoxQueueAdapter();
            requestOpenBoxViewLayout.setAdapter(requestOpenBoxQueueAdapter);
        }
        //第一个选中
        requestOpenBoxQueueAdapter.setSelectedItem(0);
        whichOpenBoxPosition = 0;
        //刷新队列
        requestOpenBoxQueueAdapter.notifyDataSetChanged();
        //列表item加监听
        requestOpenBoxViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logutil.d("Position-->>" + position);
                whichOpenBoxPosition = position;
                requestOpenBoxQueueAdapter.setSelectedItem(position);
                requestOpenBoxQueueAdapter.notifyDataSetChanged();
            }
        });
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
                            handler.sendEmptyMessage(9);
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
            handler.sendEmptyMessage(10);
        }
    }

    /**
     * 播放报警时通话的视频源
     */
    private void playAlarmCallVideo(AlarmVideoSource alarm) {

        //判断字典是否存在
        if (allSipList == null || allSipList.size() == 0) {
            return;
        }
        //播放通话视频的地址
        String rtsp = "";

        //遍历查询
        for (int i = 0; i < allSipList.size(); i++) {
            SipBean mSipBean = allSipList.get(i);
            if (mSipBean.getIpAddress().equals(alarm.getSenderIp())) {
                alarmSipNumber = mSipBean.getNumber();
                sentryName = mSipBean.getName();
                if (mSipBean.getVideoBean() != null) {
                    if (!TextUtils.isEmpty(mSipBean.getVideoBean().getRtsp())) {
                        rtsp = mSipBean.getVideoBean().getRtsp();
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else {
                    handler.sendEmptyMessage(22);
                }
            }
        }
        handler.sendEmptyMessage(4);
        //选 判断播放地址
        if (TextUtils.isEmpty(rtsp)) {
            handler.sendEmptyMessage(22);
            Logutil.e("播放通话时的面部 视频为null");
            return;
        }
        //是否正在播放
        if (alarmCallPlayer != null) {
            alarmCallPlayer.stop();
        }
        alarmCallVideoLoadingIconLayout.setAnimation(mLoadingAnim);
        //加载地址
        alarmCallPlayer.setInputUrl(rtsp);
        //播放回调
        alarmCallPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
            @Override
            public void onEventCallback(NodePlayer player, int event, String msg) {
                Logutil.d("event-->>" + event);
                if (alarmCallPlayer == player) {
                    if (event == 1001 || event == 1102 || event == 1104) {
                        handler.sendEmptyMessage(24);
                    } else {
                        handler.sendEmptyMessage(23);
                    }
                }
            }
        });
        //开始播放
        alarmCallPlayer.start();
    }

    /**
     * 处理供弹请求
     */
    @OnClick({R.id.accpet_open_box_btn_layout, R.id.refuse_open_box_btn_layout, R.id.accpet_open_all_box_btn_layout, R.id.refuse_all_open_box_btn_layout})
    public void handlerRequestOpenBoxOperate(View view) {

        switch (view.getId()) {
            case R.id.accpet_open_box_btn_layout:
                //同意供弹
                acceptOpenAmmoBox();
                break;
            case R.id.refuse_open_box_btn_layout:
                //拒绝供弹
                rejectOpenAmmoBox();
                break;
            case R.id.accpet_open_all_box_btn_layout:
                //同意全部供弹
                accpetOpenAllAmmoBox();
                break;
            case R.id.refuse_all_open_box_btn_layout:
                //拒绝全部供弹
                rejectOpenAllAmmoBox();
                break;
        }
    }

    /**
     * 同意供弹
     */
    private void acceptOpenAmmoBox() {
        //判断申请供弹队列是否有数据
        if (requestOpenBoxQueueList != null && requestOpenBoxQueueList.size() > 0 && whichOpenBoxPosition != -1) {
            //子线程去申请供弹
            new Thread(new HandlerAmmoBoxThread(requestOpenBoxQueueList.get(whichOpenBoxPosition), 0)).start();
            //移除 队列
            requestOpenBoxQueueList.remove(whichOpenBoxPosition);
        }
        //刷新适配器
        if (requestOpenBoxQueueAdapter != null) {
            requestOpenBoxQueueAdapter.setSelectedItem(0);
            whichOpenBoxPosition = 0;
            requestOpenBoxQueueAdapter.notifyDataSetChanged();
        }
        //判断供弹队列和报警队列是否有数据
        if (alarmQueueList.size() == 0 && requestOpenBoxQueueList.size() == 0) {
            //隐藏弹窗
            alarmParentLayout.setVisibility(View.GONE);
        }
        App.startSpeaking("同意供弹请求");
    }

    /**
     * 拒绝供弹
     */
    private void rejectOpenAmmoBox() {
        //判断数据是否存在
        if (requestOpenBoxQueueList != null && requestOpenBoxQueueList.size() > 0 && whichOpenBoxPosition != -1) {
            //子线程发送拒绝供弹数据
            new Thread(new HandlerAmmoBoxThread(requestOpenBoxQueueList.get(whichOpenBoxPosition), 1)).start();
            //移除队列
            requestOpenBoxQueueList.remove(whichOpenBoxPosition);
        }
        //刷新适配器
        if (requestOpenBoxQueueAdapter != null) {
            requestOpenBoxQueueAdapter.setSelectedItem(0);
            whichOpenBoxPosition = 0;
            requestOpenBoxQueueAdapter.notifyDataSetChanged();
        }
        //判断供弹队列和报警队列是否有数据
        if (alarmQueueList.size() == 0 && requestOpenBoxQueueList.size() == 0) {
            //隐藏弹窗
            alarmParentLayout.setVisibility(View.GONE);
        }
        App.startSpeaking("拒绝供弹请求");
    }

    /**
     * 同意全部供弹
     */
    private void accpetOpenAllAmmoBox() {
        //判断队列中是否有数据
        if (requestOpenBoxQueueList.size() > 0) {
            //循环的去同意
            for (OpenBoxParamater o : requestOpenBoxQueueList) {
                new Thread(new HandlerAmmoBoxThread(o, 0)).start();
            }
        }
        //清除队列
        requestOpenBoxQueueList.clear();
        //刷新适配器
        if (requestOpenBoxQueueAdapter != null) {
            requestOpenBoxQueueAdapter.notifyDataSetChanged();
        }
        //判断供弹队列和报警队列是否有数据
        if (alarmQueueList.size() == 0 && requestOpenBoxQueueList.size() == 0) {
            //隐藏弹窗
            alarmParentLayout.setVisibility(View.GONE);
        }
        App.startSpeaking("同意全部供弹");
    }

    /**
     * 拒绝全部供弹
     */
    private void rejectOpenAllAmmoBox() {
        //判断队列中是否有数据
        if (requestOpenBoxQueueList.size() > 0) {
            //循环的去同意
            for (OpenBoxParamater o : requestOpenBoxQueueList) {
                new Thread(new HandlerAmmoBoxThread(o, 1)).start();
            }
        }
        //清除队列
        requestOpenBoxQueueList.clear();
        //刷新适配器
        if (requestOpenBoxQueueAdapter != null) {
            requestOpenBoxQueueAdapter.notifyDataSetChanged();
        }
        //判断供弹队列和报警队列是否有数据
        if (alarmQueueList.size() == 0 && requestOpenBoxQueueList.size() == 0) {
            //隐藏弹窗
            alarmParentLayout.setVisibility(View.GONE);
        }
        App.startSpeaking("拒绝全部供弹");
    }

    /**
     * 处理报警信息
     */
    @OnClick({R.id.colse_alarm_btn_layout, R.id.handler_alarm_btn_layout, R.id.colse_all_alarm_btn_layout})
    public void handlerAlarmOperate(View view) {
        switch (view.getId()) {
            case R.id.colse_alarm_btn_layout:
                //关闭报警
                closeAlarm();
                break;
            case R.id.handler_alarm_btn_layout:
                //处理报警
                handlerAlarm();
                break;
            case R.id.colse_all_alarm_btn_layout:
                //关闭全部报警
                closeAllAlarm();
                break;
        }
    }

    /**
     * 关闭报警
     */
    private void closeAlarm() {
        Logutil.d("AppConfig.IS_CALLING" + AppConfig.IS_CALLING);
        //如果正在通话中
        if (AppConfig.IS_CALLING) {
            //从队列中移除
            if (alarmQueueList.size() > 0 && whichAlarmPosition != -1) {
                alarmQueueList.remove(whichAlarmPosition);
            }
            //刷新适配器
            if (mAlarmQueueAdapter != null) {
                mAlarmQueueAdapter.setSelectedItem(0);
                whichAlarmPosition = 0;
                mAlarmQueueAdapter.notifyDataSetChanged();
            }
            //关闭播放器
            if (alarmCallPlayer != null)
                alarmCallPlayer.stop();
            if (alarmPlayer != null)
                alarmPlayer.stop();
            //挂断电话
            SipManager.getLc().terminateAllCalls();
            App.startSpeaking("关闭报警");
        }
        //判断队列中是否还有未处理的报警
        if (alarmQueueList.size() > 0) {
            AlarmVideoSource alarm = null;
            //获取当前的报警对象
            if (whichAlarmPosition != -1) {
                alarm = alarmQueueList.get(whichAlarmPosition);
            }

            //判断当前报警对象是否为空
            if (alarm != null) {
                if (!AppConfig.IS_CALLING) {
                    //遍历查找当前报警对象的Sip号码
                    for (int i = 0; i < allSipList.size(); i++) {
                        SipBean mSipBean = allSipList.get(i);
                        if (mSipBean.getIpAddress().equals(alarm.getSenderIp())) {
                            alarmSipNumber = mSipBean.getNumber();
                            sentryName = mSipBean.getName();
                        }
                    }
                    Logutil.d("alarmSipNum--->>" + alarmSipNumber);
                    if (!TextUtils.isEmpty(alarmSipNumber)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        App.startSpeaking("正在呼叫" + sentryName);
                        Linphone.callTo(alarmSipNumber, false);
                        handler.sendEmptyMessage(10);
                        timingNumber = -4;
                    }
                    //播放报警视频源
                    playAlarmVideo(alarm);
                    //播放通话视频源
                    playAlarmCallVideo(alarm);
                }
            }
        } else {
            //判断申请供弹队列中是否还有未处理的
            if (requestOpenBoxQueueList.size() == 0) {
                alarmParentLayout.setVisibility(View.GONE);
                threadStop();
            }
        }
    }

    /**
     * 处理报警
     */
    private void handlerAlarm() {
        Logutil.d("AppConfig.IS_CALLING" + AppConfig.IS_CALLING);
        if (AppConfig.IS_CALLING) {
            SipManager.getLc().terminateAllCalls();
        }

        //处理报警
        AlarmVideoSource mAlarmBean = null;
        if (alarmQueueList.size() > 0 && whichAlarmPosition != -1) {
            mAlarmBean = alarmQueueList.get(whichAlarmPosition);
        }
        if (mAlarmBean != null) {
            handler.sendEmptyMessage(10);
            Logutil.d("mA-->>" + mAlarmBean.toString());
            if (!AppConfig.IS_CALLING) {
                for (int i = 0; i < allSipList.size(); i++) {
                    SipBean mSipBean = allSipList.get(i);
                    if (mSipBean.getIpAddress().equals(mAlarmBean.getSenderIp())) {
                        alarmSipNumber = mSipBean.getNumber();
                        sentryName = mSipBean.getName();
                    }
                }
                Logutil.d("alarmSipNum--->>" + alarmSipNumber);
                if (!TextUtils.isEmpty(alarmSipNumber)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    App.startSpeaking("正在呼叫" + sentryName);
                    Linphone.callTo(alarmSipNumber, false);
                    handler.sendEmptyMessage(10);
                    timingNumber = -4;
                }
                //播放报警视频源
                playAlarmVideo(mAlarmBean);
                playAlarmCallVideo(mAlarmBean);
            }
        }
    }

    /**
     * 关闭全部报警
     */
    private void closeAllAlarm() {
        //清空报警队列
        alarmQueueList.clear();
        //刷新适配器
        if (mAlarmQueueAdapter != null) {
            mAlarmQueueAdapter.notifyDataSetChanged();
        }
        //中断通话
        SipManager.getLc().terminateAllCalls();
        //停止播放报警源视频
        if (alarmPlayer != null) {
            alarmPlayer.stop();
        }
        if (alarmCallPlayer != null) {
            alarmCallPlayer.stop();
        }
        threadStop();
        handler.sendEmptyMessage(10);
        if (requestOpenBoxQueueList.size() == 0)
            alarmParentLayout.setVisibility(View.GONE);

        App.startSpeaking("报警已全部关闭");
    }

    /**
     * 向服务器发起供弹请求的子线程
     */
    class HandlerAmmoBoxThread extends Thread {
        OpenBoxParamater mOpenBoxParamater;
        int action;

        public HandlerAmmoBoxThread(OpenBoxParamater mOpenBoxParamater, int action) {
            this.mOpenBoxParamater = mOpenBoxParamater;
            this.action = action;
        }

        @Override
        public void run() {

            byte[] sendData = new byte[72];
            // 数据头
            byte[] flag = mOpenBoxParamater.getFalg().getBytes();
            System.arraycopy(flag, 0, sendData, 0, 4);
            // 版本号
            byte[] version = new byte[4];
            version[0] = 0;
            version[1] = 0;
            version[2] = 0;
            version[3] = 1;
            System.arraycopy(version, 0, sendData, 4, 4);
            // 动作， 0-请求，1-同意，2-拒绝，3-直接开启
            sendData[9] = (byte) action;
            sendData[10] = 0;
            sendData[11] = 0;
            sendData[12] = 0;

            // 随机申请 码

            // uiAction = 0, 保存设备端随机生成的申请码
            byte[] requestCode = new byte[4];

            // uiAction = 0, 保存设备端的SALT
            byte[] requestSalt = new byte[4];
            // uiAction = 1, 保存服务端根据申请码计算得到的开锁码
            byte[] responseCode = new byte[4];

            System.arraycopy(requestCode, 0, sendData, 12, 4);
            System.arraycopy(requestSalt, 0, sendData, 16, 4);
            System.arraycopy(responseCode, 0, sendData, 20, 4);

            //测试（有问题）
            byte[] senderIP = new byte[4];
            senderIP[0] = 19;
            senderIP[1] = 0;
            senderIP[2] = 0;
            senderIP[3] = 70;

            System.arraycopy(senderIP, 0, sendData, 24, 4);

            byte[] senderID = mOpenBoxParamater.getBoxId().getBytes();
            System.arraycopy(senderID, 0, sendData, 28, senderID.length);
            // System.out.println(Arrays.toString(sendData));

            Socket socket = null;
            OutputStream os = null;
            try {
                // 测试
                socket = new Socket("19.0.0.229", 2000);
                os = socket.getOutputStream();
                os.write(sendData);
                os.flush();
                System.out.println("发送成功");
            } catch (IOException e) {
                String err = e.getMessage();
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
    }

    /**
     * 注册广播用于接收屏保通知
     */
    private void registerReceiveScreenSaverBroadcast() {
        mReceiveScreenSaverBroadcast = new ReceiveScreenSaverBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.SCREEN_SAVER_ACTION);
        context.registerReceiver(mReceiveScreenSaverBroadcast, intentFilter);
    }

    /**
     * 广播用来接收主屏是否已屏保的通知
     */
    class ReceiveScreenSaverBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isScreenSaving = true;
            handler.sendEmptyMessage(19);
        }
    }

    /**
     * 屏保操作
     */
    private void handlerScreenSave() {
        isScreenSaving = true;
        //显示屏保布局
        screenSaverParentLayout.setVisibility(View.VISIBLE);
        //隐藏副屏主布局
        secondaryScreenParentLayout.setVisibility(View.GONE);
        //随机屏保文字


        if (timer != null) {
            timer.cancel();
            timer = null;
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(21);
            }
        }, 0, 10000);
    }

    /**
     * 设置屏保文字
     */
    private void displayScreenTv() {
        int num = (int) (Math.random() * 1000);
        while (num > screenTvContent.length - 1) {
            if (num <= screenTvContent.length - 1) {
                break;
            }
            num = (int) (Math.random() * 1000);
        }
        displayScreenSaverTvLayout.setText("\u3000\u3000" + screenTvContent[num]);
    }

    /**
     * 注册广播用于接收取消屏保通知
     */
    private void registerCancelReceiveScreenSaverBroadcast() {
        mReceiveCancelScreenSaverBroadcast = new ReceiveCancelScreenSaverBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.CANCEL_SCREEN_SAVER_ACTION);
        context.registerReceiver(mReceiveCancelScreenSaverBroadcast, intentFilter);
    }

    /**
     * 广播用来接收取消主屏是否已屏保的通知
     */
    class ReceiveCancelScreenSaverBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isScreenSaving = false;
            timer.cancel();
            handler.sendEmptyMessage(20);
        }
    }

    /**
     * 取消屏保操作
     */
    private void handlerCancelScreenSave() {
        screenSaverParentLayout.setVisibility(View.GONE);
        //隐藏副屏的父布局
        secondaryScreenParentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDisplayRemoved() {
        if (mReceiveAlarmBroadcast != null)
            context.unregisterReceiver(mReceiveAlarmBroadcast);
        if (mReceiveBoxBroadcast != null)
            context.unregisterReceiver(mReceiveBoxBroadcast);
        if (mSipSourcesBroadcast != null)
            context.unregisterReceiver(mSipSourcesBroadcast);
        if (mSipSourcesBroadcast != null)
            context.unregisterReceiver(mSipSourcesBroadcast);
        if (mReceiveCancelScreenSaverBroadcast != null)
            context.unregisterReceiver(mReceiveCancelScreenSaverBroadcast);
        if (mReceiveScreenSaverBroadcast != null)
            context.unregisterReceiver(mReceiveScreenSaverBroadcast);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDisplayRemoved();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //显示背景地图
                    Bitmap bitmap = (Bitmap) msg.obj;
                    disPlayBackGroupBitmap(bitmap);
                    break;
                case 2:
                    Bundle bundle = msg.getData();
                    SipGroupItemInfoBean mSipGroupItemInfoBean = (SipGroupItemInfoBean) bundle.getSerializable("sentry");
                    //处理报警信息
                    showClickSentryPopuWindow(mSipGroupItemInfoBean);
                    break;
                case 3:
                    //有报警时，判断是否 正在屏保
                    if (isScreenSaving) {
                        //隐藏屏保布局
                        screenSaverParentLayout.setVisibility(View.GONE);
                        //显示副屏主布局
                        secondaryScreenParentLayout.setVisibility(View.VISIBLE);
                        //同时把主屏的屏保activty杀列
                        if (ActivityUtils.getTopActivity().getClass().toString().equals("class com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                            ActivityUtils.getTopActivity().finish();
                        }
                        //取消定时器
                        timer.cancel();
                    }
                    //重置屏保标识
                    isScreenSaving = false;
                    //显示处理报警布局
                    alarmParentLayout.setVisibility(View.VISIBLE);
                    //刷新报警队列适配器
                    if (mAlarmQueueAdapter != null)
                        mAlarmQueueAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    //显示正在处理哪个哨位的报警信息
                    handlerSentryNameLayout.setText(sentryName);
                    break;
                case 6:
                    //提示报警源视频正在加载
                    loadingTv.setVisibility(View.VISIBLE);
                    loadingTv.setText(R.string.reconnect);
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case 7:
                    //报警源视频加载完成
                    loadingView.setVisibility(View.GONE);
                    loadingView.clearAnimation();
                    loadingTv.setVisibility(View.GONE);
                    break;
                case 8:
                    //提示报警源视频无法加载
                    loadingTv.setVisibility(View.VISIBLE);
                    loadingTv.setText("无法加载报警视频源");
                    loadingView.setVisibility(View.INVISIBLE);
                    break;
                case 9:
                    //报警时的通话计时
                    timingNumber++;
                    Logutil.d("timingNumber" + timingNumber);
                    if (timingNumber > 0)
                        handlerSenrtyTimeLayout.setText(TimeUtils.getTime(timingNumber) + "");
                    break;
                case 10:
                    //停止计时
                    handlerSenrtyTimeLayout.setText("00:00");
                    break;
                case 12:
                    //提示未加载到哨位分组数据
                    ToastUtils.showShort("未获取到数据!");
                    break;
                case 13:
                    //处理哨位分组数据
                    String sentinelGroupDataResult = (String) msg.obj;
                    handlerSentinelGroupData(sentinelGroupDataResult);
                    break;
                case 14:
                    //展示哨位分组的适配器
                    displaySentinelListAdapter();
                    break;
                case 15:
                    //加载哨位资源分组数据
                    disPlaySentinelResourcesGroupItemAdapter();
                    disPlayAllSentinelPoints();
                    break;
                case 17:
                    //处理申请供弹请求
                    OpenBoxParamater boxbean = (OpenBoxParamater) msg.obj;
                    handlerOpenBoxInfo(boxbean);
                    break;
                case 18:
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    preViewVideoloadingView.clearAnimation();
                    sentryVideoPreviewLoadingParentLayout.setVisibility(View.GONE);
                    break;
                case 19:
                    //屏保操作
                    handlerScreenSave();
                    break;
                case 20:
                    handlerCancelScreenSave();
                    break;
                case 21:
                    displayScreenTv();
                    break;
                case 22:
                    //提示报警通话视频源
                    alarmCallVideoLoadingTvLayout.setText("无法加载对方通话视频源");
                    alarmCallVideoLoadingIconLayout.setVisibility(View.INVISIBLE);
                    break;
                case 23:
                    //报警通话视频源播放正常
                    alarmCallVideoLoadingTvLayout.setVisibility(View.GONE);
                    alarmCallVideoLoadingIconLayout.setVisibility(View.INVISIBLE);
                    break;
                case 24:
                    //报警通话视频源正在连接
                    alarmCallVideoLoadingTvLayout.setText("重新连接");
                    alarmCallVideoLoadingIconLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };
}
