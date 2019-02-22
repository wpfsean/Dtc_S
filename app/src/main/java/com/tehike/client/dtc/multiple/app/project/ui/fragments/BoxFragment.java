package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.content.Context;
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

import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.SipGroupItemInfoBean;
import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.utils.HttpBasicRequest;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class BoxFragment extends BaseFragment {

    /**
     * 弹箱组
     */
    @BindView(R.id.box_group_listview_layout)
    public ListView boxListGroup;

    /**
     * 播放弹箱视频的某个视频父布局
     */
    @BindView(R.id.play_box_video_parent_layout)
    FrameLayout playVideoParentLayout;

    /**
     * 显示弹箱视频信息的名称
     */
    @BindView(R.id.display_box_item_video_info_layout)
    TextView disPlayBoxNameTv;

    /**
     * 弹箱视频加载的Loading动画
     */
    @BindView(R.id.box_video_loading_icon_layout)
    ImageView boxVideoLoadingIcon;

    /**
     * 弹箱视频加载的进度提示
     */
    @BindView(R.id.box_video_loading_tv_layout)
    TextView boxVideoLoadingTv;

    /**
     * 弹箱视频播放的View
     */
    @BindView(R.id.box_video_preview_view_layout)
    NodePlayerView boxVideoPlayView;

    /**
     * 某个弹箱组的所有数据
     */
    @BindView(R.id.box_item_gridview_layout)
    GridView boxItemGridView;

    @BindView(R.id.close_preview_btn_layout)
    Button closePreviewBoxVideo;

    /**
     * 弹箱组数据（模拟测试数据）
     */
    List<SipGroupInfoBean> boxGroupList = new ArrayList<>();

    /**
     * 某个弹箱组数据
     */
    List<SipGroupItemInfoBean> boxItemList = new ArrayList<>();

    /**
     * 弹箱Item组适配器
     */
    AllBoxItemAdapter mAllBoxItemAdapter;

    NodePlayer boxVideoPlayer;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 当前页面是否可见
     */
    boolean isCurrentFragmentVisivle = false;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_box_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        initializeBoxGroupData();

        initializePlayer();
    }

    private void initializePlayer() {

        mLoadingAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.loading);

        boxVideoPlayer = new NodePlayer(getActivity());
        boxVideoPlayer.setPlayerView(boxVideoPlayView);
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

    BoxGroupListAdapter mBoxGroupListAdapter;

    /**
     * 展示弹箱
     */
    private void disPlayAlarmList() {

         mBoxGroupListAdapter =  new BoxGroupListAdapter();
        boxListGroup.setAdapter(mBoxGroupListAdapter);
        mBoxGroupListAdapter.setSelectedItem(0);
        mBoxGroupListAdapter.notifyDataSetChanged();
        //默认加载第一组的数据
        Message handlerMess = new Message();
        handlerMess.arg1 = boxGroupList.get(0).getId();
        handlerMess.what = 5;
        handler.sendMessage(handlerMess);

        //点击事件
        boxListGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBoxGroupListAdapter.setSelectedItem(position);
                mBoxGroupListAdapter.notifyDataSetChanged();
                SipGroupInfoBean mSipGroupInfoBean = boxGroupList.get(position);
                Logutil.i("SipGroupInfoBean-->>" + mSipGroupInfoBean.toString());
                int groupId = mSipGroupInfoBean.getId();
                disPlayAllBoxItem(groupId);
            }
        });
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
                Logutil.d("result--->>" + result);
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
                                            jsonItemVideo.getString("username"),"","","","","","");
                                    groupItemInfoBean.setBean(videoBean);
                                }
                            }
                            boxItemList.add(groupItemInfoBean);
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

    int boxItemSelected = -1;

    /**
     * 展示所有的弹箱组
     */
    private void disPlayAllBoxItem() {
        if (mAllBoxItemAdapter == null)
            mAllBoxItemAdapter = new AllBoxItemAdapter(getActivity());
        boxItemGridView.setAdapter(mAllBoxItemAdapter);
        mAllBoxItemAdapter.notifyDataSetChanged();

        //item点击事件
        boxItemGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAllBoxItemAdapter != null) {
                    if (boxItemList != null && boxItemList.size() > 0) {
                        //判断选中的是否是在线状态对象
                        if (boxItemList.get(position).getState() == 1) {
                            boxItemSelected = position;
                        } else {
                            boxItemSelected = -1;
                        }
                        if (boxItemSelected != -1) {
                            Logutil.d("sipItemSelected--->>>" + boxItemSelected);
                            Logutil.d("sipItemSelected---->>>>" + boxItemList.get(boxItemSelected).toString());
                        }
                        mAllBoxItemAdapter.setSeclection(position);
                        mAllBoxItemAdapter.notifyDataSetChanged();
                    }
                }
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
            TextView boxGroupItemNameLayout;

            RelativeLayout boxGroupParentLayout;


        }
    }

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
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.activity_box_status_item, null);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.box_item_name);
                viewHolder.mRelativeLayout = (FrameLayout) convertView.findViewById(R.id.box_item_layout);
                viewHolder.mainLayout = convertView.findViewById(R.id.box_status_main_layout);
                viewHolder.deviceType = convertView.findViewById(R.id.box_device_type_layout);
                viewHolder.StatusIcon = convertView.findViewById(R.id.box_status_icon_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SipGroupItemInfoBean mSipClient = boxItemList.get(position);
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
            }

            if (clickTemp == position) {
                //默认只有在线状态对能被选中
//                if (boxItemList.get(position).getState() == 1) {
                //   viewHolder.mainLayout.setBackgroundResource(R.drawable.sip_selected_bg);
                viewHolder.mRelativeLayout.setBackgroundResource(R.mipmap.intercom_call_img_bg_free_selected);
                //   }
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

    @OnClick(R.id.offline_preview_btn_layout)
    public void boxVideoPreview(View view) {
        boxItemGridView.setVisibility(View.GONE);
        playVideoParentLayout.setVisibility(View.VISIBLE);

        disPlayBoxNameTv.setVisibility(View.VISIBLE);
        disPlayBoxNameTv.setText("一号弹箱视频源");

        boxVideoLoadingIcon.setVisibility(View.VISIBLE);
        boxVideoLoadingIcon.startAnimation(mLoadingAnim);

        boxVideoLoadingTv.setVisibility(View.VISIBLE);
        closePreviewBoxVideo.setVisibility(View.VISIBLE);
        boxVideoPlayer.setInputUrl("rtsp://admin:pass@19.0.0.211:554/H264?ch=6&subtype=1");
        boxVideoPlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
            @Override
            public void onEventCallback(NodePlayer player, int event, String msg) {

                if (event == 1001) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingIcon.setVisibility(View.GONE);
                            boxVideoLoadingTv.setVisibility(View.INVISIBLE);
                            boxVideoLoadingIcon.clearAnimation();
                        }
                    });
                }
                //视频连接失败, 会进行自动重连.
                if (event == 1003 || event == 1002) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTv.setVisibility(View.VISIBLE);
                            boxVideoLoadingTv.setText("重新连接");
                        }
                    });
                }
                //视频播放中网络异常,
                if (event == 1005) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTv.setVisibility(View.VISIBLE);
                            boxVideoLoadingTv.setText("网络异常");
                        }
                    });
                }
                //网络连接超时
                if (event == 1006) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boxVideoLoadingTv.setVisibility(View.VISIBLE);
                            boxVideoLoadingTv.setText("网络连接超时");
                        }
                    });
                }

            }
        });
        boxVideoPlayer.start();
    }


    @OnClick(R.id.close_preview_btn_layout)
    public void boxVideoStopPreView(View view){
        if (boxVideoPlayer != null && boxVideoPlayer.isPlaying()){
            boxVideoPlayer.stop();
        }
        boxItemGridView.setVisibility(View.VISIBLE);
        playVideoParentLayout.setVisibility(View.GONE);

        disPlayBoxNameTv.setVisibility(View.GONE);

        boxVideoLoadingIcon.clearAnimation();
        boxVideoLoadingIcon.setVisibility(View.GONE);
        boxVideoLoadingTv.setVisibility(View.GONE);
        closePreviewBoxVideo.setVisibility(View.GONE);

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


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Logutil.e("box无数据");
                    break;
                case 2:
                    Logutil.e("box网络异常");
                    break;
                case 3:
                    String boxData = (String) msg.obj;
                    handleBoxGroupData(boxData);
                    break;
                case 4:
                    disPlayAlarmList();
                    break;
                case 5:
                    int boxGroupId = msg.arg1;
                    disPlayAllBoxItem(boxGroupId);
                    break;
                case 6:
                    disPlayAllBoxItem();
                    break;
            }
        }
    };


}
