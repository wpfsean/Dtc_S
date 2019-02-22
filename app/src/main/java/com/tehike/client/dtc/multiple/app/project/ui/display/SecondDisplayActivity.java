package com.tehike.client.dtc.multiple.app.project.ui.display;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupItemInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述： 副屏
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/24 12:26
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class SecondDisplayActivity extends Presentation implements View.OnClickListener {


    /**
     * 屏保的父布局
     */
    RelativeLayout screenSaveraParentLayout;

    /**
     * 副屏的父布局
     */
    RelativeLayout secondaryScreenParentLayout;

    /**
     * 地图背景布局(用于显示地图)
     */
    ImageView backGrooupMapLayou;

    /**
     * 根布局
     */
    RelativeLayout parentLayout;

    /**
     * 显示报警点哨兵位置的图片
     */
    ImageView sentinelPointLayout;

    /**
     * 左侧功能区的父布局
     */
    RelativeLayout leftFunctionParentLayout;

    /**
     * 右侧功能区的父布局
     */
    RelativeLayout rightFunctionParentLayout;

    /**
     * 左侧显示隐藏的按键
     */
    ImageButton leftHideBtn;

    /**
     * 左侧显示隐藏的按键
     */
    ImageButton rightHideBtn;

    /**
     * 侧边的根布局
     */
    RelativeLayout sideParentLayout;

    /**
     * 显示当前报警信息的父布局
     */
    LinearLayout alarmParentLayout;

    /**
     * 处理报警的按键
     */
    Button handlerAlarmBtn;

    /**
     * 视频加载动画的View
     */
    ImageView loadingView;

    /**
     * 视频加载提示的View
     */
    TextView loadingTv;

    /**
     * 哨位分组布局
     */
    ListView sentinelListViewLayout;

    /**
     * 哨位资源分组布局
     */
    ListView sentinelResourcesListViewLayout;


    /**
     * 左侧功能布局是否隐藏的标识
     */
    boolean leftParentLayotHide = false;

    /**
     * 右侧功能布局是否隐藏的标识
     */
    boolean rightParentLayotHide = false;

    /**
     * 关闭报警按键的父布局
     */
    ImageButton closeAlarmParentLayout;

    /**
     * 右侧报警队表
     */
    ListView alarmQueueListViewLayout;

    /**
     * 所有布防点封装类
     */
    private AllPointAddress sentinelPointObj;

    /**
     * 上下文
     */
    Context context;

    /**
     * 网络请求到的背景图片
     */
    Bitmap backGroupBitmap = null;

    /**
     * 状态报警队列的集合
     */
    LinkedList<AlarmVideoSource> alarmQueueList = new LinkedList<>();

    /**
     * 接收报警信息的广播
     */
    public ReceiveAlarmBroadcast mReceiveAlarmBroadcast;

    /**
     * 展示报警信息的适配器
     */
    AlarmQueueAdapter mAlarmQueueAdapter;

    /**
     * 接收本地缓存的视频字典广播
     */
    public VideoSourcesBroadcast mVideoSourcesBroadcast;

    /**
     * 当前是否存在报警
     */
    boolean isHandleringAlarm = false;

    /**
     * 已处理的报警队列
     */
    ListView processedAlarmList;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 报警视频源播放器
     */
    NodePlayer alarmPlayer;

    /**
     * 播放视频源的View
     */
    NodePlayerView alarmView;

    /**
     * 哪个报警被选中时的标识
     */
    int whichAlarmSelected = -1;

    /**
     * 本地缓存的所有的视频数制（视频字典）
     */
    List<VideoBean> allVideoList;

    /**
     * 展示已处理报警队列 的适配器
     */
    ProcessedAlarmQueueAdapter mProcessedAlarmQueueAdapter;

    /**
     * 用于标识是否正在屏保
     */
    boolean isScreenSaver = false;

    /**
     * 用来接收屏保的通知的广播
     */
    ReceiveScreenSaverBroadcast mReceiveScreenSaverBroadcast;

    /**
     * 用于接收取消屏保的广播
     */
    ReceiveCancelScreenSaverBroadcast mReceiveCancelScreenSaverBroadcast;


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


    public SecondDisplayActivity(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏状态栏
        hideTitleBar();

        setContentView(R.layout.activity_seconddisplay_layout);

        //初始化View
        initializeView();

        //加载所有的报警队列数据
        initlizeAlarmQueueAdapterData();

        //从网络加载背景地图的图片
        initBackgroupBitmap(AppConfig.BACKGROUP_MAP_URL);

        //注册广播接收报警信息
        registerReceiveAlarmBroadcast();

        //注册广播接收屏保的消息
        registerReceiveScreenSaverBroadcast();

        //注册广播接收取消屏保的消息
        registerCancelReceiveScreenSaverBroadcast();

        //加载已处理的报警信息
        initProcessedAlarmData();

        //加载本地的所有的视频资源
        initVideoSources();

        //初始化哨位分组数据
        initSentinelGroupData();
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
                mlist.add(alarmVideoSource);
            } while (c.moveToNext());
        }

        if (mProcessedAlarmQueueAdapter != null) {
            mProcessedAlarmQueueAdapter = null;
        }
        mProcessedAlarmQueueAdapter = new ProcessedAlarmQueueAdapter(mlist);
        processedAlarmList.setAdapter(mProcessedAlarmQueueAdapter);
        mProcessedAlarmQueueAdapter.notifyDataSetChanged();
    }

    /**
     * 隐藏TitleBar
     */
    private void hideTitleBar() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 初始化View
     */
    private void initializeView() {
        //屏保的父布局
        screenSaveraParentLayout = this.findViewById(R.id.screen_saver_parent_layout);
        //副屏的根布局
        secondaryScreenParentLayout = this.findViewById(R.id.secondary_screen_parent_layout);
        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(context, R.anim.loading);
        //显示地图的布局
        backGrooupMapLayou = this.findViewById(R.id.backgroup_map_view_layout);
        //显示哨位信息的图片布局
        sentinelPointLayout = findViewById(R.id.police_sentinel_image_layout);
        //显示地图的父布局
        parentLayout = findViewById(R.id.sh_police_image_relative);
        //显示报警信息的父而已
        alarmParentLayout = findViewById(R.id.display_alarm_parent_layout);
        //关闭报警页面的父按键
        closeAlarmParentLayout = findViewById(R.id.close_alarm_btn);
        //左侧功能区的父布局
        leftFunctionParentLayout = this.findViewById(R.id.left_function_parent_layout);
        //右侧功能区的父布局
        rightFunctionParentLayout = this.findViewById(R.id.right_function_parent_layout);
        //左侧显示或隐藏的按键
        leftHideBtn = this.findViewById(R.id.left_hide_btn_layout);
        //右侧显示或隐藏的按键
        rightHideBtn = this.findViewById(R.id.right_hide_btn_layout);
        //侧边根布局
        sideParentLayout = this.findViewById(R.id.side_parent_layout);
        //左侧按键监听
        leftHideBtn.setOnClickListener(this);
        //右侧按键监听
        rightHideBtn.setOnClickListener(this);
        //处理报警按键监听
        closeAlarmParentLayout.setOnClickListener(this);
        //视频加载动画
        loadingView = findViewById(R.id.alarm_video_loading_icon_layout);
        //视频加载提示
        loadingTv = findViewById(R.id.alarm_video_loading_tv_layout);
        //显示报警画面的视频View
        alarmView = findViewById(R.id.alarm_video_view_layout);
        //报警视频播放器
        alarmPlayer = new NodePlayer(context);
        alarmPlayer.setPlayerView(alarmView);
        alarmPlayer.setVideoEnable(true);
        alarmPlayer.setAudioEnable(false);
        //处理按键报警
        handlerAlarmBtn = findViewById(R.id.handler_alarm_btn);
        handlerAlarmBtn.setOnClickListener(this);
        //已处理的队列
        processedAlarmList = findViewById(R.id.processed_alarm_list_layout);

        //哨位分组
        sentinelListViewLayout = findViewById(R.id.sentinel_group_listview_layout);
        //哨位资源分组
        sentinelResourcesListViewLayout = findViewById(R.id.sentinel_resources_group_listview_layout);
        //右侧报警队列的ListView
        alarmQueueListViewLayout = findViewById(R.id.alarm_queue_listview_layout);

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
            initAllSentinelPoints(AppConfig.ALL_SENTINEL_POINT);
        } else {
            Logutil.e("请求到的背景图片为空---");
        }
    }

    /**
     * 加载所有布防数据
     */
    private void initAllSentinelPoints(final String s) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(s).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    String authString = "admin" + ":" + "pass";
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = StringUtils.readTxt(in);
                        if (TextUtils.isEmpty(result)) {
                            Log.e("TAG", "请求无数据");
                            return;
                        }
                        Message returnLoginMess = new Message();
                        returnLoginMess.obj = result;
                        returnLoginMess.what = 2;
                        handler.sendMessage(returnLoginMess);
                    } else {
                        Log.e("TAG", "Sysinfo接口返回非200" + con.getResponseCode());
                    }
                    con.disconnect();
                } catch (Exception e) {
                    Logutil.e("请求哨位点数据异常--->>" + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 处理所有的哨位数据
     */
    private void handlerSentinelPointData(String result) {
        try {
            if (TextUtils.isEmpty(result)) {
                Logutil.e("handlerSentinelPointData() data is null");
                return;
            }
            sentinelPointObj = new AllPointAddress();
            List<AllPointAddress.CamerasBean> cameras = new ArrayList<>();
            List<AllPointAddress.TerminalsBean> terminals = new ArrayList<>();

            JSONObject parentJson = new JSONObject(result);
            JSONArray camerasArray = parentJson.getJSONArray("cameras");
            JSONArray terminalsArray = parentJson.getJSONArray("terminals");
            for (int i = 0; i < camerasArray.length(); i++) {
                AllPointAddress.CamerasBean camerasBean = new AllPointAddress.CamerasBean();
                AllPointAddress.CamerasBean.LocationBean mLocationBean = new AllPointAddress.CamerasBean.LocationBean();
                JSONObject cameraJsonItem = camerasArray.getJSONObject(i);
                String guid = cameraJsonItem.getString("guid");
                String mapUrl = cameraJsonItem.getString("mapUrl");
                String name = cameraJsonItem.getString("name");
                JSONObject c_locationJson = cameraJsonItem.getJSONObject("location");
                int x = c_locationJson.getInt("x");
                int y = c_locationJson.getInt("y");
                mLocationBean.setX(x);
                mLocationBean.setY(y);
                camerasBean.setGuid(guid);
                camerasBean.setMapUrl(mapUrl);
                camerasBean.setName(name);
                camerasBean.setLocation(mLocationBean);
                cameras.add(camerasBean);
            }

            for (int j = 0; j < terminalsArray.length(); j++) {
                AllPointAddress.TerminalsBean mTerminalsBean = new AllPointAddress.TerminalsBean();
                JSONObject terminalsJsonItem = terminalsArray.getJSONObject(j);

                String guid = terminalsJsonItem.getString("guid");
                String mapUrl = terminalsJsonItem.getString("mapUrl");
                String name = terminalsJsonItem.getString("name");
                JSONObject t_locationJson = terminalsJsonItem.getJSONObject("location");
                int x = t_locationJson.getInt("x");
                int y = t_locationJson.getInt("y");
                AllPointAddress.TerminalsBean.LocationBeanX mLocationBeanX = new AllPointAddress.TerminalsBean.LocationBeanX();
                mTerminalsBean.setGuid(guid);
                mTerminalsBean.setMapUrl(mapUrl);
                mTerminalsBean.setName(name);
                mLocationBeanX.setX(x);
                mLocationBeanX.setY(y);
                mTerminalsBean.setLocation(mLocationBeanX);
                terminals.add(mTerminalsBean);
            }
            sentinelPointObj.setCameras(cameras);
            sentinelPointObj.setTerminals(terminals);
            List<AllPointAddress.CamerasBean> c = sentinelPointObj.getCameras();

            Log.d("TAG", "cc" + c.size());
            Log.e("TAG", sentinelPointObj.getCameras().size() + "////" + sentinelPointObj.getTerminals().size());
            handler.sendEmptyMessage(3);
        } catch (Exception e) {
            Log.e("TAG", "解析异常---->>>" + e.getMessage());
        }
    }


    List<View> allView = new ArrayList<>();

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
                for (int n = 0; n < allView.size(); n++) {
                    parentLayout.removeView(allView.get(n));
                    allView.clear();
                }
            }
        }

        if (sentinelResourcesGroupItemList != null && sentinelResourcesGroupItemList.size() > 0) {
            for (int i = 0; i < sentinelResourcesGroupItemList.size(); i++) {
                String location = sentinelResourcesGroupItemList.get(i).getLocation();
                if (!TextUtils.isEmpty(location)) {
                    String locationArry[] = location.split(",");
                    int x = Integer.parseInt(locationArry[0]);
                    int y = Integer.parseInt(locationArry[1]);
                    // Logutil.d("x-->>" + x + "\n y---->>" + y);

                    float sentinel_width = Float.parseFloat(decimalFormat.format(x / final_format_width)) - 15;
                    float sentinel_height = Float.parseFloat(decimalFormat.format(y / final_format_height)) - 48;
                    //定义显示其他哨兵的ImageView
                    ImageView other_image = new ImageView(App.getApplication());
                    allView.add(other_image);
                    displaySentinel(other_image, layoutParams, (int) sentinel_width, (int) sentinel_height);
                }
            }
        }

        Logutil.d("sentinelResourcesGroupItemList--->>" + sentinelResourcesGroupItemList.size());
        Logutil.d("allView---->>" + allView.size());


        if (allView != null && allView.size() > 0) {
            for (int k = 0; k < allView.size(); k++) {
                final int finalK = k;
                allView.get(k).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logutil.d("图标点击了" + sentinelResourcesGroupItemList.get(finalK).getLocation() + "\t" + sentinelResourcesGroupItemList.get(finalK).getName());

                        String location = sentinelResourcesGroupItemList.get(finalK).getLocation();
                        int x = Integer.parseInt(location.split(",")[0]);

                        Message message = new Message();
                        message.what = 16;
                        message.obj = allView.get(finalK);
                        message.arg1 = x;
                        handler.sendMessage(message);

                    }
                });
            }
        }


