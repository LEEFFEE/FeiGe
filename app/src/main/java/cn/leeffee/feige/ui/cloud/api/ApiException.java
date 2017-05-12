package cn.leeffee.feige.ui.cloud.api;

/**
 * API异常类，由错误码，和错误信息构成。
 */
public class ApiException extends Exception {
    private int code;
    //用于展示的异常信息
    private String displayMessage;

    public ApiException(int code) {
        this.code = code;
    }

    public ApiException(String displayMessage, int code) {
        this.displayMessage = displayMessage;
        this.code = code;
    }

    public ApiException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
