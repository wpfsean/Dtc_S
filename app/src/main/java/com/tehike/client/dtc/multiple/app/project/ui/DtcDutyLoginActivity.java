package com.tehike.client.dtc.multiple.app.project.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.entity.SysInfoBean;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.phone.Linphone;
import com.tehike.client.dtc.multiple.app.project.phone.RegistrationCallback;
import com.tehike.client.dtc.multiple.app.project.phone.SipManager;
import com.tehike.client.dtc.multiple.app.project.services.LocationService;
import com.tehike.client.dtc.multiple.app.project.utils.ActivityUtils;
import com.tehike.client.dtc.multiple.app.project.update.AppUtils;
import com.tehike.client.dtc.multiple.app.project.utils.CryptoUtil;
import com.tehike.client.dtc.multiple.app.project.utils.FileUtil;
import com.tehike.client.dtc.multiple.app.project.utils.GsonUtils;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.NetworkUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ServiceUtil;
import com.tehike.client.dtc.multiple.app.project.utils.SharedPreferencesUtils;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.WriteLogToFile;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

/**
 * 描述：Dtc登录界面
 * 利用用户名、密码、服务器地址验证Sysinfo接口，获取本机的信息并用File保存到本地
 * <p>
 * 说明：3399开发板，已默认授于全部的动态权限，所以不需要再去申请处理（保存申请权限功能）
 * <p>
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/2 10:42
 */

public class DtcDutyLoginActivity extends BaseActivity {

    /**
     * 显示当前的版本号
     */
    @BindView(R.id.app_version_code)
    TextView appVersionCodeLayout;

    /**
     * 显示当前的版本名称
     */
    @BindView(R.id.app_version_name)
    TextView appVersionNameLayout;

    /**
     * 登录动画布局
     */
    @BindView(R.id.image_loading)
    ImageView loadingImageViewLayout;

    /**
     * 登录错误信息提示布局
     */
    @BindView(R.id.loin_error_infor_layout)
    TextView disPlayLoginErrorViewLayout;

    /**
     * 填写用户名
     */
    @BindView(R.id.edit_username_layout)
    EditText editUserNameLayout;

    /**
     * 填写密码
     */
    @BindView(R.id.edit_userpass_layout)
    EditText editUserPwdLayout;

    /**
     * 记住密码Checkbox
     */
    @BindView(R.id.remember_pass_layout)
    Checkable checkRememberPwdLayout;

    /**
     * 自动登录CheckBox
     */
    @BindView(R.id.auto_login_layout)
    Checkable checkAutoLoginLayout;

    /**
     * 填写服务器
     */
    @BindView(R.id.edit_serviceip_layout)
    EditText editServerIpLayout;

    /**
     * 修改服务器的checkbox
     */
    @BindView(R.id.remembe_serverip_layout)
    CheckBox checkUpdateServerIpLayout;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 用户是否禁止权限
     */
    boolean mShowRequestPermission = true;

    /**
     * 存放未同同意的权限
     */
    List<String> noAgreePermissions = new ArrayList<>();

    /**
     * 是否点击过了登录按键
     */
    boolean isClickLoginBtnFlag = false;

    /**
     * 获取输入框内的用户名
     */
    String enteredUserName = "";

    /**
     * 获取输入框内的密码
     */
    String enteredUserPwd = "";

    /**
     * 获取输入框内的服务器地址
     */
    String enteredServerIp = "";

    /**
     * 是否记住密码标识
     */
    boolean isRememberPwdFlag;

    /**
     * 是否自动登录标识
     */
    boolean isAutoLoginFlag;

    /**
     * 控制键盘输入
     */
    InputMethodManager mInputMethodManager;

    /**
     * 配置时的弹窗
     */
    PopupWindow systemSetPopuwindow = null;

    /**
     * 获取到的本机Ip
     */
    String ip = "";

    /**
     * 获取到的本机Dns
     */
    String dns = "";

    /**
     * 获取到的本机netmask
     */
    String netmask = "";

    /**
     * 获取到的本机gateway
     */
    String gateway = "";

    /**
     * 登录连接对象
     */
    HttpURLConnection con = null;

    /**
     * 声音管理类
     */
    AudioManager mAudioManager;

    /**
     * 需要申请的权限
     * 定位
     * 读取设备信息
     * 录音
     * 相机
     * 文件读写
     */
    String[] allPermissionList = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    protected int intiLayout() {
        return R.layout.activity_dtclogin_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

        //申请所需要的权限
        checkAllPermissions();
    }

