package com.tehike.client.dtc.multiple.app.project.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 描述：获取屏幕相关尺寸
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/11/13 15:14
 * @version V1.0
 */

public class ScreenUtils {

    private static ScreenUtils screen;

    //私有化构造方法
    private ScreenUtils() {
    }

    private static int width;
    private static int height;

    //单例模式
    public static ScreenUtils getInstance(Context mContext) {
        if (screen == null) {
            synchronized (ScreenUtils.class) {
                screen = new ScreenUtils();
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics dm = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(dm);
                width = dm.widthPixels;
                height = dm.heightPixels;
            }
        }
        return screen;
    }

    /**
     * 返回屏幕宽度
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * 返回屏幕高度
     * @return
     */
    public int getHeight() {
        return height;
    }

}
