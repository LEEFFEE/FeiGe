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

    // public final static String PERSONAL_feige = "0";
    //public final static String ENTERPRISE_feige = "1";
    //   private static String app_server = "";

    static {
        defaultProperty = new Properties();
        loadProperties(defaultProperty, PropertyUtil.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
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
    public String getServer() {
        if (TextUtils.isEmpty(SPUtil.getString(AppConfig.SERVER))) {
            //            if (TextUtils.isEmpty(app_server)) {
            //                app_server = defaultProperty.getProperty("feige.server");
            //                SPUtil.putString(AppConfig.SERVER, app_server);
            //            }
            return defaultProperty.getProperty(AppConfig.SERVER);
        } else {
            return SPUtil.getString(AppConfig.SERVER);
        }
        //        if (!app_server.endsWith(File.separator)) {
        //            app_server += File.separator;
        //        }
        //  return app_server;
    }

    //    public static void setServer(String server) {
    //        PropertyUtil.app_server = server;
    //    }


    private static boolean loadProperties(Properties props, InputStream is) {
        try {
            props.load(is);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }
    public String getBaseUrl() {
        return PropertyUtil.getInstance().getScheme(false) + PropertyUtil.getInstance().getServer() + ":" + PropertyUtil.getInstance().getPort() + File.separator;
        // return PropertyUtil.getScheme(false) + SPUtil.getString(AppConfig.SERVER);
    }

    private String getProperty(String name) {
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

    //    public String getAppServer() {
    //        //        if (PropertyUtil.ENTERPRISE_feige.equals(getProperty("feige.version.type"))) {
    //        //            return getEnterpriseServer();
    //        //        } else {
    //        //            return getProperty("feige.server");
    //        //        }
    //        return getServer();
    //    }

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

    private String getScheme(boolean useSSL) {
        return useSSL ? "https://" : "http://";
    }

    public String getPort() {
        if (TextUtils.isEmpty(SPUtil.getString(AppConfig.PORT))) {
            return getProperty(AppConfig.PORT);
        } else {
            return SPUtil.getString(AppConfig.PORT);
        }
    }

    //    public String getUcServer() {
    //        if (!PropertyUtil.ENTERPRISE_feige.equals(getProperty("feige.version.type"))) {
    //            return getProperty("feige.ucServer");
    //        }
    //        return "";
    //    }
}
