package cn.leeffee.feige.ui.cloud.adapter;

import android.view.View;

import butterknife.ButterKnife;
import cn.leeffee.feige.App;

/**
 * Created by lhfei on 2017/5/5.
 */

public abstract class BaseHolder<T> {

    private View rootView;
    private T item;

    /**
     * 当new这个对象时, 就会加载布局, 初始化控件,设置tag
     */
    public BaseHolder() {
        super();
        rootView = App.inflate(getLayoutId());
        ButterKnife.bind(this, rootView);
        // 3. 打一个标记tag
        rootView.setTag(this);
    }

    /**
     * 设置当前item的数据
     */
    public void setItem(T item) {
        this.item = item;
        refreshView(item);
    }

    /**
     * 获取当前item的数据
     */
    public T getItem() {
        return item;
    }

    /**
     * 返回item的布局对象、
     */
    public View getView() {
        return rootView;
    }

    /**
     * 1. 加载布局文件
     */
    public abstract int getLayoutId();

    /**
     * 4. 根据数据来刷新界面
     */
    public abstract void refreshView(T item);
}
