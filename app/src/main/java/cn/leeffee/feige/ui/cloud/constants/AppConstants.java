package cn.leeffee.feige.ui.cloud.constants;


public interface AppConstants {
    String PATTERN = "COOLCLOUDZECLOUD";

    /**
     * fragment的key  对应的值如下01234
     */
    String POSITION_FRAGMENT = "position";
    /**
     * MainActivity界面中Tab页个数
     */
    int MAIN_SIZE_FRAGMENT = 4;
    int POSITION_CLOUD_FRAGMENT = 0;
    int POSITION_GROUP_FRAGMENT = 1;
    int POSITION_TRANS_FRAGMENT = 2;
    int POSITION_SETTING_FRAGMENT = 3;

    String CURRENT_REMOTE_PATH = "currentRemotePath";
    String TITLE_MY_GROUP = "我的群组";

    int EMAIL_ACTIVE_TYPE = 1;
    int MESSAGE_ACTIVE_TYPE = 2;

    int RECIVER_NOT_READ_MSG = 0;

    int RECIVER_READED_MSG = 1;

    int RECIVER_DELETED_MSG = 2;

    int READED = 0;

    int DELETE = 1;

    int NOT_ADMIN_SEND_MSG = 3;

    Integer USPACE_FILE_PUBLISH_PUBLIC = 1;

    Integer USPACE_FILE_PUBLISH_PRIVATE = 2;

    Integer USPACE_FILE_PUBLISH_NONE = 0;

    int WARN_LEVEL = 20 * 1024;

    int CHANGE_PASSWORD_BOTH_SUCCESS = 2; // 修改Ecloud密码及Uspace密码同时成功！

    int CHANGE_PASSWORD_USPACE_FAILED = 3;// 修改Ecloud密码成功，Uspace密码失败！

    int GROUP_FILE = 1;

    /**
     * 上传类型key
     */
    String UPLOAD_TYPE = "uploadType";
    int UPLOAD_PICS = 0;//图片
    int UPLOAD_DOCS = 1;//文档
    int UPLOAD_MUSICS = 2;//音乐
    int UPLOAD_VIDEOS = 3;//视频
    int UPLOAD_CAPTURE = 4;//拍照上传
    int UPLOAD_ALL = 5;//所有类型
    /**
     * 是否群组上传
     */
    String IS_GROUP_UPLOAD = "isGroupUpload";

    int PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    //接口中字段默认为常量  public static final
    String BUNDLE_KEY_LOGIN_ACCOUNT = "login_account";
    String BUNDLE_KEY_PASSWORD = "password";
    String BUNDLE_KEY_PASSWORD_CONFIRM = "password_confirm";
    String BUNDLE_KEY_ACTIVE_TYPE = "active_type";

    String BUNDLE_KEY_ROOT_PATH = "root_path";
    String BUNDLE_KEY_SELECTED_FILE_PATH = "selected_file_path";

    String BUNDLE_KEY_UPLOAD_FILES = "upload_files";

    String BUNDLE_KEY_SELECTED_FILES = "selected_files";

    String BUNDLE_KEY_USPACE_TOKEN = "uspace_token";

    String BUNDLE_KEY_USPACE_TOKEN_NOT_EXIST = "uspace.token.not.exist";

    /**
     * 根路径
     */
    String ROOT_PATH = "/";

}

