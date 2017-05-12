package cn.leeffee.feige.ui.cloud.presenter;


import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.ActGroupLogContract;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by lhfei on 2017/04/18
 */

public class ActGroupLogPresenterImpl extends ActGroupLogContract.Presenter {
    /**
     * 再登录只能重复一次
     */
    private int times = 1;

    public Disposable listGroupLogs(final String groupId, final int pageIndex, final int pageSize, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listGroupLogs(groupId, pageIndex, pageSize, mToken).subscribe(new Consumer<BaseResponse<List<ApiGroupLog>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiGroupLog>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, res.getResult());
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listGroupLogs(groupId, pageIndex, pageSize, requestCode);
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