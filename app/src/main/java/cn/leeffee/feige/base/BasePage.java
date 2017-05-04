package cn.leeffee.feige.base;

import android.app.Activity;
import android.view.View;
import butterknife.ButterKnife;
import cn.leeffee.feige.App;
import cn.leeffee.feige.utils.TUtil;


/**
 * Created by lhfei on 2017/4/11.
 */

public abstract class BasePage<P extends BasePresenter, M extends BaseModel> {
    private String mTitle;
    private View mView;
    public P mPresenter;
    public M mModel;
    protected Activity mAct;

    public BasePage(Activity act) {
        this(act, null);
    }

    public BasePage(Activity act, String title) {
        mAct = act;
        this.mTitle = title;
        mView = initView();
        ButterKnife.bind(this, mView);
        mPresenter = TUtil.getT(this, 0);
        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = App.getAppContext();
        }
        this.initPresenter();
        this.initView();
    }

    protected abstract void initPresenter();

    public abstract View initView();

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

    /**
     * 销毁时执行的方法  销毁时执行的方法  有子类重写
     */
    public void onDestroy() {

    }
}
