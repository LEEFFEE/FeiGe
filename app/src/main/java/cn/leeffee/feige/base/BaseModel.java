package cn.leeffee.feige.base;


import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * des:baseModel
 */
public abstract class BaseModel {
    public Observable<BaseResponse<String>> login(String loginAccount, String password) {
        String jsonParams = "{method:'userAuthentication',params:{abc:1, userName:'" + loginAccount + "',password:'" + password + "'}}";
        LogUtil.e(jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).login(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
