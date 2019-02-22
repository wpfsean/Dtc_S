package com.tehike.client.dtc.multiple.app.project.utils;
/**
 * 描述：防止重复点击 事件间隔，在这里我定义的是1000毫秒
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/19 12:59
 * @version V1.0
 */

public class CalcUtils {

    private static long lastTime;

    public static boolean isFastDoubleClick() {
        long currentTime = System.currentTimeMillis();
        long timeD = currentTime - lastTime;
        if (timeD >= 0 && timeD <= 1000) {
            return true;
        } else {
            lastTime = currentTime;
            return false;
        }
    }
}
