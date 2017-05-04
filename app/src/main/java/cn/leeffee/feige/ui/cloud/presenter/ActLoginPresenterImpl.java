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
import io.reactivex.disposables.Disposable;

/**
 * Created by lhfei on 2017/03/28
 */

public class ActLoginPresenterImpl extends ActLoginContract.Presenter {
    Disposable disposable;

    @Override
    public void login(final String loginAccount, final String password, final String requestCode) {
        if (loginValidate(loginAccount, password)) {
            if (NetWorkUtil.isNetConnected(mContext)) {
                mView.loadBefore(requestCode);
                SPUtil.putString(AppConfig.ACCOUNT, loginAccount);
                //                disposable = mModel.login(loginAccount, password).subscribe(new Consumer<BaseResponse<String>>() {
                //                    @Override
                //                    public void accept(@NonNull BaseResponse<String> res) throws Exception {
                //                        if (res.getErrorCode() != 0) {
                //                            if (TextUtils.isEmpty(res.getResult())) {
                //                                mView.loadFailure(requestCode, "登录失败！");
                //                            } else {
                //                                mView.loadFailure(requestCode, res.getResult());
                //                            }
                //                        } else {
                //                            destroyLoginInfo(true);
                //                            rememberInClient(loginAccount, password, res.getResult());
                //                            mView.loadSuccess(requestCode, res.getResult());
                //                        }
                //                    }
                //                }, new Consumer<Throwable>() {
                //                    @Override
                //                    public void accept(@NonNull Throwable throwable) throws Exception {
                //                        throwable.printStackTrace();
                //                        mView.loadFailure(requestCode, "无法连接到服务器");
                //                    }
                //                });
                //                mRxManager.add(disposable);
                mView.loadSuccess(requestCode, "");
            } else {
                ToastUtil.showShort("无法连接到网络,请检查网络配置！！");
            }
        }
    }

    /**
     * 取消订阅  /移除订阅并dispose（）
     */
    @Override
    public void unSubscribe() {
        if (disposable != null)
            mRxManager.remove(disposable);
    }

    /**
     * 登录验证
     *
     * @param loginAccount 账户
     * @param password     密码
     * @return 验证通过返回true 否则返回false
     */
    private boolean loginValidate(String loginAccount, String password) {
        String msg = "";
        if ("".equals(loginAccount)) {
            msg = mContext.getText(R.string.login_account_empty_error).toString();
            ToastUtil.showShort(msg);
            return false;
        }

        if (loginAccount != null && loginAccount.trim().length() > 50) {
            ToastUtil.showShort(mContext.getString(R.string.error_login_account_exceed_50));
            return false;
        }

        if ("".equals(password)) {
            msg = mContext.getText(R.string.pwd_empty_error).toString();
            ToastUtil.showShort(msg);
            return false;
        }
        return true;
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
        SPUtil.putString(AppConfig.SERVER, PropertyUtil.getServer());
    }
}