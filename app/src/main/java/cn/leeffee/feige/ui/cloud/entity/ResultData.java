package cn.leeffee.feige.ui.cloud.entity;

/**
 * Created by lhfei on 2017/3/28.
 */

public class ResultData {

    /**
     * errorCode : 0
     * errorMessage : null
     * result : 7028369e55f0f0ab654066a39c377b1ed2b747eaf3859c7bdd81549495f57caad90d2d93a0fa8e65d61038b0ebff4a4ee66cfeb1e088be9d7f91be8181965b32569220f63497f690
     */

    private int errorCode;
    private Object errorMessage;
    private String result;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public Object getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
