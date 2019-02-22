package com.tehike.client.dtc.multiple.app.project.utils;

import com.tehike.client.dtc.multiple.app.project.App;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class DeviceInofUtils {


    public DeviceInofUtils() {
        throw new UnsupportedOperationException("Can't Constructed");
    }


    /**
     * 判断SystemBar是否显示
     * 隐藏状态栏
     */
    public static void hideSystemBar() {
        int temp = App.getSystemManager().ZYSystemBar(2);
        if (temp == 0) {
            App.getSystemManager().ZYSystemBar(0);
        }
    }

    /**
     * 判断SystemBar是否隐藏
     * 显示状态栏
     */
    public static void showSystemBar() {
        int temp = App.getSystemManager().ZYSystemBar(2);
        if (temp == 1) {
            App.getSystemManager().ZYSystemBar(1);
        }
    }

    /**
     * 获取当前系统的亮度
     * -1失败
     */
    public static int getBackLight() {
        int  value = App.getSystemManager().ZYgetBackLight();
        Logutil.i("获取当前系统的亮度--->>状态："+value);
        return value;
    }

    /**
     * 设置当前系统的亮度
     * 0成功
     * -1失败
     */
    public static void setBackLight(int temp) {
       int result = App.getSystemManager().ZYsetBackLight(temp);
       Logutil.i("设置当前系统的亮度--->>状态："+result);
    }

}
