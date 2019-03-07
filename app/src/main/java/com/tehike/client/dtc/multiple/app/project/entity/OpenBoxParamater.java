package com.tehike.client.dtc.multiple.app.project.entity;

import java.io.Serializable;

/**
 * 描述：请求开启弹箱的协议
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2019/2/27 16:14
 * @version V1.0
 */

public class OpenBoxParamater implements Serializable {



    private String falg;
    private String ver;
    private String action;
    private int requestCode;
    private int requestSalt;
    private int responseCode;

    @Override
    public String toString() {
        return "OpenBoxParamater{" +
                "falg='" + falg + '\'' +
                ", ver='" + ver + '\'' +
                ", action='" + action + '\'' +
                ", requestCode=" + requestCode +
                ", requestSalt=" + requestSalt +
                ", responseCode=" + responseCode +
                ", sendIp='" + sendIp + '\'' +
                ", boxId='" + boxId + '\'' +
                '}';
    }

    public String getFalg() {
        return falg;
    }

    public void setFalg(String falg) {
        this.falg = falg;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestSalt() {
        return requestSalt;
    }

    public void setRequestSalt(int requestSalt) {
        this.requestSalt = requestSalt;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    //谁发起的请求
    private String sendIp;
    //要开启的弹箱的Guid(唯一识别号)
    private String boxId;


    public OpenBoxParamater() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSendIp() {
        return sendIp;
    }

    public void setSendIp(String sendIp) {
        this.sendIp = sendIp;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

}
