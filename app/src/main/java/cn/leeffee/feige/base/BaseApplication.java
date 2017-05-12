package cn.leeffee.feige.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.View;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import cn.leeffee.feige.utils.DateUtil;
import cn.leeffee.feige.utils.LogUtil;

/**
 * APPLICATION
 */
public class BaseApplication extends Application {

    private static BaseApplication baseApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                LogUtil.e("BaseApplication uncaughtException");
                ex.printStackTrace();
                try {
                    // 将捕获到异常,保存到SD卡中
                    File file = new File(getAppContext().getFilesDir().getPath(), "uspace.log");
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                    writer.newLine();
                    writer.write(DateUtil.getCurrentTime(DateUtil.DATE_YYYY_MM_DD_HH_MM_SS) + ":================================================");
                    writer.close();
                    ex.printStackTrace(new PrintStream(new FileOutputStream(file, true)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // myPid() : 获取当前应用程序的进程id//自己把自己杀死
                // android.os.Process包下
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    public static Context getAppContext() {
        return baseApplication;
    }

    public static Resources getAppResources() {
        return baseApplication.getResources();
    }
    /**
     * 根据资源id加载布局文件
     */
    public static View inflate(int resId) {
        return View.inflate(baseApplication, resId, null);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    /**
     * 获取app版本号
     * @return
     */
    public static int getAppVersion() {
        try {
            PackageInfo info = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
    //    /**
    //     * 分包
    //     * @param base
    //     */
    //    @Override
    //    protected void attachBaseContext(Context base) {
    //        super.attachBaseContext(base);
    //        MultiDex.install(this);
    //    }

}
