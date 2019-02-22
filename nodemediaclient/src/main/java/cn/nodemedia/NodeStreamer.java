package cn.nodemedia;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * NodeStreamer 串流器,可用于将RTSP协议直播流串流为RTMP协议直播流,也可用于将本地文件按原始帧率串流为RTMP协议直播流
 * SDK内部不进行转码,文件仅支持MP4,FLV格式,编码为H.264/AAC
 * 
 * @author ALiang
 * 
 */
public class NodeStreamer {
	static {
		System.loadLibrary("NodeMediaClient");
	}

	public static final String RTSP_TRANSPORT_UDP = "udp";
	public static final String RTSP_TRANSPORT_TCP = "tcp";
	public static final String RTSP_TRANSPORT_UDP_MULTICAST = "udp_multicast";
	public static final String RTSP_TRANSPORT_HTTP = "http";

	private long id = 0;
	private NodeStreamerDelegate mNodeStreamerDelegate;
    private String rtspTransport;

	/**
	 * 创建NodeStreamer对象
	 * 
	 * @param context
	 *            Android Context
	 */
	public NodeStreamer(Context context) {
		this.id = jniInit(context);
	}

    public void setRtspTransport(@NonNull String rtspTransport) {
        this.rtspTransport = rtspTransport;
    }
	/**
	 * 开始以输入源原始帧率进行串流,多用于非直播形式的输入,如文件或点播流地址
	 * 
	 * @param inputUrl
	 *            输入地址,可以为RTMP/RTSP/HTTP等网络流,也可以为本地文件的绝对路径
	 * @param outputUrl
	 *            输出地址,RTMP协议
	 * @return 0 成功,-1 失败
	 */
	public int startNativeRateStreaming(@NonNull String inputUrl, @NonNull String outputUrl) {
		return jniStartStreaming(id, inputUrl, outputUrl, true);
	}

	/**
	 * 开始串流
	 * 
	 * @param inputUrl
	 *            输入地址,可以为RTMP/RTSP/HTTP等网络流,也可以为本地文件的绝对路径
	 * @param outputUrl
	 *            输出地址,RTMP协议
	 * @return
	 */
	public int startStreaming(@NonNull String inputUrl, @NonNull String outputUrl) {
		boolean nativeRate = false;
		if (inputUrl.startsWith("/")) {
			nativeRate = true;
		}
		return jniStartStreaming(id, inputUrl, outputUrl, nativeRate);
	}

	/**
	 * 停止串流
	 * 
	 * @return
	 */
	public int stopStreaming() {
		return jniStopStreaming(id);
	}

	/**
	 * 销毁对象
	 */
	public void deInit() {
		jniDeinit(id);
	}

	/**
	 * 设置事件回调
	 * 
	 * @param delegate NodeStreamerDelegate
	 */
	public void setDelegate(NodeStreamerDelegate delegate) {
		mNodeStreamerDelegate = delegate;
	}

	private void onEvent(int event, String msg) {
		if (mNodeStreamerDelegate != null) {
			mNodeStreamerDelegate.onEventCallback(this, event, msg);
		}
	}


	private native long jniInit(Object ctx);

	private native int jniStartStreaming(long id, String inputUrl, String outputUrl, boolean nativeRate);

	private native int jniStopStreaming(long id);

	private native void jniDeinit(long id);

}
