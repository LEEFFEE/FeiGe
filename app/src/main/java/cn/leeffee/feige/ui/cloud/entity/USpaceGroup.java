package cn.leeffee.feige.ui.cloud.entity;

import java.util.List;

public class USpaceGroup {
    private String groupId;
    private String name;
    private List<USpaceFile> uSpaceFiles;
    private long hardLimit;
    private int maxUsers;
    private int status; //2 delete
    private String createTime;
    private String description;
    private long usedSpace;
    private boolean isGroupAdmin;

    public USpaceGroup() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getHardLimit() {
        return hardLimit;
    }

    public void setHardLimit(long hardLimit) {
        this.hardLimit = hardLimit;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isGroupAdmin() {
        return isGroupAdmin;
    }

    public void setGroupAdmin(boolean isGroupAdmin) {
        this.isGroupAdmin = isGroupAdmin;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<USpaceFile> getYopanFiles() {
        return uSpaceFiles;
    }

    public void setYopanFiles(List<USpaceFile> uSpaceFiles) {
        this.uSpaceFiles = uSpaceFiles;
    }
}