    /**
     * 申请权限
     */
    private void checkAllPermissions() {
        //6.0权限动态申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            noAgreePermissions.clear();
            //遍历申请
            for (String permission : allPermissionList) {
                if (ContextCompat.checkSelfPermission(DtcDutyLoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    noAgreePermissions.add(permission);
                }
            }
            //如果存在继续申请
            if (!noAgreePermissions.isEmpty()) {
                //将List转为数组
                String[] permissions = noAgreePermissions.toArray(new String[noAgreePermissions.size()]);
                ActivityCompat.requestPermissions(DtcDutyLoginActivity.this, permissions, 1);
            } else {
                //初始化数据
                initializeData();
            }
        } else {
            //初始化数据
            initializeData();
        }
    }

    /**
     * 初始化数据
     */
    private void initializeData() {
        //测试时用于查看 sql的读写状态
        SQLiteStudioService.instance().start(this);

        //键盘控制管理
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //打印当前的版本号
        appVersionCodeLayout.setText(AppUtils.getVersionCode(App.getApplication()) + "");
        appVersionNameLayout.setText(AppUtils.getVersionName(App.getApplication()) + "");

        //动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);

        //申请系统权限
        checkSystemPermissions();

        //启动定位服务（获取经）
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (!ServiceUtil.isServiceRunning(LocationService.class))
                ServiceUtil.startService(LocationService.class);
        }

