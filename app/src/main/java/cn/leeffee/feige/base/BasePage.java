package cn.leeffee.feige.base;

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.List;

import butterknife.ButterKnife;
import cn.leeffee.feige.App;
import cn.leeffee.feige.ui.cloud.adapter.FileTransBaseAdapter;
import cn.leeffee.feige.utils.TUtil;


/**
 * Created by lhfei on 2017/4/11.
 */

public abstract class BasePage<P extends BasePresenter, M extends BaseModel> {
    private String mTitle;
    private View mView;
    public P mPresenter;
    public M mModel;
    protected Fragment mFrag;

    public BasePage(Fragment frag) {
        this(frag, "");
    }

    public BasePage(Fragment act, String title) {
        mFrag = act;
        this.mTitle = title;
        mView = createView();
        ButterKnife.bind(this, mView);
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = App.getAppContext();
        }
        this.initPresenter();
        this.initView();
    }


    public abstract View createView();

    protected abstract void initPresenter();

    protected abstract void initView();

    public void initData() {

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public abstract void isShowEmpty(List list);

    /**
     * 销毁时执行的方法  销毁时执行的方法  有子类重写
     */
    public void onDestroy() {

    }
    public abstract FileTransBaseAdapter getAdapter();
}
