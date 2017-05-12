package cn.leeffee.feige.ui.cloud.presenter;


import cn.leeffee.feige.ui.cloud.contract.ActFeedbackContract;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by lhfei on 2017/04/07
 */

public class ActFeedbackPresenterImpl extends ActFeedbackContract.Presenter {

    private int times = 1;

    public Disposable addSuggestion(final String content, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.addSuggestion(content, mToken).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mView.loadSuccess(requestCode, res.getResult());
                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        addSuggestion(content, requestCode);
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