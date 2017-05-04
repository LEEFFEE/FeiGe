package cn.leeffee.feige.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	/**
	 * 时间格式 yyyyMMddHHmmss
	 */
	public static final String DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	public static final String DATE_YYYY_MM_DD = "yyyy-MM-dd";

	public static final String DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	
	/**
	 * 字符转换为日期
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Date string2Date(String str, String pattern) {
		if (null == str || "null".equals(str) || "".equals(str)) {
			return null;
		}
		if (null == pattern) {
			pattern = "yyyy/MM/dd HH:mm:ss";
		}
		// 定义日期/时间格式
		SimpleDateFormat formate = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			// 转换日期/时间格式
			date = formate.parse(str);
		} catch (Exception e) {
			System.out.println("string2Date formate error");
		}
		return date;
	}

	public static Timestamp string2Timestamp(String str, String pattern) {
		Date date = string2Date(str, pattern);
		return date2Timestamp(date);
	}

	/**
	 * java.util.Date 转换成 Timestamp
	 * 
	 * @param date
	 * @return
	 */
	public static Timestamp date2Timestamp(Date date) {
		Timestamp tsp = null;
		if (date != null) {
			tsp = new Timestamp(date.getTime());
		}
		return tsp;
	}

	public static Date timestamp2Date(Timestamp timestamp) {
		Date date = null;
		if (timestamp != null) {
			date = new Date(timestamp.getTime());
		}
		return  date;
	}

	public static String timestamp2String(Timestamp timestamp, String pattern) {
		if (null == pattern) {
			pattern = "yyyy/MM/dd HH:mm:ss";
		}
		return date2String(timestamp2Date(timestamp), pattern);
	}

	public static String date2String(Date date, String pattern) {
		if (null == pattern) {
			pattern = "yyyy/MM/dd HH:mm:ss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static Timestamp getCurrentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * 得到当前时间
	 * 
	 * @param pattern
	 *            yyyyMMddHHmmssSSS
	 * @return
	 */
	public static String getCurrentTime(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}
	
	public static String getStandardTime(long timestamp){
		SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
		Date date = new Date(timestamp*1000);
		sdf.format(date);
		return sdf.format(date);
	}

	public static Long getTime(Date expDate) {
		Long time = null;
		if (expDate!=null) {
			time  = expDate.getTime();
		}
		return time;
	}
}
