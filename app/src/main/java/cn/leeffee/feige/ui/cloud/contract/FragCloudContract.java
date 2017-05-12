package cn.leeffee.feige.ui.cloud.contract;


import java.util.List;

import cn.leeffee.feige.base.BaseModel;
import cn.leeffee.feige.base.BasePresenter;
import cn.leeffee.feige.base.BaseView;
import cn.leeffee.feige.ui.cloud.api.ApiPath;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;

/**
 * Created by lhfei on 2017/3/21.
 */

public interface FragCloudContract {

    interface View extends BaseView {

    }

    abstract class Presenter extends BasePresenter<View, Model> {

    }

    abstract class Model extends BaseModel {
        public abstract Observable<BaseResponse<List<ApiFile>>> listDir(String currentPath, String token);

        public abstract Observable<BaseResponse<String>> makeDir(String remotePath, String token);

        public abstract Observable<BaseResponse<String>> remove(String remotePath, Integer version, String token);

        /*重命名文件或者文件夹*/
        public abstract Observable<BaseResponse<String>> moveFile(String srcPath, String descPath, String token);

        public abstract Observable<BaseResponse<String>> createPublishLink(String path, String alias, String token);

        public abstract Observable<BaseResponse<Boolean>> cancelSharedFiles(String[] sharedIds, String token);

        public abstract Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token);

        public abstract Observable<BaseResponse<String>> moveTo(List<ApiPath> jsonList, String destPath, String token);

        public abstract Observable<BaseResponse<String>> copyTo(List<ApiPath> jsonList, String destPath, String token);
    }
}