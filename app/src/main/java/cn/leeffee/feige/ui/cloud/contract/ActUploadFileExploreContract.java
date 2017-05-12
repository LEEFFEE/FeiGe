package cn.leeffee.feige.ui.cloud.contract;


import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;

/**
 * Created by lhfei on 2017/3/31.
 */

public interface ActUploadFileExploreContract {


    interface View extends BaseView {
    }

    abstract class Presenter extends BasePresenter<View, Model> {

    }

    abstract class Model extends BaseModel {
      //  public abstract Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token);
    }


}