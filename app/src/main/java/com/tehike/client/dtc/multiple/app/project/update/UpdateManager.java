package com.tehike.client.dtc.multiple.app.project.update;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.tehike.client.dtc.multiple.app.project.App;
import com.tehike.client.dtc.multiple.app.project.R;
import com.tehike.client.dtc.multiple.app.project.global.AppConfig;
import com.tehike.client.dtc.multiple.app.project.utils.Logutil;
import com.tehike.client.dtc.multiple.app.project.utils.StringUtils;
import com.tehike.client.dtc.multiple.app.project.utils.SysinfoUtils;
import com.tehike.client.dtc.multiple.app.project.utils.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 描述：手动更新apk
 * ===============================
 *
 * @author wpfse wpfsean@126.com
 * @version V1.0
 * @Create at:2018/11/16 15:09
 */

public class UpdateManager {

    private ProgressBar mProgressBar;
    private Dialog mDownloadDialog;

    private String mSavePath;
    private int mProgress;

    private boolean mIsCancel = false;

    private static final int DOWNLOADING = 1;
    private static final int DOWNLOAD_FINISH = 2;

    UpDateInfo mUpDateInfo;

    private Context mContext;

    public UpdateManager(Context context) {
        mContext = context;
    }


    private Handler mUpdateProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADING:
                    // 设置进度条
                    mProgressBar.setProgress(mProgress);
                    break;
                case DOWNLOAD_FINISH:
                    // 隐藏当前下载对话框
                    mDownloadDialog.dismiss();
                    // 安装 APK 文件
                    installAPK();
                    break;
                case 3:
                    showNoticeDialog();
                    break;
                case 4:
                    ToastUtils.showShort("最新版本不需要更新");
                    break;
            }
        }

        ;
    };

    /*
     * 检测软件是否需要更新
     */
    public void checkUpdate() {

        new Thread(new RequestUpdateInfoThread()).start();
    }

    class RequestUpdateInfoThread extends Thread {
        @Override
        public void run() {
            try {
                //http://19.0.0.229/ckx/update.xml
                String updateUrl = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig.UPDATE_APK_PATH + AppConfig.UPDATE_APK_FILE;
                HttpURLConnection connection = (HttpURLConnection) new URL(updateUrl).openConnection();
                connection.setReadTimeout(4000);
                connection.setConnectTimeout(4000);
                connection.setRequestMethod("GET");
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    String result = StringUtils.readTxt(inputStream);
                    if (TextUtils.isEmpty(result)) {
                        Logutil.e("未请求到更新文件！");
                        return;
                    }
                    try {
                        //封装更新apk的实体类
                        mUpDateInfo = StringUtils.resolveXml(result);

                        Logutil.d(mUpDateInfo.toString());

                        //服务器上版本
                        int serverApkVersion = mUpDateInfo.getVersion();
                        //现有的版本号
                        int nativeApkVersion = AppUtils.getVersionCode(App.getApplication());
                        //判断是否需要更新
                        if (serverApkVersion > nativeApkVersion) {
                            mUpdateProgressHandler.sendEmptyMessage(3);
                        } else {
                            mUpdateProgressHandler.sendEmptyMessage(4);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    Logutil.e("请求响应失败" + connection.getResponseCode());
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 有更新时显示提示对话框
     */
    protected void showNoticeDialog() {


        Logutil.d(AppConfig.ARGEE_OVERLAY_PERMISSION + "///");

        //悬浮窗口权限是否申请
        Builder builder = new Builder(mContext);
        builder.setTitle("提示");

        String desc = "已发布新版本:\n" + "<font color = red>版本描述:" + mUpDateInfo.getDescription() + "</font>";
        builder.setMessage(Html.fromHtml(desc));

        builder.setPositiveButton("更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 隐藏当前对话框
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });

        builder.setNegativeButton("下次再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 隐藏当前对话框
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /*
     * 显示正在下载对话框
     */
    protected void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("下载中");
        View view = LayoutInflater.from(mContext).inflate(R.layout.softupdate_progress, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.update_progress);
        builder.setView(view);

        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 隐藏当前对话框
                dialog.dismiss();
                // 设置下载状态为取消
                mIsCancel = true;
            }
        });

        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        // 下载文件
        downloadAPK();
    }

    /*
     * 开启新线程下载文件
     */
    private void downloadAPK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String sdPath = Environment.getExternalStorageDirectory() + "/";
                        mSavePath = sdPath + AppConfig.SD_DIR + "/" + AppConfig.SOURCES_DIR;

                        File dir = new File(mSavePath);
                        if (!dir.exists())
                            dir.mkdir();

                        String downloadFileUrl = AppConfig.WEB_HOST + SysinfoUtils.getSysinfo().getWebresourceServer() + AppConfig.UPDATE_APK_PATH + mUpDateInfo.getName();
                        // 下载文件
                        HttpURLConnection conn = (HttpURLConnection) new URL(downloadFileUrl).openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        int length = conn.getContentLength();

                        File apkFile = new File(mSavePath, mUpDateInfo.getName());
                        FileOutputStream fos = new FileOutputStream(apkFile);

                        int count = 0;
                        byte[] buffer = new byte[1024];
                        while (!mIsCancel) {
                            int numread = is.read(buffer);
                            count += numread;
                            // 计算进度条的当前位置
                            mProgress = (int) (((float) count / length) * 100);
                            // 更新进度条
                            mUpdateProgressHandler.sendEmptyMessage(DOWNLOADING);

                            // 下载完成
                            if (numread < 0) {
                                mUpdateProgressHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                                break;
                            }
                            fos.write(buffer, 0, numread);
                        }
                        fos.close();
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
     * 下载到本地后执行安装
     */
    protected void installAPK() {
        Logutil.d("Apk下载完成");
        //更新apk所在的路径
        String path = mSavePath + "/" + mUpDateInfo.getName();

        File apkFile = new File(path);
        if (!apkFile.exists()) {
            Logutil.e("Apk不存在");
            return;
        }

        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(App.getApplication(), AppUtils.getPackageName(App.getApplication()) + ".fileprovider", apkFile);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.addCategory(Intent.CATEGORY_DEFAULT);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            App.getApplication().startActivity(install);
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType("application/vnd.android.package-archive");
            intent.setData(Uri.fromFile(apkFile));
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getApplication().startActivity(intent);
        }
    }
}
