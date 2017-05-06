package cn.leeffee.feige;


import cn.leeffee.feige.base.BaseApplication;
import cn.leeffee.feige.ui.cloud.service.IBackupService;
import cn.leeffee.feige.ui.cloud.service.IDownloadService;
import cn.leeffee.feige.ui.cloud.service.IUploadService;

/**
 * Created by lhfei on 2017/3/21.
 */

public class App extends BaseApplication {
    public static final String TAG = "USpaceApplication";

    public static IDownloadService downloadService;
    public static IUploadService uploadService;

    public static IBackupService backupService;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
