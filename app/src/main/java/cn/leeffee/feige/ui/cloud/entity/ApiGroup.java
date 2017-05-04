package cn.leeffee.feige.ui.cloud.entity;

/**
 * Created by lhfei on 2017/4/17.
 */

public class ApiGroup {

    /**
     * admin : false
     * adminList : null
     * backupPath : /mnt/sd2
     * backupPathId : 2
     * createdDate : 2017-03-14 10:27:27
     * creater_id : null
     * description :
     * groupSerialNum : 5335615686
     * hardLimit : 1024
     * maxUser : 2147483647
     * name : 1
     * rootPath : /mnt/sd1
     * rootPathId : 1
     * shareGroupId : 1703141027269914a7ab5062b812fb02
     * status : 1
     * statusDate : null
     * thumbnailPath : /mnt/sdv
     * thumbnailPathId : 3
     * usedSpace : 75827717
     * userCount : 0
     * userId : 1703141023463899b8852163a4963274
     */

    private boolean admin;
    private Object adminList;
    private String backupPath;
    private String backupPathId;
    private String createdDate;
    private Object creater_id;
    private String description;
    private String groupSerialNum;
    private long hardLimit;
    private int maxUser;
    private String name;
    private String rootPath;
    private String rootPathId;
    private String shareGroupId;
    private int status;
    private Object statusDate;
    private String thumbnailPath;
    private String thumbnailPathId;
    private long usedSpace;
    private int userCount;
    private String userId;

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Object getAdminList() {
        return adminList;
    }

    public void setAdminList(Object adminList) {
        this.adminList = adminList;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getBackupPathId() {
        return backupPathId;
    }

    public void setBackupPathId(String backupPathId) {
        this.backupPathId = backupPathId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Object getCreater_id() {
        return creater_id;
    }

    public void setCreater_id(Object creater_id) {
        this.creater_id = creater_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupSerialNum() {
        return groupSerialNum;
    }

    public void setGroupSerialNum(String groupSerialNum) {
        this.groupSerialNum = groupSerialNum;
    }

    public long getHardLimit() {
        return hardLimit;
    }

    public void setHardLimit(long hardLimit) {
        this.hardLimit = hardLimit;
    }

    public int getMaxUser() {
        return maxUser;
    }

    public void setMaxUser(int maxUser) {
        this.maxUser = maxUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPathId() {
        return rootPathId;
    }

    public void setRootPathId(String rootPathId) {
        this.rootPathId = rootPathId;
    }

    public String getShareGroupId() {
        return shareGroupId;
    }

    public void setShareGroupId(String shareGroupId) {
        this.shareGroupId = shareGroupId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Object statusDate) {
        this.statusDate = statusDate;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getThumbnailPathId() {
        return thumbnailPathId;
    }

    public void setThumbnailPathId(String thumbnailPathId) {
        this.thumbnailPathId = thumbnailPathId;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
