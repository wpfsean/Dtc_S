package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.AlarmVideoSource;
import com.tehike.client.dtc.multiple.app.project.entity.EventSources;
import com.tehike.client.dtc.multiple.app.project.entity.PhoneCallRecordBean;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.ui.display.SecondDisplayActivity;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 描述：历史记录页面
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/20 9:34
 */

public class HistoryRecordFragment extends BaseFragment {

    /**
     * 查看报警日志按键
     */
    @BindView(R.id.history_alarm_btn_layout)
    LinearLayout historyAlarmBtnLayout;

    /**
     * 查看事件日志按键
     */
    @BindView(R.id.history_event_btn_layout)
    LinearLayout historyEventBtnLayout;

    /**
     * 查看电话日志按键
     */
    @BindView(R.id.history_phonecall_btn_layout)
    LinearLayout phoneCallBtnLayout;

    /**
     * 事件日志展示listview
     */
    @BindView(R.id.history_event_listview_layout)
    ListView historyEventListViewLayout;

    /**
     * 报警日志展示listview
     */
    @BindView(R.id.history_alarm_listview_layout)
    ListView historyAlarmListViewLayout;

    /**
     * 电话记录日志
     */
    @BindView(R.id.history_phonecall_listview_layout)
    ListView phoneCallListViewLayout;

    /**
     * 展示事件日志的适配器
     */
    HistoryEventAdapter mHistoryEventAdapter;

    /**
     * 展示报警日志的适配器
     */
    HistortAlarmAdapter mHistortAlarmAdapter;

