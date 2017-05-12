package cn.leeffee.feige.ui.cloud.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhfei on 2017/5/5.
 */

public abstract class USpaceBaseAdapter<T> extends BaseAdapter {
    private List<T> data;
    public USpaceBaseAdapter() {
        super();
        this.data = new ArrayList<>();
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return data != null ? data.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseHolder<T> holder;
        if (convertView == null) {
            holder = getHolder(position);
        } else {
            holder = (BaseHolder) convertView.getTag();
        }
        holder.setItem(getItem(position));
        return holder.getView();
    }

    // 返回当前页面的holder对象
    public abstract BaseHolder<T> getHolder(int position);
}
