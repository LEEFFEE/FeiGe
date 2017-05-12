package cn.leeffee.feige.ui.cloud.presenter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.FragSettingContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/03/23
 */

public class FragSettingPresenterImpl extends FragSettingContract.Presenter {

    private int times = 1;

    public Disposable getAccountProperty(final String requestCode) {
        return mModel.getAccountProperty(mToken).subscribe(new Consumer<BaseResponse<ApiAccountProp>>() {
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
                        mView.loadFailure(requestCode, res.getErrorMessage());
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                handlerThrowable(requestCode, throwable);
            }
        });

    }

    //    /**
    //     * 列出当前目录下的所有文件夹
    //     *
    //     * @param currentLocalPath
    //     * @param requestCode
    //     */
    //    public Disposable listLocalDir(String currentLocalPath, final String requestCode) {
    //        mView.loadBefore(requestCode);
    //        return Observable.just(currentLocalPath).map(new Function<String, List<USpaceFile>>() {
    //
    //            @Override
    //            public List<USpaceFile> apply(@NonNull String path) throws Exception {
    //                return listDir(path);
    //            }
    //        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<USpaceFile>>() {
    //            @Override
    //            public void accept(@NonNull List<USpaceFile> data) throws Exception {
    //                mView.loadSuccess(requestCode, data);
    //            }
    //        }, new Consumer<Throwable>() {
    //            @Override
    //            public void accept(@NonNull Throwable throwable) throws Exception {
    //                handlerThrowable(requestCode, throwable);
    //            }
    //        });
    //    }

    //    /**
    //     * 列出当前目录下的所有文件夹
    //     *
    //     * @param path
    //     * @return
    //     */
    //    private List<USpaceFile> listDir(String path) {
    //        List<USpaceFile> ufs = new ArrayList<>();
    //        File parent = new File(path);
    //        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
    //            USpaceFile uf;
    //            for (File file : parent.listFiles()) {
    //                if (file != null && file.isDirectory()) {
    //                    uf = new USpaceFile(file.getName(), file.length(), !file.isFile(), false, file.getPath(), false);
    //                    uf.setModifyTime(new Timestamp(file.lastModified()));
    //                    ufs.add(uf);
    //                }
    //            }
    //            Collections.sort(ufs);
    //        }
    //        return ufs;
    //    }

    /**
     * 清空缓存
     *
     * @param requestCode
     * @return
     */
    public void clearCache(final String requestCode) {
        Flowable<String> flowable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                try {
                    String cacheDir = FileUtil.getUserRoot();
                    FileUtil.RecursionDeleteFile(new File(cacheDir));
                    // e.onNext(cacheDir);
                    e.onComplete();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        }, BackpressureStrategy.BUFFER).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        flowable.subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);//不调用不会执行 onNext与onComplete
            }

            @Override
            public void onNext(String s) {
                LogUtil.e("onNext:" + s);
            }

            @Override
            public void onError(Throwable t) {
                handlerThrowable(requestCode, t);
            }

            @Override
            public void onComplete() {
                mView.loadSuccess(requestCode, "清空成功!");
            }
        });
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     * @param requestCode
     * @return
     */
    public Disposable changePassword(final String oldPassword, final String newPassword, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.changePassword(oldPassword, newPassword, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    SPUtil.putString(AppConfig.PASSWORD, StringUtil.encrypt(newPassword));
                    mView.loadSuccess(requestCode, "修改成功！");
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        changePassword(oldPassword, newPassword, mToken);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, res.getErrorMessage());
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                handlerThrowable(requestCode, throwable);
            }
        });
    }
}