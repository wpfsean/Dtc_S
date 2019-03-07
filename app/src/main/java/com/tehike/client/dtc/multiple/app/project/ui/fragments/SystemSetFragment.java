package com.tehike.client.dtc.multiple.app.project.ui.fragments;

import android.os.Bundle;
import android.view.View;

import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.services.ReceiverAlarmService;
import com.tehike.client.dtc.multiple.app.project.services.RemoteVoiceOperatService;
import com.tehike.client.dtc.multiple.app.project.services.RequestWebApiDataService;
import com.tehike.client.dtc.multiple.app.project.services.TerminalUpdateIpService;
import com.tehike.client.dtc.multiple.app.project.services.TimingAutoUpdateService;
import com.tehike.client.dtc.multiple.app.project.services.TimingRefreshNetworkStatus;
import com.tehike.client.dtc.multiple.app.project.services.TimingRequestAlarmTypeService;
import com.tehike.client.dtc.multiple.app.project.services.TimingSendNativeInfoService;
import com.tehike.client.dtc.multiple.app.project.services.UpdateSystemSettingService;
import com.tehike.client.dtc.multiple.app.project.ui.BaseFragment;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.ServiceUtil;
import com.tehike.client.dtc.multiple.app.project.voice.TimingCheckVoiceIsLiveService;

import butterknife.OnClick;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class SystemSetFragment extends BaseFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_systemset_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {


        AppConfig.IS_CAN_SLIDE = true;


        Logutil.d("IS_CAN_SLIDE---->>"+AppConfig.IS_CAN_SLIDE);


    }


    /**
     * 退出App
     */
    @OnClick(R.id.login_out_btn_layout)
    public void loginOutApp(View view) {
        loginout();
    }

    @OnClick(R.id.alltime_set_btn_layout)
    public  void sysTiemSet(View view){

        Logutil.d("IS_CAN_SLIDE---->>"+AppConfig.IS_CAN_SLIDE);
    }

    private void loginout() {

        if (ServiceUtil.isServiceRunning(RemoteVoiceOperatService.class)) {
            ServiceUtil.stopService(RemoteVoiceOperatService.class);
        }
        if (ServiceUtil.isServiceRunning(ReceiverAlarmService.class)) {
            ServiceUtil.stopService(ReceiverAlarmService.class);
        }
        if (ServiceUtil.isServiceRunning(TerminalUpdateIpService.class)) {
            ServiceUtil.stopService(TerminalUpdateIpService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingAutoUpdateService.class)) {
            ServiceUtil.stopService(TimingAutoUpdateService.class);
        }
        if (ServiceUtil.isServiceRunning(RemoteVoiceOperatService.class)) {
            ServiceUtil.stopService(RemoteVoiceOperatService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingRefreshNetworkStatus.class)) {
            ServiceUtil.stopService(TimingRefreshNetworkStatus.class);
        }
        if (ServiceUtil.isServiceRunning(TimingRequestAlarmTypeService.class)) {
            ServiceUtil.stopService(TimingRequestAlarmTypeService.class);
        }
        if (ServiceUtil.isServiceRunning(RequestWebApiDataService.class)) {
            ServiceUtil.stopService(RequestWebApiDataService.class);
        }
        if (ServiceUtil.isServiceRunning(UpdateSystemSettingService.class)) {
            ServiceUtil.stopService(UpdateSystemSettingService.class);
        }
        if (ServiceUtil.isServiceRunning(TimingSendNativeInfoService.class)) {
            ServiceUtil.stopService(TimingSendNativeInfoService.class);
        }
        ActivityUtils.removeAllActivity();
        if (getActivity() != null)
            getActivity().finish();
    }
}
