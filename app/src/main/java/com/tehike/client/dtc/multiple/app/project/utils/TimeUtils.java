package com.tehike.client.dtc.multiple.app.project.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Root on 2018/9/4.
 */

public class TimeUtils {


    /**
     * int转成时间 00:00
     */
    public static String getTime(int num) {
        if (num < 10) {
            return "00:0" + num;
        }
        if (num < 60) {
            return "00:" + num;
        }
        if (num < 3600) {
            int minute = num / 60;
            num = num - minute * 60;
            if (minute < 10) {
                if (num < 10) {
                    return "0" + minute + ":0" + num;
                }
                return "0" + minute + ":" + num;
            }
            if (num < 10) {
                return minute + ":0" + num;
            }
            return minute + ":" + num;
        }
        int hour = num / 3600;
        int minute = (num - hour * 3600) / 60;
        num = num - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (num < 10) {
                    return "0" + hour + ":0" + minute + ":0" + num;
                }
                return "0" + hour + ":0" + minute + ":" + num;
            }
            if (num < 10) {
                return "0" + hour + ":" + minute + ":0" + num;
            }
            return "0" + hour + ":" + minute + ":" + num;
        }
        if (minute < 10) {
            if (num < 10) {
                return hour + ":0" + minute + ":0" + num;
            }
            return hour + ":0" + minute + ":" + num;
        }
        if (num < 10) {
            return hour + ":" + minute + ":0" + num;
        }
        return hour + ":" + minute + ":" + num;
    }


    /**
     * 获取秒的时间戳（用于向服务发送时间）
     *
     * @return
     */
    public static long dateStamp() {
        long time = System.currentTimeMillis() / 1000;
        return time;
    }


    public static String timeStamp2Date(String seconds) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        String format = "HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    public static String longTime2Short(String longTime) {
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = sf.parse(longTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = sdf.format(date);
        return result;
    }


    public static String long2Time(String createTime) {
        long msgCreateTime = Long.parseLong(createTime) * 1000L;
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        String time = format.format(new Date(msgCreateTime));
        return time;
    }


    /**
     * 格式到毫秒
     */
    public static String getSMillon(long time) {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(time);
    }


    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return    year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
    }
}
