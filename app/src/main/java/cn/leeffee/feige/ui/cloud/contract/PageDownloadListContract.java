package cn.leeffee.feige.ui.cloud.contract;


import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;

/**
 * Created by lhfei on 2017/4/11.
 */

public interface PageDownloadListContract {

    interface View extends BaseView {
        void loadBefore(String requestCode);

        /**
         * 请求成功后调用
         *
         * @param requestCode 请求码
         * @param result      成功返回集合
         */
        void loadSuccess(String requestCode, Object result);

        /**
         * 请求失败后调用
         *
         * @param requestCode requestCode 请求码
         * @param msg         错误消息
         */
        void loadFailure(String requestCode, String msg);
    }

    abstract class Presenter extends BasePresenter<View, Model> {

    }

    abstract class Model extends BaseModel {
    }
}