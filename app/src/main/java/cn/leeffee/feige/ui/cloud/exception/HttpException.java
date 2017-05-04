/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      HttpException.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-1 下午01:23:20
 * 版本：           
 *
 */
package cn.leeffee.feige.ui.cloud.exception;

/**
 * HTTP StatusCode is not 200
 * 
 * @author Jacky Wang
 */
public class HttpException extends Exception {
	private int statusCode = -1;

	public HttpException(String msg) {
		super(msg);
	}

	public HttpException(Exception cause) {
		super(cause);
	}
	
	public HttpException(int statusCode) {
		this.statusCode = statusCode;
	}
	public HttpException(String msg, int statusCode) {
		super(msg);
		this.statusCode = statusCode;
	}

	public HttpException(String msg, Exception cause) {
		super(msg, cause);
	}

	public HttpException(String msg, Exception cause, int statusCode) {
		super(msg, cause);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return this.statusCode;
	}
}
