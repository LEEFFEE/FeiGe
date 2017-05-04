package cn.leeffee.feige.ui.cloud.exception;

public interface IClientExceptionCode {
	static int ERROR = 1000000;
	
	static int SDCARD_NOT_AVAILABLE_ERROR = ERROR + 1; // sdcard 不可用错误！
	
	static int CLIENT_IOEXCEPTION_ERROR = ERROR + 2; //手机端文件流异常

	static int FILE_NOT_FOUND_ERROR = ERROR +3; //手机端文件不存在异常
	
	static int APP_USERNAME_NULL_ERROR = ERROR + 4; //登录情况下若为null则会出现null文件夹
}
