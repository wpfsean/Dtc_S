package com.tehike.client.dtc.multiple.app.project.ui.views;

import android.view.MotionEvent;
import android.view.View;

/**
 * 描述：单击或双击事件处理
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/10/19 15:19
 * @version V1.0
 */

public class OnMultiTouchListener implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private int count = 0;
    private long firClick = 0;
    private long secClick = 0;
    private final int interval = 1000;//间隔时间
    private MultiClickCallback mCallback;
    private ClickCallback mclick;

    public interface MultiClickCallback {
        void onDoubleClick();
    }

    public interface ClickCallback {
        void onClick();
    }

    public OnMultiTouchListener(MultiClickCallback callback, ClickCallback mclick) {
        super();
        this.mCallback = callback;
        this.mclick = mclick;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            count++;
            if (1 == count) {
                firClick = System.currentTimeMillis();
                if (mclick != null) {
                    mclick.onClick();
                }
            } else if (2 == count) {
                secClick = System.currentTimeMillis();
                if (secClick - firClick < interval) {
                    if (mCallback != null) {
                        mCallback.onDoubleClick();
                    }
                    count = 0;
                    firClick = 0;
                } else {
                    firClick = secClick;
                    count = 1;
                }
                secClick = 0;
            }
        }
        return true;
    }
}