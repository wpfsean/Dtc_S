package com.tehike.client.dtc.multiple.app.project.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;
import android.text.TextUtils;

import com.tehike.client.dtc.multiple.app.project.global.AppConfig;


/**
 * 文件操作工具包
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FileUtil {

    //sources




    /**
     * 向sd卡写入文件
     *
     * @param content
     * @param fileName
     * @return
     */
    public static boolean writeFile(String content, String fileName) {
        //判断写入内容是否为空
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        //新建一个文件夹
        String path = Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/";
        File newPath = new File(path + AppConfig.SD_DIR + "/" + AppConfig.SOURCES_DIR);
        if (!newPath.exists()){
            newPath.mkdirs();
        }
        //判断路径是否存在
        if (TextUtils.isEmpty(newPath.toString())) {
            return false;
        }
        //新建file
        File file = new File(newPath.toString(), fileName);

        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入sd卡
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            IOUtils.close(fileWriter);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static StringBuilder readFile( String fileName) {
        String path = Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/"+AppConfig.SD_DIR + "/" + AppConfig.SOURCES_DIR;
        File file = new File(path+"/"+fileName);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file),"utf-8");
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            IOUtils.close(reader);
        }
    }
}