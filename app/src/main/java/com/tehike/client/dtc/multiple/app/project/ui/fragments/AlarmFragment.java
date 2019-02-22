package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.ui.views.CustomViewPagerSlide;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.RecordLog;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述：用于显示报警信息的Fragment页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/2 10:56
 */
public class AlarmFragment extends BaseFragment {
    /**
     * 展示报警队列的ListView
     */
    @BindView(R.id.alarm_list_layout)
    public ListView disPlayAlarmListViewLayout;

    /**
     * 显示加载报警视频源的LoadingView
     */
    @BindView(R.id.alarm_video_loading_icon_layout)
    ImageView disPlayAlarmVideoLoadingIconLayout;

    /**
     * 显示加载 报警视频源的LoadingTv
     */
    @BindView(R.id.alarm_video_loading_tv_layout)
    TextView disPlayAlarmVideoLoadingTvLayout;

    /**
     * 播放报警通话视频源的View
     */
    @BindView(R.id.alarm_call_video_view_layout)
    public NodePlayerView alarmCallVideoViewLayout;

    /**
     * 显示报警通话的加载View
     */
    @BindView(R.id.alarm_call_video_loading_icon_layout)
    ImageView alarmCallVideoLoadingIconLayout;

    /**
     * 显示报警通话的加载Tv
     */
    @BindView(R.id.alarm_call_video_loading_tv_layout)
    TextView alarmCallVideoLoadingTvLayout;

    /**
     * 播放报警视频源的View
     */
    @BindView(R.id.alarm_video_view_layout)
    public NodePlayerView alarmVideoViewLayout;

    /**
     * 状态报警队列的集合
     */
    LinkedList<AlarmVideoSource> allaAlarmSourceList = new LinkedList<>();

    /**
     * 本地缓存的所有的视频数制（视频字典）
     */
    List<VideoBean> allVideoList;

    /**
     * 接收报警信息的广播
     */
    ReceiveAlarmBroadcast mReceiveAlarmBroadcast;

    /**
     * 接收本地缓存的视频字典广播
     */
    VideoSourcesBroadcast mVideoSourcesBroadcast;

    /**
     * 展示报警信息的适配器
     */
    AlarmListAdapter mAlarmListAdapter;

    /**
     * 报警视频源播放器
     */
    NodePlayer alarmPlayer;

    /**
     * 报警视频源播放器
     */
    NodePlayer alarmCallPlayer;

