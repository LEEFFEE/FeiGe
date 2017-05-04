package cn.leeffee.feige.utils;

import android.util.Log;

public class LogUtil {

    public static boolean isShowLog = true;//true:打印log，false:不打印log
    public static final String TAG="FeiGe";
    public static void e(String message) {
        if (!isShowLog)
            return;
        Log.e(TAG, message);
    }

    public static void d(String message) {
        if (!isShowLog)
            return;
        Log.d(TAG, message);
    }

    public static void i(String message) {
        if (!isShowLog)
            return;
        Log.i(TAG, message);
    }
}
