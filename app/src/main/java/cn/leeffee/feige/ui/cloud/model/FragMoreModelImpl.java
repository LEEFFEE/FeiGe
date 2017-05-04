package cn.leeffee.feige.ui.cloud.model;


import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.FragMoreContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/03/23
 */

public class FragMoreModelImpl extends FragMoreContract.Model {

    @Override
    public Observable<BaseResponse<ApiAccountProp>> getAccountProperty(String token) {
        String jsonParams = "{method:'getAccountProperty',params:{propertyName:'all'},token:'" + token + "'}";
        // LogUtil.e("jsonParams=" + jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).getAccountProperty(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}