/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * �ļ���      Configuration.java
 * ����:     Jacky Wang
 * �������ڣ� 2011-9-1 ����12:55:56
 * �汾��           
 *
 */
package cn.leeffee.feige.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;

/**
 * @author lvhf
 */
public class PropertyUtil {
    private static Properties defaultProperty;

    private final static String PROPERTY_FILE = "feige.properties";
    private static String app_server = "";

    public static String getServer() {
        if (TextUtils.isEmpty(SPUtil.getString(AppConfig.SERVER))) {
            if (TextUtils.isEmpty(app_server)) {
                app_server = defaultProperty.getProperty("feige.server");
                SPUtil.putString(AppConfig.SERVER, app_server);
            }
        } else {
            app_server = SPUtil.getString(AppConfig.SERVER);
        }
        if (!app_server.endsWith(File.separator)) {
            app_server += File.separator;
        }
        return app_server;
    }

    public static void setServer(String server) {
        PropertyUtil.app_server = server;
    }

    static {
        init();
    }

    static void init() {
        defaultProperty = new Properties();
        loadProperties(defaultProperty, PropertyUtil.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
    }

    private static boolean loadProperties(Properties props, InputStream is) {
        try {
            props.load(is);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }

    private static PropertyUtil single;

    public static PropertyUtil getInstance() {
        synchronized (PropertyUtil.class) {
            if (single == null) {
                single = new PropertyUtil();
            }
            return single;
        }
    }

    public String getBaseUrl() {
        return PropertyUtil.getInstance().getScheme(false) + PropertyUtil.getServer();
        // return PropertyUtil.getScheme(false) + SPUtil.getString(AppConfig.SERVER);
    }

    public String getProperty(String name) {
        return defaultProperty.getProperty(name);
    }

    public boolean getBooleanProperty(String name) {
        String value = getProperty(name);
        return Boolean.valueOf(value);
    }

    public int getIntProperty(String name) {
        String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public long getLongProperty(String name) {
        String value = getProperty(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    public int getSharedFileListPageSize() {
        return getIntProperty("feige.sharedfilelist.pagesize");
    }


    public String getRoot() {
        return getProperty("feige.local.rootdir");
    }

    public long getDownloadFileThreshold() {
        return getLongProperty("feige.file.download.threshold");
    }

    public String getLocalSharedRoot() {
        return getProperty("feige.local.sharedfile.rootdir");
    }

    public String getScheme(boolean useSSL) {
        return useSSL ? "https://" : "http://";
    }
}
