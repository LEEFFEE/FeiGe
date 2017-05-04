package cn.leeffee.feige.ui.cloud.api;

/**
 * API异常类，由错误码，HTTP状态码和错误信息构成。
 *
 * @author Jacky Wang
 */
public class ApiException extends Exception {
    private int code;

    private int statusCode;

    public ApiException(int code) {
        this.code = code;
    }

    public ApiException(int code, int statusCode, String message) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
