package com.tehike.client.dtc.multiple.app.project.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tehike.client.dtc.multiple.app.project.ui.DtcDutyLoginActivity;

/**
 * 描述：开机自启动（测试用）
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/16 19:55
 * @version V1.0
 */

public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, DtcDutyLoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}