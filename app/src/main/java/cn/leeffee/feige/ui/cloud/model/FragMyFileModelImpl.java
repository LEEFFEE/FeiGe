package cn.leeffee.feige.ui.cloud.model;


import java.util.List;

import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.contract.FragMyFileContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/03/21
 */

public class FragMyFileModelImpl extends FragMyFileContract.Model {

    @Override
    public Observable<BaseResponse<List<ApiFile>>> listDir(String currentPath, String token) {
        String jsonParams = "{method:'listDir',params:{path:'" + currentPath + "'},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listDir(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> makeDir(String remotePath, String token) {
        String jsonParams = "{method:\"makeDir\",params:{path:\"" + remotePath + "\"},token:\"" + token + "\"}";
        LogUtil.e(jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).makeDir(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> remove(String remotePath, Integer version, String token) {
        String jsonParams = "{method:'remove',params:{pathList:[{path:'" + remotePath + "',version:'" + version + "'}]},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).remove(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> moveFile(String srcPath, String descPath, String token) {
        String jsonParams = "{method:'moveFile',params:{srcFilePath:{path:'" + srcPath + "',version:''}, destFilePath:'" + descPath + "', userId:''},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).moveFile(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> createPublishLink(String filePath, String alias, String token) {
        String jsonParams = "{method:'createPublishLink',params:{alias:'" + alias + "',path:'" + filePath + "'}, token:'" + token + "'}";
        // LogUtil.e(jsonParams);
        return ApiClient.getDefault(HostType.HOST_USPACE).createPublishLink(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<Boolean>> cancelSharedFiles(String[] sharedIds, String token) {
        StringBuffer strSharedIds = new StringBuffer();
        for (int i = 0; i < sharedIds.length; i++) {
            strSharedIds.append("'");
            strSharedIds.append(sharedIds[i]);
            strSharedIds.append("'");
            if (i != sharedIds.length - 1) {
                strSharedIds.append(",");
            }
        }
        String jsonParams = "{method:'cancelShared',params:{sharedId:[" + strSharedIds.toString() + "]}, token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).cancelSharedFiles(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(String remotePath, String token) {
        String jsonParams = "{method:'listDirOnlyDir',params:{path:'" + remotePath + "'},token:'" + token + "'}";
        return ApiClient.getDefault(HostType.HOST_USPACE).listDirOnlyDir(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<BaseResponse<String>> move(String srcPath, String destPath, String token) {
        String jsonParams = "{method:\"move\",params:{srcPathList:[{path:\"" + srcPath + "\",version:''}], destPath:\"" + destPath + "\", userId:''},token:\"" + token + "\"}";
        return ApiClient.getDefault(HostType.HOST_USPACE).move(jsonParams).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}