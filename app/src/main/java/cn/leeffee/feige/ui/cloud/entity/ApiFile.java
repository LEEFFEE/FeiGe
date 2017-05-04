package cn.leeffee.feige.ui.cloud.entity;

/**
 * Created by lhfei on 2017/3/29.
 */

public class ApiFile {

    /**
     * changedfolderright : false
     * createTime : 2017-03-06 05:12:01
     * diskPath : /Photos
     * fileOwner : null
     * fileOwnerId : 170306051201026896c73bdda91703c5
     * fileType : 0
     * folder : true
     * ispause : false
     * linkShare : -1
     * modifyTime : 2017-03-06 05:12:01
     * name : Photos
     * oprightstr : null
     * recycleBinId : null
     * sharedId : null
     * size : 0
     * vsesionNumber : null
     */

    private boolean changedfolderright;
    private String createTime;
    private String diskPath;
    private String fileOwner;
    private String fileOwnerId;
    private int fileType;
    private boolean folder;
    private boolean ispause;
    private int linkShare;
    private String modifyTime;
    private String name;
    private String oprightstr;
    private String recycleBinId;
    private String sharedId;
    private int size;
    private int vsesionNumber;

    public boolean isChangedfolderright() {
        return changedfolderright;
    }

    public void setChangedfolderright(boolean changedfolderright) {
        this.changedfolderright = changedfolderright;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }

    public String getFileOwner() {
        return fileOwner;
    }

    public void setFileOwner(String fileOwner) {
        this.fileOwner = fileOwner;
    }

    public String getFileOwnerId() {
        return fileOwnerId;
    }

    public void setFileOwnerId(String fileOwnerId) {
        this.fileOwnerId = fileOwnerId;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public boolean isIspause() {
        return ispause;
    }

    public void setIspause(boolean ispause) {
        this.ispause = ispause;
    }

    public int getLinkShare() {
        return linkShare;
    }

    public void setLinkShare(int linkShare) {
        this.linkShare = linkShare;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOprightstr() {
        return oprightstr;
    }

    public void setOprightstr(String oprightstr) {
        this.oprightstr = oprightstr;
    }

    public String getRecycleBinId() {
        return recycleBinId;
    }

    public void setRecycleBinId(String recycleBinId) {
        this.recycleBinId = recycleBinId;
    }

    public String getSharedId() {
        return sharedId;
    }

    public void setSharedId(String sharedId) {
        this.sharedId = sharedId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getVsesionNumber() {
        return vsesionNumber;
    }

    public void setVsesionNumber(int vsesionNumber) {
        this.vsesionNumber = vsesionNumber;
    }
}
