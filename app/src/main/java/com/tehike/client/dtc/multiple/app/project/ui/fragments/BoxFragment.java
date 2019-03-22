package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SysInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.ui.views.CustomDialog;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
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
 * 描述：弹箱列表界面
 * 思路：
 * 选通过接口获取支持弹箱的型号有哪些，
 * 再加载sip分组，
 * 通过组id加载每个弹箱组数据
 * 再通过定时器定时刷新onlinedevices接口，
 * 比对guid查看弹箱数据
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/2/27 9:16
 */

public class BoxFragment extends BaseFragment {

    /**
     * 弹箱组布局
     */
    @BindView(R.id.box_group_listview_layout)
    public ListView boxListGroupLayout;

    /**
     * 播放弹箱视频的父布局
     */
    @BindView(R.id.play_box_video_parent_layout)
    FrameLayout playVideoParentLayout;

    /**
     * 显示弹箱视频信息的名称
     */
    @BindView(R.id.display_box_item_video_info_layout)
    TextView disPlayBoxNameTvLayout;

    /**
     * 弹箱视频加载的Loading动画
     */
    @BindView(R.id.box_video_loading_icon_layout)
    ImageView boxVideoLoadingIconLayout;

    /**
     * 弹箱视频加载的进度提示
     */
    @BindView(R.id.box_video_loading_tv_layout)
    TextView boxVideoLoadingTvLayout;

    /**
     * 弹箱视频播放的View
     */
    @BindView(R.id.box_video_preview_view_layout)
    NodePlayerView boxVideoPlayViewLayout;

    /**
     * 某个弹箱组的所有数据
     */
    @BindView(R.id.box_item_gridview_layout)
    GridView boxItemGridViewLayout;

    /**
     * 关闭预览按键
     */
    @BindView(R.id.close_preview_btn_layout)
    Button closePreviewBoxVideoLayout;

    /**
     * 弹箱组数据（模拟测试数据）
     */
    List<SipGroupInfoBean> boxGroupList = new ArrayList<>();

    /**
     * 某个弹箱组数据
     */
    List<BoxBean> boxItemList = new ArrayList<>();

    /**
     * 弹箱Item组适配器
     */
    AllBoxItemAdapter mAllBoxItemAdapter;

    /**
     * 弹箱组的适配器
     */
    BoxGroupListAdapter mBoxGroupListAdapter;

    /**
     * 弹箱面部视频播放器
     */
    NodePlayer boxVideoPlayer;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 当前页面是否可见
     */
    boolean isCurrentFragmentVisivle = false;

    /**
     * 当前支持弹箱的类型（接口获取）
     */
    String supporDeviceType = "";

    /**
     * 被选中弹箱的下标
     */
    int boxItemSelected = -1;

    /**
     * 定时的线程池任务
     */
    ScheduledExecutorService timingPoolTaskService;

    /**
     * 所有video字典
     */
    List<VideoBean> allVideoResourcesList;

    /**
     * 所有的Sip字典
     */
    List<SipBean> allSipResourcesList;

    /**
     * Sip是否完成缓存的广播
     */
    RefreshSipDataBroadcast broadcast;

    /**
     * 弹箱状态数据集合
     */
    public static List<BoxStatusBean> boxStatusList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_box_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //加载webapi接口判断支持的AmmoType
        initSupportAmmoxBoxDeviceType();

        //初始化弹箱面部视频播放器
        initializePlayer();

