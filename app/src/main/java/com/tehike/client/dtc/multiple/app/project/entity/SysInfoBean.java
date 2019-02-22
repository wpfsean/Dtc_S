package com.tehike.client.dtc.multiple.app.project.entity;

/**
 * 描述：Sysinfo实体类封装(WebApi)
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/5 9:16
 * @version V1.0
 */
import java.io.Serializable;

public class SysInfoBean implements Serializable {
    //报警发送的端口
    int alertPort;
    //报警地址
    String alertServer;
    //本机的唯一识别号
    String deviceGuid;
    String deviceName;
    int fingerprintPort;
    String fingerprintServer;
    int heartbeatPort;
    String heartbeatServer;
    String sipPassword;
    String sipServer;
    String sipUsername;
    int webresourcePort;
    String webresourceServer;
    int neighborWatchPort;


    public SysInfoBean(int alertPort, String alertServer, String deviceGuid, String deviceName, int fingerprintPort, String fingerprintServer, int heartbeatPort, String heartbeatServer, String sipPassword, String sipServer, String sipUsername, int webresourcePort, String webresourceServer, int neighborWatchPort) {
        this.alertPort = alertPort;
        this.alertServer = alertServer;
        this.deviceGuid = deviceGuid;
        this.deviceName = deviceName;
        this.fingerprintPort = fingerprintPort;
        this.fingerprintServer = fingerprintServer;
        this.heartbeatPort = heartbeatPort;
        this.heartbeatServer = heartbeatServer;
        this.sipPassword = sipPassword;
        this.sipServer = sipServer;
        this.sipUsername = sipUsername;
        this.webresourcePort = webresourcePort;
        this.webresourceServer = webresourceServer;
        this.neighborWatchPort = neighborWatchPort;
    }

    public int getAlertPort() {

        return alertPort;
    }

    public void setAlertPort(int alertPort) {
        this.alertPort = alertPort;
    }

    public String getAlertServer() {
        return alertServer;
    }

    public void setAlertServer(String alertServer) {
        this.alertServer = alertServer;
    }

    public String getDeviceGuid() {
        return deviceGuid;
    }

    public void setDeviceGuid(String deviceGuid) {
        this.deviceGuid = deviceGuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getFingerprintPort() {
        return fingerprintPort;
    }

    public void setFingerprintPort(int fingerprintPort) {
        this.fingerprintPort = fingerprintPort;
    }

    public String getFingerprintServer() {
        return fingerprintServer;
    }

    public void setFingerprintServer(String fingerprintServer) {
        this.fingerprintServer = fingerprintServer;
    }

    public int getHeartbeatPort() {
        return heartbeatPort;
    }

    public void setHeartbeatPort(int heartbeatPort) {
        this.heartbeatPort = heartbeatPort;
    }

    public String getHeartbeatServer() {
        return heartbeatServer;
    }

    public void setHeartbeatServer(String heartbeatServer) {
        this.heartbeatServer = heartbeatServer;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        this.sipPassword = sipPassword;
    }

    public String getSipServer() {
        return sipServer;
    }

    public void setSipServer(String sipServer) {
        this.sipServer = sipServer;
    }

    public String getSipUsername() {
        return sipUsername;
    }

    public void setSipUsername(String sipUsername) {
        this.sipUsername = sipUsername;
    }

    public int getWebresourcePort() {
        return webresourcePort;
    }

    public void setWebresourcePort(int webresourcePort) {
        this.webresourcePort = webresourcePort;
    }

    public String getWebresourceServer() {
        return webresourceServer;
    }

    public void setWebresourceServer(String webresourceServer) {
        this.webresourceServer = webresourceServer;
    }

    public int getNeighborWatchPort() {
        return neighborWatchPort;
    }

    public void setNeighborWatchPort(int neighborWatchPort) {
        this.neighborWatchPort = neighborWatchPort;
    }

    @Override
    public String toString() {
        return "SysInfoBean{" +
                "alertPort=" + alertPort +
                ", alertServer='" + alertServer + '\'' +
                ", deviceGuid='" + deviceGuid + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", fingerprintPort=" + fingerprintPort +
                ", fingerprintServer='" + fingerprintServer + '\'' +
                ", heartbeatPort=" + heartbeatPort +
                ", heartbeatServer='" + heartbeatServer + '\'' +
                ", sipPassword='" + sipPassword + '\'' +
                ", sipServer='" + sipServer + '\'' +
                ", sipUsername='" + sipUsername + '\'' +
                ", webresourcePort=" + webresourcePort +
                ", webresourceServer='" + webresourceServer + '\'' +
                ", neighborWatchPort=" + neighborWatchPort +
                '}';
    }
}