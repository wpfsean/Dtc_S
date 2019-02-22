package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：取cms上sip内容对应的实体类
 * <p>
 * 属性：详见文档
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/12 15:56
 */

public class SipBean implements Serializable {
    //设备类型
    private String deviceType;
    //sip帐号的唯一Id
    private String id;
    //ip地址
    private String ipAddress;
    //用户名
    private String name;
    //s号码
    private String number;
    //区分值班室 的id标识
    private String sentryId;
    //sip中的面部视频
    private VideoBean videoBean;

    public SipBean() {
    }

    public SipBean(String deviceType, String id, String ipAddress, String name, String number, String sentryId, VideoBean videoBean) {
        this.deviceType = deviceType;
        this.id = id;
        this.ipAddress = ipAddress;
        this.name = name;
        this.number = number;
        this.sentryId = sentryId;
        this.videoBean = videoBean;
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

    public String getSentryId() {
        return sentryId;
    }

    public void setSentryId(String sentryId) {
        this.sentryId = sentryId;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }

    public void setVideoBean(VideoBean videoBean) {
        this.videoBean = videoBean;
    }


    @Override
    public String toString() {
        return "SipBean{" +
                "deviceType='" + deviceType + '\'' +
                ", id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", sentryId='" + sentryId + '\'' +
                ", videoBean=" + videoBean +
                '}';
    }
}

