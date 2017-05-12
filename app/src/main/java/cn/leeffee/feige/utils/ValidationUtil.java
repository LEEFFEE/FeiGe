/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      Validation.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-20 下午05:00:41
 * 版本：           
 *
 */
package cn.leeffee.feige.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;

import static java.util.regex.Pattern.compile;

/**
 * @author Jacky Wang
 */
public class ValidationUtil {
    public static boolean validateEmail(String email) {
        boolean tag = true;
        String pattern1 = "(\\w+)([\\-+.][\\w]+)*@(\\w[\\-\\w]*\\.){1,5}([A-Za-z]){2,6}";
        Matcher mat = compile(pattern1).matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean validateMobile(String mobile) {
        boolean tag = true;
        String pattern1 = "(^0?[1][358][0-9]{9}$)";
        Matcher mat = compile(pattern1).matcher(mobile);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean validatePassword(String password) {
        boolean tag = true;
        String pattern1 = "^[a-zA-Z0-9]{6,12}$";
        Matcher mat = compile(pattern1).matcher(password);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean validateName(String name) {
        boolean tag = true;
        String pattern1 = "^[a-zA-Z\u4E00-\u9FFF]{1,10}$";
        Matcher mat = compile(pattern1).matcher(name);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean isFileNameLegal(String name) {
        Matcher matcher = compile("[/\\:*?\"<>|]").matcher(name);
        return !matcher.find();
    }

    public static boolean validateAge(int age) {
        return age > 0 && age < 100;
    }

    public static boolean validatePort(int port) {
        return port > 0 && port <= 65535;
    }

    public static boolean validateIpAddress(String ipAddress) {
        String pattern = "^((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))$";
        Matcher mat = Pattern.compile(pattern).matcher(ipAddress);
        return mat.find() ? true : false;
    }

    public static boolean validateDomain(String domain) {
        String pattern = "^([a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?)$";
        Matcher mat = Pattern.compile(pattern).matcher(domain);
        return mat.find() ? true : false;
    }

    /**
     * 账户基本验证
     *
     * @param loginAccount 账户
     * @param password     密码
     * @return 验证通过返回true 否则返回false
     */
    public static boolean validateAccount(String loginAccount, String password) {
        String msg = "";
        if ("".equals(loginAccount)) {
            msg = App.getAppContext().getText(R.string.login_account_empty_error).toString();
            ToastUtil.showShort(msg);
            return false;
        }

        if (loginAccount != null && loginAccount.trim().length() > 50) {
            ToastUtil.showShort(App.getAppContext().getString(R.string.error_login_account_exceed_50));
            return false;
        }

        if ("".equals(password)) {
            msg = App.getAppContext().getText(R.string.pwd_empty_error).toString();
            ToastUtil.showShort(msg);
            return false;
        }
        if (password != null && password.trim().length() > 50) {
            ToastUtil.showShort(App.getAppContext().getString(R.string.error_login_account_exceed_50));
            return false;
        }
        return true;
    }
}
