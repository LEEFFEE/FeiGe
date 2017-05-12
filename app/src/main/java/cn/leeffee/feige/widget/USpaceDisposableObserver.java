package cn.leeffee.feige.widget;


import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.api.ApiException;
import cn.leeffee.feige.utils.ToastUtil;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by lhfei on 2017/5/12.
 */

public abstract class USpaceDisposableObserver<T> extends DisposableObserver<T> {
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof ApiException) {
            onError((ApiException) e);
        } else {
            onError(new ApiException(App.getAppContext().getText(R.string.loading_throwable_tips).toString(), 123));
        }
    }

    /**
     * 错误回调
     */
    protected void onError(ApiException ex) {
        ToastUtil.showShort(ex.getDisplayMessage());
    }
}
