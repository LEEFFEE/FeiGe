package cn.leeffee.feige.ui.cloud.api;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.OkHttpUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;

import static cn.leeffee.feige.ui.cloud.api.ApiConstants.FILE_URL;

/**
 * Created by lhfei on 2017/4/12.
 */

public class ApiOkHttp {
    /**
     * 登录到网盘
     */
    public static String login() {
        String loginAccount = SPUtil.getString(AppConfig.ACCOUNT);
        String password = StringUtil.decrypt(SPUtil.getString(AppConfig.PASSWORD));
        String json = "jsonParams={method:'userAuthentication',params:{abc:1, userName:'" + loginAccount + "',password:'" + password + "'}}";
        try {
            BaseResponse<String> res = OkHttpUtil.getInstance().<String>postSync(PropertyUtil.getInstance().getBaseUrl() + ApiConstants.USER_URL, json);
            if (res.getErrorCode() == 0) {
                return res.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long uploadShareGroupPreProcess(String uploadPath, long fileLength, int version, int operationState, String groupId, String token) {
        long uploadedSize = 0;
        String json = "jsonParams={method:\"preprocess\",params:{filePath:{path:\"" + uploadPath + "\",version:" + version + "},size:" + fileLength + ",operationState:" + operationState + ",groupId:\"" + groupId + "\",userId:''},token:\"" + token + "\"}";
        try {
            BaseResponse<Double> res = OkHttpUtil.getInstance().postSync(PropertyUtil.getInstance().getBaseUrl() + ApiConstants.GROUP_URL, json);
            if (res.getErrorCode() == 0) {
                double result = res.getResult();
                uploadedSize = (long) result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadedSize;
    }

    public static long uploadPreProcess(String uploadPath, long fileLength, int version, int operationState, String token) {
        long uploadedSize = 0;
        String json = "jsonParams={method:\"preprocess\",params:{filePath:{path:\"" + uploadPath + "\",version:" + version + "}, size:" + fileLength + ", operationState:" + operationState + ",userId:''},token:\"" + token + "\"}";
        try {
            BaseResponse<Double> res = OkHttpUtil.getInstance().postSync(PropertyUtil.getInstance().getBaseUrl() + FILE_URL, json);
            if (res.getErrorCode() == 0) {
                double result = res.getResult();
                uploadedSize = (long) result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.e(uploadedSize + "");
        return uploadedSize;
    }

    public static boolean isNeedUploadByMd5(String uploadPath, String srcMd5, int version, String token) {
        String json = "jsonParams={method:\"isNeedUploadByMd5\",params:{filePath:{path:\"" + uploadPath + "\",version:" + version + "}, srcMd5:\"" + srcMd5 + "\",userId:''},token:\"" + token + "\"}";
        boolean isNeedUpload = false;
        try {
            BaseResponse<Boolean> res = OkHttpUtil.getInstance().postSync(PropertyUtil.getInstance().getBaseUrl() + FILE_URL, json);
            if (res.getErrorCode() == 0) {
                isNeedUpload = res.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isNeedUpload;
    }
}
