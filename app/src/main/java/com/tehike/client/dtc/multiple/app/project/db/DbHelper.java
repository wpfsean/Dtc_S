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
    //当前的 表明
    public static final String TAB_NAME = "AlarmRecordTab";

    //重写构造方法
    private DbHelper(Context context) {
        super(context, DATABASE_NAME,null, 1);
    }
    private DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME,null, 1);
    }

    //创建数据库的方法
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql="create table if not exists "+TAB_NAME+"(" +
                "_id integer primary key," +
                "time text," +
                "senderIp text," +
                "faceVideoId text," +
                "faceVideoName text," +
                "alarmType text," +
                "isHandler text" +
                ")";
        sqLiteDatabase.execSQL(sql);
    }

    //当数据库发生版本更新的方法
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql="drop table "+TAB_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    //单例模式
    private static DbHelper dbHelper;

    public static synchronized DbHelper getInstance(Context context){
        if(dbHelper==null)
            dbHelper=new DbHelper(context);
        return dbHelper;
    }
}