package cn.leeffee.feige.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import cn.leeffee.feige.manager.RxManager;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.TUtil;

/**
 * des:基类fragment
 */

/***************
 * 使用例子
 *********************/

public abstract class BaseFragment<P extends BasePresenter, M extends BaseModel> extends BasicLazyFragment {
    protected View rootView;
    public P mPresenter;
    public M mModel;
    public RxManager mRxManager;
    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
    // Unbinder bk;

    @Override
    public void onAttach(Context context) {
        LogUtil.e(this.getClass().getSimpleName() + ":onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.e(this.getClass().getSimpleName() + ":onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.e(this.getClass().getSimpleName() + ":onCreateView");
        if (rootView == null)
            rootView = inflater.inflate(getLayoutResource(), container, false);
        mRxManager = new RxManager();
        ButterKnife.bind(this, rootView);
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = this.getActivity();
        }
        //填充各控件的数据
        initPresenter();
        initToolbar();
        initView();
        isPrepared = true;
        lazyLoad();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.e(this.getClass().getSimpleName() + ":onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        LogUtil.e(this.getClass().getSimpleName() + ":onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        LogUtil.e(this.getClass().getSimpleName() + ":onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtil.e(this.getClass().getSimpleName() + ":onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtil.e(this.getClass().getSimpleName() + ":onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        LogUtil.e(this.getClass().getSimpleName() + ":onDestroyView");
        super.onDestroyView();
        if (mPresenter != null)
            mPresenter.onDestroy();
        mRxManager.clear();
    }

    @Override
    public void onDestroy() {
        LogUtil.e(this.getClass().getSimpleName() + ":onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        LogUtil.e(this.getClass().getSimpleName() + ":onDetach");
        super.onDetach();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        LogUtil.e(this.getClass().getSimpleName() + ":lazyLoad");
        initData();
    }

    /**
     * 在initPresenter之后，initView之前调用
     */
    protected void initToolbar() {
    }

    //获取布局文件
    protected abstract int getLayoutResource();

    //简单页面无需mvp就不用管此方法即可,完美兼容各种实际场景的变通
    public abstract void initPresenter();

    /**
     * 初始化view
     */
    protected abstract void initView();

    /**
     * 初始化数据
     */
    protected void initData() {
    }

    //    public void showToastWithImg(String text, int res) {
    //        ToastUtil.showToastWithImg(text, res);
    //    }

    //    /**
    //     * 网络访问错误提醒
    //     */
    //    public void showNetErrorTip() {
    //        ToastUitl.showToastWithImg(getText(R.string.net_error).toString(),R.drawable.ic_wifi_off);
    //    }
    //
    //    public void showNetErrorTip(String error) {
    //        ToastUitl.showToastWithImg(error,R.drawable.ic_wifi_off);
    //    }


}
