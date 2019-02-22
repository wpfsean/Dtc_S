package com.tehike.client.dtc.multiple.app.project.onvif;


import java.io.Serializable;

/**
 * 视频内部信息类
 */

public class MediaProfile implements Serializable {
    //token
    private String token;
    //name
    private String name;

    private VideoEncoderConfiguration videoEncode;
    private AudioEncoderConfiguration audioEncode;

    private PTZConfiguration ptzConfiguration;


    public MediaProfile() {
        this.videoEncode = new VideoEncoderConfiguration();
        this.audioEncode = new AudioEncoderConfiguration();
        this.ptzConfiguration = new PTZConfiguration();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VideoEncoderConfiguration getVideoEncode() {
        return videoEncode;
    }

    public void setVideoEncode(VideoEncoderConfiguration videoEncode) {
        this.videoEncode = videoEncode;
    }

    public AudioEncoderConfiguration getAudioEncode() {
        return audioEncode;
    }

    public void setAudioEncode(AudioEncoderConfiguration audioEncode) {
        this.audioEncode = audioEncode;
    }

    public PTZConfiguration getPtzConfiguration() {
        return ptzConfiguration;
    }

    public void setPtzConfiguration(PTZConfiguration ptzConfiguration) {
        this.ptzConfiguration = ptzConfiguration;
    }

    @Override
    public String toString() {
        return "MediaProfile{" +
                "token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", videoEncode=" + videoEncode +
                ", audioEncode=" + audioEncode +
                +
                        '}';
    }

    //视频编码信息
    public class VideoEncoderConfiguration implements Serializable {
        private String token;
        //编码格式
        private String encoding;
        //分辨率
        private int width;
        private int height;
        //帧率
        private int frameRate;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getFrameRate() {
            return frameRate;
        }

        public void setFrameRate(int frameRate) {
            this.frameRate = frameRate;
        }

        @Override
        public String toString() {
            return "VideoEncoderConfiguration{" +
                    "token='" + token + '\'' +
                    ", encoding='" + encoding + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", frameRate=" + frameRate +
                    '}';
        }
    }

    //音频编码信息
    public class AudioEncoderConfiguration implements Serializable {
        private String token;
        //编码格式
        private String encoding;
        //采样率
        private int sampleRate;
        //比特率
        private int bitrate;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public int getBitrate() {
            return bitrate;
        }

        public void setBitrate(int bitrate) {
            this.bitrate = bitrate;
        }

        @Override
        public String toString() {
            return "AudioEncoderConfiguration{" +
                    "token='" + token + '\'' +
                    ", encoding='" + encoding + '\'' +
                    ", sampleRate=" + sampleRate +
                    ", bitrate=" + bitrate +
                    '}';
        }
    }

    //PTZ 信息
    public class PTZConfiguration implements Serializable {
        //PTZ Token
        private String token;
        //Node Token
        private String nodeToken;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNodeToken() {
            return nodeToken;
        }

        public void setNodeToken(String nodeToken) {
            this.nodeToken = nodeToken;
        }

        @Override
        public String toString() {
            return "PTZConfiguration{" +
                    "token='" + token + '\'' +
                    ", nodeToken='" + nodeToken + '\'' +
                    '}';
        }
    }

}
