package com.tehike.client.dtc.multiple.app.project.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.SysInfoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.phone.SipManager;
import com.tehike.client.dtc.multiple.app.project.phone.SipService;
import com.tehike.client.dtc.multiple.app.project.services.ReceiveOpenBoxRequestService;
import com.tehike.client.dtc.multiple.app.project.services.ReceiverAlarmService;
import com.tehike.client.dtc.multiple.app.project.services.RemoteVoiceOperatService;
import com.tehike.client.dtc.multiple.app.project.services.RequestWebApiDataService;
import com.tehike.client.dtc.multiple.app.project.services.TerminalUpdateIpService;
import com.tehike.client.dtc.multiple.app.project.services.TimingAutoUpdateService;
import com.tehike.client.dtc.multiple.app.project.services.TimingCheckSipStatus;
import com.tehike.client.dtc.multiple.app.project.services.TimingRefreshNetworkStatus;
import com.tehike.client.dtc.multiple.app.project.services.TimingRequestAlarmTypeService;
import com.tehike.client.dtc.multiple.app.project.services.TimingSendNativeInfoService;
import com.tehike.client.dtc.multiple.app.project.services.UpdateSystemSettingService;
import com.tehike.client.dtc.multiple.app.project.services.UpdateSystemTimeService;
import com.tehike.client.dtc.multiple.app.project.ui.display.SecondDisplayActivity;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.BoxFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.HistoryRecordFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.IntercomCallFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.NetworkBroadcastFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.ScreenControlFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.ServiceMangementFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.VideoMonitorFragment;
import com.tehike.client.dtc.multiple.app.project.ui.fragments.SystemSetFragment;
import com.tehike.client.dtc.multiple.app.project.ui.views.CustomViewPagerSlide;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ServiceUtil;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.voice.TimingCheckVoiceIsLiveService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * 描述：勤务综合管控终端
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/4 11:03
 */

public class DtcDutyMainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, IntercomCallFragment.CallBackValue {

    /**
     * 主界面滑动的ViewPager布局
     */
    @BindView(R.id.main_viewpager_layout)
    public CustomViewPagerSlide CustomViewPagerLayout;

    /**
     * 显示当前的哨位名
     */
    @BindView(R.id.current_user_name_layout)
    public TextView currentNameLayout;

    /**
     * 显示当前连接状态
     */
    @BindView(R.id.current_connected_status_layout)
    public TextView currentConntectLayout;

    /**
     * 显示当前时间
     */
    @BindView(R.id.current_time_layout)
    public TextView currentTimeLayout;

    /**
     * 显示当前日期
     */
    @BindView(R.id.current_date_layout)
    public TextView currentDateLayout;

    /**
     * 底部的radioGroup
     */
    @BindView(R.id.bottom_radio_group_layout)
    public RadioGroup bottomRadioGroupLayout;

    /**
     * 显示cpu状态信息(测试用)
     */
    @BindView(R.id.display_cpu_tv_layout)
    public TextView displayCpuLayout;

    /**
     * 页面Fragment集合
     */
    private List<Fragment> allFragmentList = new ArrayList<>();

    /**
     * 时分秒显示的格式
     */
    SimpleDateFormat hoursFormat = null;

    /**
     * 显示时间的线程是否正在运行
     */
    boolean threadIsRun = true;

    /**
     * 来电广播
     */
    InComingCallBroadcast incomingBroadcast;

    /**
     * 刷新网络状态广播
     */
    NetworkStatusBroadcast mFreshNetworkStatusBroadcast;

    /**
     * Cpu和rom监听广播
     */
    CpuAndRomBroadcast cpuAndRomBroadcast;

    @Override
    protected int intiLayout() {
        return R.layout.activity_dtcduty_layout;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //Log CurrentIp
        Logutil.e("Ip--->>" + NetworkUtils.getIPAddress(true));

        //注册来电监听
        registerComingBroadcast();

        //注册网络状态变化广播
        registerNetworkChangedBroadcast();

        //注册广播接收cpu变化
        registerCpuAndRomBroadcast();

        //启动服务
        startAllService();

        //去注册SIp
        registerSipToServer(SysinfoUtils.getSysinfo());

        //初始化页面
        initializeViewPagerFragment();

        //初始化显示时间
        initializeTime();

        //初始化数据
        initializeData();

        //开启屏保计时功能
        new Thread(new TimingScreenSaverThread()).start();

        //初始化副屏功能
        initializeSecondaryScreen();

    }

