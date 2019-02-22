package com.tehike.client.dtc.multiple.app.project.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import com.tehike.client.dtc.multiple.app.project.App;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述：server工具类
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/11/14 17:19
 * @version V1.0
 */

public final class ServiceUtil {

    private ServiceUtil() {
        throw new UnsupportedOperationException("Can't constaucted");
    }

    /**
     * Return all of the services are running.
     *
     * @return all of the services are running
     */
    public static Set getAllRunningServices() {
        ActivityManager am =
                (ActivityManager) App.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return Collections.emptySet();
        List<RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
        Set<String> names = new HashSet<>();
        if (info == null || info.size() == 0) return null;
        for (RunningServiceInfo aInfo : info) {
            names.add(aInfo.service.getClassName());
        }
        return names;
    }

    /**
     * Start the service.
     *
     * @param className The name of class.
     */
    public static void startService(final String className) {
        try {
            startService(Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the service.
     *
     * @param cls The service class.
     */
    public static void startService(final Class<?> cls) {
        Intent intent = new Intent(App.getApplication(), cls);
        App.getApplication().startService(intent);
    }

    /**
     * Stop the service.
     *
     * @param className The name of class.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean stopService(final String className) {
        try {
            return stopService(Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stop the service.
     *
     * @param cls The name of class.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean stopService(final Class<?> cls) {
        Intent intent = new Intent(App.getApplication(), cls);
        return App.getApplication().stopService(intent);
    }


    /**
     * Return whether service is running.
     *
     * @param cls The service class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isServiceRunning(final Class<?> cls) {
        return isServiceRunning(cls.getName());
    }

    /**
     * Return whether service is running.
     *
     * @param className The name of class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isServiceRunning(final String className) {
        ActivityManager am =
                (ActivityManager) App.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
        if (info == null || info.size() == 0) return false;
        for (RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }
}
