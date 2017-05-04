package cn.leeffee.feige.base;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import cn.leeffee.feige.listener.FragmentBackListener;

/**
 * Created by lhfei on 2017/3/27.
 */

public abstract class BasicLazyFragment extends Fragment implements FragmentBackListener {
    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible;

    /**
     * 在这里实现Fragment数据的缓加载.
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected abstract void lazyLoad();

    protected void onInvisible() {
    }

    /**
     *Fragment回退键拦截， 默认不拦截
     * @return 是否拦截 默认false；
     * @param keyCode
     * @param event
     */
    @Override
    public boolean onBackForward(int keyCode, KeyEvent event) {
        return false;
    }
}
