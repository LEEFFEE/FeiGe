package cn.leeffee.feige.ui.cloud.exception;

public interface FileHandler {
	boolean isCancel();
	void cancel();
	void send(long len);
}
