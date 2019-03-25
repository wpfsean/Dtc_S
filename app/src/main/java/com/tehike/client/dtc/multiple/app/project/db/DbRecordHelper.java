package com.tehike.client.dtc.multiple.app.project.db;

import android.content.ContentValues;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.utils.TimeUtils;

/**
 * 描述：数据库记录帮助类
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/3/25 9:04
 */

public class DbRecordHelper {

    /**
     * 记录电话记录
     * @param status 电话状态（来电，去电）
     * @param from 电话来源
     * @param to  电话打向哪
     */
    public static void phoneCallRecordInsert(String status, String from, String to) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", TimeUtils.getCurrentTime());
        contentValues.put("phoneStatus", status);
        contentValues.put("phoneFrom", from);
        contentValues.put("phoneTo", to);
        new DbUtils(App.getApplication()).insert(DbHelper.PHONE_CALL_TAB_NAME, contentValues);
    }
}
