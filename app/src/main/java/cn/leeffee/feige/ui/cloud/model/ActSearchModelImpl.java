package cn.leeffee.feige.ui.cloud.model;

import java.util.List;

import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.ActSearchContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
* Created by lhfei on 2017/04/24
*/

public class ActSearchModelImpl extends ActSearchContract.Model{
    @Override
    public Observable<BaseResponse<List<ApiFile>>> listSearchFiles(String keyword, String token) {
        String jsonParams = "{method:'listSearchDir',params:{searchString :'" + keyword + "',userId :\"\"},token:\"" + token + "\"}";
        LogUtil.e("jsonParams=" + jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).listSearchDir(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}