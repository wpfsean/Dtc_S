package com.tehike.client.dtc.multiple.app.project.onvif;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import com.tehike.client.dtc.multiple.app.project.entity.VideoBean;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 描述：解析webapi上video资源的Rtsp和ShotPic
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/25 11:56
 * @version V1.0
 */

public class ResolveVideoSourceRtsp extends Thread {

    //鉴权获取meidaService和PtzService
    String getServices = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
            "  <s:Header xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
            "      <wsse:UsernameToken>\n" +
            "        <wsse:Username>%s</wsse:Username>\n" +
            "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>\n" +
            "        <wsse:Nonce>%s</wsse:Nonce>\n" +
            "        <wsu:Created>%s</wsu:Created>\n" +
            "      </wsse:UsernameToken>\n" +
            "    </wsse:Security>\n" +
            "  </s:Header>\n" +
            "  <soap:Body>\n" +
            "    <tds:GetServices>\n" +
            "      <tds:IncludeCapability>false</tds:IncludeCapability>\n" +
            "    </tds:GetServices>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";

    //鉴权获取所有的token参数
    String GET_PROFILES = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "<s:Header>\n" +
            "<wsse:Security xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
            "<wsse:UsernameToken>\n" +
            "<wsse:Username>%s</wsse:Username>\n" +
            "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>\n" +
            "<wsse:Nonce>%s</wsse:Nonce>\n" +
            "<wsu:Created>%s</wsu:Created>\n" +
            "</wsse:UsernameToken>\n" +
            "</wsse:Security>\n" +
            "</s:Header>\n" +
            "<s:Body xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>\n" +
            "</s:Body>\n" +
            "</s:Envelope>";

    //鉴权获取播放的rtsp地址
    String GET_URI = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "<s:Header>\n" +
            "<wsse:Security xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
            "<wsse:UsernameToken>\n" +
            "<wsse:Username>%s</wsse:Username>\n" +
            "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>\n" +
            "<wsse:Nonce>%s</wsse:Nonce>\n" +
            "<wsu:Created>%s</wsu:Created>\n" +
            "</wsse:UsernameToken>\n" +
            "</wsse:Security>\n" +
            "</s:Header>\n" +
            "<s:Body xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "<GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\">\n" +
            "<StreamSetup>\n" +
            "<Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream>\n" +
            "<Transport xmlns=\"http://www.onvif.org/ver10/schema\">\n" +
            "<Protocol>RTSP</Protocol>\n" +
            "</Transport>\n" +
            "</StreamSetup>\n" +
            "<ProfileToken>%s</ProfileToken>\n" +
            "</GetStreamUri>\n" +
            "</s:Body>\n" +
            "</s:Envelope>";

    //鉴权获取截图URL
    String GET_SHOT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
            "  \n" +
            "  <s:Header xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
            "      <wsse:UsernameToken>\n" +
            "        <wsse:Username>%s</wsse:Username>\n" +
            "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>\n" +
            "        <wsse:Nonce>%s</wsse:Nonce>\n" +
            "        <wsu:Created>%s</wsu:Created>\n" +
            "      </wsse:UsernameToken>\n" +
            "    </wsse:Security>\n" +
            "  </s:Header>\n" +
            "\n" +
            "<soap:Body>\n" +
            "    <trt:GetSnapshotUri>\n" +
            "      <trt:ProfileToken>%s</trt:ProfileToken>\n" +
            "    </trt:GetSnapshotUri>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";

    //传递过来 要解析的对象
    VideoBean videoBean;

    //鉴权时必要的参数
    private String mCreated, mNonce, mAuthPwd;

    //回调
    GetRtspCallback listern;

    //构造方法
    public ResolveVideoSourceRtsp(  VideoBean videoBean, GetRtspCallback listern) {
        this.videoBean = videoBean;
        this.listern = listern;
        createAuthString();
    }

