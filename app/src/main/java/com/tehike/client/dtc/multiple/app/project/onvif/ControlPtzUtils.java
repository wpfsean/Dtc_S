package com.tehike.client.dtc.multiple.app.project.onvif;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Root on 2018/5/19.
 *
 * 去于云台控制中心
 * parms:
 * ptz_url
 * token
 */

public class ControlPtzUtils implements Runnable {

    //移动指令
    public static final String PTZ_MOVE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header/><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>%s</ProfileToken><Velocity><PanTilt x=\"%s\" y=\"%s\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove></s:Body></s:Envelope>";
    //缩放指令
    public static final String PTZ_ZOOM = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header/><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><ContinuousMove xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>%s</ProfileToken><Velocity><Zoom x=\"%s\" space=\"http://www.onvif.org/ver10/tptz/ZoomSpaces/VelocityGenericSpace\" xmlns=\"http://www.onvif.org/ver10/schema\"/></Velocity></ContinuousMove></s:Body></s:Envelope>";
    //停止指令
    public static final String PTZ_STOP = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header/><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><Stop xmlns=\"http://www.onvif.org/ver20/ptz/wsdl\"><ProfileToken>%s</ProfileToken><PanTilt>%s</PanTilt><Zoom>%s</Zoom></Stop></s:Body></s:Envelope>";
    HttpURLConnection mUrlConn;
    String ptz_url;
    String token;
    String flage;
    double x;
    double y;

    public ControlPtzUtils(String ptz_url, String token, String flage, double x, double y) {
        this.ptz_url = ptz_url;
        this.token = token;
        this.flage = flage;
        this.x = x;
        this.y = y;
    }

    @Override
    public void run() {
        synchronized (this) {//同步锁，防止同时发送指令
            switch (flage) {
                case "left":
                    moveCamera();
                    break;
                case "right":
                    moveCamera();
                    break;

                case "top":
                    moveCamera();
                    break;
                case "below":
                    moveCamera();
                    break;
                case "zoom_b":
                    zoomCamera();
                    break;
                case "zoom_s":
                    zoomCamera();
                    break;
                case "stop":
                    stopCamera();
                    break;

            }
        }
    }

    //
    public void start() {
        new Thread(this).start();
    }

    /**
     * 移动摄像头
     */
    public void moveCamera() {
        try {
            String content = String.format(PTZ_MOVE, token, x, y);
            String result = postRequest(ptz_url, content);
            System.out.println(result);
        } catch (Exception e) {
        	System.err.println("error:"+e.getMessage());
        }
    }

    /**
     * 缩放摄像头
     */
    public void zoomCamera() {
        try {
            String content = String.format(PTZ_ZOOM, token, x);
            String result = postRequest(ptz_url, content);
            System.out.println(result);
        } catch (Exception e) {
        	System.err.println("error:"+e.getMessage());
        }
    }

    /**
     * 停止摄像头动作
     */
    public void stopCamera() {
        try {
            String content = String.format(PTZ_STOP, token, true, true);
            String result = postRequest(ptz_url, content);
            System.out.println(result);
        } catch (Exception e) {
        	System.err.println("error:"+e.getMessage());
        }
    }


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
	            throw new Exception("ResponseCodeError : " + urlConn.getResponseCode());
	        }
	        // 关闭连接
	        urlConn.disconnect();
	        return receive;
	    }

}
