package cn.leeffee.feige.ui.cloud.contract;


import java.util.List;

import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;

/**
 * Created by lhfei on 2017/3/31.
 */

public interface ActUploadFileExploreContract {


    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void listDirOnlyDir(String remotePath,String requestCode);
    }

    abstract class Model extends BaseModel {
        public abstract Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token);
    }


}