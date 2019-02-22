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

public class SipGroupItemInfoBean  implements Serializable{

    private String deviceType;
    private String id;
    private String ipAddress;
    private String location;
    private String name;
    private String number;
    private int sentryId;
    private int state;
    private VideoBean bean;

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

    public int getSentryId() {
        return sentryId;
    }

    public void setSentryId(int sentryId) {
        this.sentryId = sentryId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "SipGroupItemInfoBean{" +
                "deviceType='" + deviceType + '\'' +
                ", id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", location='" + location + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", sentryId=" + sentryId +
                ", state=" + state +
                ", bean=" + bean +
                '}';
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public VideoBean getBean() {
        return bean;
    }

    public void setBean(VideoBean bean) {
        this.bean = bean;
    }

    public SipGroupItemInfoBean() {
    }
}
