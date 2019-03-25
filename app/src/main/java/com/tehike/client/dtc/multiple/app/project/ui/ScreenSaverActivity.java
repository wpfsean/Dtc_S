package com.tehike.client.dtc.multiple.app.project.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;

/**
 * 描述：屏保界面（显示日期和时间）
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/3 14:46
 */

public class ScreenSaverActivity extends BaseActivity {

    /**
     * 显示时间
     */
    @BindView(R.id.display_screen_time_layout)
    TextView displayScreenTimeLayout;

    /**
     * 显示日期
     */
    @BindView(R.id.display_screen_date_layout)
    TextView displayScreenDateLayout;

    /**
     * 时间格式
     */
    SimpleDateFormat hoursFormat;

    /**
     * 日期格式
     */
    SimpleDateFormat dateFormat;

    /**
     * 显示时间的线程是否正在运行
     */
    boolean threadIsRun = true;


    @Override
    protected int intiLayout() {
        return R.layout.activity_dtc_screen;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        initializeCurrentTime();
    }

    /**
     * 初始化时间显示
     */
    private void initializeCurrentTime() {
        //时分秒格式
        hoursFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        //日期格式
        dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        //开启定时刷新线程
        TimingRefreshThread timeThread = new TimingRefreshThread();
        new Thread(timeThread).start();
    }

    /**
     * 获取当天星期几
     */
    public static String getWeek() {
        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);
        switch (i) {
            case 1:
                return "星期日";
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
            default:
                return "";
        }
    }

    /**
     * 每隔1秒刷新一下时间的线程
     */
    class TimingRefreshThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            } while (threadIsRun);
        }
    }

    /**
     * 显示当前的时期和时间
     */
    private void disPlayDateAndTime() {
        //显示当前的时分秒
        Date currentDate = new Date();
        if (hoursFormat != null) {
            String hoursStr = hoursFormat.format(currentDate);
            String date = dateFormat.format(currentDate);
            //可见时显示时间日期
            if (isVisible) {
                displayScreenTimeLayout.setText(hoursStr);
                displayScreenDateLayout.setText(date + "\t\t\t" + getWeek());
            }
        }
    }

    /**
     * 事件分发机制
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //发送取消屏保的通知
                ScreenSaverActivity.this.sendBroadcast(new Intent(AppConfig.CANCEL_SCREEN_SAVER_ACTION));
                //finish掉栈顶的acitivity
                ActivityUtils.getTopActivity().finish();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        //重置计时标识
        threadIsRun = false;
        //发送取消屏保的通知
        ScreenSaverActivity.this.sendBroadcast(new Intent(AppConfig.CANCEL_SCREEN_SAVER_ACTION));
        //移除handler监听
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    /**
     * handler监听子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    disPlayDateAndTime();
                    break;
            }
        }
    };

}
