package com.tehike.client.dtc.multiple.app.project.services;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 描述：定时获取 cpu和内存信息
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2019/2/14 9:52
 */

public class CpuAndRamUtils implements Runnable {
    /**
     * 单例对象
     */
    private volatile static CpuAndRamUtils instance = null;

    /**
     * 定时线程服务
     */
    private ScheduledExecutorService timingScheduledExecutorService;

    /**
     * Activity管理
     */
    private ActivityManager activityManager;

    /**
     * 定时服务执行时间隔时长
     */
    private long freq;

    /**
     * cpu变化时的时间
     */
    private Long lastCpuTime;

    /**
     * cpu变化时的时间
     */
    private Long lastAppCpuTime;

    /**
     * 实例支持读取和写入
     */
    private RandomAccessFile procStatFile;

    /**
     * 实例支持读取和写入
     */
    private RandomAccessFile appStatFile;

    /**
     * 私有构造函数
     */
    private CpuAndRamUtils() {
        timingScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 向外提供此类单例对象
     */
    public static CpuAndRamUtils getInstance() {
        if (instance == null) {
            synchronized (CpuAndRamUtils.class) {
                if (instance == null) {
                    instance = new CpuAndRamUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context, long freq) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.freq = freq;
    }

    /**
     * 执行子线程
     */
    public void start() {
        timingScheduledExecutorService.scheduleWithFixedDelay(this, 0L, freq, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        double cpu = getCpuInfo();
        //double保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        double androidCpu = Double.parseDouble(df.format(cpu));

        List<String> deviceTempInfoList = getThermalInfo();
        String cpuTemp = deviceTempInfoList.get(0);
        String gpuTemp = deviceTempInfoList.get(1);

        Intent intent = new Intent();
        intent.setAction(AppConfig.CPU_AND_ROM_ACTION);
        intent.putExtra("cpu", androidCpu);
        intent.putExtra("cpuTemp", cpuTemp);
        intent.putExtra("gpuTemp", gpuTemp);
        App.getApplication().sendBroadcast(intent);

    }

    /**
     * 获取当前设备温度的集合
     */
    public static List<String> getThermalInfo() {
        List<String> result = new ArrayList<>();
        BufferedReader br = null;

        try {
            File dir = new File("/sys/class/thermal/");

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (Pattern.matches("thermal_zone[0-9]+", file.getName())) {
                        return true;
                    }
                    return false;
                }
            });

            final int SIZE = files.length;
            String line = null;
            String type = null;
            String temp = null;
            for (int i = 0; i < SIZE; i++) {
                br = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone" + i + "/type"));
                line = br.readLine();
                if (line != null) {
                    type = line;
                }

                br = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone" + i + "/temp"));
                line = br.readLine();
                if (line != null) {
                    long temperature = Long.parseLong(line);
                    if (temperature < 0) {
                        temp = "Unknow";
                    } else {
                        temp = (float) (temperature / 1000.0) + "°C";
                    }

                }

                result.add(type + " : " + temp);
            }

            br.close();
        } catch (FileNotFoundException e) {
            result.add(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * 获取Cpu信息
     */
    private double getCpuInfo() {
        long cpuTime;
        long appTime;
        double sampleValue = 0.0D;
        try {
            if (procStatFile == null || appStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
                appStatFile = new RandomAccessFile("/proc/" + Process.myPid() + "/stat", "r");
            } else {
                procStatFile.seek(0L);
                appStatFile.seek(0L);
            }
            String procStatString = procStatFile.readLine();
            String appStatString = appStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            String appStats[] = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (lastCpuTime == null && lastAppCpuTime == null) {
                lastCpuTime = cpuTime;
                lastAppCpuTime = appTime;
                return sampleValue;
            }
            sampleValue = ((double) (appTime - lastAppCpuTime) / (double) (cpuTime - lastCpuTime)) * 100D;
            lastCpuTime = cpuTime;
            lastAppCpuTime = appTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sampleValue;
    }

    /**
     * 获取内存信息
     */
    public long[] getRomMemroy() {
        long[] romInfo = new long[2];
        //Total rom memory
        romInfo[0] = getTotalInternalMemorySize();

        //Available rom memory
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        romInfo[1] = blockSize * availableBlocks;
        return romInfo;
    }

    /**
     * 获取内存总大小
     */
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

}
