package cn.leeffee.feige.base;


import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.leeffee.feige.utils.TUtil;

/**
 * 基类
 */
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
     * 网络访问错误提醒
     */
    //    public void showNetErrorTip() {
    //        ToastUtil.showToastWithImg(getText(R.string.net_error).toString(),R.drawable.ic_wifi_off);
    //    }
    //    public void showNetErrorTip(String error) {
    //        ToastUtil.showToastWithImg(error,R.drawable.ic_wifi_off);
    //    }
    @Override
    protected void onResume() {
        super.onResume();
        //debug版本不统计crash
        //        if(!BuildConfig.LOG_DEBUG) {
        //            //友盟统计
        //            MobclickAgent.onResume(this);
        //        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //debug版本不统计crash
        //        if(!BuildConfig.LOG_DEBUG) {
        //            //友盟统计
        //            MobclickAgent.onPause(this);
        //        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.onDestroy();
    }
}
