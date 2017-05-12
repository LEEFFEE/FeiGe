package cn.leeffee.feige.ui.cloud.api;


import cn.leeffee.feige.utils.PropertyUtil;

/**
 * Created by lhfei on 2017/3/27.
 */

public class ApiConstants {
    /**
     * 时光网
     * http://api.m.mtime.cn/PageSubArea/TrailerList.api
     */
    private static final String HOST_MTIME = "http://api.m.mtime.cn/";
    /**
     * 干货集中营
     * http://gank.io/api/data/Android/40/1
     */
    private static final String HOST_GANK = "http://gank.io/";
    /**
     * USPACE主机地址
     * http://172.16.60.202/
     */
    // private static final String HOST_USPACE = "http://172.16.60.202/";
    // private static String HOST_USPACE = getBaseUrl();

    /**
     * 获取对应的host
     *
     * @param hostType host类型
     * @return host
     */
    public static String getHost(int hostType) {
        String host;
        switch (hostType) {
            case HostType.HOST_MTIME:
                host = HOST_MTIME;
                break;
            case HostType.HOST_GANK:
                host = HOST_GANK;
                break;
            case HostType.HOST_USPACE:
                host = PropertyUtil.getInstance().getBaseUrl();
                break;
            default:
                host = "";
                break;
        }
        return host;
    }

    public static final String REQUEST_PARAMS_KEY = "jsonParams";
    private static final String TAG = "USpaceAPI";
    private static final String RESPONSE_ERROR_CODE = "errorCode";
    private static final String RESPONSE_ERROR_MESSAGE = "errorMessage";
    public static final String RESPONSE_RESULT = "result";
    public static final int CLIENT_TYPE = 4;
    public static final String STRING_PARSE_ERROR = "解析数据错误.";

    // public static final String BASE_URL = PropertyUtil.getScheme(false) +
    // PropertyUtil.getAppServer();
    public static final String USER_URL = "uspace/user.action";
    public static final String FILE_URL = "uspace/file.action";
    public static final String DOWNLOAD_SHARED_FILE_URL = "uspace/file!pickup.action";
    public static final String UPLOAD_FILE_URL = "uspace/FileUpload";
    public static final String DOWNLOAD_FILE_URL = "uspace/file/downFile";
    public static final String GET_PUBLISH_INFO_URL = "uspace/file!getPublishInfoByCode.action";
    public static final String SHARED_URL = "uspace/shared";
    public static final String INCREASE_URL = "uspace/increase";
    public static final String SYSTEM_URL = "uspace/system";
    public static final String GROUP_URL = "uspace/shareGroup";
    public static final String DOWNLOAD_GROUP_FILE_URL = "uspace/shareGroup/downFile";
    public static final String UPLOAD_GROUP_FILE_URL = "uspace/ShareGroupFileUpload";
    public static final String GROUP_LOGS_URL = "uspace/log";
    public static final String REGISTER_URL = "ucenter/subscriber.action";
    public static final String SEARCH_MY_FILES_URL = "uspace/file";
    //http://172.16.60.202/pick/picklink.html?filecode=5b2bcdb4900ead920302aadd2f45792f
    public static final String PICK_PICKLINK = "pick/picklink.html?filecode=";
}
