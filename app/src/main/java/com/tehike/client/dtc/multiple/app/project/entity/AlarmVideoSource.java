package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：接收报警类型的数据对象的封装
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/12/25 11:33
 */

public class AlarmVideoSource implements Serializable {

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    //报警发送者
    private String senderIp;
    //报警视频的唯一ID
    private String faceVideoId;
    //报警视频的名称
    private String faceVideoName;
    //报警类型
    private String alarmType;

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public AlarmVideoSource() {
    }

    public String getSenderIp() {

        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getFaceVideoId() {
        return faceVideoId;
    }

    public void setFaceVideoId(String faceVideoId) {
        this.faceVideoId = faceVideoId;
    }

    public String getFaceVideoName() {
        return faceVideoName;
    }

    public void setFaceVideoName(String faceVideoName) {
        this.faceVideoName = faceVideoName;
    }

    @Override
    public String toString() {
        return "AlarmVideoSource{" +
                "time='" + time + '\'' +
                ", senderIp='" + senderIp + '\'' +
                ", faceVideoId='" + faceVideoId + '\'' +
                ", faceVideoName='" + faceVideoName + '\'' +
                ", alarmType='" + alarmType + '\'' +
                '}';
    }
}
