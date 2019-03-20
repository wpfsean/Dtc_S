package com.tehike.client.dtc.multiple.app.project.global;


/**
 * Created by Root on 2018/8/5.
 */

public class AppConfig {

    public AppConfig() {
        throw new UnsupportedOperationException("不能被实例化");
    }


    public static boolean IS_CALLING = false;

    /**
     * 会议号码
     */
    public static String DUTY_NUMBER = "0000000000";

    /**
     * 用来提交本机信息的Url
     */
    public static String COMMIT_NATIVE_INFO_PATH = "http://19.0.0.20/RecordTheNumForData/RecordLog.php?paramater=";


//    /**
//     * 本机获取到的所有的哨位点
//     */
//    public static String ALL_SENTINEL_POINT = "http://19.0.0.229:8080/webapi/locations";

    /**
     * webApi接口的host
     */
    public static String WEB_HOST = "http://";

    /**
     * webapi获取sysinfo
     */
    public static String _SYSINFO = ":8080/webapi/sysinfo";

    /**
     * 根据设备guid关联视频
     */
    public static String _LINKED_VIDEO = ":8080/webapi/sentryvideos?guid=";


    /**
     * 获取服务器时间的URl
     */
    public static String SERVER_TIME = ":8080/webapi/ntp";

    /**
     * 背景地图
     */
    public static String BACKGROUP_MAP_URL = ":8080/images/Beijing256-2.JPG";

    /**
     * webapi获取支持的设备类型
     */
    public static String _SUPPORT_DEVICE_TYPE = ":8080/webapi/devicetypes";

    /**
     * webapi获取弹箱信息
     */
    public static String _BOX_DEVICES = ":8080/webapi/onlinedevices";


    /**
     * webapi获取sip分组
     */
    public static String _USIPGROUPS = ":8080/webapi/sipgroups";

    /**
     * webapi根据sip组id获取当前组数据
     */
    public static String _USIPGROUPS_GROUP = ":8080/webapi/sips?groupid=";

    /**
     * webapi广播
     * 示例（http://19.0.0.229:8080/webapi/siplisten?target=1008,1016,1001&moderator=1002）
     */
    public static String _BROADCAST_URL = ":8080/webapi/sipbroadcast?target=";

    /**
     * webapi监听
     */
    public static String _LISTEN_URL = ":8080/webapi/siplisten?target=";

    /**
     * webapi会议
     */
    public static String _MEETING_URL = ":8080/webapi/sipmeeting?target=";

    /**
     * webapi强拆
     */
    public static String _RELEASE_URL = ":8080/webapi/siprelease?number=";

    /**
     * webapi获取所有的视频组
     */
    public static String _VIDEO_GROUP = ":8080/webapi/videogroups";

    /**
     * webapi根据组Id获取某个组内数据
     */
    public static String _VIDEO_GROUP_ITEM = ":8080/webapi/videos?groupid=";

    /**
     * webapi获取当前sip用户状态
     */
    public static String _SIS_STATUS = ":8080/webapi/sipstatus";

    /**
     * 报警颜色入类型对应表
     */
    public static String _ALARM_COLOR = ":8080/webapi/alertdefines";

    /**
     * 获取webapi上全部的video数据
     */
    public static String _WEBAPI_VIDEO_SOURCE = ":8080/webapi/videos?groupid=0";

    /**
     * 获取webapi上全部的Sip数据
     */
    public static String _WEBAPI_SIP_SOURCE = ":8080/webapi/sips?groupid=0";


    /**
     * Sip强拆的URl(中断别人通话)
     */
    public static String _SIP_RELEASE = ":8080/webapi/siprelease?number=";

    /**
     * 屏保计时
     */
    public static int SCREEN_SAVE_TIME = 30;

    /**
     * 更新apk的路径(远程服务器文件夹名)
     */
    public static String UPDATE_APK_PATH = ":8080/Dtc/";

    /**
     * 更新apk的路径
     */
    public static String UPDATE_APK_FILE = "update.xml";

    /**
     * 当前登录的用户名
     */
    public static String _UNAME = "";

    /**
     * 当前登录的密码
     */
    public static String _UPWD = "";

    /**
     * 中心服務器的IP地址
     */
    public static String _USERVER = "";

    /**
     * 悬浮窗口权限是否申请成功
     */
    public static boolean ARGEE_OVERLAY_PERMISSION = false;

    /**
     * dns(第二個默認的Dns)
     */
    public static String DNS = "119.29.29.29";

    /**
     * 经纬度
     */
    public static double LOCATION_LAT = 0;

    public static double LOCATION_LOG = 0;

    /**
     * 本机Cpu信息
     */
    public static double DEVICE_CPU = 0;

    /**
     * 本机的Rom信息
     */
    public static double DEVICE_RAM = 0;

    /**
     * 上位机监听端口
     */
    public static int S_PORT = 32321;

    /**
     * 下位机的监听端口
     */
    public static int X_PORT = 32323;

    /**
     * SIp是否注册成功
     */
    public static boolean SIP_STATUS = false;

    /**
     * SD父目录
     */
    public static String SD_DIR = "tehike";

    /**
     * 存放资源的目录
     */
    public static String SOURCES_DIR = "sources";

    /**
     * 视频资源的文件名
     */
    public static String SOURCES_VIDEO = "videoResource.bin";

    /**
     * Sip资源的文件名
     */
    public static String SOURCES_SIP = "sipResource.bin";

    /**
     * Sip资源的文件名
     */
    public static String SYSINFO = "sysinfo.bin";

    /**
     * 报警类型颜色对象
     */
    public static String ALARM_COLOR = "alarmColor.bin";

    /**
     * 主页面是否可以滑动
     */
    public static boolean IS_CAN_SLIDE = false;


    /**
     * 是否播放声音
     */
    public static boolean ISVIDEOSOUNDS = false;

    /**
     * 每隔15分钟去加载刷新一下数据
     */
    public static int REFRESH_DATA_TIME = 15 * 60 * 1000;

    /**
     * 远程 喊话
     */
    public static int REMOTE_PORT = 18720;

    /**
     * 单向广播时向喊话端发送的端口
     */
    public static int BROCAST_PORT = 8899;


    /**
     * 定时刷新网络状态广播的Action
     */
    public static String REFRESH_NETWORK_ACTION = "TimingRefrehshNetworkStatus";

    /**
     * 来电广播Action
     */
    public static String INCOMING_CALL_ACTION = "IncomingCall";

    /**
     * 刷新cpu和rom使用率Action
     */
    public static String CPU_AND_ROM_ACTION = "CpuAndRom";

    /**
     * 接收报警的Action
     */
    public static String ALARM_ACTION = "receiveAlarm";

    /**
     * 接收申请开启子弹箱的Action
     */
    public static String BOX_ACTION = "receivebox";


    /**
     * video资源解析完成广播的Action
     */
    public static String RESOLVE_VIDEO_DONE_ACTION = "refreshVideoData";


    /**
     * 接收屏保通知的广播的Action
     */
    public static String SCREEN_SAVER_ACTION = "receiveScreenSaverAction";

    /**
     * 接收屏保取消通知的广播的Action
     */
    public static String CANCEL_SCREEN_SAVER_ACTION = "receiveCancelScreenSaverAction";


}
