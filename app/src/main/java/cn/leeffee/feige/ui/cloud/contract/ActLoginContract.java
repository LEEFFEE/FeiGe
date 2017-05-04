package cn.leeffee.feige.ui.cloud.contract;


import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;

/**
 * Created by lhfei on 2017/3/28.
 */

public interface ActLoginContract {

    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void login(String loginAccount, String password,String requestCode);
        public abstract void unSubscribe();
    }

    abstract class Model extends BaseModel {

    }
}