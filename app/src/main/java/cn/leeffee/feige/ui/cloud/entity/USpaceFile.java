/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      UspaceFile.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-9 下午02:08:13
 * 版本：           
 *
 */
package cn.leeffee.feige.ui.cloud.entity;

import java.sql.Timestamp;

/**
 * @author lvhf
 */
public class USpaceFile implements Comparable<USpaceFile> {
    private String name;

    private boolean isFolder;

    private long size;

    private String diskPath;

    private Timestamp modifyTime;

    private boolean isParent = false;

    private boolean isShared;

    private Integer version;

    private String extractionCode;

    private String createrId;

    private String createrName;

	/*后面添加字段start*/

    private String groupId;
    private String groupName;
    private int isGroupFile;

    /**
     * 是否在搜索出来的数据
     */
    private boolean isSearchFile;

    public boolean isSearchFile() {
        return isSearchFile;
    }

    /**
     * 设置文件的类型
     * @param searchFile 搜索出来的为true  否则为false
     */
    public void setSearchFile(boolean searchFile) {
        isSearchFile = searchFile;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getIsGroupFile() {
        return isGroupFile;
    }

    public void setIsGroupFile(int isGroupFile) {
        this.isGroupFile = isGroupFile;
    }

    /*后面添加字段end*/
    public USpaceFile() {
    }

    public USpaceFile(String filename, long size, boolean isFolder, boolean isParent, String filePath, boolean isShared) {
        this.name = filename;
        this.size = size;
        this.isFolder = isFolder;
        this.isParent = isParent;
        this.diskPath = filePath;
        this.isShared = isShared;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean isShared) {
        this.isShared = isShared;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getExtractionCode() {
        return extractionCode;
    }

    public void setExtractionCode(String extractionCode) {
        this.extractionCode = extractionCode;
    }

    @Override
    public int compareTo(USpaceFile another) {
        if (isParent) {
            return -1;
        }
        if (another.isParent()) {
            return 1;
        }
        if (isFolder) {
            if (another.isFolder()) {
                return modifyTime.compareTo(another.getModifyTime());
            } else {
                return -1;
            }
        } else {
            if (another.isFolder()) {
                return 1;
            } else {
                return modifyTime.compareTo(another.getModifyTime());
            }
        }

    }

    public String getCreaterId() {
        return createrId;
    }

    public void setCreaterId(String createrId) {
        this.createrId = createrId;
    }

    public String getCreaterName() {
        return createrName;
    }

    public void setCreaterName(String createrName) {
        this.createrName = createrName;
    }

}
