package com.tehike.client.dtc.multiple.app.project.utils;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


import com.tehike.client.dtc.multiple.app.project.global.AppConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志写入
 * <p>
 * <p>
 * Log日志写入文件
 */
public class HbLogToFile {
    /**
     * 开发阶段(打印日志 )
     */
    private static final int DEVELOP = 0;
    /**
     * 公开测试(把日志写入文件 )
     */
    private static final int BATE = 2;
    /**
     * 正式版(不打印日志 )
     */
    private static final int RELEASE = 3;

    /**
     * 当前阶段标示
     */
    private static int currentStage = BATE;
    private static String path;
    private static File file;
    private static FileOutputStream outputStream;
    private static String pattern = "yyyy-MM-dd HH:mm:ss";

    static {
        if (isSDcardExist()) {
            if (getSDFreeSize() > 1) {
                File externalStorageDirectory = Environment
                        .getExternalStorageDirectory();
                path = externalStorageDirectory.getAbsolutePath()
                        + "/" + AppConfig.SD_DIR + "/";
                File directory = new File(path);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                file = new File(new File(path), "HbLog.txt");
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                } catch (Exception e) {
                    Log.w("TAG", "异常：" + e.getMessage());
                }
                try {
                    outputStream = new FileOutputStream(file, true);
                } catch (FileNotFoundException e) {
                }
            } else {
                Log.w("TAG", "sd存储不足");
            }
        } else {
            Log.w("TAG", "sd卡不存在");
        }
    }

    public static void info(String msg) {
        info(HbLogToFile.class, msg);
    }

    public static void info(Class clazz, String msg) {
        switch (currentStage) {
            case DEVELOP:
                Log.i(clazz.getSimpleName(), msg);
                break;
            case BATE:
                SimpleDateFormat df = new SimpleDateFormat(pattern);// 设置日期格式
                String time = df.format(new Date());
                if (isSDcardExist()) {
                    if (outputStream != null && getSDFreeSize() > 1) {
                        try {
                            outputStream.write(time.getBytes());
                            String className = "";
                            if (clazz != null) {
                                className = clazz.getSimpleName();
                            }
//                            outputStream.write(("    " + className + "\r\n")
//                                    .getBytes());
                            outputStream.write(msg.getBytes());
                            outputStream.write("\r\n".getBytes());
                            outputStream.flush();
                        } catch (IOException e) {

                        }
                    } else {
                        Log.i("SDCAEDTAG",
                                "file is null or storage insufficient");
                    }
                }
                break;
            case RELEASE:
                // 一般不做日志记录
                break;
        }
    }

    public static boolean isSDcardExist() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {//
            return true;
        } else {
            return false;
        }
    }

    public static long getSDFreeSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long freeBlocks = sf.getAvailableBlocks();
        return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
    }
}
