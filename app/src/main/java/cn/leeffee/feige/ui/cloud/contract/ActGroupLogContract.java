package cn.leeffee.feige.ui.cloud.contract;


import java.util.List;

import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;

/**
 * Created by lhfei on 2017/4/18.
 */

public interface ActGroupLogContract {

    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {
    }

    abstract class Model extends BaseModel {
        public abstract Observable<BaseResponse<List<ApiGroupLog>>> listGroupLogs(String groupId, int pageIndex, int pageSize, String token);
    }
}