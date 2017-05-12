package cn.leeffee.feige.ui.cloud.adapter;

import java.util.ArrayList;
import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.service.Checkable;

/**
 * Created by lhfei on 2017/5/5.
 */

public abstract class FileTransBaseAdapter<T extends Checkable> extends USpaceBaseAdapter<T> {
    private boolean multiCheck = false;
    public DBTool mDBTool;

    public boolean isMultiCheck() {
        return multiCheck;
    }

    public void setMultiCheck(boolean multiCheck) {
        this.multiCheck = multiCheck;
    }

    public FileTransBaseAdapter() {
        super();
        mDBTool = new DBTool(App.getAppContext());
    }

    /**
     * 切换是否多模式
     *
     * @return 返回改变后的模式
     */
    public boolean changeMode() {
        multiCheck = !multiCheck;
        if (multiCheck) {
            for (T chk : getData()) {
                chk.setChecked(false);
            }
        }
        notifyDataSetChanged();
        return multiCheck;
    }

    /**
     * 全选
     *
     * @return 选中的数量
     */
    public int selectAll() {
        for (T chk : getData()) {
            chk.setChecked(true);
        }
        notifyDataSetChanged();
        return getData().size();
    }

    /**
     * 全不选
     *
     * @return 选中的数量
     */
    public void selectNoAll() {
        for (T chk : getData()) {
            chk.setChecked(false);
        }
        notifyDataSetChanged();
    }

    /**
     * 通知是否选中对应位置的条目
     *
     * @param taskId  该条目在data数据中的位置
     * @param isChecked 状态是否选中
     */
    //    public void notifyIsChecked(int taskId, boolean isChecked) {
    //       // getData().get(position).setChecked(isChecked);
    //    }

    /**
     * 获取选中的条目数量
     *
     * @return 返回选中的条目数量
     */
    public List<T> getSelectedTasks() {
        List<T> list = new ArrayList<>();
        for (T chk : getData()) {
            if (chk.isChecked())
                list.add(chk);
        }
        return list;
    }

    /**
     * 删除选中的条目数据
     */
    public abstract void delete();

    //    {
    //        Iterator<T> it = getData().iterator();
    //        while (it.hasNext() && it.next().isChecked()) {
    //            it.remove();
    //        }
    //        notifyDataSetChanged();
    //    }

}