        //判断是否自动登录
        boolean isAutoLogin = (boolean) SharedPreferencesUtils.getObject(DtcDutyLoginActivity.this, "autologin", false);
        if (isAutoLogin) {
            direcLoginSuccess();
        }

        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(DtcDutyLoginActivity.this, "isremember", false);
        if (isrePwd) {
            checkRememberPwdLayout.setChecked(true);
            //填充用户名框
            String db_name = SysinfoUtils.getUserName();
            if (!TextUtils.isEmpty(db_name)) {
                editUserNameLayout.setText(db_name);
            }
            //填充密码框
            String db_pwd = SysinfoUtils.getUserPwd();
            if (!TextUtils.isEmpty(db_pwd)) {
                editUserPwdLayout.setText(db_pwd);
            }
            //填充服务器地址框
            if (SysinfoUtils.getSysinfo() != null) {
                String db_server = SysinfoUtils.getSysinfo().getWebresourceServer();
                if (!TextUtils.isEmpty(db_server)) {
                    editServerIpLayout.setText(db_server);
                    editServerIpLayout.setEnabled(false);
                }
            }
            //修改服务器地址
            checkUpdateServerIpLayout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        editServerIpLayout.setEnabled(true);
                    } else {
                        editServerIpLayout.setEnabled(false);
                    }
                }
            });
        }
    }

    /**
     * 申请系统权限（允许弹窗，修改系统亮度）
     */
    private void checkSystemPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(DtcDutyLoginActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 2);
            } else {
                AppConfig.ARGEE_OVERLAY_PERMISSION = true;
            }
        } else {
            AppConfig.ARGEE_OVERLAY_PERMISSION = false;
        }
    }

    /**
     * 登录
     */
    @OnClick(R.id.userlogin_button_layout)
    public void loginCMS(View view) {
        disPlayLoginErrorViewLayout.setText("");
        //防止点击过快
        //获取当前输入框内的内容
        enteredUserName = editUserNameLayout.getText().toString().trim();
        enteredUserPwd = editUserPwdLayout.getText().toString().trim();
        enteredServerIp = (String) SharedPreferencesUtils.getObject(DtcDutyLoginActivity.this, "serverIp", "");
        //判断信息是否齐全
        if (!TextUtils.isEmpty(enteredUserName) && !TextUtils.isEmpty(enteredUserPwd) && !TextUtils.isEmpty(enteredServerIp)) {
            //判断是否有网
            if (NetworkUtils.isConnected()) {
                //加载动画显示
                loadingImageViewLayout.setVisibility(View.VISIBLE);
                loadingImageViewLayout.startAnimation(mLoadingAnim);
                //正则表达
                if (!NetworkUtils.isboolIp(enteredServerIp)) {
                    handler.sendEmptyMessage(6);
                    return;
                }
                //判断是否正在登录
                if (!isClickLoginBtnFlag) {
                    TcpLoginCmsThread thread = new TcpLoginCmsThread(enteredUserName, enteredUserPwd, enteredServerIp);
                    new Thread(thread).start();
                    isClickLoginBtnFlag = true;
                } else {
                    //取消登录，并关闭socket
                    isClickLoginBtnFlag = false;
                    //提示取消登录
                    handler.sendEmptyMessage(9);
                }
            } else {
                //提示无网络
                handler.sendEmptyMessage(3);
            }
        } else {
            //提示信息缺失
            handler.sendEmptyMessage(5);
        }
    }

    /**
     * 取消登录
     */
    @OnClick(R.id.userlogin_button_cancel_layout)
    public void test(View view) {
        if (con != null) {
            con.disconnect();
            con = null;
            handler.sendEmptyMessage(9);
            isClickLoginBtnFlag = false;
        }
    }

    /**
     * 点击空白处使键盘消失
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (DtcDutyLoginActivity.this.getCurrentFocus() != null) {
                if (DtcDutyLoginActivity.this.getCurrentFocus().getWindowToken() != null) {
                    mInputMethodManager.hideSoftInputFromWindow(DtcDutyLoginActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 右下角的设置
     */
    @OnClick(R.id.system_set_btn_layout)
    public void systemSet(View view) {
        //判断网络是否连接正常
        if (!NetworkUtils.isConnected()) {
            showProgressFail("网络异常！");
            return;
        }
        //设置弹窗
        disPlaySystemSetDialog();
    }

    int systemVoiceValue = 0;
    int systemCallValue = 0;
    int systemRingValue = 0;


    /**
     * 弹出设置弹窗
     */
    @SuppressLint("WrongConstant")
    private void disPlaySystemSetDialog() {
        //加载view
        View view = LayoutInflater.from(this).inflate(R.layout.activity_system_setting_layout, null);
        //popuwindow显示
        systemSetPopuwindow = new PopupWindow(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        systemSetPopuwindow.setSoftInputMode(systemSetPopuwindow.INPUT_METHOD_NEEDED);
        systemSetPopuwindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        systemSetPopuwindow.setContentView(view);
        //保存按键
        Button saveBtn = view.findViewById(R.id.system_set_save_btn_layout);
        //取消按钮
        Button cancelBtn = view.findViewById(R.id.system_set_cancel_btn_layout);
        //四个網絡設置输入框
        final EditText editSystemIp = view.findViewById(R.id.system_ip_set_btn_layout);
        final EditText editSystemGetway = view.findViewById(R.id.system_getway_set_btn_layout);
        final EditText editSystemNetmask = view.findViewById(R.id.system_netmask_set_btn_layout);
        final EditText editSystemDns = view.findViewById(R.id.system_dns_set_btn_layout);

        final EditText editSystemVoice = view.findViewById(R.id.system_voice_layout);
        final EditText editSystemCallVoice = view.findViewById(R.id.system_call_voice_layout);
        final EditText editSystemRingVoice = view.findViewById(R.id.system_ring_voice_layout);
        //兩個中心服務設置框
        final EditText editSystemServerIp = view.findViewById(R.id.system_serverip_layout);
        //從本地取出serverIp
        String serverIp = (String) SharedPreferencesUtils.getObject(DtcDutyLoginActivity.this, "serverIp", "");
        if (TextUtils.isEmpty(serverIp)) {
            serverIp = AppConfig._USERVER;
        }
        editSystemServerIp.setHint(serverIp);

        //在当前根布局中显示view
        View rootview = LayoutInflater.from(DtcDutyLoginActivity.this).inflate(R.layout.activity_dtclogin_layout, null);
        systemSetPopuwindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        systemSetPopuwindow.setBackgroundDrawable(new BitmapDrawable());
        //设置获取焦点
        systemSetPopuwindow.setFocusable(true);
        systemSetPopuwindow.setTouchable(true);
        //设置点击外部不可取消
        systemSetPopuwindow.setOutsideTouchable(false);
        systemSetPopuwindow.update();
        //设置背景的透明度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);
        //popu消失回调，使变回不透明
        systemSetPopuwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                //设置透明背景
                final WindowManager.LayoutParams lp = getWindow().getAttributes();
                getWindow().setAttributes(lp);
                lp.alpha = 1f;
                getWindow().setAttributes(lp);

                //popu消失时隐藏软键盘
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(DtcDutyLoginActivity.this.getCurrentFocus().getWindowToken(), 0);
            }
        });
        //获取当前的类型（有线，无线）
        final int netWorkType = NetworkUtils.getNetMode(App.getApplication());
        //本机ip
        if (netWorkType == 1) {
            ip = App.getSystemManager().ZYgetEthIp();
        } else {
            ip = App.getSystemManager().ZYgetWifiIp();
        }
        editSystemIp.setHint(ip);
        //本机子网掩码
        if (netWorkType == 1) {
            netmask = App.getSystemManager().ZYgetEthNetMask();
            ;
        } else {
            netmask = App.getSystemManager().ZYgetWifiNetMask();
        }
        editSystemNetmask.setHint(netmask);
        //本机网关
        if (netWorkType == 1) {
            gateway = App.getSystemManager().ZYgetEthGatWay();
        } else {
            gateway = App.getSystemManager().ZYgetWifiGatWay();
        }
        editSystemGetway.setHint(gateway);
        //本机dns
        if (netWorkType == 1) {
            dns = App.getSystemManager().ZYgetEthDns1();
        } else {
            dns = App.getSystemManager().ZYgetWifiDns1();
        }
        editSystemDns.setHint(dns);
        //设置声音
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int systemValues = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int systemCallValues = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        int systemRingValues = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        editSystemVoice.setHint("系统音量:" + systemValues + "");
        editSystemCallVoice.setHint("通话音量:" + systemCallValues + "");
        editSystemRingVoice.setHint("响铃音量:" + systemRingValues + "");

        //保存按钮点击事件
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入框的声音值

