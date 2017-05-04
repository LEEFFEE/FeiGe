package cn.leeffee.feige.ui.cloud.presenter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.FragMyGroupContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiGroup;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.entity.USpaceGroup;
import cn.leeffee.feige.utils.DateUtil;
import cn.leeffee.feige.utils.StringUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by lhfei on 2017/03/21
 */

public class FragMyGroupPresenterImpl extends FragMyGroupContract.Presenter {

    @Override
    public void onStart() {
        super.onStart();
    }

    int times = 1;

    /**
     * 获取当前用户所有的群组信息
     *
     * @param requestCode
     */
    public Disposable listGroups(final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listGroups(mToken).subscribe(new Consumer<BaseResponse<List<ApiGroup>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiGroup>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    List<ApiGroup> groups = res.getResult();
                    List<USpaceGroup> ugs = new ArrayList<>();
                    if (groups != null && groups.size() > 0) {
                        for (int i = 0; i < groups.size(); i++) {
                            ApiGroup ag = groups.get(i);
                            USpaceGroup ug = new USpaceGroup();
                            ug.setGroupId(ag.getShareGroupId());
                            ug.setName(ag.getName());
                            ug.setGroupAdmin(ag.isAdmin());
                            ug.setUsedSpace(ag.getUsedSpace());
                            ug.setHardLimit(ag.getHardLimit());
                            ug.setStatus(ag.getStatus());
                            ugs.add(ug);
                        }
                    }
                    times = 1;
                    mView.loadSuccess(requestCode, ugs);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listGroups(requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "请求失败");
            }
        });
    }

    /**
     * 根据群组id获取群里的文件信息
     *
     * @param currentRemotePath
     * @param groupId
     * @param requestCode
     */
    public Disposable listGroupFile(final String currentRemotePath, final String groupId, final String groupName, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listGroupFile(currentRemotePath, groupId, mToken).subscribe(new Consumer<BaseResponse<List<ApiFile>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiFile>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    List<ApiFile> afs = res.getResult();
                    List<USpaceFile> ufs = new ArrayList<>();
                    if (afs != null && afs.size() > 0) {
                        for (int i = 0; i < afs.size(); i++) {
                            ApiFile af = afs.get(i);
                            USpaceFile uf = new USpaceFile();
                            uf.setName(af.getName());
                            uf.setSize(af.getSize());
                            uf.setDiskPath(af.getDiskPath());
                            uf.setModifyTime(DateUtil.string2Timestamp(af.getModifyTime(), "yyyy-MM-dd hh:mm:ss"));
                            uf.setFolder(af.isFolder());
                            String sharedId = af.getSharedId();
                            if (!StringUtil.isEmpty(sharedId) && !"null".equalsIgnoreCase(sharedId)) {
                                uf.setShared(true);
                                uf.setExtractionCode(sharedId);
                            } else {
                                uf.setShared(false);
                            }
                            if (!uf.isFolder()) {
                                int version = -1;
                                try {
                                    version = af.getVsesionNumber();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                uf.setVersion(version);
                            }
                            uf.setCreaterId(af.getFileOwnerId());
                            uf.setCreaterName(af.getFileOwner());
                            //群组特别标识
                            uf.setIsGroupFile(AppConstants.GROUP_FILE);//标识为群组文件
                            uf.setGroupId(groupId);
                            uf.setGroupName(groupName);
                            ufs.add(uf);
                        }
                    }
                    times = 1;
                    Collections.sort(ufs);
                    mView.loadSuccess(requestCode, ufs);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listGroupFile(currentRemotePath, groupId, groupName, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }

    /**
     * 创建群组文件夹
     *
     * @param remotePath
     * @param groupId
     * @param requestCode
     */
    public Disposable makeGroupDir(final String remotePath, final String groupId, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.makeGroupDir(remotePath, groupId, mToken).subscribe(new Consumer<BaseResponse<Object>>() {
            @Override
            public void accept(@NonNull BaseResponse<Object> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, "创建目录成功!");
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        makeGroupDir(remotePath, groupId, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, res.getErrorMessage());
                    }
                } else if (60005 == res.getErrorCode()) {
                    mView.loadFailure(requestCode, "文件或者文件名重复");
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }

    public Disposable removeGroupFile(final USpaceFile[] files, final String groupId, final String requestCode) {
        return mModel.removeGroupFile(files, groupId, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, res.getResult());
                } else if (res.getErrorCode() == 60122) {
                    //{"errorMessage":"Not allowed to delete folder [ggg], because its not empty! ","errorCode":60122,"result":null}
                    mView.loadFailure(requestCode, "文件夹不为空!");//，不允许删除！
                } else if (res.getErrorCode() == 60112) {
                    //{"errorMessage":"Not allowed to delete other's file","errorCode":60112,"result":null}
                    mView.loadFailure(requestCode, "不能删除其他人的文件");//，不允许删除！
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        removeGroupFile(files, groupId, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }

    public Disposable listDirOnlyDir(final String remotePath, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listDirOnlyDir(remotePath, mToken).subscribe(new Consumer<BaseResponse<List<ApiOnlyFolder>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiOnlyFolder>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    List<ApiOnlyFolder> apiFiles = res.getResult();
                    List<USpaceFile> fileList = new ArrayList<>();
                    if (apiFiles != null && apiFiles.size() > 0) {
                        for (int i = 0; i < apiFiles.size(); i++) {
                            ApiOnlyFolder _item = apiFiles.get(i);
                            USpaceFile _file = new USpaceFile();
                            _file.setName(_item.getText());
                            _file.setDiskPath(_item.getId());
                            _file.setFolder(_item.isIsfolder());
                            fileList.add(_file);
                        }
                    }
                    times = 1;
                    if (!remotePath.equalsIgnoreCase("/")) {
                        fileList.add(0, new USpaceFile(App.getAppResources().getText(R.string.strBackToParent).toString(), 0, true, true, remotePath, false));
                    }
                    mView.loadSuccess(requestCode, fileList);

                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listDirOnlyDir(remotePath, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "加载失败，请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, "加载失败");
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }

    public Disposable copy2PrivateSpace(final USpaceFile[] copyFiles, final String copyToPath, final String groupId, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.copy2PrivateSpace(copyFiles, copyToPath, groupId, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, res.getResult());
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        copy2PrivateSpace(copyFiles, copyToPath, groupId, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }
}