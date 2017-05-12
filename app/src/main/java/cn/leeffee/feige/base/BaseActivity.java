package cn.leeffee.feige.base;


import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.leeffee.feige.utils.StatusBarCompat;
import cn.leeffee.feige.utils.TUtil;


/**
 * 基类
 */

/***************
 * 使用例子
 *********************/
public abstract class BaseActivity<P extends BasePresenter, M extends BaseModel> extends BasicActivity {
    public P mPresenter;
    public M mModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = this;
        }
        this.initPresenter();
        initToolBar();
        this.initView();
        //        if (Build.VERSION.SDK_INT >= 19) {
        //            //SetStatusBarColor(R.color.uspace_colorPrimary);
        //            SetTranslucentBar();
        //        }
        // StatusBarUtil.setWindowStatusBarColor(this,R.color.uspace_colorPrimary);
        // new SystemBarTintManager();

    }


    @Override
    public void onBasicLoad(@Nullable Bundle savedInstanceState) {

    }

    //简单页面无需mvp就不用管此方法即可,完美兼容各种实际场景的变通
    public abstract void initPresenter();

    //初始化toolbar
    protected void initToolBar() {
    }

    //初始化view
    public abstract void initView();

    /**
     * 着色状态栏（4.4以上系统有效）
     */
    protected void SetStatusBarColor(int color) {
        StatusBarCompat.setStatusBarColor(this, color);
    }

    /**
     * 沉浸状态栏（4.4以上系统有效）
     */
    protected void SetTranslucentBar() {
        StatusBarCompat.translucentStatusBar(this);
    }

    /**
     * 网络访问错误提醒
     */
    //    public void showNetErrorTip() {
    //        ToastUtil.showToastWithImg(getText(R.string.net_error).toString(),R.drawable.ic_wifi_off);
    //    }
    //    public void showNetErrorTip(String error) {
    //        ToastUtil.showToastWithImg(error,R.drawable.ic_wifi_off);
    //    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.onDestroy();
    }
}
