package cn.leeffee.feige.utils;

import android.util.Log;

public class LogUtil {

    public static boolean isShowLog = true;//true:打印log，false:不打印log

    public static void e(String message) {
        if (!isShowLog)
            return;
        Log.e("USpace", message);
    }

    public static void d(String message) {
        if (!isShowLog)
            return;
        Log.d("USpace", message);
    }

    public static void i(String message) {
        if (!isShowLog)
            return;
        Log.i("USpace", message);
    }
}
