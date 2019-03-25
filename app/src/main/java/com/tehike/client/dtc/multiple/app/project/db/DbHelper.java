package com.tehike.client.dtc.multiple.app.project.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 描述：数据库用于记录
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/19 10:20
 */

public class DbHelper extends SQLiteOpenHelper {

    //声明数据库的名称
    private static final String DATABASE_NAME = "tehike.db";

    //用来记录报警信息的表
    public static final String TAB_NAME = "AlarmRecordTab";

    //用来记录事件的表
    public static final String EVENT_TAB_NAME = "EventRecordTab";

    //用来记录电话的表
    public static final String PHONE_CALL_TAB_NAME = "PhoneCallRecordTab";

    //重写构造方法
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    private DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, 1);
    }

    //创建数据库的方法
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //创建记录报警信息的表
        String sql = "create table if not exists " + TAB_NAME + "(" +
                "_id integer primary key," +
                "time text," +
                "senderIp text," +
                "faceVideoId text," +
                "faceVideoName text," +
                "alarmType text," +
                "isHandler text" +
                ")";
        //创建记录事件的表（报警，通话，申请供弹）
        String eventSql = "create table if not exists " + EVENT_TAB_NAME + "(" +
                "_id integer primary key," +
                "time text," +
                "event text" +
                ")";
        //创建电话记录的表
        String phoneCallSql = "create table if not exists " + PHONE_CALL_TAB_NAME + "(" +
                "_id integer primary key," +
                "time text," +
                "phoneStatus text," +
                "phoneFrom text," +
                "phoneTo text" +
                ")";

        //创建事件表
        sqLiteDatabase.execSQL(eventSql);
        //创建报警记录表
        sqLiteDatabase.execSQL(sql);
        //创建电话记录表
        sqLiteDatabase.execSQL(phoneCallSql);
    }

    //当数据库发生版本更新的方法
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql1 = "drop table " + TAB_NAME;
        String sql2 = "drop table " + EVENT_TAB_NAME;
        String sql3 = "drop table " + PHONE_CALL_TAB_NAME;
        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);
        sqLiteDatabase.execSQL(sql3);
        onCreate(sqLiteDatabase);
    }

    //单例模式
    private static DbHelper dbHelper;


    /**
     * 向个提供当前对象的实例
     */
    public static synchronized DbHelper getInstance(Context context) {
        if (dbHelper == null)
            dbHelper = new DbHelper(context);
        return dbHelper;
    }
}