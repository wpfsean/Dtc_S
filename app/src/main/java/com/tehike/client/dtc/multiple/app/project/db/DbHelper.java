package com.tehike.client.dtc.multiple.app.project.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 描述：数据库用于记录
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2019/1/19 10:20
 * @version V1.0
 */

public class DbHelper extends SQLiteOpenHelper {

    //声明数据库的名称
    private static final String DATABASE_NAME="tehike.db";

    //用来记录报警信息的表
    public static final String TAB_NAME = "AlarmRecordTab";

    //用来记录事件有表
    public static final String EVENT_TAB_NAME = "EventRecordTab";

    //重写构造方法
    public DbHelper(Context context) {
        super(context, DATABASE_NAME,null, 1);
    }
    private DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME,null, 1);
    }

    //创建数据库的方法
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //创建记录报警信息的表
        String sql="create table if not exists "+TAB_NAME+"(" +
                "_id integer primary key," +
                "time text," +
                "senderIp text," +
                "faceVideoId text," +
                "faceVideoName text," +
                "alarmType text," +
                "isHandler text" +
                ")";
        //创建记录事件的表（报警，通话，申请供弹）
        String eventSql="create table if not exists "+EVENT_TAB_NAME+"(" +
                "_id integer primary key," +
                "time text," +
                "event text" +
                ")";
        sqLiteDatabase.execSQL(eventSql);
        sqLiteDatabase.execSQL(sql);
    }

    //当数据库发生版本更新的方法
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql1="drop table "+TAB_NAME;
        String sql2="drop table "+EVENT_TAB_NAME;
        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);
        onCreate(sqLiteDatabase);
    }

    //单例模式
    private static DbHelper dbHelper;


    /**
     * 向个提供当前对象的实例
     */
    public static synchronized DbHelper getInstance(Context context){
        if(dbHelper==null)
            dbHelper=new DbHelper(context);
        return dbHelper;
    }
}