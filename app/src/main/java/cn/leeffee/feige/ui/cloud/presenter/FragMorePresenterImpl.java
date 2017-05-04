package cn.leeffee.feige.ui.cloud.presenter;


import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.FragMoreContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/03/23
 */

public class FragMorePresenterImpl extends FragMoreContract.Presenter {

    private int times = 1;

    public void getAccountProperty(final String requestCode) {
        mRxManager.add(mModel.getAccountProperty(mToken).subscribe(new Consumer<BaseResponse<ApiAccountProp>>() {
            @Override
            public void accept(@NonNull BaseResponse<ApiAccountProp> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, res.getResult());
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        getAccountProperty(requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "加载失败，请稍后重试");
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
        }));

    }

    /**
     * 列出当前目录下的所有文件夹
     *
     * @param currentLocalPath
     * @param requestCode
     */
    public Disposable listLocalDir(String currentLocalPath, final String requestCode) {
        mView.loadBefore(requestCode);
        return Observable.just(currentLocalPath).map(new Function<String, List<USpaceFile>>() {

            @Override
            public List<USpaceFile> apply(@NonNull String path) throws Exception {
                return listDir(path);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<USpaceFile>>() {
            @Override
            public void accept(@NonNull List<USpaceFile> data) throws Exception {
                mView.loadSuccess(requestCode, data);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, throwable.getMessage());
            }
        });
    }

    /**
     * 列出当前目录下的所有文件夹
     *
     * @param path
     * @return
     */
    private List<USpaceFile> listDir(String path) {
        List<USpaceFile> ufs = new ArrayList<>();
        File parent = new File(path);
        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
            USpaceFile uf;
            for (File file : parent.listFiles()) {
                if (file != null && file.isDirectory()) {
                    uf = new USpaceFile(file.getName(), file.length(), !file.isFile(), false, file.getPath(), false);
                    uf.setModifyTime(new Timestamp(file.lastModified()));
                    ufs.add(uf);
                }
            }
            Collections.sort(ufs);
        }
        return ufs;
    }
}