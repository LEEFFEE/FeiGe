package cn.leeffee.feige.ui.cloud.presenter;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.contract.FragMyFileContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.DateUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by lhfei on 2017/03/21
 */

public class FragMyFilePresenterImpl extends FragMyFileContract.Presenter {
    /**
     * 再登录只能重复一次
     */
    private int times = 1;

    public Disposable listDir(final String currentPath, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listDir(currentPath, mToken).subscribe(new Consumer<BaseResponse<List<ApiFile>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiFile>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    List<ApiFile> apiFiles = res.getResult();
                    List<USpaceFile> fileList = new ArrayList<>();
                    if (apiFiles != null && apiFiles.size() > 0) {
                        for (int i = 0; i < apiFiles.size(); i++) {
                            ApiFile _item = apiFiles.get(i);
                            USpaceFile _file = new USpaceFile();
                            _file.setName(_item.getName());
                            _file.setSize(_item.getSize());
                            _file.setDiskPath(_item.getDiskPath());
                            _file.setModifyTime(DateUtil.string2Timestamp(_item.getModifyTime(), "yyyy-MM-dd hh:mm:ss"));
                            _file.setFolder(_item.isFolder());
                            String sharedId = _item.getSharedId();
                            if (!TextUtils.isEmpty(sharedId) && !"null".equalsIgnoreCase(sharedId)) {
                                _file.setShared(true);
                                _file.setExtractionCode(sharedId);
                            } else {
                                _file.setShared(false);
                            }
                            if (!_file.isFolder()) {
                                _file.setVersion(_item.getVsesionNumber());
                            }
                            fileList.add(_file);
                        }
                    }
                    times = 1;
                    mView.loadSuccess(requestCode, fileList);

                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listDir(currentPath, requestCode);
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
     * 新建文件夹
     *
     * @param remotePath  要创建文件夹的路径
     * @param requestCode 请求字符串
     */
    public Disposable makeDir(final String remotePath, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.makeDir(remotePath, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        makeDir(remotePath, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, res.getErrorMessage());
                    }
                } else if (60005 == res.getErrorCode()) {
                    mView.loadFailure(requestCode, "文件或者文件名重复");
                } else {
                    // {"errorCode":60005,"errorMessage":"Duplicate file or folder name","result":null}
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file
     * @param requestCode
     */
    public Disposable remove(final USpaceFile file, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.remove(file.getDiskPath(), file.getVersion(), mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        remove(file, requestCode);
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
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });
    }


    /**
     * 从新命名
     *
     * @param currentRemotePath 当前远程路径
     * @param oldName           老名字
     * @param newName           新名字
     * @param requestCode       请求码
     */
    public Disposable rename(String currentRemotePath, final String oldName, final String newName, final String requestCode) {
        if (!currentRemotePath.equalsIgnoreCase("/")) {
            currentRemotePath += "/";
        }
        final String remotePath = currentRemotePath;
        String srcPath = currentRemotePath + oldName.trim();
        String destPath = currentRemotePath + newName.trim();
        mView.loadBefore(requestCode);
        return mModel.moveFile(srcPath, destPath, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        rename(remotePath, oldName, newName, requestCode);
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
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });
    }

    /**
     * 创建共享链接
     *
     * @param file
     * @param requestCode
     */
    public Disposable createPublishLink(final USpaceFile file, final String requestCode) {
        final String remotePath = file.getDiskPath();
        final String alias = file.getName();
        return mModel.createPublishLink(remotePath, alias, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    file.setShared(true);//设置已经分享
                    file.setExtractionCode(res.getResult());//设置分享后的id
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        createPublishLink(file, requestCode);
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
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });

    }

    /**
     * 取消分享
     *
     * @param file
     * @param requestCode
     */
    public Disposable cancelSharedFiles(final USpaceFile file, final String requestCode) {
        String[] shareIds = new String[]{file.getExtractionCode()};
        return mModel.cancelSharedFiles(shareIds, mToken).subscribe(new Consumer<BaseResponse<Boolean>>() {
            @Override
            public void accept(@NonNull BaseResponse<Boolean> res) throws Exception {
                if (res.getErrorCode() == 0 && res.getResult()) {
                    file.setShared(false);
                    file.setExtractionCode(null);
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        cancelSharedFiles(file, requestCode);
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
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });
    }

    /**
     * 只列出服务器上的目录
     *
     * @param remotePath
     * @param requestCode
     */
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

    /**
     * 移动文件 或者文件夹
     *
     * @param srcPath     源路径
     * @param destPath    目标路径
     * @param requestCode 请求码
     */
    public Disposable move(final String srcPath, final String destPath, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.move(srcPath, destPath, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, null);
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        move(srcPath, destPath, requestCode);
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
                mView.loadFailure(requestCode, "连接服务器失败");
            }
        });
    }
}