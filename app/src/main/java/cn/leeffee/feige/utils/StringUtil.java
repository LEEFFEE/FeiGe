/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      StringUtils.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-13 上午10:03:47
 * 版本：           
 *
 */
package cn.leeffee.feige.utils;


import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import cn.leeffee.feige.ui.cloud.constants.AppConstants;

public class StringUtil
{
	public static String getFileSize(long size)
	{
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public static boolean isEmpty(String s)
	{
		return s == null || s.length() == 0;
	}

	public static String ints2Strs(Integer[] ids)
	{
		String strs = "";

		for (Integer id : ids)
		{
			strs += id + ",";
		}
		if (strs.length() > 0)
		{
			strs = strs.substring(0, strs.lastIndexOf(","));
		}
		return strs;
	}

	public static String join(String[] src, String link)
	{
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < src.length; i++)
		{
			result.append(src[i]);
			if (i != src.length - 1)
			{
				result.append(link);
			}
		}
		return result.toString();
	}

	public static String encrypt(String val)
	{
		if (isEmpty(val))
		{
			return val;
		}
		String result = new String(Base64.encode((XXTEA.encrypt(val.getBytes(), AppConstants.PATTERN.getBytes()))));
		return result;
	}

	public static String decrypt(String val)
	{
		if (isEmpty(val))
		{
			return val;
		}
		byte[] bytes = XXTEA.decrypt(Base64.decode(val), AppConstants.PATTERN.getBytes());
		return new String(bytes);
	}
	
	public static String getStringByISO8859(String srcStr){
		try {
			String newStr = new String(srcStr.getBytes(), "ISO-8859-1");
			return newStr;
		} catch (UnsupportedEncodingException e) {
			return srcStr;
		}
	}
	public static String getStringByUTF8(String srcStr){
		try {
			String newStr = new String(srcStr.getBytes(), "UTF-8");
			return newStr;
		} catch (UnsupportedEncodingException e) {
			return srcStr;
		}
	}

}