    @Override
    public void run() {
        String ip = videoBean.getIpaddress();
        String uName = videoBean.getUsername();
        String uPwd =videoBean.getPassword();
        //判断待处理的数据是否为空
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(uName) || TextUtils.isEmpty(uPwd)) {
            // Log.e("TAG", "Onvif对象的ip为空");
            listern.getDeviceInfoResult("", false, videoBean);
            return;
        }
        //判断当前的待处理的数据的ip是否可以ping通
        boolean isPingConnect = isIpReachable(ip);
        if (!isPingConnect) {
            //  Log.e("TAG", device.getVideoBen().getName()+"\t"+"Onvif对象的ip不能ping通过");
            listern.getDeviceInfoResult("", false, videoBean);
            return;
        }
        try {
            //拼加需要鉴权参数
            String parms = String.format(getServices, videoBean.getUsername(), mAuthPwd, mNonce, mCreated);
            //通过鉴权请求media_service和ptz_service
            String result = postRequest(videoBean.getServiceUrl(), parms);
            //解析mediaService
            resolveMediaUrlByXml(result);
            String media_Service = videoBean.getMediaUrl();

            //判断是否获取到mediaService
            if (TextUtils.isEmpty(media_Service)) {
//                Log.e("TAG", "media_Service为空");
                listern.getDeviceInfoResult("", false, videoBean);
                return;
            }
            //获取profile（带鉴权）
            String getProfileParamater = String.format(GET_PROFILES,
                   videoBean.getUsername(), mAuthPwd, mNonce, mCreated);
            //请求返回的token参数
            String ProfileResult = postRequest(media_Service, getProfileParamater);
            if (TextUtils.isEmpty(ProfileResult)) {
//                Log.e("TAG", "ProfileResult为空，获取token参数失败");
                listern.getDeviceInfoResult("", false, videoBean);
                return;
            }

           videoBean.addProfiles(getMediaProfiles(ProfileResult));

            //通过channel计算当前请求的是哪个通道
            String channel = videoBean.getChannel();
            int position = Integer.parseInt(channel);
            int aisle = -1;
            //判断解析主码流或子码流
            boolean isMainStream = false;
            if (isMainStream) {
                aisle = 2 * position - 2;
            } else {
                aisle = 2 * position - 1;
            }

            //最终计算不需要哪个token（主码流token,子码流token，第三码流token等）
            String token = videoBean.getProfiles().get(aisle).getToken();
            //给对象设置正常的token值
            videoBean.setToken(token);

            //拼加获取rtsp的参数
            String getRtspParamater = String.format(GET_URI, videoBean.getUsername(),
                    mAuthPwd, mNonce, mCreated, token);
            //获取rtsp
            String getRtstResult = postRequest(media_Service, getRtspParamater);
            //判断
            if (TextUtils.isEmpty(getRtstResult)) {
                Log.e("TAG", "getRtstResult为空"+videoBean.getName()+"\t"+videoBean.getChannel() );
                listern.getDeviceInfoResult("", false, videoBean);
                return;
            }
            //解析rtsp
            String rtsp = getRtspByXml(getRtstResult);
            if (TextUtils.isEmpty(rtsp)) {
                Log.e("TAG", "rtsp为空");
                listern.getDeviceInfoResult("", false, videoBean);
                return;
            }
            //拼加用户名和密码
            String newUrl = "";
            if (!TextUtils.isEmpty(rtsp)) {
                if (!rtsp.contains("@")) {
                    String[] flage = rtsp.split("//");
                    String header = flage[0];
                    String footer = flage[1];
                    newUrl = header + "//" + videoBean.getUsername() + ":" + videoBean.getPassword() + "@" + footer;
                    videoBean.setRtsp(newUrl);
                }
            }
            //拼加截图参数
            String getShotParamater = String.format(GET_SHOT, videoBean.getUsername(),
                    mAuthPwd, mNonce, mCreated, token);
            //请求截图的url
            String getShotResult = postRequest(media_Service, getShotParamater);
            if (TextUtils.isEmpty(getShotResult)) {
                Log.e("TAG", "getShotResult为空");
                listern.getDeviceInfoResult("", false, videoBean);
                return;
            }
            //解析
            String shotPic_url = getRtspByXml(getShotResult);
            if (TextUtils.isEmpty(shotPic_url)) {
                listern.getDeviceInfoResult("", false, videoBean);
            }
            videoBean.setShotPicUrl(shotPic_url);

            //最后回调
            listern.getDeviceInfoResult(newUrl, true, videoBean);
        } catch (Exception e) {
            Log.e("TAG", "Onvif解析异常-->>" + e.getMessage());
            Logutil.d("Onvif-->>" + videoBean.getName() + "\t" +videoBean.getIpaddress() + "\t" + videoBean.getChannel());
        }
    }

    /**
     * Ip是否可以ping通
     *
     * @param ip
     * @return
     */
    public static boolean isIpReachable(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr.isReachable(2000)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Post请求
     *
     * @param baseUrl
     * @param params
     * @return
     * @throws Exception
     */
    public static String postRequest(String baseUrl, String params) throws Exception {
        String receive = "";
        // 新建一个URL对象
        URL url = new URL(baseUrl);
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        //设置请求允许输入 默认是true
        urlConn.setDoInput(true);
        // Post请求必须设置允许输出 默认false
        urlConn.setDoOutput(true);
        // 设置为Post请求
        urlConn.setRequestMethod("POST");
        // Post请求不能使用缓存
        urlConn.setUseCaches(false);
        //设置本次连接是否自动处理重定向
        urlConn.setInstanceFollowRedirects(true);
        // 配置请求Content-Type,application/soap+xml
        urlConn.setRequestProperty("Content-Type",
                "application/soap+xml;charset=utf-8");
        // 开始连接
        urlConn.connect();
        // 发送请求数据
        urlConn.getOutputStream().write(params.getBytes());
        // 判断请求是否成功
        if (urlConn.getResponseCode() == 200) {
            // 获取返回的数据
            InputStream is = urlConn.getInputStream();
            byte[] data = new byte[1024];
            int n;
            while ((n = is.read(data)) != -1) {
                receive = receive + new String(data, 0, n);
            }
        } else {
        //    Log.e("TAG", "ResponseCodeError : " + urlConn.getResponseCode());
            return "";
            //throw new Exception("ResponseCodeError : " + urlConn.getResponseCode());
        }
        // 关闭连接
        urlConn.disconnect();
        return receive;
    }

    /**
     * 生成必要的参数
     */
    private void createAuthString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.CHINA);
        mCreated = df.format(new Date());
        mNonce = getNonce();
        mAuthPwd = getPasswordEncode(mNonce, videoBean.getPassword(), mCreated);
    }

    /**
     * 密码加密
     *
     * @param nonce
     * @param password
     * @param date
     * @return
     */
    public String getPasswordEncode(String nonce, String password, String date) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 从官方文档可以知道我们nonce还需要用Base64解码一次
            byte[] b1 = Base64.decode(nonce.getBytes(), Base64.DEFAULT);
            // 生成字符字节流
            byte[] b2 = date.getBytes(); // "2013-09-17T09:13:35Z";
            byte[] b3 = password.getBytes();
            // 根据我们传得值的长度生成流的长度
            byte[] b4 = new byte[b1.length + b2.length + b3.length];
            // 利用sha-1加密字符
            md.update(b1, 0, b1.length);
            md.update(b2, 0, b2.length);
            md.update(b3, 0, b3.length);
            // 生成sha-1加密后的流
            b4 = md.digest();
            // 生成最终的加密字符串
            String result = new String(Base64.encode(b4, Base64.DEFAULT));
            return result.replace("\n", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 随机数据
     *
     * @return
     */
    public String getNonce() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 24; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 解析mediaUrl和ptzUrl
     *
     * @param xml
     * @return
     */
    public String resolveMediaUrlByXml(String xml) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        try {
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT || done) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String node = parser.getName();
                        if (node.equals("Service")) {
                            eventType = parser.next();
                            String url = parser.nextText();
                            if (url.equals("http://www.onvif.org/ver10/media/wsdl")) {
                                eventType = parser.next();
                                String mediaUrl = parser.nextText();
                                if (!TextUtils.isEmpty(mediaUrl))
                                    videoBean.setMediaUrl(mediaUrl);
                            }
                            if (url.equals("http://www.onvif.org/ver20/ptz/wsdl")) {
                                eventType = parser.next();
                                String ptzUrl = parser.nextText();
                                if (!TextUtils.isEmpty(ptzUrl))
                                    videoBean.setPtzUrl(ptzUrl);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取所有的token及视频信息
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public ArrayList<MediaProfile> getMediaProfiles(String xml) throws Exception {
        //初始化XmlPullParser
        XmlPullParser parser = Xml.newPullParser();
        //
        ArrayList<MediaProfile> profiles = new ArrayList<>();
        MediaProfile profile = null;
        //tag 用来判断当前解析Video还是Audio
        String tag = "";
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        parser.setInput(input, "UTF-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    //serviceUrl
                    if (parser.getName().equals("Profiles")) {
                        profile = new MediaProfile();
                        //获取token
                        profile.setToken(parser.getAttributeValue(1));
                        parser.next();
                        //获取name
                        if (parser.getName() != null && parser.getName().equals("Name")) {
                            profile.setName(parser.nextText());
                        }
                    } else if (parser.getName().equals("VideoEncoderConfiguration") && profile != null) {
                        //获取VideoEncode Token
                        int count = parser.getAttributeCount();
                        if (count == 2) {
                            profile.getVideoEncode().setToken(parser.getAttributeValue(1));
                        } else if (count == 1) {
                            profile.getVideoEncode().setToken(parser.getAttributeValue(0));
                        }

                        tag = "Video";
                    } else if (parser.getName().equals("AudioEncoderConfiguration") && profile != null) {
                        //获取AudioEncode Token
                        int count = parser.getAttributeCount();
                        if (count == 2) {
                            profile.getAudioEncode().setToken(parser.getAttributeValue(1));
                        } else if (count == 1) {
                            profile.getAudioEncode().setToken(parser.getAttributeValue(0));
                        }

                        tag = "Audio";
                    } else if (parser.getName().equals("Width") && profile != null) {
                        //分辨率宽
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setWidth(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("Height") && profile != null) {
                        //分辨率高
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setHeight(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("FrameRateLimit") && profile != null) {
                        //帧率
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setFrameRate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("Encoding") && profile != null) {
                        //编码格式
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setEncoding(text);
                        } else if (tag.equals("Audio")) {
                            profile.getAudioEncode().setEncoding(text);
                        }
                    } else if (parser.getName().equals("Bitrate") && profile != null) {
                        //Bitrate
                        String text = parser.nextText();
                        if (tag.equals("Audio")) {
                            profile.getAudioEncode().setBitrate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("SampleRate") && profile != null) {
                        //SampleRate
                        String text = parser.nextText();
                        if (tag.equals("Audio")) {
                            profile.getAudioEncode().setSampleRate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("PTZConfiguration") && profile != null) {
                        //获取VideoEncode Token
                        profile.getPtzConfiguration().setToken(parser.getAttributeValue(0));
                        tag = "Ptz";
                    } else if (parser.getName().equals("NodeToken") && profile != null) {
                        //NodeToken
                        String text = parser.nextText();
                        if (tag.equals("Ptz")) {
                            profile.getPtzConfiguration().setNodeToken(text);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("Profiles")) {
                        profiles.add(profile);
                    }
                    if (parser.getName().equals("AudioEncoderConfiguration")
                            || parser.getName().equals("VideoEncoderConfiguration") || parser.getName().equals("PTZConfiguration")) {
                        tag = "";
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }

        return profiles;
    }

    /**
     * 解析rtsp地址
     *
     * @param xml
     * @return
     */
    public String getRtspByXml(String xml) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        try {
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT || done) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("Uri")) {
                            eventType = parser.next();
                            return parser.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:

                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 回调
     */
    public interface GetRtspCallback {
        void getDeviceInfoResult(String rtsp, boolean isSuccess, VideoBean videoBean);
    }


}