//        for (int i = 0; i < sentinelPointObj.getCameras().size(); i++) {
//            //计算哨位报警图距离显示图片控件左上角的xy坐标
//            float sentinel_width = Float.parseFloat(decimalFormat.format(sentinelPointObj.getCameras().get(i).getLocation().getX() / final_format_width)) - 15;
//            float sentinel_height = Float.parseFloat(decimalFormat.format(sentinelPointObj.getCameras().get(i).getLocation().getY() / final_format_height)) - 48;
//            //定义显示其他哨兵的ImageView
//            ImageView other_image = new ImageView(App.getApplication());
//
//
//            displaySentinel(other_image, layoutParams, (int) sentinel_width, (int) sentinel_height);
//        }
    }
    //Popuwindow
    PopupWindow window;

    /**
     * 根据View位置显示Popuwindow
     */
    private void showPopu(View v, int x) {
        //显示View
        View inflate = LayoutInflater.from(context).inflate(R.layout.popu_layout, null);
        //Popuwindow
        window = new PopupWindow(inflate, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //背景
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);
        window.setOutsideTouchable(true);
        //位置
        window.showAsDropDown(v, 2500, -100);
        window.update();
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_hide_btn_layout:
                //隐藏或显示左侧的功能布局
                hideLeftParentLayout();
                break;
            case R.id.right_hide_btn_layout:
                //隐藏或显示右侧的功能布局
                hideRightParentLayout();
                break;
            case R.id.close_alarm_btn:
                Logutil.d("点击了啊。 ");
                isHandleringAlarm = false;
                //关闭报警页面
//                if (isHandleringAlarm)
                alarmParentLayout.setVisibility(View.GONE);
                break;

            case R.id.handler_alarm_btn:
                // context.sendBroadcast(new Intent("handlerAlarm"));
                break;
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
            handler.sendEmptyMessage(9);
        }
    }

    /**
     * 屏保操作
     */
    private void handlerScreenSaver() {
        Logutil.d("副屏要屏保了");
        //更改正在屏保的标识
        isScreenSaver = true;
        //显示屏保
        screenSaveraParentLayout.setVisibility(View.VISIBLE);
        //隐藏副屏的父布局
        secondaryScreenParentLayout.setVisibility(View.GONE);
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
            handler.sendEmptyMessage(10);
        }
    }

    /**
     * 取消屏保的操作
     */
    private void handlerCancelScreenSaver() {
        Logutil.e("副屏要取消屏保了");
        //更改正在屏保的标识
        isScreenSaver = false;
        //显示屏保
        screenSaveraParentLayout.setVisibility(View.GONE);
        //隐藏副屏的父布局
        secondaryScreenParentLayout.setVisibility(View.VISIBLE);
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
            AlarmVideoSource alarm = (AlarmVideoSource) intent.getSerializableExtra("alarm");
            Logutil.i("Alarm-->>" + alarm);

            if (TextUtils.isEmpty(alarm.getFaceVideoName()) && TextUtils.isEmpty(alarm.getAlarmType())) {
                Logutil.e("alarm--- is null");
                return;
            }
            alarmQueueList.add(alarm);
            Logutil.d("alarmQueueList--- size-->>" + alarmQueueList.size());

            if (mAlarmQueueAdapter != null)
                mAlarmQueueAdapter.notifyDataSetChanged();

            initProcessedAlarmData();

            Message message = new Message();
            message.what = 5;
            message.obj = alarm;
            handler.sendMessage(message);

            playAlarmVideo(alarm);
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
            viewHolder.alarmName.setText(alarmQueueList.get(position).getFaceVideoName() + "\n" + alarmQueueList.get(position).getAlarmType());

            if (position == selectedItem) {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
                viewHolder.alarmName.setTextColor(0xffff0000);
            } else {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
                viewHolder.alarmName.setTextColor(0xffffffff);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            TextView alarmName;
        }
    }

    /**
     * 已处理的的报警队列的适配器
     */
    class ProcessedAlarmQueueAdapter extends BaseAdapter {

        LinkedList<AlarmVideoSource> mlist;

        private int selectedItem = -1;

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
            viewHolder.alarmName.setText(mlist.get(position).getFaceVideoName());

            if (position == selectedItem) {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
                viewHolder.alarmName.setTextColor(0xffff0000);
            } else {
                viewHolder.alarmName.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
                viewHolder.alarmName.setTextColor(0xffffffff);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            TextView alarmName;
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
        whichAlarmSelected = 0;
        mAlarmQueueAdapter.notifyDataSetChanged();


        alarmQueueListViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAlarmQueueAdapter.setSelectedItem(position);
                whichAlarmSelected = position;
                mAlarmQueueAdapter.notifyDataSetChanged();
                playAlarmVideo(alarmQueueList.get(whichAlarmSelected));
            }
        });

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
                    handler.sendEmptyMessage(15);
                } catch (JSONException e) {
                    Logutil.e("Sip组内数据解析异常::" + e.getMessage());
                }
            }
        });
        new Thread(httpThread).start();
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

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        Logutil.d("副屏点击" + ActivityUtils.getTopActivity());
        if (window != null && window.isShowing()){
            window.dismiss();
        }

        if (ActivityUtils.getTopActivity().equals("com.tehike.client.jst.app.project.ui.ScreenSaverActivity")) {
            ActivityUtils.getTopActivity().finish();
            handlerCancelScreenSaver();
        }
        // ActivityUtils.removeActivity();
        return super.onTouchEvent(event);
    }

    @Override
    public void onDisplayRemoved() {
        if (mReceiveAlarmBroadcast != null)
            context.unregisterReceiver(mReceiveAlarmBroadcast);
        if (mVideoSourcesBroadcast != null)
            context.unregisterReceiver(mVideoSourcesBroadcast);
        if (mReceiveScreenSaverBroadcast != null)
            context.unregisterReceiver(mReceiveScreenSaverBroadcast);
        if (mReceiveCancelScreenSaverBroadcast != null)
            context.unregisterReceiver(mReceiveCancelScreenSaverBroadcast);
        super.onDetachedFromWindow();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    disPlayBackGroupBitmap(bitmap);
                    break;
                case 2:
                    String result = (String) msg.obj;
                    //处理所有的哨位点信息
                    handlerSentinelPointData(result);
                    break;
                case 3:
                    disPlayAllSentinelPoints();
                    break;
                case 5:
                    if (!isHandleringAlarm) {
                        alarmParentLayout.setVisibility(View.VISIBLE);
                        isHandleringAlarm = true;
                    }
                    break;
                case 6:
                    loadingTv.setVisibility(View.VISIBLE);
                    loadingTv.setText(R.string.reconnect);
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case 7:
                    loadingView.setVisibility(View.GONE);
                    loadingView.clearAnimation();
                    loadingTv.setVisibility(View.GONE);
                    break;
                case 8:
                    loadingTv.setVisibility(View.VISIBLE);
                    loadingTv.setText(R.string.notconnect);
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case 9:
                    //屏倮设置
                    handlerScreenSaver();
                    break;
                case 10:
                    //取消屏保设置
                    handlerCancelScreenSaver();
                    break;
                case 11:
                    //提示网络异常
                    ToastUtils.showShort("网络异常!");
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
                case 16:
                    View v = (View) msg.obj;
                    int x = msg.arg1;

                    showPopu(v,x);
                    break;
            }
        }
    };


}
