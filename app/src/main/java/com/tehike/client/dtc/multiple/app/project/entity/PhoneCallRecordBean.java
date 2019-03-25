package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class PhoneCallRecordBean implements Serializable {
    private String time;
    private String status;
    private String from;
    private String to;

    @Override
    public String toString() {
        return "PhoneCallRecordBean{" +
                "time='" + time + '\'' +
                ", status='" + status + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

    public PhoneCallRecordBean() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
