package com.tehike.client.dtc.multiple.app.project.utils;

import android.util.Xml;

import com.tehike.client.dtc.multiple.app.project.update.UpDateInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class StringUtils {

    /**
     * 判断字符串中是否包含中文
     */
    public static boolean isChineseChar(char c) {
        try {
            return String.valueOf(c).getBytes("UTF-8").length > 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 流转可见字符
     */
    public static String readTxt(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }


    /**
     * 解析自更新类文件
     */
    public static UpDateInfo resolveXml(String xml) {
        UpDateInfo upDateInfo = new UpDateInfo();
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
                        if (node.equals("version")) {
                            String version = parser.nextText();
                            upDateInfo.setVersion(Integer.parseInt(version));
                        }
                        if (node.equals("description")) {
                            String desc = parser.nextText();
                            upDateInfo.setDescription(desc);
                        }
                        if (node.equals("file")) {
                            String fileName = parser.nextText();
                            if (fileName.contains("apk")) {
                                upDateInfo.setName(fileName);
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
            Logutil.e("解析异常-->>>" + e.getMessage());
        }
        return upDateInfo;
    }

    /**
     * 读音转换
     */
    public static String voiceConVersion(String str) {
        String newStr = "";
        if (str.length() == 3) {
            newStr = str.substring(0, 1) + "佰" + str.substring(1, 2) + "十" + str.substring(2, 3);
        } else if (str.length() == 2) {
            if (!str.substring(0, 1).equals("1"))
                newStr = str.substring(0, 1) + "十" + str.substring(1, 2);
            else
                newStr = "十" + str.substring(1, 2);
        } else if (str.length() == 4) {
            newStr = str.substring(0, 1) + "仟" + str.substring(1, 2) + "佰" + str.substring(2, 3) + "十" + str.substring(3, 4);
        }else if (str.length() ==1){
            newStr = str;
        }
        return newStr;
    }


}
