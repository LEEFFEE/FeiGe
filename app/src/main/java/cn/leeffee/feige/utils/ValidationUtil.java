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

/**
 * @author leeffee
 */
public class ValidationUtil {
	public static boolean validateEmail(String email) {
		boolean tag = true;
		final String pattern1 = "(\\w+)([\\-+.][\\w]+)*@(\\w[\\-\\w]*\\.){1,5}([A-Za-z]){2,6}";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	public static boolean validateMobile(String mobile) {
		boolean tag = true;
		final String pattern1 = "(^0?[1][358][0-9]{9}$)";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(mobile);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	public static boolean validatePassword(String password) {
		boolean tag = true;
		final String pattern1 = "^[a-zA-Z0-9]{6,12}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(password);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	public static boolean validateName(String name) {
		boolean tag = true;
		final String pattern1 = "^[a-zA-Z\u4E00-\u9FFF]{1,10}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(name);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	public static boolean isFileNameLegal(String name) {
		final Pattern pattern = Pattern.compile("[/\\:*?\"<>|]");
		final Matcher matcher = pattern.matcher(name);
		return !matcher.find();
	}
	
	public static boolean validateAge(int age){
		return age > 0 && age < 100;
	}

	public static void main(String[] args) {
		System.out.println("\"你好?\" ->"+ ValidationUtil.isFileNameLegal("你好"));
	}
}