    /**
     * 加载动画
     */
    Animation mLoadingAnim;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_alarm_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.loading);

        //取出本地缓存的所有的Video数据
        try {
            allVideoList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_VIDEO).toString()), VideoBean.class);
            Logutil.d("我获取到数据了" + allVideoList.toString());
        } catch (Exception e) {
            Logutil.e("取video字典广播异常---->>>" + e.getMessage());
            registerAllVideoSourceDoneBroadcast();
        }

        //注册广播接收报警信息
      //  registerReceiveAlarmBroadcast();

        //初始化播放器信息
        initializePlayer();

        //初始化报警信息数据
        initlizeAlarmData();

    }

    /**
     * 注册广播监听所有的视频数据是否解析完成
     */
    private void registerAllVideoSourceDoneBroadcast() {
        mVideoSourcesBroadcast = new VideoSourcesBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.RESOLVE_VIDEO_DONE_ACTION);
        getActivity().registerReceiver(mVideoSourcesBroadcast, intentFilter);
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
     * 初始化播放器
     */
    private void initializePlayer() {
        alarmPlayer = new NodePlayer(getActivity());
        alarmPlayer.setPlayerView(alarmVideoViewLayout);
        alarmPlayer.setVideoEnable(true);
        alarmPlayer.setAudioEnable(false);


        alarmCallPlayer = new NodePlayer(getActivity());
        alarmCallPlayer.setPlayerView(alarmCallVideoViewLayout);
        alarmCallPlayer.setVideoEnable(true);
        alarmCallPlayer.setAudioEnable(false);
    }

    /**
     * 哪个报警被选中时的标识
     */
    int whichAlarmSelected = -1;

    /**
     * 初始化数据
     */
    private void initlizeAlarmData() {

        //显示报警列表的适配器
        mAlarmListAdapter = new AlarmListAdapter();
        disPlayAlarmListViewLayout.setAdapter(mAlarmListAdapter);
        //默认第一个选中
        mAlarmListAdapter.setSelectedItem(0);
        whichAlarmSelected = 0;
        mAlarmListAdapter.notifyDataSetChanged();


        disPlayAlarmListViewLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAlarmListAdapter.setSelectedItem(position);
                whichAlarmSelected = position;
                mAlarmListAdapter.notifyDataSetChanged();
                playAlarmVideo(allaAlarmSourceList.get(whichAlarmSelected));
            }
        });

    }

    /**
     * 注册接收报警信息广播
     */
    private void registerReceiveAlarmBroadcast() {
        mReceiveAlarmBroadcast = new ReceiveAlarmBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.ALARM_ACTION);
        getActivity().registerReceiver(mReceiveAlarmBroadcast, intentFilter);
    }

    /**
     * 广播接收报警信息
     */
    class ReceiveAlarmBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AlarmVideoSource alarm = (AlarmVideoSource) intent.getSerializableExtra("alarm");
            Logutil.i("Alarm-->>" + alarm);
            Message message = new Message();
            message.what = 1;
            message.obj = alarm;
            handler.sendMessage(message);
        }
    }

    /**
     * 关闭报警
     */
    @OnClick(R.id.colse_alarm_btn_layout)
    public void closeAlarmBtn(View view) {
        if (allaAlarmSourceList != null && allaAlarmSourceList.size() > 0) {

            if (whichAlarmSelected == -1) {
                Logutil.d("报警未选中");
                return;
            }
            allaAlarmSourceList.remove(whichAlarmSelected);
            RecordLog.wirteLog(allaAlarmSourceList.get(whichAlarmSelected).toString() + "报警已处理");
            if (mAlarmListAdapter != null) {
                mAlarmListAdapter.setSelectedItem(0);
                whichAlarmSelected = 0;
                mAlarmListAdapter.notifyDataSetChanged();
            }



//            RecordLog.wirteLog(allaAlarmSourceList.get(whichAlarmSelected).toString() + "报警已处理");
//            allaAlarmSourceList.remove();
//            if (mAlarmListAdapter != null) {
//                mAlarmListAdapter.notifyDataSetChanged();
//            }
//            if (alarmPlayer != null && alarmPlayer.isPlaying()) {
//                alarmPlayer.stop();
//            }
//            if (alarmCallPlayer != null && alarmCallPlayer.isPlaying()) {
//                alarmCallPlayer.stop();
//            }
//            if (allaAlarmSourceList.size() >= 1) {
//                //播放报警源视频信息
//                playAlarmVideo(allaAlarmSourceList.get(0));
//
//                //播放报警时对方面部视频
//                playAlarmCallVideo(allaAlarmSourceList.get(0));
//            }
        }
    }

    @Override
    public void onDestroyView() {
        //移除监听
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        //取消接收报警广播
        if (mReceiveAlarmBroadcast != null) {
            getActivity().unregisterReceiver(mReceiveAlarmBroadcast);
        }
        //取消接收监听video资源的广播
        if (mVideoSourcesBroadcast != null) {
            getActivity().unregisterReceiver(mVideoSourcesBroadcast);
        }
        super.onDestroyView();
    }

    /**
     * 展示报警队列的适配器
     */
    class AlarmListAdapter extends BaseAdapter {

        private int selectedItem = -1;

        @Override
        public int getCount() {
            return allaAlarmSourceList.size();
        }

        @Override
        public Object getItem(int position) {
            return allaAlarmSourceList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_alarm_listview_layout, null);
                viewHolder.alarmName = convertView.findViewById(R.id.alarm_list_item_name_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.alarmName.setText(allaAlarmSourceList.get(position).getFaceVideoName());

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
     * 更改可见的Ui状态
     */
    private void updateUiStatus() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        CustomViewPagerSlide mViewPage = (CustomViewPagerSlide) activity.findViewById(R.id.main_viewpager_layout);
        RadioGroup bottomRadioGroup = activity.findViewById(R.id.bottom_radio_group_layout);
        bottomRadioGroup.check(bottomRadioGroup.getChildAt(4).getId());
        mViewPage.setCurrentItem(3);

    }

    /**
     * 播放报警源的视频
     */
    private void playAlarmVideo(AlarmVideoSource mAlarmVideoSource) {
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
                            handler.sendEmptyMessage(2);
                            alarmPlayer.setInputUrl(rtsp);
                            alarmPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                                @Override
                                public void onEventCallback(NodePlayer player, int event, String msg) {
                                    if (getActivity() != null) {
                                        if (event == 1001) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    disPlayAlarmVideoLoadingIconLayout.setVisibility(View.GONE);
                                                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.INVISIBLE);
                                                    disPlayAlarmVideoLoadingIconLayout.clearAnimation();
                                                }
                                            });
                                        }
                                        //视频连接失败, 会进行自动重连.
                                        if (event == 1003 || event == 1002) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                    disPlayAlarmVideoLoadingTvLayout.setText("重新连接");
                                                }
                                            });
                                        }
                                        //视频播放中网络异常,
                                        if (event == 1005) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                    disPlayAlarmVideoLoadingTvLayout.setText("网络异常");
                                                }
                                            });
                                        }
                                        //网络连接超时
                                        if (event == 1006) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                    disPlayAlarmVideoLoadingTvLayout.setText("网络连接超时");
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            alarmPlayer.start();
                        } else {
                            handler.sendEmptyMessage(3);
                        }
                    } else {
                        handler.sendEmptyMessage(3);
                    }
                } else {
                    handler.sendEmptyMessage(3);
                }
            }
        }
    }

    /**
     * 播放报警通话时对方视频源的面部视频
     */
    private void playAlarmCallVideo(AlarmVideoSource mAlarmVideoSource) {
        if (alarmCallPlayer != null && alarmCallPlayer.isPlaying()) {
            alarmCallPlayer.stop();
        }

        try {
            List<SipBean> sipData = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
            for (int i = 0; i < sipData.size(); i++) {
                SipBean mSipInfoBean = sipData.get(i);
                String ip = mSipInfoBean.getIpAddress();
                //测试
                if (ip.equals(mAlarmVideoSource.getSenderIp())) {
                    VideoBean mVideoBean = mSipInfoBean.getVideoBean();
                    if (mVideoBean == null) {
                        handler.sendEmptyMessage(5);
                    } else {
                        VideoBean mVideoSourceInfoBean = mSipInfoBean.getVideoBean();
                        if (mVideoSourceInfoBean == null) {
                            handler.sendEmptyMessage(5);
                        } else {
                            String rtsp = mVideoSourceInfoBean.getRtsp();
                            if (TextUtils.isEmpty(rtsp)) {
                                handler.sendEmptyMessage(5);
                            } else {
                                handler.sendEmptyMessage(4);
                                alarmCallPlayer.setInputUrl(rtsp);
                                alarmCallPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
                                    @Override
                                    public void onEventCallback(NodePlayer player, int event, String msg) {
                                        if (getActivity() != null) {
                                            if (alarmCallPlayer == player) {
                                                if (event == 1001) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            alarmCallVideoLoadingIconLayout.setVisibility(View.GONE);
                                                            alarmCallVideoLoadingTvLayout.setVisibility(View.INVISIBLE);
                                                            alarmCallVideoLoadingIconLayout.clearAnimation();
                                                        }
                                                    });
                                                }
                                                //视频连接失败, 会进行自动重连.
                                                if (event == 1003 || event == 1002) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            alarmCallVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                            alarmCallVideoLoadingTvLayout.setText("重新连接");
                                                        }
                                                    });
                                                }
                                                //视频播放中网络异常,
                                                if (event == 1005) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            alarmCallVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                            alarmCallVideoLoadingTvLayout.setText("网络异常");
                                                        }
                                                    });
                                                }
                                                //网络连接超时
                                                if (event == 1006) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            alarmCallVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                                                            alarmCallVideoLoadingTvLayout.setText("网络连接超时");
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                });
                                alarmCallPlayer.start();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }


    /**
     * 接收报警数据
     */
    private void receiveAlarmData(AlarmVideoSource mAlarmVideoSource) {

        if (TextUtils.isEmpty(mAlarmVideoSource.getFaceVideoName()) && TextUtils.isEmpty(mAlarmVideoSource.getAlarmType())) {
            return;
        }
        allaAlarmSourceList.add(mAlarmVideoSource);
        //更改List可见状态
        if (mAlarmListAdapter != null)
            mAlarmListAdapter.notifyDataSetChanged();

        //更改可见Ui状态
        updateUiStatus();

        //播放报警源视频信息
        playAlarmVideo(mAlarmVideoSource);

        //播放报警时对方面部视频
        playAlarmCallVideo(mAlarmVideoSource);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser) {
            if (alarmPlayer != null && alarmPlayer.isPlaying()) {
                alarmPlayer.stop();
            }
            if (alarmCallPlayer != null && alarmCallPlayer.isPlaying()) {
                alarmCallPlayer.stop();
            }
        } else {
            if (alarmPlayer != null && !alarmPlayer.isPlaying()) {
                alarmPlayer.start();
            }
            if (alarmCallPlayer != null && !alarmCallPlayer.isPlaying()) {
                alarmCallPlayer.start();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //获取数据
                    AlarmVideoSource mAlarmVideoSource = (AlarmVideoSource) msg.obj;
                    receiveAlarmData(mAlarmVideoSource);
                    break;

                case 2:
                    //报警视频源提示正在加载
                    disPlayAlarmVideoLoadingIconLayout.setVisibility(View.VISIBLE);
                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                    disPlayAlarmVideoLoadingIconLayout.startAnimation(mLoadingAnim);
                    disPlayAlarmVideoLoadingTvLayout.setText("正在加载...");

                    break;
                case 3:
                    //提示加载失败
                    disPlayAlarmVideoLoadingIconLayout.setVisibility(View.INVISIBLE);
                    disPlayAlarmVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                    disPlayAlarmVideoLoadingTvLayout.setText("重新连接...");
                    break;
                case 4:
                    //报警通话视频源正在加载提示
                    alarmCallVideoLoadingIconLayout.setVisibility(View.VISIBLE);
                    alarmCallVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                    alarmCallVideoLoadingIconLayout.startAnimation(mLoadingAnim);
                    alarmCallVideoLoadingTvLayout.setText("正在加载...");
                    break;
                case 5:
                    alarmCallVideoLoadingIconLayout.setVisibility(View.INVISIBLE);
                    alarmCallVideoLoadingTvLayout.setVisibility(View.VISIBLE);
                    alarmCallVideoLoadingIconLayout.clearAnimation();
                    alarmCallVideoLoadingTvLayout.setText("未加载到对方视频源...");
                    break;
            }
        }
    };
}
