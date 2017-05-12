package cn.leeffee.feige.ui.cloud.contract;


import java.util.List;

import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiGroup;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import io.reactivex.Observable;


/**
 * Created by lhfei on 2017/3/21.
 */

public interface FragGroupContract {

    interface View extends BaseView {

    }

    abstract class Presenter extends BasePresenter<View, Model> {

    }

    abstract class Model extends BaseModel {
        public abstract Observable<BaseResponse<List<ApiGroup>>> listGroups(String token);

        public abstract Observable<BaseResponse<List<ApiFile>>> listGroupFile(String currentRemotePath, String groupId, String token);

        public abstract Observable<BaseResponse<Object>> makeGroupDir(String remotePath, String groupId, String token);

        public abstract Observable<BaseResponse<String>> removeGroupFile(USpaceFile[] files, String groupId, String token);

        public abstract Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token);

        public abstract Observable<BaseResponse<String>> copy2PrivateSpace(USpaceFile[] copyFiles, String copyToPath, String groupId, String token);
    }
}