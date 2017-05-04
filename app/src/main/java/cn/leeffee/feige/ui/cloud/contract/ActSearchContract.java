package cn.leeffee.feige.ui.cloud.contract;


import java.util.List;

import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;

/**
 * Created by lhfei on 2017/4/24.
 */

public interface ActSearchContract {

    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {
    }

    abstract class Model extends BaseModel {
        public abstract Observable<BaseResponse<List<ApiFile>>> listSearchFiles(String keyword, String token);

    }
}