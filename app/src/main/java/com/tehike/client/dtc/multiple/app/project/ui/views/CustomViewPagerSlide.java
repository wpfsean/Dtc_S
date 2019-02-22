package com.tehike.client.dtc.multiple.app.project.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 描述：$desc$ 自定义ViewPager是否能滑动
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class CustomViewPagerSlide extends ViewPager {

    //是否滑动标识
    private boolean isCanSlide = true;

    public CustomViewPagerSlide(Context context) {
        super(context);
    }

    public CustomViewPagerSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置是否可以滑动
     */
    public void setScanScroll(boolean isCanSlide) {
        this.isCanSlide = isCanSlide;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isCanSlide && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isCanSlide && super.onTouchEvent(ev);
    }
}