package cn.leeffee.feige.ui.cloud.presenter;


import cn.leeffee.feige.ui.cloud.contract.ActUploadFileExploreContract;

/**
 * Created by lhfei on 2017/03/31
 */

public class ActUploadFileExplorePresenterImpl extends ActUploadFileExploreContract.Presenter {
    /**
     * 再登录只能重复一次
     */
    private int times = 1;

//    public Disposable listDirOnlyDir(final String remotePath, final String requestCode) {
//        mView.loadBefore(requestCode);
//        return mModel.listDirOnlyDir(remotePath, mToken).subscribe(new Consumer<BaseResponse<List<ApiOnlyFolder>>>() {
//            @Override
//            public void accept(@NonNull BaseResponse<List<ApiOnlyFolder>> res) throws Exception {
//                if (res.getErrorCode() == 0) {
//                    List<ApiOnlyFolder> apiFiles = res.getResult();
//                    List<USpaceFile> fileList = new ArrayList<>();
//                    if (apiFiles != null && apiFiles.size() > 0) {
//                        for (int i = 0; i < apiFiles.size(); i++) {
//                            ApiOnlyFolder _item = apiFiles.get(i);
//                            USpaceFile _file = new USpaceFile();
//                            _file.setName(_item.getText());
//                            _file.setDiskPath(_item.getId());
//                            _file.setFolder(_item.isIsfolder());
//                            fileList.add(_file);
//                        }
//                    }
//                    times = 1;
//                    if (!remotePath.equalsIgnoreCase("/")) {
//                        fileList.add(0, new USpaceFile(App.getAppResources().getText(R.string.strBackToParent).toString(), 0, true, true, remotePath, false));
//                    }
//                    mView.loadSuccess(requestCode, fileList);
//
//                } else if (checkCode(res.getErrorCode())) {
//                    if (times < 2) {
//                        reLogin(requestCode);
//                        listDirOnlyDir(remotePath, requestCode);
//                        times++;
//                    } else {
//                        times = 1;
//                        mView.loadFailure(requestCode, res.getErrorMessage());
//                    }
//                } else {
//                    mView.loadFailure(requestCode, res.getErrorMessage());
//                }
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(@NonNull Throwable throwable) throws Exception {
//               failureHandler(requestCode, throwable);
//            }
//        });
//    }
}