package cn.leeffee.feige.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

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
                ex.printStackTrace();
                try {
                    // 将捕获到异常,保存到SD卡中
                    File file = new File(getAppContext().getFilesDir().getPath(), "uspace.log");
                    ex.printStackTrace(new PrintStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // myPid() : 获取当前应用程序的进程id//自己把自己杀死
                // android.os.Process包下
                // Process.killProcess(Process.myPid());
            }
        });
    }

    public static Context getAppContext() {
        return baseApplication;
    }

    public static Resources getAppResources() {
        return baseApplication.getResources();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
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
