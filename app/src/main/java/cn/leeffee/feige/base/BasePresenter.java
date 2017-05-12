package cn.leeffee.feige.base;

import android.content.Context;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.manager.RxManager;
import cn.leeffee.feige.ui.cloud.api.ApiException;
import cn.leeffee.feige.ui.cloud.api.ApiExceptionEngine;
import cn.leeffee.feige.ui.cloud.constants.AppCode;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * des:基类presenter
 */
public abstract class BasePresenter<V extends BaseView, M extends BaseModel> {
    public Context mContext;
    public V mView;
    public M mModel;
    public RxManager mRxManager = new RxManager();

    protected String mToken;
    public FileUtil mFileUtil;

    public DBTool mDBTool;

    public void setVM(V v, M m) {
        this.mView = v;
        this.mModel = m;
        this.onStart();
    }

    public void onStart() {
        mToken = SPUtil.getString(AppConfig.TOKEN);
        checkSDCard();
        mDBTool = new DBTool(mContext);
    }

    public void onDestroy() {
        mRxManager.clear();
    }

    public void destroyLoginInfo(boolean force) {
        if (force) {
            SPUtil.clear();
        }
        FileUtil.rebuild();
    }

    protected boolean checkCode(int code) {
        return code == AppCode.TOKEN_NOT_EXIST || code == AppCode.USER_NO_LOGIN || code == AppCode.NOT_AUTHORIZATION;
    }

    protected void checkSDCard() {
        try {
            mFileUtil = FileUtil.newInstance();
        } catch (ClientIOException e) {
            LogUtil.e(e.getMessage());
            ToastUtil.showShort(BaseApplication.getAppResources().getString(R.string.sdcard_not_available));
        }
    }

    /**
     * 再次登录
     *
     * @param requestCode
     */
    protected void reLogin(final String requestCode) {
        String password = StringUtil.decrypt(SPUtil.getString(AppConfig.PASSWORD));
        mModel.login(SPUtil.getString(AppConfig.ACCOUNT), password).subscribe(new Consumer<BaseResponse<String>>() {
            @Override
            public void accept(@NonNull BaseResponse<String> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    mToken = res.getResult();
                    SPUtil.putString(AppConfig.TOKEN, res.getResult());
                } else {
                    LogUtil.d("再次登录失败" + requestCode);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
               handlerThrowable(requestCode,throwable);
            }
        });
    }

    /**
     * 根据资源id获取字符串
     *
     * @param resId
     * @return
     */
    protected String getText(int resId) {
        return App.getAppContext().getText(resId).toString();
    }

    /**
     * 失败处理
     *
     * @param requestCode 请求码
     * @param throwable
     */
    protected void handlerThrowable(String requestCode, Throwable throwable) {
        if (!NetWorkUtil.isNetConnected(App.getAppContext())) {
            mView.loadFailure(requestCode, getText(R.string.network_not_available));
        } else {
            String msg;
            if (throwable instanceof ApiException) {
                ApiException ex = ApiExceptionEngine.handleException(throwable);
                msg = ex.getDisplayMessage();
            } else {
                msg = getText(R.string.loading_throwable_tips);
            }
            throwable.printStackTrace();
            mView.loadFailure(requestCode, msg);
        }
    }
}
