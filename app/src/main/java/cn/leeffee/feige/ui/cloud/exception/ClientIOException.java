package cn.leeffee.feige.ui.cloud.exception;

import java.io.IOException;

/**
 * @author lvhf
 */
public class ClientIOException extends IOException {
	private static final long serialVersionUID = 1L;
	
	private int code = -1;
	
	public ClientIOException() {
	}
	
	public ClientIOException(int code) {
		this.code = code;
	}

	public ClientIOException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
