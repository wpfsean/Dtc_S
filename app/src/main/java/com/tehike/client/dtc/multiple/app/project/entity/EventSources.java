package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：事件信息封装实体类(测试用)
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2019/2/24 11:12
 * @version V1.0
 */

public class EventSources implements Serializable {

    //时间
    private String time;
    //发生的事件
    private String event;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }


    public EventSources(String time, String event) {
        this.time = time;
        this.event = event;
    }

    public EventSources() {
    }

    @Override
    public String toString() {
        return "EventSources{" +
                "time='" + time + '\'' +
                ", event='" + event + '\'' +
                '}';
    }
}
