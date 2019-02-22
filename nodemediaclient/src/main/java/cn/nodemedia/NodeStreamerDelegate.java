package cn.nodemedia;

public interface NodeStreamerDelegate {
	void onEventCallback(NodeStreamer streamer, int event, String msg);
}
