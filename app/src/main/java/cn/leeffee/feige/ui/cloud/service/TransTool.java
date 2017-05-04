package cn.leeffee.feige.ui.cloud.service;

import android.text.TextUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.ToastUtil;

import static cn.leeffee.feige.utils.FileUtil.getUserRoot;


/**
 * Created by lhfei on 2017/4/18.
 */

public class TransTool {
    DBTool mDBTool;

    public TransTool() {
        mDBTool = new DBTool(App.getAppContext());
    }
    /**
     * 下载业务
     *
     * @param file            要下载的文件
     * @param mIsDownloadBind 下载的服务是否绑定
     */
    public void download(USpaceFile file, boolean mIsDownloadBind) {
        DownloadTask task = new DownloadTask();
        String savePath;
        if (file.getIsGroupFile() == AppConstants.GROUP_FILE) {
            savePath = FileUtil.getUSpaceLocalGroupRoot(file.getGroupName()) + file.getDiskPath();
            task.setGroupId(file.getGroupId());
            task.setIsGroupFile(AppConstants.GROUP_FILE);
            task.setOwnId(file.getCreaterId());
        } else {
            savePath = getUserRoot() + file.getDiskPath();
        }
        task.setSavePath(savePath);
        task.setPath(file.getDiskPath());
        task.setVersion(file.getVersion());
        task.setOffset(0L);
        task.setName(file.getName());
        task.setFileLength(file.getSize());
        task.setType(DownloadTask.OWN_FILE_TYPE);

        task.setOwnId(file.getCreaterId());
        if (mDBTool.isExistInDownloadQueue(DownloadTask.OWN_FILE_TYPE, task.getSavePath())) {
            ToastUtil.showShort(R.string.downing);
        } else {
            mDBTool.saveDownloadTask(task);// 存入数据库
            String msg = MessageFormat.format(App.getAppResources().getText(R.string.msg_add_file_into_download_task).toString(), file.getName());
            ToastUtil.showShort(msg);
        }
        if (mIsDownloadBind && App.downloadService != null) {
            App.downloadService.unlock();
        }
    }

    /**
     * 上传文件
     *
     * @param path          上传到网盘的路径
     * @param files         要上传文件的集合
     * @param mIsUploadBind 上传服务是否已经绑定
     */
    public void uploadFiles(String path, List<File> files, boolean mIsUploadBind, String groupId) {
        List<File> inQueue = new ArrayList<>();
        List<File> uploaded = new ArrayList<>();
        for (File file : files) {
            if (mDBTool.isExistInUploadQueue(file.getPath())) {
                inQueue.add(file);
            } else {
                uploaded.add(file);
            }
        }
        if (inQueue.size() > 0) {
            String joinName = FileUtil.getFileJoinName(inQueue);
            String msg = MessageFormat.format(App.getAppResources().getText(R.string.msg_file_exist_in_upload_task).toString(), joinName);
            ToastUtil.showShort(msg);
        }
        if (uploaded.size() > 0) {
            List<UploadTask> taskList = new ArrayList<>();
            UploadTask task;
            for (File _upload : uploaded) {
                task = new UploadTask();
                task.setName(_upload.getName());
                task.setRemotePath(path);
                task.setVersion(1);
                task.setOffset(0L);
                task.setLocalPath(_upload.getPath());
                task.setFileLength(_upload.length());
                //群组上传
                if (!TextUtils.isEmpty(groupId)) {
                    task.setIsGroupFile(AppConstants.GROUP_FILE);
                    task.setGroupId(groupId);
                }
                taskList.add(task);
            }
            mDBTool.saveUploadTask(taskList);
            String joinName = FileUtil.getFileJoinName(uploaded);
            String msg = MessageFormat.format(App.getAppResources().getText(R.string.msg_add_file_into_upload_task).toString(), joinName);
            if (uploaded.size() > 1) {
                msg = MessageFormat.format(App.getAppResources().getText(R.string.msg_add_multy_file_into_upload_task).toString(), joinName, uploaded.size());
            }
            ToastUtil.showShort(msg);
            if (mIsUploadBind && App.uploadService != null) {
                App.uploadService.unlock();
            }
        }
    }

    /**
     * 文件是否正在下载中
     *
     * @param file
     * @return
     */
    public boolean isFileDownloading(USpaceFile file) {
        String savePath = getUserRoot() + file.getDiskPath();
        return mDBTool.isExistInDownloadQueue(DownloadTask.OWN_FILE_TYPE, savePath);
    }

    /**
     * 验证是否能移动
     *
     * @param srcFile
     * @return
     */
    public boolean validateMove(USpaceFile srcFile, String moveToRemotePath) {
        if (srcFile.getDiskPath().equalsIgnoreCase(moveToRemotePath)) {
            ToastUtil.showShort("目标文件夹和原文件夹相同！");
            return false;
        }
        if (moveToRemotePath.indexOf(srcFile.getDiskPath()) != -1) {
            ToastUtil.showShort("目标文件夹是原文件夹的子文件夹！");
            return false;
        }
        if (!srcFile.isFolder()) {
            String parentPath = srcFile.getDiskPath().substring(0, srcFile.getDiskPath().lastIndexOf("/"));
            if (parentPath.equals("")) {
                parentPath = "/";
            }
            if (parentPath.equalsIgnoreCase(moveToRemotePath)) {
                ToastUtil.showShort("目标文件夹和原文件所在文件夹相同！");
                return false;
            }
        }
        return true;
    }

}