    /**
     * 展示电话日志的适配器
     */
    PhoneCallRecordAdapter mPhoneCallRecordAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_history_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //初始化报警日志
        initHistortyAlarmtData();
    }

    /**
     * 左侧按键点击事件
     */
    @OnClick({R.id.history_alarm_btn_layout, R.id.history_event_btn_layout, R.id.history_phonecall_btn_layout})
    public void btnClickEvent(View view) {
        switch (view.getId()) {
            case R.id.history_alarm_btn_layout:
                //查看报警日志记录
                switchAlarm();
                break;
            case R.id.history_event_btn_layout:
                //查看事件日志记录
                switchEvent();
                break;
            case R.id.history_phonecall_btn_layout:
                switchPhoneCallRecord();
                break;
        }
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
     * 切换查看事件日志记录
     */
    private void switchEvent() {
        historyAlarmBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        historyEventBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        phoneCallBtnLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
        historyAlarmListViewLayout.setVisibility(View.GONE);
        historyEventListViewLayout.setVisibility(View.VISIBLE);
        phoneCallListViewLayout.setVisibility(View.GONE);
        initHistortyEventData();
    }

    /**
     * 切换查看报警日志记录
     */
    private void switchAlarm() {
        phoneCallBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        historyAlarmBtnLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
        historyEventBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        historyAlarmListViewLayout.setVisibility(View.VISIBLE);
        historyEventListViewLayout.setVisibility(View.GONE);
        phoneCallListViewLayout.setVisibility(View.GONE);
        initHistortyAlarmtData();
    }

    /**
     * 切换查看电话日志
     */
    private void switchPhoneCallRecord() {
        historyAlarmBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        historyEventBtnLayout.setBackgroundResource(R.drawable.system_set_btn_bg);
        phoneCallBtnLayout.setBackgroundResource(R.mipmap.dtc_btn1_bg_selected);
        historyAlarmListViewLayout.setVisibility(View.GONE);
        historyEventListViewLayout.setVisibility(View.GONE);
        phoneCallListViewLayout.setVisibility(View.VISIBLE);
        initPhoneCallRecordData();
    }

    /**
     * 加载电话历史日志数据
     */
    private void initPhoneCallRecordData() {
        //清除集合
        LinkedList<PhoneCallRecordBean> mlist = new LinkedList<>();
        mlist.clear();
        //获取遍历的custor
        Cursor c = new DbUtils(App.getApplication()).query(DbHelper.PHONE_CALL_TAB_NAME, null, null, null, null, null, null, null);
        if (c == null) {
            Logutil.e("c is null");
            return;
        }
        //遍历
        if (c.moveToFirst()) {
            do {
                PhoneCallRecordBean v = new PhoneCallRecordBean();
                String time = c.getString(c.getColumnIndex("time"));
                String status = c.getString(c.getColumnIndex("phoneStatus"));
                String from = c.getString(c.getColumnIndex("phoneFrom"));
                String to = c.getString(c.getColumnIndex("phoneTo"));
                v.setStatus(status);
                v.setTime(time);
                v.setFrom(from);
                v.setTo(to);
                mlist.add(v);
            } while (c.moveToNext());
        }
        //返回
        mlist = reverseLinkedList(mlist);
        //适配数据
        if (mPhoneCallRecordAdapter == null) {
            mPhoneCallRecordAdapter = new PhoneCallRecordAdapter(mlist);
            phoneCallListViewLayout.setAdapter(mPhoneCallRecordAdapter);
        }
        //刷新
        mPhoneCallRecordAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化报警日志的适配器
     */
    private void initHistortyAlarmtData() {
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
                alarmVideoSource.setSenderIp(senderIp);
                alarmVideoSource.setFaceVideoId(faceVideoId);
                alarmVideoSource.setAlarmType(alarmType);
                alarmVideoSource.setFaceVideoName(faceVideoName);
                alarmVideoSource.setTime(time);
                mlist.add(alarmVideoSource);
            } while (c.moveToNext());
        }

        mlist = reverseLinkedList(mlist);

        if (mHistortAlarmAdapter == null) {
            mHistortAlarmAdapter = new HistortAlarmAdapter(mlist);
            historyAlarmListViewLayout.setAdapter(mHistortAlarmAdapter);
        }
        mHistortAlarmAdapter.notifyDataSetChanged();

    }

    /**
     * 初始化事件日志数据
     */
    private void initHistortyEventData() {
        LinkedList<EventSources> eventQueueList = new LinkedList<>();
        eventQueueList.clear();
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
        eventQueueList = reverseLinkedList(eventQueueList);
        //适配器展示
        if (mHistoryEventAdapter == null) {
            mHistoryEventAdapter = new HistoryEventAdapter(eventQueueList);
            historyEventListViewLayout.setAdapter(mHistoryEventAdapter);
        }
        mHistoryEventAdapter.notifyDataSetChanged();

    }

    /**
     * 展示事件信息的适配器
     */
    class HistoryEventAdapter extends BaseAdapter {

        LinkedList<EventSources> eventQueueList;

        public HistoryEventAdapter(LinkedList<EventSources> eventQueueList) {
            this.eventQueueList = eventQueueList;
        }

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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_processed_event_item_layout, null);
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
     * 已处理的的报警队列的适配器
     */
    class HistortAlarmAdapter extends BaseAdapter {

        LinkedList<AlarmVideoSource> mlist;


        public HistortAlarmAdapter(LinkedList<AlarmVideoSource> mlist) {
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_alarm_processed_event_item_layout, null);
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
     * 电话记录的适配器
     */
    class PhoneCallRecordAdapter extends BaseAdapter {

        LinkedList<PhoneCallRecordBean> mlist;


        public PhoneCallRecordAdapter(LinkedList<PhoneCallRecordBean> mlist) {
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_alarm_processed_event_item_layout, null);
                viewHolder.alarmEventName = convertView.findViewById(R.id.alarm_processed_event_name_layout);
                viewHolder.alarmType = convertView.findViewById(R.id.alarm_processed_event_type_layout);
                viewHolder.alarmTime = convertView.findViewById(R.id.alarm_processed_event_time_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.alarmEventName.setText("状态:"+mlist.get(position).getStatus());
            viewHolder.alarmType.setText(mlist.get(position).getFrom()+"\t"+mlist.get(position).getTo());
            viewHolder.alarmTime.setText("时间:"+mlist.get(position).getTime());
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
}
