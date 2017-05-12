package cn.leeffee.feige.ui.cloud.model;


import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.ActFeedbackContract;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/04/07
 */

public class ActFeedbackModelImpl extends ActFeedbackContract.Model {

    @Override
    public Observable<BaseResponse<String>> addSuggestion(String content, String token) {
        String jsonParams = "{method:\"addSuggestion\",params:{content:\"" + content + "\"},token:\"" + token + "\"}";
      //  LogUtil.e(jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).addSuggestion(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }
}