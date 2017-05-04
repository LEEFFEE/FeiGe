/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      FileViewer.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-16 下午03:49:14
 * 版本：           
 *
 */
package cn.leeffee.feige.utils;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * @author Jacky Wang
 */
public class FileViewer {
	public static final String[] allowed = { "pdf", "jpg", "jpeg", "png", "gif", "bmp", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx" };

	public static boolean canOpen(String fileName) {
		String ext = getExtension(fileName);
		return isRight(ext, allowed);
	}

	public static boolean isRight(String ext, String[] allowed) {
		boolean flag = false;
		for (int i = 0; i < allowed.length; i++) {
			if (allowed[i].equalsIgnoreCase(ext)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public static String getExtension(String fileName) {
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos + 1).toLowerCase();
	}

	public static Intent getOpenFileIntent(String remoteFilePath) {

		if (isPdf(remoteFilePath)) {
			return getPdfFileIntent(remoteFilePath);
		} else if (isImage(remoteFilePath)) {
			return getImageFileIntent(remoteFilePath);
		} else if (isTxt(remoteFilePath)) {
			return getTextFileIntent(remoteFilePath);
		} else if (isWord(remoteFilePath)) {
			return getWordFileIntent(remoteFilePath);
		} else if (isExcel(remoteFilePath)) {
			return getExcelFileIntent(remoteFilePath);
		} else if (isPpt(remoteFilePath)) {
			return getPptFileIntent(remoteFilePath);
		} else {
			return null;
		}
	}

	public static Intent openFileIntent(String remoteFilePath) {
		if (isPdf(remoteFilePath)) {
			return getPdfFileIntent(remoteFilePath);
		} else if (isImage(remoteFilePath)) {
			return getImageFileIntent(remoteFilePath);
		} else if (isTxt(remoteFilePath)) {
			return getTextFileIntent(remoteFilePath);
		} else if (isWord(remoteFilePath)) {
			return getWordFileIntent(remoteFilePath);
		} else if (isExcel(remoteFilePath)) {
			return getExcelFileIntent(remoteFilePath);
		} else if (isPpt(remoteFilePath)) {
			return getPptFileIntent(remoteFilePath);
		} else {
			return null;
		}
	}

	/**
	 * 获取一个用于打开图片文件的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getImageFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	/**
	 * 获取一个用于打开PDF文件的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getPdfFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	/**
	 * 获取一个打开TXT文件的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getTextFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "text/plain");
		return intent;
	}

	/**
	 * 获取一个打开word的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getWordFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	/**
	 * 获取一个打开excel的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getExcelFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	/**
	 * 获取一个打开ppt的intent
	 * 
	 * @param
	 * @return
	 * @throws
	 */
	public static Intent getPptFileIntent(String param) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}
	public static boolean isPdf(String filePath) {
		String ext = getExtension(filePath);
		return "pdf".equalsIgnoreCase(ext);
	}

	public static boolean isTxt(String filePath) {
		String ext = getExtension(filePath);
		return "txt".equalsIgnoreCase(ext);
	}

	public static boolean isWord(String filePath) {
		String ext = getExtension(filePath);
		return "doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext);
	}
	
	public static boolean isExcel(String filePath) {
		String ext = getExtension(filePath);
		return "xls".equalsIgnoreCase(ext) || "xlsx".equalsIgnoreCase(ext);
	}
	
	public static boolean isPpt(String filePath) {
		String ext = getExtension(filePath);
		return "ppt".equalsIgnoreCase(ext) || "pptx".equalsIgnoreCase(ext);
	}

	public static boolean isImage(String filePath) {
		String ext = getExtension(filePath);
		return "jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext) || "bmp".equalsIgnoreCase(ext);
	}
	
	public static boolean isZip(String filePath){
		String ext = getExtension(filePath);
		return "zip".equalsIgnoreCase(ext);
	}
}
