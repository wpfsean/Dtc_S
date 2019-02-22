package com.tehike.client.dtc.multiple.app.project.phone;

import android.content.Context;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LpConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Mark Xu on 17/3/13.
 * 语音通话工具类
 */

public class SipUtils {
    private static final String TAG = "SipUtils";
    private static volatile SipUtils sLinphoneUtils;
    private LinphoneCore mLinphoneCore = null;

    public static SipUtils getInstance() {
        if (sLinphoneUtils == null) {
            synchronized (SipUtils.class) {
                if (sLinphoneUtils == null) {
                    sLinphoneUtils = new SipUtils();
                }
            }
        }
        return sLinphoneUtils;
    }

    private SipUtils() {
        mLinphoneCore = SipManager.getLc();
        mLinphoneCore.enableEchoCancellation(true);
        mLinphoneCore.enableEchoLimiter(true);

    }

    /**
     * 注册到服务器
     *
     * @param name
     * @param password
     * @param host
     * @throws LinphoneCoreException
     */
    public void registerUserAuth(String name, String password, String host) throws LinphoneCoreException {
        String identify = "sip:" + name + "@" + host;
        String proxy = "sip:" + host;
        LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
        LinphoneAddress identifyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identify);
        LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(name, null, password,
                null, null, host);
        LinphoneProxyConfig prxCfg = mLinphoneCore.createProxyConfig(identifyAddr.asString(),
                proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true);
        prxCfg.enableAvpf(false);
        prxCfg.setAvpfRRInterval(0);
        prxCfg.setExpires(5);
        prxCfg.enableQualityReporting(false);
        prxCfg.setQualityReportingCollector(null);
        prxCfg.setQualityReportingInterval(0);
        prxCfg.enableRegister(true);
        mLinphoneCore.addProxyConfig(prxCfg);
        mLinphoneCore.addAuthInfo(authInfo);
        mLinphoneCore.setDefaultProxyConfig(prxCfg);
    }

    public LinphoneCall startSingleCallingTo(PhoneBean bean, boolean isVideoCall) {
        LinphoneAddress address;
        LinphoneCall call = null;
        try {
            address = mLinphoneCore.interpretUrl(bean.getUserName() + "@" + bean.getHost());
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            return null;
        }
        address.setDisplayName(bean.getDisplayName());
        LinphoneCallParams params = mLinphoneCore.createCallParams(null);
        if (isVideoCall) {
            params.setVideoEnabled(true);
            params.enableLowBandwidth(false);
        } else {
            params.setVideoEnabled(false);
        }
        try {
            call = mLinphoneCore.inviteAddressWithParams(address, params);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
        return call;
    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateCall(currentCall);
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }
    }

    /**
     * 是否静音
     *
     * @param isMicMuted
     */
    public void toggleMicro(boolean isMicMuted) {
        mLinphoneCore.muteMic(isMicMuted);
    }

    /**
     * 是否外放
     *
     * @param isSpeakerEnabled
     */
    public void toggleSpeaker(boolean isSpeakerEnabled) {
        mLinphoneCore.enableSpeaker(isSpeakerEnabled);
    }


    /**
     * 切换视频通话画面
     *
     * @param context
     * @param resourceId
     * @param target
     * @throws IOException
     */
//    public void sw(View view) {
//
//        try {
//            int videoDeviceId = LinphoneManager.getLc().getVideoDevice();
//            Logutils.i("videoDeviceId:"+videoDeviceId);
//            videoDeviceId = (videoDeviceId + 1) % AndroidCameraConfiguration.retrieveCameras().length;
//
//            Logutils.i("videoDeviceId:"+videoDeviceId);
//            LinphoneManager.getLc().setVideoDevice(videoDeviceId);
//
//            LinphoneUtils.updateCall();
//
//            if (mPreviewView != null) {
//                LinphoneManager.getLc().setPreviewWindow(mPreviewView);
//            }
//        } catch (ArithmeticException ae) {
//            Log.e("TAG","Cannot switch camera : no camera");
//        }
//    }
    public static void copyIfNotExist(Context context, int resourceId, String target) throws IOException {
        File fileToCopy = new File(target);
        if (!fileToCopy.exists()) {
            copyFromPackage(context, resourceId, fileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int resourceId, String target) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(target, 0);
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, readByte);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public static LpConfig getConfig(Context context) {
        LinphoneCore lc = getLc();
        if (lc != null) {
            return lc.getConfig();
        }

        if (SipManager.isInstanceiated()) {
            org.linphone.mediastream.Log.w("SipManager not instanciated yet...");
            return LinphoneCoreFactory.instance().createLpConfig(context.getFilesDir().getAbsolutePath() + "/.linphonerc");
        }

        return LinphoneCoreFactory.instance().createLpConfig(SipManager.getInstance().mLinphoneConfigFile);
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static LinphoneCore getLc() {
        if (!SipManager.isInstanceiated()) {
            return null;
        }
        return SipManager.getLcIfManagerNotDestroyOrNull();
    }
}