    SecondDisplayActivity differentDislay;
    DisplayManager manager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initializeSecondaryScreen() {
        if (AppConfig.ARGEE_OVERLAY_PERMISSION) {
            if (manager == null)
                manager = (DisplayManager) App.getApplication().getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = manager.getDisplays();
            if (differentDislay == null)
                differentDislay = new SecondDisplayActivity(this, displays[1]);
            differentDislay.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            differentDislay.show();

        }
    }

    //屏保计时数据
    int count = 0;

    //是否正在通话的标识
    boolean flag = true;

    /**
     * 实现接口（接收Fragment传递的数据）
     */
    @Override
    public void SendMessageValue(String strValue) {
        if (strValue.equals("true")) {
            //如果正在通话（不计时）
            count = 0;
            flag = false;
        } else if (strValue.equals("false")) {
            //如果停止通话（开始计时）
            count = 0;
            new Thread(new TimingScreenSaverThread()).start();
            flag = true;
        }
    }

    /**
     * 屏保计时
     */
    class TimingScreenSaverThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(9);
            } while (flag);
        }
    }

    /**
     * 注册监听cpu和rom的广播
     */
    private void registerCpuAndRomBroadcast() {
        cpuAndRomBroadcast = new CpuAndRomBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.CPU_AND_ROM_ACTION);
        this.registerReceiver(cpuAndRomBroadcast, intentFilter);
    }

    /**
     * 显示cpu和rom使用率的广播
     */
    class CpuAndRomBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double cpu = intent.getDoubleExtra("cpu", 0.0F);

            String cpuTemp = intent.getStringExtra("cpuTemp");
            String gpuTemp = intent.getStringExtra("gpuTemp");
            String content = cpu + "," + cpuTemp + "," + gpuTemp;

            Message message = new Message();
            message.what = 7;
            message.obj = content;
            handler.sendMessage(message);
        }
    }

    /**
     * 注册刷新网络状态的广播
     */
    private void registerNetworkChangedBroadcast() {
        mFreshNetworkStatusBroadcast = new NetworkStatusBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.REFRESH_NETWORK_ACTION);
        this.registerReceiver(mFreshNetworkStatusBroadcast, intentFilter);

    }

    /**
     * 广播接收网络状态变化（判断网线是否拨出）
     */
    public class NetworkStatusBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isNormal = intent.getBooleanExtra("isNormal", false);
            if (isNormal) {
                handler.sendEmptyMessage(5);
            } else {
                handler.sendEmptyMessage(6);
            }
        }
    }

    /**
     * 启动必要的服务
     */
    private void startAllService() {

        //启动获取SipResource的服务
        if (!ServiceUtil.isServiceRunning(RequestWebApiDataService.class)) {
            ServiceUtil.startService(RequestWebApiDataService.class);
        }
        //报警报警颜色及对类型对应表
        if (!ServiceUtil.isServiceRunning(TimingRequestAlarmTypeService.class)) {
            ServiceUtil.startService(TimingRequestAlarmTypeService.class);
        }
        //启动接收报警
        if (!ServiceUtil.isServiceRunning(ReceiverAlarmService.class)) {
            ServiceUtil.startService(ReceiverAlarmService.class);
        }
        //定时更新apk的服务
        if (!ServiceUtil.isServiceRunning(TimingAutoUpdateService.class)) {
            ServiceUtil.startService(TimingAutoUpdateService.class);
        }
        //定时的上传本机信息
        if (!ServiceUtil.isServiceRunning(TimingSendNativeInfoService.class)) {
            ServiceUtil.startService(TimingSendNativeInfoService.class);
        }
        //更新系统时间的服务
        if (!ServiceUtil.isServiceRunning(UpdateSystemTimeService.class)) {
            ServiceUtil.startService(UpdateSystemTimeService.class);
        }
        if (!ServiceUtil.isServiceRunning(ReceiveOpenBoxRequestService.class)) {
            ServiceUtil.startService(ReceiveOpenBoxRequestService.class);
        }
    }

    /**
     * 注册来电监听
     */
    private void registerComingBroadcast() {
        incomingBroadcast = new InComingCallBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.INCOMING_CALL_ACTION);
        registerReceiver(incomingBroadcast, intentFilter);
    }

    /**
     * 监听来电广播（切换到来电页面）
     */
    class InComingCallBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.sendEmptyMessage(8);
        }

    }

    /**
     * 初始化数据
     */
    private void initializeData() {
        try {
            //取出本地保存的sysinfo数据
            String infor = CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SYSINFO).toString());
            //判断是否为空
            if (TextUtils.isEmpty(infor)) {
                Logutil.e("无数据！");
                return;
            }
            //转对对象
            SysInfoBean cc = GsonUtils.GsonToBean(infor, SysInfoBean.class);
            if (cc != null) {
                //显示哨位名
                currentNameLayout.setText("哨位名称:" + cc.getDeviceName());
            }
        } catch (Exception e) {
            Logutil.e("获取数据异常--->>>" + e.getMessage());
        }
    }

    /**
     * 初始化显示时间
     */
    private void initializeTime() {
        //显示当前的年月日
        Date date = new Date();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy年MM月dd日");
        String currntYearDate = yearFormat.format(date);
        currentDateLayout.setText(currntYearDate);

        hoursFormat = new SimpleDateFormat("HH:mm:ss");
        TimingThread timeThread = new TimingThread();
        new Thread(timeThread).start();
    }

    /**
     * 每隔1秒刷新一下时间的线程
     */
    class TimingThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(4);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的时间
     */
    private void disPlayCurrentTime() {
        Date currentDate = new Date();
        if (hoursFormat != null) {
            String hoursStr = hoursFormat.format(currentDate);
            if (isVisible) {
                currentTimeLayout.setText(hoursStr);
            }
        }
    }

    /**
     * 把Sip注册到sip服务器
     */
    private void registerSipToServer(SysInfoBean sysInfoBean) {
        if (sysInfoBean == null) {
            handler.sendEmptyMessage(1);
            return;
        }
        //启动sip服务
        if (!SipService.isReady() || !SipManager.isInstanceiated()) {
            Linphone.startService(getApplicationContext());
        }
        //判断获取的sip数据是为空
        if (TextUtils.isEmpty(sysInfoBean.getSipUsername()) || TextUtils.isEmpty(sysInfoBean.getSipPassword()) || TextUtils.isEmpty(sysInfoBean.getSipServer())) {
            Logutil.e("SIp信息为空");
            handler.sendEmptyMessage(1);
            return;
        }
        //当前的sip是否在线
        if (AppConfig.SIP_STATUS) {
            Logutil.i("已经注册了");
            return;
        }
        //去注册
        Linphone.setAccount(sysInfoBean.getSipUsername(), sysInfoBean.getSipPassword(), sysInfoBean.getSipServer());
        Linphone.login();
    }

    /**
     * 底部RadioGroup监听
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.bottom_intercom_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(0);
                break;
            case R.id.bottom_networkbroadcast_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(1);
                break;
            case R.id.bottom_video_monitor_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(2);
                break;
            case R.id.bottom_screen_control_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(3);
                break;
            case R.id.bottom_box_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(4);
                break;
            case R.id.bottom_service_mangement_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(5);
                break;
            case R.id.bottom_history_record_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(6);
                break;
            case R.id.bottom_systemt_setting_radio_btn_layout:
                CustomViewPagerLayout.setCurrentItem(7);
                break;
        }
    }

    /**
     * 初始化ViewPager页面
     */
    private void initializeViewPagerFragment() {
        //底部radiogroup监听
        bottomRadioGroupLayout.setOnCheckedChangeListener(this);

        //添加要滑动的Fragment
        allFragmentList.add(new IntercomCallFragment());
        allFragmentList.add(new NetworkBroadcastFragment());
        allFragmentList.add(new VideoMonitorFragment());
        allFragmentList.add(new ScreenControlFragment());
        allFragmentList.add(new BoxFragment());
        allFragmentList.add(new ServiceMangementFragment());
        allFragmentList.add(new HistoryRecordFragment());
        allFragmentList.add(new SystemSetFragment());
        //适配显示
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        CustomViewPagerLayout.setAdapter(adapter);
        //预加载全部
        CustomViewPagerLayout.setOffscreenPageLimit(allFragmentList.size());
        CustomViewPagerLayout.setCurrentItem(0);
        CustomViewPagerLayout.setScanScroll(AppConfig.IS_CAN_SLIDE);
        CustomViewPagerLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Logutil.i("当前页面---->>" + position);

                if (AppConfig.IS_CAN_SLIDE) {
                    bottomRadioGroupLayout.getChildAt(position).setEnabled(true);
                    updateRadioGroupStatus(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 更改底部的radioGroup选中状态
     */
    private void updateRadioGroupStatus(int select) {
        for (int i = 0; i < bottomRadioGroupLayout.getChildCount(); i++) {
            if (select == i) {
                bottomRadioGroupLayout.check(bottomRadioGroupLayout.getChildAt(select).getId());
                continue;
            } else {
            }
        }
    }

    /**
     * ViewPager适配器
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int arg0) {
            return allFragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return allFragmentList == null ? 0 : allFragmentList.size();
        }
    }


    @Override
    protected void onRestart() {
        Logutil.d("哈哈。我又可见了");
        count = 0;
        flag = true;
        new Thread(new TimingScreenSaverThread()).start();
        super.onRestart();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        count = 0;
        Logutil.d("点击了" + count);
        //  Logutil.d("点击了");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Logutil.d("退出");

        count = 0;

        flag = false;

        exitApp();

        Linphone.getLC().clearProxyConfigs();

    }

    /**
     * 退出登录(测试)
     */
    private void exitApp() {

        if (differentDislay != null)
            differentDislay.dismiss();

        if (ServiceUtil.isServiceRunning(RemoteVoiceOperatService.class)) {
            ServiceUtil.stopService(RemoteVoiceOperatService.class);
        }
        if (ServiceUtil.isServiceRunning(ReceiverAlarmService.class)) {
            ServiceUtil.stopService(ReceiverAlarmService.class);
        }
        if (ServiceUtil.isServiceRunning(TerminalUpdateIpService.class)) {
            ServiceUtil.stopService(TerminalUpdateIpService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingAutoUpdateService.class)) {
            ServiceUtil.stopService(TimingAutoUpdateService.class);
        }
        if (ServiceUtil.isServiceRunning(RemoteVoiceOperatService.class)) {
            ServiceUtil.stopService(RemoteVoiceOperatService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingRefreshNetworkStatus.class)) {
            ServiceUtil.stopService(TimingRefreshNetworkStatus.class);
        }
        if (ServiceUtil.isServiceRunning(TimingRequestAlarmTypeService.class)) {
            ServiceUtil.stopService(TimingRequestAlarmTypeService.class);
        }
        if (ServiceUtil.isServiceRunning(RequestWebApiDataService.class)) {
            ServiceUtil.stopService(RequestWebApiDataService.class);
        }
        if (ServiceUtil.isServiceRunning(UpdateSystemSettingService.class)) {
            ServiceUtil.stopService(UpdateSystemSettingService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingSendNativeInfoService.class)) {
            ServiceUtil.stopService(TimingSendNativeInfoService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingCheckVoiceIsLiveService.class)) {
            ServiceUtil.stopService(TimingCheckVoiceIsLiveService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingCheckSipStatus.class)) {
            ServiceUtil.stopService(TimingCheckSipStatus.class);
        }

        ActivityUtils.removeAllActivity();


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onDestroy() {

        threadIsRun = false;

        //取消广播
        if (incomingBroadcast != null) {
            unregisterReceiver(incomingBroadcast);
        }

        if (mFreshNetworkStatusBroadcast != null) {
            unregisterReceiver(mFreshNetworkStatusBroadcast);
        }
        if (cpuAndRomBroadcast != null) {
            unregisterReceiver(cpuAndRomBroadcast);
        }

        if (handler != null)
            handler.removeCallbacksAndMessages(null);


//        if (differentDislay.mReceiveAlarmBroadcast != null) {
//            this.unregisterReceiver(differentDislay.mReceiveAlarmBroadcast);
//        }
//
//        if (differentDislay.mVideoSourcesBroadcast != null) {
//            this.unregisterReceiver(differentDislay.mVideoSourcesBroadcast);
//        }


        differentDislay.dismiss();

        super.onDestroy();
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
                    if (isVisible)
                        showProgressFail(getString(R.string.str_resource_no_data));
                    break;
                case 2:
                    //提示无网络
                    if (isVisible)
                        showProgressFail(getString(R.string.str_resource_no_network));
                    break;
                case 4:
                    //每隔一秒刷新一下时间
                    disPlayCurrentTime();
                    break;
                case 5:
                    currentConntectLayout.setTextColor(0xff6adeff);
                    currentConntectLayout.setText("网络状态:连接正常");
                    break;
                case 6:
                    currentConntectLayout.setTextColor(0xffff0000);
                    currentConntectLayout.setText("网络状态:已断开");
                    break;
                case 7:
                    //获取cpu相关的信息
                    String content = (String) msg.obj;
                    String[] cpuResult = content.split(",");
                    if (isVisible)
                        displayCpuLayout.setText("cpu:" + cpuResult[0] + "%" + "\n" + cpuResult[1] + "\n" + cpuResult[2]);
                    break;
                case 8:
                    //更改底部radio选中状态
                    bottomRadioGroupLayout.check(bottomRadioGroupLayout.getChildAt(0).getId());
                    CustomViewPagerLayout.setCurrentItem(0);
                    break;
                case 9:
                    count++;
                  //  Logutil.d("count-->>" + count);
                    if (count == AppConfig.SCREEN_SAVE_TIME) {
                        openActivity(ScreenSaverActivity.class);
                        DtcDutyMainActivity.this.sendBroadcast(new Intent(AppConfig.SCREEN_SAVER_ACTION));
                        flag = false;
                        count = 0;
                    }
                    break;
            }
        }
    };
}
