package com.tehike.client.dtc.multiple.app.project.utils;

import android.os.Environment;
import android.util.Log;

import com.tehike.client.dtc.multiple.app.project.global.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述：$desc$
 * ===============================
 *
 * @author $user$ wpfsean@126.com
 * @version V1.0
 * @Create at:$date$ $time$
 */

public class RecordLog {

    public static void wirteLog(String content) {

        //外部目录
        File externalStorageDirectory = Environment
                .getExternalStorageDirectory();
        //文件存放路径
        String path = externalStorageDirectory.getAbsolutePath()
                + "/" + AppConfig.SD_DIR + "/AlarmLog";
        File directory = new File(path);
        //判断路径是否存在
        if (!directory.exists()) {
            directory.mkdirs();
        }
        //根据日期生成文件名
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = simpleDateFormat.format(date);
        File file = new File(new File(path), fileName + "-Log.txt");

        //判断当前的日志文件是否存在
        try {
            if (!file.exists()) {
                file.createNewFile();
                Logutil.e("File创建成功");
            }
            //记录日志生成的时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            String time = df.format(new Date());
            //追加模式
            FileOutputStream    outputStream = new FileOutputStream(file, true);
            //写入日志
            outputStream.write((time+"\t").getBytes());
            outputStream.write(content.getBytes());
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
            outputStream.close();
            Logutil.d("报警日志写入成功");
        } catch (Exception e) {
            Log.w("TAG", "异常：" + e.getMessage());
        }
    }
}
