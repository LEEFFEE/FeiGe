package cn.leeffee.feige.ui.cloud.model;


import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.FragSettingContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/03/23
 */

public class FragSettingModelImpl extends FragSettingContract.Model {

    @Override
    public Observable<BaseResponse<ApiAccountProp>> getAccountProperty(String token) {
        String jsonParams = "{method:'getAccountProperty',params:{propertyName:'all'},token:'" + token + "'}";
        // LogUtil.e("jsonParams=" + jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).getAccountProperty(ApiClient.NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> changePassword(String oldPassword, String newPassword, String token) {
        String jsonParams = "{method:'changePassword',params:{oldPassword:'" + oldPassword + "',newPassword:'" + newPassword + "'},token:'" + token + "'}";
        // LogUtil.e("jsonParams=" + jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).changePassword(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}