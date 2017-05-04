package cn.leeffee.feige.ui.cloud.api;
import java.util.List;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.ApiGroup;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;
import cn.leeffee.feige.ui.cloud.entity.ApiOnlyFolder;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by lhfei on 2017/3/24.
 */

public interface ApiService {
    //请求不同类型干货（通用）
    //    @GET("api/data/{type}/40/{page}")
    //    Observable<GanHuoData> getGanHuoData(@Path("type") String type, @Path("page") int page);

    /**
     * 用户登录
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.USER_URL)
    Observable<BaseResponse<String>> login(@Field("jsonParams") String jsonParams);

    /**
     * 请求当前路径下的文件和文件夹
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<List<ApiFile>>> listDir(@Field("jsonParams") String jsonParams);

    /**
     * 请求远程当前路径下的文件夹
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<List<ApiOnlyFolder>>> listDirOnlyDir(@Field("jsonParams") String jsonParams);

    /**
     * 创建目录
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<String>> makeDir(@Field("jsonParams") String jsonParams);

    /**
     * 删除文件或者文件夹  或者文件集合
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<String>> remove(@Field("jsonParams") String jsonParams);

    /**
     * 重命名 文件或者文件夹
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<String>> moveFile(@Field("jsonParams") String jsonParams);

    /**
     * 创建共享链接
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.SHARED_URL)
    Observable<BaseResponse<String>> createPublishLink(@Field("jsonParams") String jsonParams);

    /**
     * 取消分享
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.SHARED_URL)
    Observable<BaseResponse<Boolean>> cancelSharedFiles(@Field("jsonParams") String jsonParams);

    /**
     * 加载用户信息
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.USER_URL)
    Observable<BaseResponse<ApiAccountProp>> getAccountProperty(@Field("jsonParams") String jsonParams);

    /**
     * 意见反馈
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.SYSTEM_URL)
    Observable<BaseResponse<String>> addSuggestion(@Field("jsonParams") String jsonParams);

    /**
     * 获取最新的客户端版本
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.SYSTEM_URL)
    Observable<BaseResponse<String>> getLatestClient(@Field("jsonParams") String jsonParams);

    /**
     * 移动文件或者文件夹
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.FILE_URL)
    Observable<BaseResponse<String>> move(@Field("jsonParams") String jsonParams);

    /**
     * 查找当前用户所有组信息
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_URL)
    Observable<BaseResponse<List<ApiGroup>>> findshareGroupByUser(@Field("jsonParams") String jsonParams);

    /**
     * 列出当前群组的文文件信息
     *
     * @param jsonParams 请求参数{method:"listDir",params:{path:"" + path + "\",groupId:\"" + groupId + "\"},token:\"" + token + ""}
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_URL)
    Observable<BaseResponse<List<ApiFile>>> listGroupFile(@Field("jsonParams") String jsonParams);

    /**
     * 创建群组目录
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_URL)
    Observable<BaseResponse<Object>> makeGroupDir(@Field("jsonParams") String jsonParams);

    /**
     * 删除组文件或者文件夹
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_URL)
    Observable<BaseResponse<String>> removeGroupFile(@Field("jsonParams") String jsonParams);

    /**
     * 复制到个人(私人)空间
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_URL)
    Observable<BaseResponse<String>> copy2PrivateSpace(@Field("jsonParams") String jsonParams);

    /**
     * 创建群组目录
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.GROUP_LOGS_URL)
    Observable<BaseResponse<List<ApiGroupLog>>> listGroupLogs(@Field("jsonParams") String jsonParams);
    /**
     * 搜索文件
     *
     * @param jsonParams
     * @return
     */
    @FormUrlEncoded
    @POST(ApiConstants.SEARCH_MY_FILES_URL)
    Observable<BaseResponse<List<ApiFile>>> listSearchDir(@Field("jsonParams") String jsonParams);
}

