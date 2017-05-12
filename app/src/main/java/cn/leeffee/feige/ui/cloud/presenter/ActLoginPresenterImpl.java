package cn.leeffee.feige.ui.cloud.presenter;


import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.ActLoginContract;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.utils.ValidationUtil;
import io.reactivex.disposables.Disposable;

/**
 * Created by lhfei on 2017/03/28
 */

public class ActLoginPresenterImpl extends ActLoginContract.Presenter {
    Disposable disposable;
    @Override
    public void login(final String loginAccount, final String password, final String requestCode) {
        if (ValidationUtil.validateAccount(loginAccount, password)) {
            if (NetWorkUtil.isNetConnected(mContext)) {
//                mView.loadBefore(requestCode);
//                SPUtil.putString(AppConfig.ACCOUNT, loginAccount);
//                disposable = mModel.login(loginAccount, password).subscribe(new Consumer<BaseResponse<String>>() {
//                    @Override
//                    public void accept(@NonNull BaseResponse<String> res) throws Exception {
//                        if (res.getErrorCode() != 0) {
//                            mView.loadFailure(requestCode, res.getErrorMessage());
//                        } else {
//                            destroyLoginInfo(true);
//                            rememberInClient(loginAccount, password, res.getResult());
//                            mView.loadSuccess(requestCode, res.getResult());
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(@NonNull Throwable throwable) throws Exception {
//                        handlerThrowable(requestCode, throwable);
//                    }
//                });
//                mRxManager.add(disposable);
                mView.loadSuccess(requestCode, "");
            } else {
                ToastUtil.showShort(getText(R.string.network_not_available));
            }
        }
    }

    /**
     * 取消订阅  /移除订阅并dispose（）
     */
    @Override
    public void unSubscribe() {
        mRxManager.remove(disposable);
    }

    /**
     * 记住账号及密码
     */
    public void rememberInClient(String loginAccount, String password, String token) {
        SPUtil.putString(AppConfig.ACCOUNT, loginAccount);
        SPUtil.putString(AppConfig.PASSWORD, StringUtil.encrypt(password));
        SPUtil.putString(AppConfig.TOKEN, token);
        LogUtil.e(token);
        SPUtil.putBoolean(AppConfig.AUTO_LOGIN, true);
        SPUtil.putString(AppConfig.SERVER, PropertyUtil.getInstance().getServer());
    }
}