package cn.leeffee.feige.ui.cloud.service;

public class DownloadTask extends Checkable {

    public final static int SHARED_FILE_TYPE = 0; // 共享文件

    public final static int OWN_FILE_TYPE = 1; // 自己的文件

    public final static int SHARED_FOLDER_TYPE = 2;

    private String name;
    private int type = 0; // type : 0 公有发布下载（by code），1下载 （by token）
    private String code;
    private String path;
    private String savePath; // 保存到sdcard的路径，必须是一个完整路径
    private int version;
    private String addQueueTime;
    private String finishTime;

    private long offset = 0L;
    private long fileLength = 0L; // 文件总长度，该字段会在开始下载的时候写入
    private long downloadLength = 0L; // 已经下载的字节数

    private boolean cancel = false; // 是否已经取消该队列，用于控制取消下载（删除本地文件）
    private int status = 1; // status : 1等待运行，2运行，3 完成，4下载异常未完成 。 5取消下载（未完成, 删除已下载的文件.tmp），6删除已完成下载记录（不删除已下载文件），7提取码错误

    private int percent = 0; // 下载百分比

    private int isGroupFile; // 组文件
    private String groupId;
    private String ownId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
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

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
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

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public int getIsGroupFile() {
        return isGroupFile;
    }

    public void setIsGroupFile(int isGroupFile) {
        this.isGroupFile = isGroupFile;
    }
}