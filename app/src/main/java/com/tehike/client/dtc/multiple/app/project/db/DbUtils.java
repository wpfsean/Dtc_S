package com.tehike.client.dtc.multiple.app.project.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 描述：数据库封闭
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/1/19 10:24
 */
public class DbUtils {
    //声明SQLite数据库
    SQLiteDatabase db;

    //创建构造方法并实例化SQLiteDatabase对象
    public DbUtils(Context context) {
        db = DbHelper.getInstance(context).getReadableDatabase();
    }

    //调用SQLiteDatabase对象的 添加 方法进行数据添加，返回值为添加数据行的ID
    public long insert(String tableName, ContentValues values) {
        long id = db.insert(tableName, null, values);
        return id;
    }

    //调用SQLiteDatabase对象的 修改 方法进行数据添加，返回值为修改数据行的ID
    public int update(String tableName, ContentValues values, String where, String[] whereArgs) {
        int i = db.update(tableName, values, where, whereArgs);
        return i;
    }

    //调用SQLiteDatabase对象的 删除 方法进行数据添加，返回值为删除行
    public int delete(String tableName, String where, String[] whereArgs) {
        int i = db.delete(tableName, where, whereArgs);
        return i;
    }

    //调用SQLiteDatabase对象的 查询 方法进行数据添加，返回值为游标
    public Cursor query(String tableName, String[] columns, String selection, String[] selectArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cursor = db.query(tableName, columns, selection, selectArgs, groupBy, having, orderBy, limit);
        return cursor;
    }
}
