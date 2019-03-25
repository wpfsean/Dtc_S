package com.tehike.client.dtc.multiple.app.project.phone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.db.DbHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbRecordHelper;
import com.tehike.client.dtc.multiple.app.project.db.DbUtils;
import com.tehike.client.dtc.multiple.app.project.entity.SipBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactoryImpl;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SipService extends Service implements LinphoneCoreListener {
    private static final String TAG = "SipService";
    private PendingIntent mKeepAlivePendingIntent;
    private static SipService instance;
    private static PhoneCallback sPhoneCallback;
    private static RegistrationCallback sRegistrationCallback;
    private static MessageCallback sMessageCallback;

    public static boolean isReady() {
        return instance != null;
    }


    /**
     * 盛放所有电话的集合
     */
    public static List<LinphoneCall> allCallList;

    /**
     * 当前电话是否接通
     */
    boolean isConnected = false;


    @Override
    public void onCreate() {
        super.onCreate();
        LinphoneCoreFactoryImpl.instance();
        SipManager.createAndStart(SipService.this);
        instance = this;
        allCallList = new ArrayList<>();
        Intent intent = new Intent(this, KeepAliveHandler.class);
        mKeepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, 60000, mKeepAlivePendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllCallback();
        SipManager.getLc().destroy();
        SipManager.destroy();
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).cancel(mKeepAlivePendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void addPhoneCallback(PhoneCallback phoneCallback) {
        sPhoneCallback = phoneCallback;
    }

    public static void removePhoneCallback() {
        if (sPhoneCallback != null) {
            sPhoneCallback = null;
        }
    }

    public static void addMessageCallback(MessageCallback messageCallback) {
        sMessageCallback = messageCallback;
    }

    public static void removeMessageCallback() {
        if (sMessageCallback != null) {
            sMessageCallback = null;
        }

    }

    public static void addRegistrationCallback(RegistrationCallback registrationCallback) {
        sRegistrationCallback = registrationCallback;
    }

    public static void removeRegistrationCallback() {
        if (sRegistrationCallback != null) {
            sRegistrationCallback = null;
        }
    }

    public void removeAllCallback() {
        removePhoneCallback();
        removeRegistrationCallback();
        removeMessageCallback();
    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig,
                                  LinphoneCore.RegistrationState registrationState, String s) {
        String state = registrationState.toString();
        if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationNone.toString())) {
            sRegistrationCallback.registrationNone();
            //   Logutil.i("registrationNone");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationProgress.toString())) {
            //  sRegistrationCallback.registrationProgress();
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationOk.toString())) {
            sRegistrationCallback.registrationOk();
            AppConfig.SIP_STATUS = true;
            // Logutil.i("Ok");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationCleared.toString())) {
            sRegistrationCallback.registrationCleared();
            //  Logutil.i("registrationCleared");
        } else if (sRegistrationCallback != null && state.equals(LinphoneCore.RegistrationState.RegistrationFailed.toString())) {
            sRegistrationCallback.registrationFailed();
            // Logutil.i("registrationFailed");
            AppConfig.SIP_STATUS = false;
        }
    }

    List<SipBean> allSipList;

    String comingUserName = "";

    @Override
    public void callState(final LinphoneCore linphoneCore, final LinphoneCall linphoneCall, LinphoneCall.State state, String s) {
        if (state == LinphoneCall.State.IncomingReceived && sPhoneCallback != null) {

            //来电号码
            String inComingNumber = linphoneCall.getRemoteAddress().getUserName();

            //判断当前是否是会议号码
            if (inComingNumber.equals(AppConfig.DUTY_NUMBER)) {
                try {
                    SipManager.getLc().acceptCall(SipManager.getLc().getCurrentCall());
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            } else {
                //回调
                sPhoneCallback.incomingCall(linphoneCall);
                //发送来电广播
                App.getApplication().sendBroadcast(new Intent(AppConfig.INCOMING_CALL_ACTION));
                //结束屏保页面
                if (ActivityUtils.getTopActivity().getClass().getName().equals("com.tehike.client.dtc.multiple.app.project.ui.ScreenSaverActivity")) {
                    ActivityUtils.getTopActivity().finish();
                }

                try {
                    //遍历得到哨位Id
                    allSipList = GsonUtils.GsonToList(CryptoUtil.decodeBASE64(FileUtil.readFile(AppConfig.SOURCES_SIP).toString()), SipBean.class);
                    for (int i = 0; i < allSipList.size(); i++) {
                        SipBean mSipBean = allSipList.get(i);
                        if (mSipBean != null) {
                            if (mSipBean.getNumber().equals(inComingNumber)) {
                                comingUserName = mSipBean.getSentryId();
                            }
                        }
                    }
                    //播报几号哨来电
                    if (!TextUtils.isEmpty(comingUserName))
                        App.startSpeaking(StringUtils.voiceConVersion(comingUserName) + "号哨来电");
                    else
                        App.startSpeaking(inComingNumber + "来电");

                    //保存到数据库
//                    ContentValues contentValues1 = new ContentValues();
//                    contentValues1.put("time", TimeUtils.getCurrentTime());
//                    contentValues1.put("event", comingUserName + "号哨来电");
//                    new DbUtils(App.getApplication()).insert(DbHelper.EVENT_TAB_NAME, contentValues1);

                    try {
                        DbRecordHelper.phoneCallRecordInsert("来电", inComingNumber, SysinfoUtils.getSysinfo().getSipUsername());
                    } catch (Exception e) {
                        Logutil.e(Thread.currentThread().getStackTrace()[2].getClassName() + "保存记录异常--->>>" + e.getMessage());
                    }
                } catch (Exception e) {
                    Logutil.e("SipService异常-->>>" + e.getMessage());
                }
            }
        }
        if (state == LinphoneCall.State.OutgoingInit && sPhoneCallback != null) {
            sPhoneCallback.outgoingInit();
        }

        if (state == LinphoneCall.State.Connected && sPhoneCallback != null) {
            sPhoneCallback.callConnected();
        }

        if (state == LinphoneCall.State.Error && sPhoneCallback != null) {
            sPhoneCallback.error();
        }

        if (state == LinphoneCall.State.CallEnd && sPhoneCallback != null) {
            sPhoneCallback.callEnd();
        }

        if (state == LinphoneCall.State.CallReleased && sPhoneCallback != null) {
            sPhoneCallback.callReleased();
            isConnected = false;
        }
    }

    @Override
    public void authInfoRequested(LinphoneCore linphoneCore, String s, String s1, String s2) {

    }

    @Override
    public void authenticationRequested(LinphoneCore linphoneCore, LinphoneAuthInfo linphoneAuthInfo, LinphoneCore.AuthMethod authMethod) {

    }

    @Override
    public void callStatsUpdated(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCallStats linphoneCallStats) {

    }

    @Override
    public void newSubscriptionRequest(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend, String s) {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend) {

    }

    @Override
    public void dtmfReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, int i) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneAddress linphoneAddress, byte[] bytes) {

    }

    @Override
    public void transferState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state) {

    }

    @Override
    public void infoReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneInfoMessage linphoneInfoMessage) {

    }

    @Override
    public void subscriptionStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, SubscriptionState subscriptionState) {

    }

    @Override
    public void publishStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, PublishState publishState) {

    }

    @Override
    public void show(LinphoneCore linphoneCore) {

    }

    @Override
    public void displayStatus(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayMessage(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayWarning(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, int i) {

    }

    @Override
    public void fileTransferRecv(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, byte[] bytes, int i) {

    }

    @Override
    public int fileTransferSend(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, ByteBuffer byteBuffer, int i) {
        return 0;
    }

    @Override
    public void callEncryptionChanged(LinphoneCore linphoneCore, LinphoneCall linphoneCall, boolean b, String s) {

    }

    @Override
    public void isComposingReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom) {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore linphoneCore, LinphoneCore.EcCalibratorStatus ecCalibratorStatus, int i, Object o) {

    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {

    }

    @Override
    public void uploadProgressIndication(LinphoneCore linphoneCore, int i, int i1) {

    }

    @Override
    public void uploadStateChanged(LinphoneCore linphoneCore, LinphoneCore.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void friendListCreated(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void friendListRemoved(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void networkReachableChanged(LinphoneCore linphoneCore, boolean b) {

    }

    @Override
    public void messageReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

        //接收短消息 的回调
        if (sMessageCallback != null) {
            sMessageCallback.receiverMessage(linphoneChatMessage);
        }
    }


    @Override
    public void messageReceivedUnableToDecrypted(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, String s, LinphoneContent linphoneContent) {

    }

    @Override
    public void configuringStatus(LinphoneCore linphoneCore, LinphoneCore.RemoteProvisioningState remoteProvisioningState, String s) {

    }
}
