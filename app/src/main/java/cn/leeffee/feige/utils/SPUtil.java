package cn.leeffee.feige.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cn.leeffee.feige.App;


/**
 * Created by lhfei on 2017/3/28.
 */

public class SPUtil {
    private static SharedPreferences sp;
    private static SPUtil spUtil;

    private SPUtil() {
    }

    static {
        sp = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    /**
     * 放入字符串
     *
     * @param key
     * @param value
     * @return 修改成功则返回true
     */
    public static void putString(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    /**
     * 取出字符串 没有找到对应的键则返回"";
     *
     * @param key
     * @return 返回键指定键所对应的值， 没有找到对应的键则返回空串;
     */
    public static String getString(String key) {
        return sp.getString(key, "");
    }

    /**
     * 放入boolean值
     *
     * @param key
     * @param value
     * @return 修改成功则返回true
     */
    public static boolean putBoolean(String key, boolean value) {
        return sp.edit().putBoolean(key, value).commit();
    }

    /**
     * 取出boolean的值   没有找到对应的键则返回false
     *
     * @param key
     * @return 返回键指定键所对应的值， 没有找到对应的键则返回false
     */
    public static boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    /**
     * 放入整型值
     *
     * @param key
     * @param value
     * @return 修改成功则返回true
     */
    public static boolean putInt(String key, int value) {
        return sp.edit().putInt(key, value).commit();
    }

    /**
     * 取出整型值，没有找到对应的键则返回0
     *
     * @param key
     * @return 返回键指定键所对应的值， 没有找到对应的键则返回0
     */
    public static int getInt(String key) {
        return sp.getInt(key, 0);
    }

    /**
     * 清空sharePreferences
     */
    public static void clear() {
        sp.edit().clear().commit();
    }
}
