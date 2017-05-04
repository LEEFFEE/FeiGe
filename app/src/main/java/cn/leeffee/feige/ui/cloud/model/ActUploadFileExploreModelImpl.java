package cn.leeffee.feige.ui.cloud.model;


import java.util.List;

import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.ActUploadFileExploreContract;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by lhfei on 2017/03/31
 */

public class ActUploadFileExploreModelImpl extends ActUploadFileExploreContract.Model {
    @Override
    public Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token) {
        String jsonParams = "{method:'listDirOnlyDir',params:{path:'" + remotePath + "'},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listDirOnlyDir(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}