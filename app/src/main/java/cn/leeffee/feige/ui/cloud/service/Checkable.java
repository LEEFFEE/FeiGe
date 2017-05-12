package cn.leeffee.feige.ui.cloud.service;

import java.io.Serializable;

/**
 * Created by lhfei on 2017/5/5.
 */

public class Checkable implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean checked = false;
    private int _id;

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