        //加载本地缓存的所有的Sip数据
        initializeCacheSipData();
    }

    /**
     * 初始化视频数据
     */
    private void initializeCacheSipData() {
        //先判断本地的视频源数据是否存在(报异常)
        try {
            String videoSourceStr = FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString();
            allSipResourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            allVideoResourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(videoSourceStr), VideoBean.class);
        } catch (Exception e) {
            //异常后，注册广播监听videoSource数据是否初始化成功
            registerRefreshVideoDataBroadcast();
        }
    }

    /**
     * 注册广播用，用于接收sip是否全部缓存完成
     */
    private void registerRefreshVideoDataBroadcast() {
        broadcast = new RefreshSipDataBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SipDone");
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        getActivity().registerReceiver(broadcast, intentFilter);
    }

    /**
     * 广播接收视频资源缓存完成后获取视频数据
     */
    class RefreshSipDataBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //取出本地缓存的所有的Video数据
                allVideoResourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
                allSipResourcesList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
                Logutil.d("allSipResourcesList--->>>" + allSipResourcesList.toString());
                initializeBoxGroupData();
            } catch (Exception e) {
                Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            }
        }
    }

    /**
     * 初始化设备类型（用于判断当前设备是否有弹箱功能）
     */
    private void initSupportAmmoxBoxDeviceType() {

        String deviceTypeUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._SUPPORT_DEVICE_TYPE;

        HttpBasicRequest deviceRequest = new HttpBasicRequest(deviceTypeUrl, new HttpBasicRequest.GetHttpData() {
            @Override
            public void httpData(String result) {
                Message message = new Message();
                message.what = 7;
                message.obj = result;
                handler.sendMessage(message);
            }
        });
        new Thread(deviceRequest).start();
    }

    /**
     * 初始化播放器
     */
    private void initializePlayer() {
        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.loading);
        boxVideoPlayer = new NodePlayer(getActivity());
        boxVideoPlayer.setPlayerView(boxVideoPlayViewLayout);
        boxVideoPlayer.setAudioEnable(false);
        boxVideoPlayer.setVideoEnable(true);
    }

    /**
     * 初始化所有弹箱的资源数据
     */
    private void initializeBoxGroupData() {

        //判断网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
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

                Logutil.d("当前弹箱组数据--->>>" + result);
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
     * 处理获取到的弹箱数据
     */
    private void handleBoxGroupData(String result) {
        //先清空集合数据
        if (boxGroupList != null && boxGroupList.size() > 0) {
            boxGroupList.clear();
        }
        //解析Json数据
        try {
            JSONObject jsonObject = new JSONObject(result);
            int sipCount = jsonObject.getInt("count");
            if (sipCount > 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("groups");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonItem = jsonArray.getJSONObject(i);
                    SipGroupInfoBean sipGroupInfoBean = new SipGroupInfoBean();
                    sipGroupInfoBean.setId(jsonItem.getInt("id"));
                    sipGroupInfoBean.setMember_count(jsonItem.getString("member_count"));
                    sipGroupInfoBean.setName(jsonItem.getString("name"));
                    boxGroupList.add(sipGroupInfoBean);
                }
            }
            Logutil.d("某个弹箱组数据--->>" + boxGroupList.toString());
            handler.sendEmptyMessage(4);
        } catch (Exception e) {
            Logutil.e("解析Sip分组数据异常");
            handler.sendEmptyMessage(1);
        }
    }

    /**
     * 展示弹箱
     */
    private void disPlayBoxListAdapter() {

        mBoxGroupListAdapter = new BoxGroupListAdapter();
        boxListGroupLayout.setAdapter(mBoxGroupListAdapter);
        mBoxGroupListAdapter.setSelectedItem(0);
        mBoxGroupListAdapter.notifyDataSetChanged();
        //默认加载第一组的数据
        Message handlerMess = new Message();
        handlerMess.arg1 = boxGroupList.get(0).getId();
        handlerMess.what = 5;
        handler.sendMessage(handlerMess);

        //点击事件
        boxListGroupLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBoxGroupListAdapter.setSelectedItem(position);
                mBoxGroupListAdapter.notifyDataSetChanged();
                handler.sendEmptyMessage(12);
                SipGroupInfoBean mSipGroupInfoBean = boxGroupList.get(position);
                Logutil.i("SipGroupInfoBean-->>" + mSipGroupInfoBean.toString());
                int groupId = mSipGroupInfoBean.getId();
                disPlayAllBoxItem(groupId);

            }
        });


        //定时线程任务池
        if (timingPoolTaskService == null || timingPoolTaskService.isShutdown())
            timingPoolTaskService = Executors.newSingleThreadScheduledExecutor();
        //开户定时的线程滠
        if (!timingPoolTaskService.isShutdown()) {
            timingPoolTaskService.scheduleWithFixedDelay(new TimingRefreshBoxStatus(), 0L, 6 * 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 定时请求Box状态
     */
    class TimingRefreshBoxStatus extends Thread {
        @Override
        public void run() {
            //可见时刷新
            if (isVisible() && isCurrentFragmentVisivle) {
                //地址
                String boxStatusUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._BOX_DEVICES;
                //basic请求
                HttpBasicRequest httpBasicRequest = new HttpBasicRequest(boxStatusUrl, new HttpBasicRequest.GetHttpData() {
                    @Override
                    public void httpData(String result) {
                        Message message = new Message();
                        message.what = 8;
                        message.obj = result;
                        handler.sendMessage(message);
                    }
                });
                new Thread(httpBasicRequest).start();
                //Logutil.d("可见刷新");
            } else {
                // Logutil.d("不可见，不刷新");
            }
        }
    }

    /**
     * 处理设备类型数据
     */
    private void handlerDeviceTypeData(String deviceTypeData) {
        //判断数据是否为空
        if (TextUtils.isEmpty(deviceTypeData)) {
            if (getActivity() != null && isCurrentFragmentVisivle) {
                showProgressFail("无设备类型数据!!!");
            }
            return;
        }

        //解析Json获取数据
        try {
            JSONObject jsonObject = new JSONObject(deviceTypeData);
            //本页面只支持弹箱
            JSONArray jsonArray = jsonObject.getJSONArray("AmmoDeviceType");
            //遍历
            for (int i = 0; i < jsonArray.length(); i++) {
                supporDeviceType += jsonArray.getString(i);
            }
            Logutil.d("支持弹箱的设备-->>" + supporDeviceType);

            initializeBoxGroupData();
        } catch (Exception e) {
            WriteLogToFile.info("support box type error --->>>" + e.getMessage());
            Logutil.e("解析设备类型Exception--->>>" + e.getMessage());
        }
    }

    /**
     * 根据弹箱组获取数据
     */
    private void disPlayAllBoxItem(int id) {

        //提示无网络
        if (!NetworkUtils.isConnected()) {
            handler.sendEmptyMessage(2);
            return;
        }
        if (mAllBoxItemAdapter != null) {
            mAllBoxItemAdapter = null;
            boxItemList.clear();
        }

        //获取某个组内数据
        String sipGroupItemUrl = AppConfig.WEB_HOST + SysinfoUtils.getServerIp() + AppConfig._USIPGROUPS_GROUP;

        Logutil.i("id--->>" + id);
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
                //解析sip资源
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.isNull("errorCode")) {
                        Logutil.w("请求某个弹箱组信息异常" + result);
                        WriteLogToFile.info("请求某个弹箱组信息异常:" + result);
                        return;
                    }

                    int count = jsonObject.getInt("count");
                    if (count > 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("resources");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonItem = jsonArray.getJSONObject(i);
                            //解析
                            BoxBean mBoxBean = new BoxBean();
                            mBoxBean.setDeviceType(jsonItem.getString("deviceType"));
                            mBoxBean.setId(jsonItem.getString("id"));
                            mBoxBean.setIpAddress(jsonItem.getString("ipAddress"));
                            mBoxBean.setName(jsonItem.getString("name"));
                            mBoxBean.setNumber(jsonItem.getString("number"));
                            mBoxBean.setSentryId(jsonItem.getInt("sentryId"));
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
                                    mBoxBean.setVodeobean(videoBean);
                                }
                            }
                            if (supporDeviceType.contains(mBoxBean.getDeviceType())) {
                                boxItemList.add(mBoxBean);
                            } else {
                                continue;
                            }
                        }
                    }
                    //赋值当前弹的视频对象
                    if (allVideoResourcesList != null && allVideoResourcesList.size() > 0) {
                        for (int i = 0; i < allVideoResourcesList.size(); i++) {
                            for (int n = 0; n < boxItemList.size(); n++) {
                                String videoId = allVideoResourcesList.get(i).getId();
                                VideoBean v = boxItemList.get(n).getVodeobean();
                                if (v != null) {
                                    String videoId1 = v.getId();
                                    if (!TextUtils.isEmpty(videoId) && !TextUtils.isEmpty(videoId1)) {
                                        if (videoId.equals(videoId1)) {
                                            boxItemList.get(n).setVodeobean(allVideoResourcesList.get(i));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    handler.sendEmptyMessage(6);
                } catch (JSONException e) {
                    WriteLogToFile.info("弹箱组内数据解析异常::" + e.getMessage());
                    Logutil.e("弹箱组内数据解析异常::" + e.getMessage());
                }
            }
        });

        new Thread(httpThread).start();
    }

    /**
     * 展示所有的弹箱组
     */
    private void disPlayBoxItemAdapter() {
        if (mAllBoxItemAdapter == null)
            mAllBoxItemAdapter = new AllBoxItemAdapter(getActivity());
        boxItemGridViewLayout.setAdapter(mAllBoxItemAdapter);
        mAllBoxItemAdapter.notifyDataSetChanged();
        //item点击事件
        boxItemGridViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (boxItemList.get(position).getAmmoBox() == 1) {
                    boxItemSelected = position;
                } else {
                    boxItemSelected = -1;
                }

                Logutil.d("sipItemSelected--->>>" + boxItemSelected);
                //  Logutil.d("sipItemSelected---->>>>" + boxItemList.get(boxItemSelected).toString());
                mAllBoxItemAdapter.setSeclection(position);
                mAllBoxItemAdapter.notifyDataSetChanged();

            }
        });
    }

    /**
     * 弹箱组适配器
     */
    class BoxGroupListAdapter extends BaseAdapter {

        private int selectedItem = -1;

        @Override
        public int getCount() {
            return boxGroupList.size();
        }

        @Override
        public Object getItem(int position) {
            return boxGroupList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_box_listview_layout, null);
                viewHolder.boxGroupItemNameLayout = convertView.findViewById(R.id.box_group_item_name_layout);
                viewHolder.boxGroupParentLayout = convertView.findViewById(R.id.box_sip_group_parent_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SipGroupInfoBean itemBean = boxGroupList.get(position);
            viewHolder.boxGroupItemNameLayout.setText(itemBean.getName());

            if (position == selectedItem) {
                viewHolder.boxGroupParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
            } else {
                viewHolder.boxGroupParentLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_normal);
            }
            return convertView;
        }

        //内部类
        class ViewHolder {
            //显示弹箱分组名
            TextView boxGroupItemNameLayout;
            //设置选中的颜色
            RelativeLayout boxGroupParentLayout;
        }
    }

    /**
     * 展示某个组内的所有弹箱状态
     */
    class AllBoxItemAdapter extends BaseAdapter {
        //选中对象的标识
        private int clickTemp = -1;
        //布局加载器
        private LayoutInflater layoutInflater;

        //构造函数
        public AllBoxItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return boxItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return boxItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSeclection(int position) {
            clickTemp = position;
            notifyDataSetChanged();
        }

        //刷新item背景
        public void refreshItemBg(int index) {
            //判断view是否空
            if (boxItemGridViewLayout == null) {
                return;
            }
            //得到当前的item
            View v = boxItemGridViewLayout.getChildAt(index);
            LinearLayout mainLayout = v.findViewById(R.id.box_status_main_layout);
            //设置背景
            mainLayout.setBackgroundColor(Color.TRANSPARENT);
            mainLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_normal);
            //重置标识
            boxItemSelected = -1;
            clickTemp = -1;
            //适配器刷新
            notifyDataSetChanged();
            Logutil.d("刷新了item");
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            //复用ConvertView
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_box_status_item, null);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.box_item_name);
                viewHolder.mRelativeLayout = (FrameLayout) convertView.findViewById(R.id.box_item_layout);
                viewHolder.mainLayout = convertView.findViewById(R.id.box_status_main_layout);
                viewHolder.deviceType = convertView.findViewById(R.id.box_device_type_layout);
                viewHolder.StatusIcon = convertView.findViewById(R.id.box_status_icon_layout);
                viewHolder.boxStatus = convertView.findViewById(R.id.box_status_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //当前的弹箱对象
            BoxBean mBoxBean = boxItemList.get(position);
            //显示名称
            viewHolder.itemName.setText(mBoxBean.getName());
            //判断当前弹箱是否开户状态
            if (boxStatusList != null) {
                for (int i = 0; i < boxStatusList.size(); i++) {
                    BoxStatusBean boxBean = boxStatusList.get(i);
                    int ammoCode = boxBean.getDeviceStatus().getAmmoBox();
                    if (boxBean.getID().equals(mBoxBean.getId())) {
                        if (ammoCode == 0) {
                            viewHolder.boxStatus.setText("已开启");
                        } else {
                            viewHolder.boxStatus.setText("关闭");
                        }
                        mBoxBean.setAmmoBox(ammoCode);
                        viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_normal);
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_free);
                    } else {
                        viewHolder.StatusIcon.setBackgroundResource(R.mipmap.intercom_call_icon_offline);
                    }
                }
            }
            if (clickTemp == position) {
                if (mBoxBean.getAmmoBox() == 1) {
                    viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_selected);
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

            TextView boxStatus;
        }
    }

    /**
     * 处理弹箱状态数据
     */
    private void handlerBoxStatusData(String boxStatusData) {
        //提示无弹箱 数据
        if (TextUtils.isEmpty(boxStatusData)) {
            if (getActivity() != null && isCurrentFragmentVisivle) {
                showProgressFail("无弹箱状态数据！！！");
            }
            return;
        }
        //清空集合
        if (boxStatusList != null && boxStatusList.size() > 0) {
            boxStatusList.clear();
        }
        //json解析
        try {
            JSONArray jsonArray = new JSONArray(boxStatusData);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String boxID = jsonObject.getString("ID");
                    int Latitude = jsonObject.getInt("Latitude");
                    int Longitude = jsonObject.getInt("Longitude");
                    String Stamp = jsonObject.getString("Stamp");
                    JSONObject jsonItem = jsonObject.getJSONObject("DeviceStatus");
                    BoxStatusBean.DeviceStatus deviceStatus = new BoxStatusBean.DeviceStatus();
                    BoxStatusBean boxBean = new BoxStatusBean();
                    deviceStatus.setAmmoBox(jsonItem.getInt("AmmoBox"));
                    deviceStatus.setBlueTooth(jsonItem.getInt("BlueTooth"));
                    deviceStatus.setCPU(jsonItem.getInt("CPU"));
                    deviceStatus.setMem(jsonItem.getInt("Mem"));
                    boxBean.setDeviceStatus(deviceStatus);
                    boxBean.setID(boxID);
                    boxBean.setLatitude(Latitude);
                    boxBean.setLongitude(Longitude);
                    boxBean.setStamp(Stamp);
                    boxStatusList.add(boxBean);
                }
                handler.sendEmptyMessage(9);
            }

        } catch (Exception e) {
            Logutil.e("解析弹箱状态数据异常-->>" + e.getMessage() + "\n" + boxStatusData);
        }
    }

    /**
     * 弹箱封装的实体类
     */
    class BoxBean implements Serializable {
        private String deviceType;
        private String id;
        private String ipAddress;
        private String location;
        private String name;
        private String number;
        private int sentryId;
        private int state;
        private VideoBean vodeobean;
        //弹箱状态
        private int AmmoBox;

        @Override
        public String toString() {
            return "BoxBean{" +
                    "deviceType='" + deviceType + '\'' +
                    ", id='" + id + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", location='" + location + '\'' +
                    ", name='" + name + '\'' +
                    ", number='" + number + '\'' +
                    ", sentryId=" + sentryId +
                    ", state=" + state +
                    ", vodeobean=" + vodeobean +
                    ", AmmoBox=" + AmmoBox +
                    '}';
        }

        public BoxBean() {
        }

        public String getDeviceType() {

            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public int getSentryId() {
            return sentryId;
        }

        public void setSentryId(int sentryId) {
            this.sentryId = sentryId;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public VideoBean getVodeobean() {
            return vodeobean;
        }

        public void setVodeobean(VideoBean vodeobean) {
            this.vodeobean = vodeobean;
        }

        public int getAmmoBox() {
            return AmmoBox;
        }

        public void setAmmoBox(int ammoBox) {
            AmmoBox = ammoBox;
        }
    }

    /**
     * 弹箱状态封装的实体类
     */
    static class BoxStatusBean implements Serializable {

//    {
//        "DeviceStatus": {
//        "AmmoBox": 1,
//                "BlueTooth": 0,
//                "CPU": 59,
//                "Mem": 68,
//                "Power": 100,
//                "Signal": 255
//    },
//        "ID": "{05290699-a455-4395-a237-473637b79caa}",
//            "Latitude": 0,
//            "Longitude": 0,
//            "Stamp": "2019-02-25 10:17:16"
//    },


        //状态
        DeviceStatus deviceStatus;
        //唯一ID
        String ID;
        //经纬度
        int Latitude;
        int Longitude;
        //最后一次心跳时间戳
        String Stamp;

        /**
         * 弹箱状态
         */
        static class DeviceStatus implements Serializable {

            int AmmoBox;
            int BlueTooth;
            int CPU;
            int Mem;
            int Signal;


            public int getAmmoBox() {
                return AmmoBox;
            }

            public void setAmmoBox(int ammoBox) {
                AmmoBox = ammoBox;
            }

            public int getBlueTooth() {
                return BlueTooth;
            }

            public void setBlueTooth(int blueTooth) {
                BlueTooth = blueTooth;
            }

            public int getCPU() {
                return CPU;
            }

            public void setCPU(int CPU) {
                this.CPU = CPU;
            }

            public int getMem() {
                return Mem;
            }

            public void setMem(int mem) {
                Mem = mem;
            }

            public int getSignal() {
                return Signal;
            }

            public void setSignal(int signal) {
                Signal = signal;
            }

            public DeviceStatus() {
            }
        }


        public BoxStatusBean() {
        }

        public DeviceStatus getDeviceStatus() {
            return deviceStatus;
        }

        public void setDeviceStatus(DeviceStatus deviceStatus) {
            this.deviceStatus = deviceStatus;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public int getLatitude() {
            return Latitude;
        }

        public void setLatitude(int latitude) {
            Latitude = latitude;
        }

        public int getLongitude() {
            return Longitude;
        }

        public void setLongitude(int longitude) {
            Longitude = longitude;
        }

        public String getStamp() {
            return Stamp;
        }

        public void setStamp(String stamp) {
            Stamp = stamp;
        }

        @Override
        public String toString() {
            return "BoxStatusBean{" +
                    "deviceStatus=" + deviceStatus +
                    ", ID='" + ID + '\'' +
                    ", Latitude=" + Latitude +
                    ", Longitude=" + Longitude +
                    ", Stamp='" + Stamp + '\'' +
                    '}';
        }
    }

    /**
     * 关闭弹箱预览
     */
    private void closeBoxVideoPreView() {
        //弹箱视频停止播放
        if (boxVideoPlayer != null && boxVideoPlayer.isPlaying()) {
            boxVideoPlayer.stop();
        }
        //隐藏和显示Ui
        boxItemGridViewLayout.setVisibility(View.VISIBLE);
        playVideoParentLayout.setVisibility(View.GONE);
        disPlayBoxNameTvLayout.setVisibility(View.GONE);
        //清除动画
        boxVideoLoadingIconLayout.clearAnimation();
        boxVideoLoadingIconLayout.setVisibility(View.GONE);
        boxVideoLoadingTvLayout.setVisibility(View.GONE);
        closePreviewBoxVideoLayout.setVisibility(View.GONE);
    }

    /**
     * 预览
     */
    @OnClick(R.id.offline_preview_btn_layout)
    public void boxVideoPreview(View view) {
        //判断是否选中
        if (boxItemSelected == -1) {
            handler.sendEmptyMessage(14);
            return;
        }
        //获取播放地址和弹箱名称
        String rtsp = "";
        String boxName = "";
        if (boxItemSelected != -1) {
            BoxBean mBoxBean = boxItemList.get(boxItemSelected);
            if (mBoxBean != null) {
                Logutil.d("BoxBean-->>>" + mBoxBean.toString());
                boxName = mBoxBean.getName();

                VideoBean v = null;
                if (allSipResourcesList != null) {
                    for (int i = 0; i < allSipResourcesList.size(); i++) {
                        if (allSipResourcesList.get(i).getId().equals(mBoxBean.getId())) {
                            v = allSipResourcesList.get(i).getAmmoBean();
                        }
                    }
                }
                if (v != null) {
                    rtsp = v.getRtsp();
                    if (TextUtils.isEmpty(rtsp)) {
                        handler.sendEmptyMessage(10);
                        return;
                    }
                } else {
                    handler.sendEmptyMessage(10);
                    return;
                }
            } else {
                handler.sendEmptyMessage(10);
                return;
            }
        } else {
            handler.sendEmptyMessage(10);

            return;
        }

        boxItemGridViewLayout.setVisibility(View.GONE);
        playVideoParentLayout.setVisibility(View.VISIBLE);

        disPlayBoxNameTvLayout.setVisibility(View.VISIBLE);
        disPlayBoxNameTvLayout.setText(boxName + "弹箱视频");

        boxVideoLoadingIconLayout.setVisibility(View.VISIBLE);
        boxVideoLoadingIconLayout.startAnimation(mLoadingAnim);

        boxVideoLoadingTvLayout.setVisibility(View.VISIBLE);
        closePreviewBoxVideoLayout.setVisibility(View.VISIBLE);
        boxVideoPlayer.setInputUrl(rtsp);
        boxVideoPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
            @Override
            public void onEventCallback(NodePlayer player, int event, String msg) {

                if (event == 1001) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingIconLayout.setVisibility(View.GONE);
                            boxVideoLoadingTvLayout.setVisibility(View.INVISIBLE);
                            boxVideoLoadingIconLayout.clearAnimation();
                        }
                    });
                }
                //视频连接失败, 会进行自动重连.
                if (event == 1003 || event == 1002) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                            boxVideoLoadingTvLayout.setText("重新连接");
                        }
                    });
                }
                //视频播放中网络异常,
                if (event == 1005) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                            boxVideoLoadingTvLayout.setText("网络异常");
                        }
                    });
                }
                //网络连接超时
                if (event == 1006) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                            boxVideoLoadingTvLayout.setText("网络连接超时");
                        }
                    });
                }

            }
        });
        boxVideoPlayer.start();

    }

    /**
     * 关闭预览
     */
    @OnClick(R.id.close_preview_btn_layout)
    public void boxVideoStopPreView(View view) {
        closeBoxVideoPreView();
    }

    /**
     * 开启某个弹箱
     */
    @OnClick(R.id.quick_open_box_btn_layout)
    public void openBox(View view) {
        //判断弹箱是否选中
        if (boxItemSelected == -1) {
            handler.sendEmptyMessage(14);
            return;
        }
        //Log
        Logutil.d("boxItemSelected-->>" + boxItemSelected);
        //获取当前弹箱的实体类
        final BoxBean mBoxBean = boxItemList.get(boxItemSelected);
        //判断数据是否为空
        if (mBoxBean != null) {
            //弹出确认框
            new CustomDialog(getActivity(), R.style.dialog, "确定要开" + mBoxBean.getName() + "启子弹箱？", new CustomDialog.OnCloseListener() {
                @Override
                public void onClick(Dialog dialog, boolean confirm) {
                    if (confirm) {
                        dialog.dismiss();
                        requestOpenBox(mBoxBean);
                        //消除选中后的背景
                        if (mAllBoxItemAdapter != null)
                            mAllBoxItemAdapter.refreshItemBg(boxItemSelected);
                    }
                }
            }).setTitle("重要提示").show();
        }
    }

    /**
     * 开启所有的弹箱
     */
    @OnClick(R.id.all_quick_open_box_btn_layout)
    public void openAllBox(View view) {

        new CustomDialog(getActivity(), R.style.dialog, "确定要开启子弹箱？", new CustomDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    if (boxStatusList != null && boxStatusList.size() > 0) {
                        for (int i = 0; i < boxStatusList.size(); i++) {
                            BoxStatusBean mBoxStatusBean = boxStatusList.get(i);
                            Logutil.d("开启所有：" + mBoxStatusBean.toString());
                            BoxStatusBean.DeviceStatus mDeviceStatus = mBoxStatusBean.getDeviceStatus();
                            if (mDeviceStatus != null) {
                                int ammoCode = mDeviceStatus.getAmmoBox();
                                if (ammoCode == 1) {
                                    String boxId = mBoxStatusBean.getID();
                                    if (!TextUtils.isEmpty(boxId)) {
                                        OpenBoxThread openBoxThread = new OpenBoxThread(0, boxId);
                                        new Thread(openBoxThread).start();
                                        Logutil.d("开启" + i);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                }
                            }
                        }
                    }
                }
            }
        }).setTitle("重要提示").show();
    }

    /**
     * 离线开启
     */
    @OnClick(R.id.offline_open_box_btn_layout)
    public void offlineOpenBox(View view) {
        if (boxItemSelected == -1) {
            handler.sendEmptyMessage(14);
            return;
        }
        //消除选中后的背景
        if (mAllBoxItemAdapter != null)
            mAllBoxItemAdapter.refreshItemBg(boxItemSelected);

        if (getActivity() != null && isCurrentFragmentVisivle) {
            showProgressFail("正在开发！！！");
        }
    }

    /**
     * 向服务器请求开启子弹箱
     */
    private void requestOpenBox(BoxBean mBoxBean) {
        if (mBoxBean != null) {
            App.startSpeaking("开启" + mBoxBean.getName() + "子弹箱");
            int ammoCode = mBoxBean.getAmmoBox();
            if (ammoCode == 1) {
                String boxId = mBoxBean.getId();
                if (!TextUtils.isEmpty(boxId)) {

                    //保存事件到数据库
                    ContentValues contentValues1 = new ContentValues();
                    contentValues1.put("time", TimeUtils.getCurrentTime());
                    contentValues1.put("event", SysinfoUtils.getSysinfo().getDeviceName() + "开启" + mBoxBean.getName() + "的子弹箱");
                    new DbUtils(App.getApplication()).insert(DbHelper.EVENT_TAB_NAME, contentValues1);

                    OpenBoxThread openBoxThread = new OpenBoxThread(3, boxId);
                    new Thread(openBoxThread).start();
                }
            } else {
                Logutil.e("开启了");
            }
        }
    }

    /**
     * 向服务器申请开启弹箱的子线程
     */
    class OpenBoxThread extends Thread {
        //请求动作
        int actionId;
        //要开启弹箱的唯一ID
        String boxId;
        //本机的Ip
        String nativeIp;
        //服务器IP
        String serverIp;
        //申请开启弹箱的服务器端口
        int port;


        public OpenBoxThread(int actionId, String boxId) {
            this.actionId = actionId;
            this.boxId = boxId;
        }


        @Override
        public void run() {
            //本机Ip
            if (NetworkUtils.isConnected())
                nativeIp = NetworkUtils.getIPAddress(true);
            //服务器Ip
            serverIp = SysinfoUtils.getServerIp();
            //服务器端口
            SysInfoBean mSysInfoBean = SysinfoUtils.getSysinfo();
            if (mSysInfoBean != null) {
                port = mSysInfoBean.getAlertPort();
            }
            //判断参数
            if (TextUtils.isEmpty(serverIp) || port == 0) {
                Logutil.e("丢失参数!");
                return;
            }
            //拼加请求协议
            byte[] sendData = new byte[72];
            // 数据头
            byte[] flag = "ReqB".getBytes();
            System.arraycopy(flag, 0, sendData, 0, 4);
            // 版本号
            byte[] version = new byte[4];
            version[0] = 0;
            version[1] = 0;
            version[2] = 0;
            version[3] = 1;
            System.arraycopy(version, 0, sendData, 4, 4);
            // 动作， 0-请求，1-同意，2-拒绝，3-直接开启
            sendData[9] = (byte) actionId;
            sendData[10] = 0;
            sendData[11] = 0;
            sendData[12] = 0;

            // uiAction = 0, 保存设备端随机生成的申请码
            byte[] requestCode = new byte[4];

            // uiAction = 0, 保存设备端的SALT
            byte[] requestSalt = new byte[4];
            // uiAction = 1, 保存服务端根据申请码计算得到的开锁码
            byte[] responseCode = new byte[4];

            System.arraycopy(requestCode, 0, sendData, 12, 4);
            System.arraycopy(requestSalt, 0, sendData, 16, 4);
            System.arraycopy(responseCode, 0, sendData, 20, 4);

            byte[] senderIP = nativeIp.getBytes();

            System.arraycopy(senderIP, 0, sendData, 24, 4);

            byte[] senderID = boxId.getBytes();
            System.arraycopy(senderID, 0, sendData, 28, senderID.length);
            System.out.println(Arrays.toString(sendData));

            Socket socket = null;
            OutputStream os = null;
            try {
                // 获取报警服务器ip
                socket = new Socket(serverIp, port);
                os = socket.getOutputStream();
                os.write(sendData);
                os.flush();

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(13);
//                InputStream in = socket.getInputStream();
//                byte[] headers = new byte[72];
//                int returnLength = in.read(headers);
//                //读取返回的Action
//                byte[] action = new byte[4];
//                System.arraycopy(action, 0, headers, 8, 4);
//                Logutil.d("Action" + Arrays.toString(action));

            } catch (IOException e) {
                String err = e.getMessage();
                Logutil.e("error-->>" + err);
                handler.sendEmptyMessage(11);
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        isCurrentFragmentVisivle = isVisibleToUser;

        if (isVisibleToUser) {
            if (mAllBoxItemAdapter != null) {
                mAllBoxItemAdapter.notifyDataSetChanged();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroyView() {
        //停止时计
        if (timingPoolTaskService != null) {
            timingPoolTaskService.shutdown();
            timingPoolTaskService = null;
        }
        //取消广播
        if (broadcast != null) {
            getActivity().unregisterReceiver(broadcast);
            broadcast = null;
        }
        //移除handler所有消息队列
        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroyView();
    }

    /**
     * handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //提示无弹箱数据
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressFail("无弹箱数据!!!");
                    }
                    break;
                case 2:
                    //提示网络异常
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressFail("网络异常!!!");
                    }
                    break;
                case 3:
                    //处理弹箱组数据
                    String boxData = (String) msg.obj;
                    handleBoxGroupData(boxData);
                    break;
                case 4:
                    //展示弹箱组数据
                    disPlayBoxListAdapter();
                    break;
                case 5:
                    //根据弹箱组ID处理某个组内弹箱数据
                    int boxGroupId = msg.arg1;
                    disPlayAllBoxItem(boxGroupId);
                    break;
                case 6:
                    //展示某个组内的弹箱数据
                    disPlayBoxItemAdapter();
                    break;
                case 7:
                    //处理设备类型数据
                    String deviceTypeData = (String) msg.obj;
                    handlerDeviceTypeData(deviceTypeData);
                    break;
                case 8:
                    //处理弹箱状态数据
                    String boxStatusData = (String) msg.obj;
                    handlerBoxStatusData(boxStatusData);
                    break;
                case 9:
                    //刷新弹箱状态
                    if (mAllBoxItemAdapter != null)
                        mAllBoxItemAdapter.notifyDataSetChanged();
                    break;
                case 10:
                    //提示无弹箱视频
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressFail("无弹箱面部视频!");
                        //消除选中后的背景
                        if (mAllBoxItemAdapter != null)
                            mAllBoxItemAdapter.refreshItemBg(boxItemSelected);
                    }
                    break;
                case 11:
                    //提示申请开箱失败
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressFail("申请开箱失败!");
                    }
                    break;
                case 12:
                    //关闭弹箱预览
                    closeBoxVideoPreView();
                    break;
                case 13:
                    //提示开锁命令已发送
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressSuccess("开启子弹箱命令已发送!");
                        App.startSpeaking("开启子弹箱命令已发送");
                    }
                    break;
                case 14:
                    //提示弹箱未选中
                    if (getActivity() != null && isCurrentFragmentVisivle) {
                        showProgressFail("请选择子弹箱!!!");
                    }
                    break;
            }
        }
    };
}
