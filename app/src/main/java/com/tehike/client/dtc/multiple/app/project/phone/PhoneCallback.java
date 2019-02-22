package com.tehike.client.dtc.multiple.app.project.phone;

import org.linphone.core.LinphoneCall;

/**
 *  sip电话状态的回调（来电，接通，打电话 ，挂电话，释放电话，连接失败）
 */

public abstract class PhoneCallback {
    /**
     * 来电状态
     * @param linphoneCall
     */
    public void incomingCall(LinphoneCall linphoneCall) {}

    /**
     * 呼叫初始化
     */
    public void outgoingInit() {}

    /**
     * 电话接通
     */
    public void callConnected() {}

    /**
     * 电话挂断
     */
    public void callEnd() {}

    /**
     * 释放通话
     */
    public void callReleased() {}

    /**
     * 连接失败
     */
    public void error() {}
}
