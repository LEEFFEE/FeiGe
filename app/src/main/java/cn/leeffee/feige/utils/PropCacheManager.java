package cn.leeffee.feige.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.db.DBTool;

public class PropCacheManager {

    private static Map<String, String> hm = null;

    private static PropCacheManager instance = null;

    private String autoBackup = "autoBackup";

    private String wifi = "wifi";

    private String cacheDir = "cacheDir";

    public final static String WIFI_ON = "on";
    public final static String WIFI_OFF = "off";

    public final static String BACKUP_ON = "on";
    public final static String BACKUP_OFF = "off";

    private PropCacheManager() {
        hm = new HashMap<>();
    }

    public static synchronized PropCacheManager getInstance() {
        if (instance == null) {
            instance = new PropCacheManager();
        }
        return instance;
    }

    public synchronized Object get(String key) {
        if (hm.containsKey(key)) {
            return hm.get(key);
        }
        return null;
    }

    public String getCacheDir(String account, Context ctx) {
        String val = "";
        String key = cacheDir + "-" + account;
        if (hm.containsKey(key)) {
            val = hm.get(key);
        } else {
            DBTool dbTool = new DBTool(ctx);
            val = dbTool.queryCacheDir(account);
            hm.put(key, val);
        }
        return val;
    }

    public void setCacheDir(String dir, String account, Context ctx) {
        DBTool dbTool = new DBTool(ctx);
        dbTool.saveCacheDir(dir, account);
        String key = cacheDir + "-" + account;
        if (hm.containsKey(key)) {
            hm.remove(key);
        }
        hm.put(key, dir);
    }

    public synchronized boolean isBackupOn(Context ctx) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String account = mPreferences.getString(AppConfig.ACCOUNT, null);
        String val = "";
        String key = autoBackup + "-" + account;
        if (hm.containsKey(key)) {
            val = hm.get(key);
        } else {
            DBTool dbTool = new DBTool(ctx);
            val = dbTool.queryCacheBackupSetting(account);
            hm.put(key, val);
        }
        if (val.equals(BACKUP_ON)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void setBackupStatus(String val, Context ctx) {
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        DBTool dbTool = new DBTool(ctx);
        dbTool.saveCacheBackupSetting(val, account);
        String key = autoBackup + "-" + account;
        if (hm.containsKey(key)) {
            hm.remove(key);
        }
        hm.put(key, val);
    }

    public synchronized boolean isWifiOn(Context ctx) {
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        String val = "";
        String key = wifi + "-" + account;
        if (hm.containsKey(key)) {
            val = hm.get(key);
        } else {
            DBTool dbTool = new DBTool(ctx);
            val = dbTool.queryCacheWifiSetting(account);
        }
        return val.equals(WIFI_ON);
    }

    public synchronized void setWifiStatus(String val, Context ctx) {
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        DBTool dbTool = new DBTool(ctx);
        dbTool.saveCacheWifiSetting(val, account);
        String key = wifi + "-" + account;
        if (hm.containsKey(key)) {
            hm.remove(key);
        }
        hm.put(key, val);
    }
}
