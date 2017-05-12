package cn.leeffee.feige.ui.cloud.presenter;

import android.text.TextUtils;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.ActMainContract;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.ToastUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by lhfei on 2017/04/12
 */

public class ActMainPresenterImpl extends ActMainContract.Presenter {

    @Override
    public void login(final String requestCode) {
        if (NetWorkUtil.isNetConnected(mContext)) {
            mView.loadBefore(requestCode);
            String account = SPUtil.getString(AppConfig.ACCOUNT);
            String pwd = SPUtil.getString(AppConfig.PASSWORD);
            mModel.login(account, pwd).subscribe(new Consumer<BaseResponse<String>>() {
                @Override
                public void accept(@NonNull BaseResponse<String> res) throws Exception {
                    if (res.getErrorCode() != 0 && TextUtils.isEmpty(res.getResult())) {
                        mView.loadSuccess(requestCode, res.getResult());
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
        } else {
            ToastUtil.showShort("无法连接到网络,请检查网络配置！！");
        }
    }
}