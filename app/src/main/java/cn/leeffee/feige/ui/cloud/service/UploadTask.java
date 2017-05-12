package cn.leeffee.feige.ui.cloud.service;

public class UploadTask extends Checkable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String url;
    private String remotePath;
    private String localPath;
    private int version;
    private String addQueueTime;
    private String finishTime;

    private long offset = 0L;
    private long fileLength = 0L;
    private long uploadLength = 0L;

    private int status = 1;
    private int percent = 0;

    private int isGroupFile; // 组文件
    private String groupId;
    private String ownId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getAddQueueTime() {
        return addQueueTime;
    }

    public void setAddQueueTime(String addQueueTime) {
        this.addQueueTime = addQueueTime;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public int getStatus() {
        synchronized (this) {
            return status;
        }
    }

    public void setStatus(int status) {
        synchronized (this) {
            this.status = status;
        }
    }


    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getUploadLength() {
        return uploadLength;
    }

    public void setUploadLength(long uploadLength) {
        this.uploadLength = uploadLength;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public int getIsGroupFile() {
        return isGroupFile;
    }

    public void setIsGroupFile(int isGroupFile) {
        this.isGroupFile = isGroupFile;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOwnId() {
        return ownId;
    }

    public void setOwnId(String ownId) {
        this.ownId = ownId;
    }

}