//                String strSystemVoiceValue = editSystemVoice.getText().toString().trim();
//                String strSystemCallValue = editSystemCallVoice.getText().toString().trim();
//                String strSystemRingValue = editSystemRingVoice.getText().toString().trim();
//                if (!TextUtils.isEmpty(strSystemVoiceValue) && Pattern.compile("[0-9]").matcher(strSystemVoiceValue).matches()) {
//                    systemVoiceValue = Integer.parseInt(strSystemVoiceValue);
//                    if (systemVoiceValue != 0) {
//                        if (mAudioManager != null)
//                            mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, systemVoiceValue, 0);
//                    }
//                }
//                if (!TextUtils.isEmpty(strSystemCallValue)) {
//                    systemCallValue = Integer.parseInt(strSystemCallValue);
//                    if (systemCallValue != 0) {
//                        if (mAudioManager != null)
//                            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, systemCallValue, 0);
//                    }
//                }
//                if (!TextUtils.isEmpty(strSystemRingValue) && Pattern.compile("[0-9]").matcher(strSystemVoiceValue).matches()) {
//                    systemRingValue = Integer.parseInt(strSystemRingValue);
//                    if (systemRingValue != 0) {
//                        if (mAudioManager != null)
//                            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, systemRingValue, 0);
//                    }
//                }
                //判斷Ip輸入框是否為空
                String edIp = editSystemIp.getText().toString().trim();
                if (TextUtils.isEmpty(edIp)) {
                    edIp = editSystemIp.getHint().toString();
                } else {
                    if (!NetworkUtils.isboolIp(edIp)) {
                        edIp = editSystemIp.getHint().toString();
                    }
                }
                //判斷子網掩碼
                String edNetMask = editSystemNetmask.getText().toString().trim();
                if (TextUtils.isEmpty(edNetMask)) {
                    edNetMask = editSystemNetmask.getHint().toString();
                } else {
                    if (!NetworkUtils.isboolIp(edNetMask)) {
                        edNetMask = editSystemNetmask.getHint().toString();
                    }
                }
                //判斷網関
                String edGetway = editSystemGetway.getText().toString().trim();
                if (TextUtils.isEmpty(edGetway)) {
                    edGetway = editSystemGetway.getHint().toString();
                } else {
                    if (!NetworkUtils.isboolIp(edGetway)) {
                        edGetway = editSystemGetway.getHint().toString();
                    }
                }
                //判斷Dns
                String edDns = editSystemDns.getText().toString();
                if (TextUtils.isEmpty(edDns)) {
                    edDns = editSystemDns.getHint().toString();
                } else {
                    if (!NetworkUtils.isboolIp(edDns)) {
                        edDns = editSystemDns.getHint().toString();
                    }
                }
                //保存網絡設置
                if (netWorkType == 1)
                    App.getSystemManager().ZYsetEthStaticMode(edIp, edGetway, edNetMask, edDns, AppConfig.DNS);
                else
                    App.getSystemManager().ZYsetWifiStaticMode(edIp, edGetway, edNetMask, edDns, AppConfig.DNS);

                //獲取中心服務器的Ip地址
                String edServerIp = editSystemServerIp.getText().toString().trim();

                //保存中心服務器的IP地址
                if (!TextUtils.isEmpty(edServerIp)) {
                    if (NetworkUtils.isboolIp(edServerIp)) {
                        SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "serverIp", edServerIp);
                        AppConfig._USERVER = edServerIp;
                    }
                }
                //popu消失
                if (systemSetPopuwindow != null && systemSetPopuwindow.isShowing()) {
                    systemSetPopuwindow.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (systemSetPopuwindow != null && systemSetPopuwindow.isShowing()) {
                    systemSetPopuwindow.dismiss();
                }
            }
        });
    }

    /**
     * 子线程验证webapi的Sysinfo接口
     */
    class TcpLoginCmsThread extends Thread {
        //用户名
        String name;
        //密码
        String pwd;
        //登录服务器Ip
        String serverIp;

        //构造函数
        public TcpLoginCmsThread(String name, String pwd, String serverIp) {
            this.name = name;
            this.pwd = pwd;
            this.serverIp = serverIp;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    String requestLoginUrl = AppConfig.WEB_HOST + serverIp + AppConfig._SYSINFO;
                    con = (HttpURLConnection) new URL(requestLoginUrl).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    String authString = name + ":" + pwd;
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(authString.getBytes(), 0)));
                    con.connect();
                    if (con.getResponseCode() == 200) {
                        InputStream in = con.getInputStream();
                        String result = StringUtils.readTxt(in);
                        if (TextUtils.isEmpty(result)) {
                            handler.sendEmptyMessage(8);
                            Logutil.e("Sysinfo接口返回数据为空--->>" + result);
                            return;
                        }
                        Message returnLoginMess = new Message();
                        returnLoginMess.obj = result;
                        returnLoginMess.what = 11;
                        handler.sendMessage(returnLoginMess);
                    } else {
                        Logutil.e("Sysinfo接口返回非200" + con.getResponseCode());
                        handler.sendEmptyMessage(8);
                    }
                    con.disconnect();
                } catch (Exception e) {
                    Logutil.e("登录异常"+e.getMessage());
                    handler.sendEmptyMessage(12);
                    WriteLogToFile.info("登录异常"+e.getMessage());
                }
            }
        }
    }

    /**
     * 处理登录 接口的sysinfo数据
     */
    private void handlerSysinfoData(String result) {

        if (TextUtils.isEmpty(result)) {
            WriteLogToFile.info(getString(R.string.str_sysinfo_empty));
            return;
        }
        //解析Sisinfo数据
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject != null) {
                // jsonObject.getString("fingerprintServer")
                //jsonObject.getInt("fingerprintPort")
                SysInfoBean sysInfoBean = new SysInfoBean(jsonObject.getInt("alertPort"),
                        jsonObject.getString("alertServer"), jsonObject.getString("deviceGuid"),
                        jsonObject.getString("deviceName"), -1,
                        "", jsonObject.getInt("heartbeatPort"),
                        jsonObject.getString("heartbeatServer"), jsonObject.getString("sipPassword"),
                        jsonObject.getString("sipServer"), jsonObject.getString("sipUsername"),
                        jsonObject.getInt("webresourcePort"), jsonObject.getString("webresourceServer"), jsonObject.getInt("neighborWatchPort"));

                //把Sysinfo数据写入文件
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    FileUtil.writeFile(CryptoUtil.encodeBASE64(GsonUtils.GsonString(sysInfoBean)), AppConfig.SYSINFO);
                else
                    WriteLogToFile.info("写入sysinfo时没开启WRITE_EXTERNAL_STORAGE权限");
                //登录成功
                handler.sendEmptyMessage(10);
            }
        } catch (Exception e) {
            WriteLogToFile.info("解析Sysinfo数据异常");
            Logutil.e("解析Sysinfo数据异常--->>>" + e.getMessage());
        }
    }

    /**
     * 登录成功
     */
    private void LoginSuccessMethond() {
        //获取记住密码的状态
        isRememberPwdFlag = checkRememberPwdLayout.isChecked();
        //获取自动登录的状态
        isAutoLoginFlag = checkAutoLoginLayout.isChecked();
        //判断当前是否记住密码，如果记住密码就把配置信息提前插入数据库
        if (isRememberPwdFlag) {
            //保存记住密码的状态
            SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "isremember", isRememberPwdFlag);
        }
        //保存自动登录的状态
        if (isAutoLoginFlag) {
            SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "autologin", isRememberPwdFlag);
        }

        SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "serverIp", enteredServerIp);
        SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "userPwd", enteredUserPwd);
        SharedPreferencesUtils.putObject(DtcDutyLoginActivity.this, "userName", enteredUserName);
        //赋值给常量
        AppConfig._UNAME = enteredUserName;
        AppConfig._UPWD = enteredUserPwd;
        AppConfig._USERVER = enteredServerIp;
        //延迟半秒后登录成功（取消动画的加载状态）
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                direcLoginSuccess();
            }
        }, 500);
    }

    /**
     * 成功登录cms（cms验证通过）
     */
    public void direcLoginSuccess() {
        handler.sendEmptyMessage(4);
        //跳转到主页面并finish本页面
        openActivityAndCloseThis(DtcDutyMainActivity.class);
        ActivityUtils.removeActivity(DtcDutyLoginActivity.this);
        DtcDutyLoginActivity.this.finish();
    }

    /**
     * 权限申请回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(DtcDutyLoginActivity.this, permissions[i]);
                        if (showRequestPermission) {
                            //重新申请权限
                            checkAllPermissions();
                            return;
                        } else {
                            //已经禁止
                            mShowRequestPermission = false;
                            String permisson = permissions[i];
                            Logutil.e("未授予的权限:" + permisson);
                            WriteLogToFile.info("用户禁止申请以下的权限:" + permisson);
                        }
                    }
                }
                //初始化参数
                initializeData();
                break;
            default:
                break;
        }
    }

    /**
     * Activity的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                //悬浮窗口权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        AppConfig.ARGEE_OVERLAY_PERMISSION = true;
                    } else {
                        AppConfig.ARGEE_OVERLAY_PERMISSION = false;
                    }
                }
                break;
        }
    }

    /**
     * 按home键时保存当前的输入状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("enteredUserName", editUserNameLayout.getText().toString().trim());
        outState.putString("enteredUserPwd", editUserPwdLayout.getText().toString().trim());
        outState.putString("serverip", editServerIpLayout.getText().toString().trim());
    }

    /**
     * 恢复刚才的输入状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        editUserNameLayout.setText(savedInstanceState.getString("enteredUserName"));
        editUserPwdLayout.setText(savedInstanceState.getString("enteredUserPwd"));
        editServerIpLayout.setText(savedInstanceState.getString("serverip"));
    }

    @Override
    protected void onResume() {

        if (!SipManager.isInstanceiated()) {
            Linphone.startService(this);
        }
        //回调，判断当前的Sip状态
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationOk() {
                AppConfig.SIP_STATUS = true;
            }

            @Override
            public void registrationFailed() {
                AppConfig.SIP_STATUS = false;
            }
        }, null);
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        loadingImageViewLayout.clearAnimation();

        if (mLoadingAnim != null)
            mLoadingAnim = null;

        if (handler != null)
            handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    /**
     * Handler处理子线程发送的消息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //网络异常
                    if (isVisible) {
                        showProgressFail(getString(R.string.str_network_error));
                    }
                    break;
                case 3:
                    //提示网络不可用
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_no_network);
                    break;
                case 4:
                    //登录成功
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText("");
                    break;
                case 5:
                    //登录信息缺失
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_set_server_ip);
                    break;
                case 6:
                    //ip不合法
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_server_illegal);
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    break;
                case 7:
                    //ip不合法
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_not_get_server_ip);
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    break;
                case 8:
                    //登录失败
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_login_fail);
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    break;
                case 9:
                    //取消登录
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText(R.string.str_login_cancel);
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    break;
                case 10:
                    //登录成功
                    LoginSuccessMethond();
                    break;
                case 11:
                    //处理sysinfo数据
                    String result = (String) msg.obj;
                    handlerSysinfoData(result);
                    break;
                case 12:
                    //登录失败(超时)
                    disPlayLoginErrorViewLayout.setVisibility(View.VISIBLE);
                    disPlayLoginErrorViewLayout.setText("登录异常");
                    loadingImageViewLayout.setVisibility(View.GONE);
                    loadingImageViewLayout.clearAnimation();
                    break;
            }
        }
    };
}
