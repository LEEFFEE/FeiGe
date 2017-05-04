package cn.leeffee.feige.ui.cloud.model;


import java.util.List;

import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.ActGroupLogContract;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static cn.leeffee.feige.ui.cloud.api.ApiConstants.CLIENT_TYPE;

/**
 * Created by lhfei on 2017/04/18
 */

public class ActGroupLogModelImpl extends ActGroupLogContract.Model {

    @Override
    public Observable<BaseResponse<List<ApiGroupLog>>> listGroupLogs(String groupId, int pageIndex, int pageSize, String token) {
        // String jsonParams = "{method:'queryShareGroupLogs',params:{'groupId':'" + groupId + "','startDate':'','endDate':'','pageIndex':'" + pageIndex + "','pageSize':'" + pageSize + "','clientType':'" + CLIENT_TYPE + "'},token:'" + token + "'}";
        String jsonParams = "{\"method\":\"queryShareGroupLogs\",\"params\":{\"groupId\":\"" + groupId + "\",\"startDate\":\"\",\"endDate\":\"\",\"pageIndex\":\"" + pageIndex + "\",\"pageSize\":\"" + pageSize + "\",\"clientType\":\"" + CLIENT_TYPE + "\"},\"token\":\"" + token + "\"}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listGroupLogs(jsonParams).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
}