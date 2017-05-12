package cn.leeffee.feige.ui.cloud.model;


import java.util.List;

import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.FragGroupContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiGroup;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by lhfei on 2017/03/21
 */

public class FragGroupModelImpl extends FragGroupContract.Model {

    @Override
    public Observable<BaseResponse<List<ApiGroup>>> listGroups(String token) {
        String jsonParams = "{method:'findshareGroupByUser', token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).findShareGroupByUser(ApiClient.NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<List<ApiFile>>> listGroupFile(String currentRemotePath, String groupId, String token) {
        String jsonParams = "{method:'listDir',params:{path:'" + currentRemotePath + "',groupId:'" + groupId + "'},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listGroupFile(ApiClient.NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<Object>> makeGroupDir(String remoteDir, String groupId, String token) {
        String jsonParams = "{method:'makeDir',params:{path:'" + remoteDir + "', groupId:'" + groupId + "'},token:'" + token + "'}";
        //成功后返回的数据
        //        {
        //            "createTime": "2017-04-19T13:56:24",
        //                "fileOwnerId": "170306051201026896c73bdda91703c5",
        //                "folderId": 94,
        //                "name": "cccc1232242423"
        //        }
        return ApiClient.getDefault(HostType.HOST_USPACE).makeGroupDir(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> removeGroupFile(USpaceFile[] files, String groupId, String token) {
        String paths = "";
        for (USpaceFile file : files) {
            paths += "{path:\"" + file.getDiskPath() + "\",version:\"" + file.getVersion() + "\",ownerId:\"" + file.getCreaterId() + "\"},";
        }
        paths = paths.substring(0, paths.lastIndexOf(","));
        String jsonParams = "{method:\"remove\",params:{pathList:[" + paths + "], groupId:\"" + groupId + "\"},token:\"" + token + "\"}";
        return ApiClient.getDefault(HostType.HOST_USPACE).removeGroupFile(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token) {
        String jsonParams = "{method:'listDirOnlyDir',params:{path:'" + remotePath + "'},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listDirOnlyDir(ApiClient.NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> copy2PrivateSpace(USpaceFile[] copyFiles, String copyToPath, String groupId, String token) {
        String paths = "";
        for (USpaceFile file : copyFiles) {
            paths += "{path:\"" + file.getDiskPath() + "\",version:\"" + file.getVersion() + "\",ownerId:\"" + file.getCreaterId() + "\"},";
        }
        paths = paths.substring(0, paths.lastIndexOf(","));
        String jsonParams = "{method:\"copy2PrivateSpace\",params:{pathList:[" + paths + "], destPath:\"" + copyToPath + "\", groupId:\"" + groupId + "\"}, token:\"" + token + "\"}";
        return ApiClient.getDefault(HostType.HOST_USPACE).copy2PrivateSpace(ApiClient.NO_NEED_CACHE,jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}