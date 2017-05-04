package cn.leeffee.feige.ui.cloud.contract;


import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;

/**
 * Created by lhfei on 2017/4/10.
 */

public interface ActBackupContract {

    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {
    }

    abstract class Model extends BaseModel {
    }


}