package cn.leeffee.feige.ui.cloud.entity;

/**
 * Created by lhfei on 2017/4/19.
 */

public class ApiGroupLog {

    /**
     * clientType : 1
     * groupId : 1703141027269914a7ab5062b812fb02
     * logId : 1704180720523954a526cf4e190ad8c6
     * logTime : 2017-04-18T07:20:52
     * message : llq@uit:上传了文件：/pig/pp/20170329150319.PNG
     * type : 10
     * userId : null
     * userName : llq@uit
     */

    private int clientType;
    private String groupId;
    private String logId;
    private String logTime;
    private String message;
    private int type;
    private String userId;
    private String userName;

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
