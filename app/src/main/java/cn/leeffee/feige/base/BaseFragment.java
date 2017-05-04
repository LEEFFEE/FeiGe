package cn.leeffee.feige.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import cn.leeffee.feige.manager.RxManager;
import cn.leeffee.feige.utils.TUtil;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * des:基类fragment
 */

/***************
 * 使用例子
 *********************/
//1.mvp模式
//public class SampleFragment extends BaseFragment<NewsChanelPresenter, NewsChannelModel>implements NewsChannelContract.View {
//    @Override
//    public int getLayoutId() {
//        return R.layout.activity_news_channel;
//    }
//
//    @Override
//    public void initPresenter() {
//        mPresenter.setVM(this, mModel);
//    }
//
//    @Override
//    public void initView() {
//    }
//}
//2.普通模式
//public class SampleFragment extends BaseFragment {
//    @Override
//    public int getLayoutResource() {
//        return R.layout.activity_news_channel;
//    }
//
//    @Override
//    public void initPresenter() {
//    }
//
//    @Override
//    public void initView() {
//    }
//}
public abstract class BaseFragment<P extends BasePresenter, M extends BaseModel> extends BasicLazyFragment {
    protected View rootView;
    public P mPresenter;
    public M mModel;
    public RxManager mRxManager;
    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
   // Unbinder bk;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(getLayoutResource(), container, false);
        mRxManager = new RxManager();
        ButterKnife.bind(this, rootView);
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = this.getActivity();
        }
        isPrepared = true;
        lazyLoad();
        return rootView;
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        //填充各控件的数据
        initPresenter();
        initToolbar();
        initView();
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

    //初始化view
    protected abstract void initView();


    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /**
     * 通过Class跳转界面
     **/
    public void startActivityForResult(Class<?> cls, int requestCode) {
        startActivityForResult(cls, null, requestCode);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }


    //    /**
    //     * 开启加载进度条
    //     */
    //    public void startProgressDialog() {
    //        LoadingDialog.showDialogForLoading(getActivity());
    //    }
    //
    //    /**
    //     * 开启加载进度条
    //     *
    //     * @param msg
    //     */
    //    public void startProgressDialog(String msg) {
    //        LoadingDialog.showDialogForLoading(getActivity(), msg, true);
    //    }
    //
    //    /**
    //     * 停止加载进度条
    //     */
    //    public void stopProgressDialog() {
    //        LoadingDialog.cancelDialogForLoading();
    //    }

    public void showToastWithImg(String text, int res) {
        ToastUtil.showToastWithImg(text, res);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null)
            mPresenter.onDestroy();
        mRxManager.clear();
    }
}
