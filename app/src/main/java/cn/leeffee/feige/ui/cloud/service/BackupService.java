package cn.leeffee.feige.ui.cloud.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.activity.BackupActivity;
import cn.leeffee.feige.ui.cloud.api.ApiConstants;
import cn.leeffee.feige.ui.cloud.api.ApiException;
import cn.leeffee.feige.ui.cloud.api.ApiOkHttp;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.exception.HttpException;
import cn.leeffee.feige.utils.DateUtil;
import cn.leeffee.feige.utils.FileMD5;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropCacheManager;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;


public class BackupService extends Service {

    private static final String TAG = "BackupService";
    private boolean isStop = false;
    private byte[] lock = new byte[0];

    //服务对外接口
    private BackupBinder mBinder;

    //上下文及数据库处理
    private Context ctx = this;
    private DBTool dbTool = new DBTool(ctx);

    //任务调度处理
    private BackupTask excTask = null;
    private BackupQueueListener listener = null;

    //通知管理
    private NotificationManager nManager;
    public final static int BACKUP_NOTIFY_ID = 3;

    private final static int UPLOAD_FILE_SIZE = 1024 * 8;

    //广播大的目录备份任务
    public static final String BACKUP_RECEIVER_ACTION = "com.uit.uspace.activity.BackupActivity.RECEIVER";
    //广播每一个小的上传文件到我的文件列表
    public static final String BACKUP_FOR_MYFILE_RECEIVER_ACTION = "com.uit.uspace.activity.BackupActivity.ForMyFile.RECEIVER";

    private int last_task_id = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("BackupService onCreate ");
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBinder = new BackupBinder();
        listener = new BackupQueueListener();
        listener.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    /**
     * 对外提供方法
     *
     * @author tangzhiming
     */
    class BackupBinder extends Binder implements IBackupService {
        @Override
        public void unlock() {
            synchronized (lock) {
                Log.i(TAG, "\t\t=== begin to notify bakcupService thread ===");
                lock.notify();
            }
        }

        @Override
        public boolean cancelTask(int id) {
            boolean flag = false;
            if (excTask != null && excTask.getId() == id) {// 正在执行的任务
                excTask.setStatus(ITransferConstants.STATUS_CANCEL);
                Log.i(TAG, "############ Id=" + excTask.getId() + ",userName=" + excTask.getUserName() + ", localPath=" + excTask.getLocalPath() + "############");
                flag = true;
            } else {
                dbTool.deleteBackupTask(id);
                flag = true;
            }
            return flag;
        }

        @Override
        public BackupTask getExcTask() {
            return BackupService.this.getExcTask();
        }

        @Override
        public boolean cancelAllTask() throws RemoteException {
            if (excTask != null) {
                excTask.setStatus(ITransferConstants.STATUS_CANCEL);
            }
            return true;
        }

        @Override
        public void off() {
            if (excTask != null) {
                excTask.setStatus(ITransferConstants.STATUS_PAUSE);
            }
        }

        @Override
        public void on() {

        }

    }

    class BackupQueueListener extends Thread {
        @Override
        public void run() {
            while (!isStop) {
                try {
                    while (!PropCacheManager.getInstance().isBackupOn(ctx) || !NetWorkUtil.isNetConnected(ctx)) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ie) {
                            Log.d(TAG, "\t\t=== backupPrepared thread sleep error ===");
                        }
                    }

                    if (excTask != null) {
                        Log.i(TAG, "--> exc [id=" + excTask.getId() + ", status=" + excTask.getStatus() + "]");
                        if (excTask.getStatus() == ITransferConstants.STATUS_WAIT || excTask.getStatus() == ITransferConstants.STATUS_RUN) {
                            excTask.setStatus(ITransferConstants.STATUS_RUN);
                            updateBackupStatus(excTask.getId(), ITransferConstants.STATUS_RUN);
                            Log.i(TAG, "begin backup======================");
                            backup();
                            Log.i(TAG, "end backup======================");
                        } else if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                            //防止出现取消状态到此，一般不可能
                            Log.i(TAG, "backupTask is cancel status, delete it.");
                            dbTool.deleteBackupTask(excTask.getId());
                            nManager.cancel(BACKUP_NOTIFY_ID);
                            excTask = null;
                        }
                    } else {
                        List<BackupTask> list = dbTool.listBackupQueue(ITransferConstants.STATUS_WAIT, ITransferConstants.STATUS_RUN);
                        if (list.size() > 0) {
                            excTask = list.get(0);
                        } else {
                            synchronized (lock) {
                                Log.i(TAG, "\t\t====== BackupService thread locked ======");
                                lock.wait();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString(), e);
                }
                Log.i(TAG, "BackupService[" + Thread.currentThread() + "]");
            }

            // 保存现场
            if (last_task_id != -1) {
                updateBackupStatus(last_task_id, ITransferConstants.STATUS_WAIT);
                last_task_id = -1;
            }
        }
    }

    /**
     * 备份主方法
     */
    private synchronized void backup() {
        String localFolderPath = excTask.getLocalPath();
        String remotePath = excTask.getRemotePath();
        sendBroadCastForBackup(excTask);
        Notification notify = newBackupNotification();
        // 获取本地目录下的所有文件列表，一个一个上传
        List<File> files = null;
        try {
            files = FileUtil.newInstance().getAllSubFiles(localFolderPath);
        } catch (Exception e) {
            Log.e(TAG, "get sdcard files failure.", e);
            excTask.setStatus(ITransferConstants.SDCARD_IO_EXCEPTION_ERROR);
            updateBackupStatus(excTask.getId(), ITransferConstants.SDCARD_IO_EXCEPTION_ERROR);
            sendBroadCastForBackup(excTask);
            nManager.cancel(BACKUP_NOTIFY_ID);
            excTask = null;
            return;
        }
        String localFilePath = "";
        String uploadPath = "";
        File tmpFile = null;
        int totalNum = files.size();
        excTask.setTotalNum(totalNum);
        UploadTask uTask = null;
        int overNum = 0;
        updateBackupNotification(notify);
        for (int i = 0; i < totalNum; i++) {
            tmpFile = files.get(i);
            localFilePath = tmpFile.getPath();
            uploadPath = getRealUploadPath(localFolderPath, localFilePath, remotePath);
            Log.i(TAG, "begin to backup file to cloudSvr. file:" + localFilePath);
            try {
                uTask = uploadOneFileForBP(localFilePath, uploadPath);
                if (uTask == null) {
                    overNum++;
                    excTask.setOverNum(overNum);
                    continue;
                }
            } catch (Exception e) {
                Log.i(TAG, "backup one file to cloudSvr error and continue to backup failure file.", e);
                modifyStatusByExcept(e);
                if (excTask.getStatus() == ITransferConstants.NET_EXCEPTION) {
                    //网络异常，等待网络ok继续重做
                    excTask.setStatus(ITransferConstants.STATUS_PAUSE);
                }
                if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                    //备份被取消
                    dbTool.deleteBackupTask(excTask.getId());
                    nManager.cancel(BACKUP_NOTIFY_ID);
                    excTask = null;
                    return;
                }
                if (excTask.getStatus() == ITransferConstants.STATUS_PAUSE) {
                    //备份被关闭
                    excTask.setStatus(ITransferConstants.STATUS_WAIT);
                    updateBackupStatus(excTask.getId(), ITransferConstants.STATUS_WAIT);
                    sendBroadCastForBackup(excTask);
                    nManager.cancel(BACKUP_NOTIFY_ID);
                    excTask = null;
                    return;
                }
                //其他异常，重新执行
                i--;
                continue;
            }

            //上传单个文件成功,发送广播
            overNum++;
            excTask.setOverNum(overNum);
            sendBroadCastForUpload(uTask);
            updateBackupNotification(notify);
        }

        //当前目录备份全部ok
        excTask.setStatus(ITransferConstants.STATUS_FINISH);
        excTask.setFinishTime(DateUtil.date2String(new Date(), DateUtil.DATE_YYYY_MM_DD_HH_MM));
        updateBackupFinish(excTask, ITransferConstants.STATUS_FINISH);
        sendBroadCastForBackup(excTask);
        updateBackupNotification(notify);
        excTask = null;
        if (isStop) {
            nManager.cancel(BACKUP_NOTIFY_ID);
        }
    }

    private void modifyStatusByExcept(Exception e) {
        if (e instanceof IOException) {
            if (e instanceof ClientIOException) {
                excTask.setStatus(ITransferConstants.SDCARD_IO_EXCEPTION_ERROR);
            } else if (e instanceof FileNotFoundException) { // token=null时服务端异常
                excTask.setStatus(ITransferConstants.SERVER_RESPONSE_ERROR);
            } else {
                excTask.setStatus(ITransferConstants.NET_EXCEPTION);
            }
        } else if (e instanceof HttpException) {
            excTask.setStatus(ITransferConstants.NET_EXCEPTION);
        } else if (e instanceof ApiException) {
            int code = ((ApiException) e).getCode();
            switch (code) {
                case ITransferConstants.SERVER_RESPONSE_ERROR:
                    excTask.setStatus(ITransferConstants.SERVER_RESPONSE_ERROR);
                    break;
                case ITransferConstants.FILE_NOT_EXIST:
                    excTask.setStatus(ITransferConstants.FILE_NOT_EXIST);
                    break;
                case ITransferConstants.TRANSFER_FAIL_ERROR:
                    excTask.setStatus(ITransferConstants.TRANSFER_FAIL_ERROR);
                    break;
                case ITransferConstants.STATUS_CANCEL:
                    excTask.setStatus(ITransferConstants.STATUS_CANCEL);
                    break;
                case ITransferConstants.STATUS_PAUSE:
                    excTask.setStatus(ITransferConstants.STATUS_PAUSE);
                    break;
                default:
                    excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
                    break;
            }
        } else {
            excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
        }
    }

    /**
     * 断点上传某一个文件,上传完毕后生成一个UploadTask广播到MyFilesActivity
     *
     * @throws ApiException
     * @throws HttpException
     * @throws IOException
     * @throws JSONException
     */
    private synchronized UploadTask uploadOneFileForBP(String localFullPath, String remoteFullPath) throws HttpException, ApiException, IOException, JSONException {

        FileInputStream fis = null;
        DataOutputStream dos = null;
        BufferedReader reader = null;
        RandomAccessFile raFile = null;
        boolean isNeedBackup = true;
        try {
            File file = new File(localFullPath);
            if (!file.exists()) {
                return null;
            }
            String token = SPUtil.getString(AppConfig.TOKEN);
            if (StringUtil.isEmpty(token)) {
                token = ApiOkHttp.login();
            }
            //根据MD5值判断是否需要上传预处理
            String md5 = FileMD5.getFileMD5ByBlock(file);
            isNeedBackup = ApiOkHttp.isNeedUploadByMd5(remoteFullPath, md5, 1, token);
            if (!isNeedBackup) {
                return null;
            }
            //断点续传预处理，获取已上传的文件大小
            long total = file.length();
            long uploadedSize = -1;
            uploadedSize = ApiOkHttp.uploadPreProcess(remoteFullPath, total, 1, 1, token);
            Log.i(TAG, "remoteFile:" + excTask.getRemotePath() + "uploaded-size:" + uploadedSize);

            //写入头及json参数
            String strUrl = getUrlForBP();
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(ApiConstants.REQUEST_PARAMS_KEY, getParamsForBPUpload(remoteFullPath, uploadedSize, total, token));

            //获取偏移量继续上传
            conn.setChunkedStreamingMode(UPLOAD_FILE_SIZE);
            dos = new DataOutputStream(conn.getOutputStream());
            raFile = new RandomAccessFile(file, "rw");
            raFile.seek(uploadedSize);
            byte[] buffer = new byte[UPLOAD_FILE_SIZE];
            int count = 0;
            long length = uploadedSize;
            while (!isStop && excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = raFile.read(buffer)) != -1) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new HttpException(ITransferConstants.WIFI_EXCEPTION);
                }
                dos.write(buffer, 0, count);
                dos.flush();
                length += count;
            }

            if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                throw new ApiException(ITransferConstants.STATUS_CANCEL);
            }
            if (excTask.getStatus() == ITransferConstants.STATUS_PAUSE) {
                throw new ApiException(ITransferConstants.STATUS_PAUSE);
            }
            if (!isStop && length == total) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String retString = reader.readLine();
                JSONObject jsonObj = new JSONObject(retString);
                if (isBPUploadSuccess(jsonObj)) {
                    //构造上传成功的单个uploadTask，用于广播
                    UploadTask uTask = new UploadTask();
                    uTask.setStatus(ITransferConstants.STATUS_FINISH);
                    uTask.setName(file.getName());
                    uTask.setRemotePath(remoteFullPath);
                    uTask.setAddQueueTime(DateUtil.date2String(new Date(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
                    uTask.setFileLength(total);
                    uTask.setUploadLength(length);
                    uTask.setVersion(1);
                    return uTask;
                } else {
                    throw new ApiException(ITransferConstants.SERVER_RESPONSE_ERROR);
                }
            } else {
                throw new ApiException(ITransferConstants.TRANSFER_FAIL_ERROR);
            }
        } finally {
            closeStream(fis, dos, raFile, reader);
        }
    }

    private void closeStream(InputStream in, OutputStream out, RandomAccessFile raFile, BufferedReader br) {
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (Exception e) {
            }
        }
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
            }
        }
        if (raFile != null) {
            try {
                raFile.close();
                raFile = null;
            } catch (Exception e) {
            }
        }
        if (br != null) {
            try {
                br.close();
                br = null;
            } catch (IOException e) {
            }
        }
    }

    private String getUrlForBP() {
        String strUrl = PropertyUtil.getInstance().getScheme(false) + PropertyUtil.getInstance().getServer() + ApiConstants.FILE_URL;
        return strUrl;
    }

    private String getParamsForBPUpload(String remoteFullFilePath, long offset, long fileLength, String token) {
        String _pvalue = "";
        _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + remoteFullFilePath + "\",version:1},offset:" + offset + ",fileLength:" + fileLength + ",userId:''},token:\"" + token + "\"}";
        // _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + StringUtil.getStringByUTF8(remotFullfilePath) + "\",version:1},offset:" + offset + ",fileLength:" + fileLength + ",userId:''},token:\"" + token + "\"}";
        return _pvalue;
    }

    private void updateBackupStatus(Integer taskId, Integer status) {
        Log.i(TAG, "updateBackupStatus : " + "taskId=" + taskId + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<String, Object>();
        statusMap.put("status", status);
        dbTool.updateBackupTask(taskId, statusMap);
    }

    private void updateBackupFinish(BackupTask task, Integer status) {
        Log.i(TAG, "updateBackupFinish : " + "taskId=" + task.getId() + ", finishTime=" + task.getFinishTime() + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<String, Object>();
        statusMap.put("status", status);
        String finishTime = task.getFinishTime();
        if (finishTime != null && !finishTime.equals("")) {
            statusMap.put("finishTime", task.getFinishTime());
        }
        dbTool.updateBackupTask(task.getId(), statusMap);
    }

    public BackupTask getExcTask() {
        return excTask;
    }

    public void setExcTask(BackupTask excTask) {
        this.excTask = excTask;
    }

    @Override
    public void onDestroy() {
        isStop = true;// 自然结束
        if (excTask != null) {
            last_task_id = excTask.getId();
            excTask.setStatus(ITransferConstants.STATUS_WAIT);// 结束当前任务
            excTask = null;
        }
        nManager.cancel(BACKUP_NOTIFY_ID);
        Log.i(TAG, "************* backupService destroy *****************");
        super.onDestroy();
    }

    /**
     * 判断断点续传是否成功
     * 成功：{"errorCode":0,"errorMessage":null,"result":1}
     * 失败：{"errorCode":60007,"errorMessage":"Failed to move file","result":null}
     *
     * @param jsonObj
     * @return
     */
    public static boolean isBPUploadSuccess(JSONObject jsonObj) {
        try {
            int retVer = jsonObj.getInt("result");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 发送广播，更新我的文件列表(MyFilesActivity)
     *
     * @param uTask
     */
    public void sendBroadCastForUpload(UploadTask uTask) {
        Intent intent = new Intent(BackupService.BACKUP_FOR_MYFILE_RECEIVER_ACTION);
        intent.putExtra("task", uTask);
        ctx.sendBroadcast(intent);
    }

    /**
     * 发送广播，更新备份页面(BackupActivity)
     *
     * @param bTask
     */
    public void sendBroadCastForBackup(BackupTask bTask) {
        Intent intent = new Intent(BackupService.BACKUP_RECEIVER_ACTION);
        intent.putExtra("task", bTask);
        ctx.sendBroadcast(intent);
    }

    public Notification newBackupNotification() {
        //通知标题
        String tickText = "自动备份";
        // 通过通知来进入备份列表
        Intent intent = new Intent(BackupService.this, BackupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Notification notification = new Notification(R.mipmap.backup_title, tickText, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.contentView = new RemoteViews(getPackageName(), R.layout.backup_progress);
        notification.contentView.setTextViewText(R.id.backup_dir_name, "备份目录:" + excTask.getLocalPath());
        notification.contentView.setTextViewText(R.id.backup_complete_size, "");

        final Context cxt = getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pendingIntent;
        return notification;
    }

    public void updateBackupNotification(Notification notify) {
        int what = excTask.getStatus();
        String completeSize = "已完成:" + excTask.getOverNum() + "个|总数" + excTask.getTotalNum() + "个";
        switch (what) {
            case ITransferConstants.STATUS_RUN:
                notify.contentView.setTextViewText(R.id.backup_dir_name, "正在备份:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, completeSize);
                break;
            case ITransferConstants.STATUS_FINISH:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份完成:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, completeSize);
                break;
            case ITransferConstants.SERVER_RESPONSE_ERROR:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "服务器响应错误，请重新上传");
                break;
            case ITransferConstants.NET_EXCEPTION:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "网络异常，停止上传");
                break;
            case ITransferConstants.WIFI_EXCEPTION:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "WIFI异常，停止上传");
                break;
            case ITransferConstants.FILE_NOT_EXIST:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "上传文件不存在");
                break;
            case ITransferConstants.UNKNOWN_ERROR:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "未知异常，请重新上传");
                break;
            default:
                notify.flags = Notification.FLAG_AUTO_CANCEL;
                notify.icon = android.R.drawable.stat_notify_error;
                notify.contentView.setImageViewResource(R.id.backupImg, android.R.drawable.stat_sys_warning);
                notify.contentView.setTextViewText(R.id.backup_dir_name, "备份失败:" + excTask.getLocalPath());
                notify.contentView.setTextViewText(R.id.backup_complete_size, "备份失败");
                break;
        }
        nManager.notify(BackupService.BACKUP_NOTIFY_ID, notify);
    }

    /**
     * 获取云端上传路径
     *
     * @param localFolderPath /sdcard/test/xxx
     * @param localFilePath   /sdcard/test/xxx/test.txt
     * @param remotePath      /MyBackup/ME525+
     * @return /MyBackup/ME525+/xxx/test.txt
     */
    public static String getRealUploadPath(String localFolderPath, String localFilePath, String remotePath) {
        //sdcard绝对路径/mnt/sdcard
        //String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String realRemotePath = "";
        String folderName = localFolderPath.substring(localFolderPath.lastIndexOf("/") + 1);
        realRemotePath = remotePath + File.separator + folderName + localFilePath.substring(localFolderPath.length());
        return realRemotePath;
    }

    private void updateStatus(Integer taskId, Integer status) {
        Log.d(TAG, "updateBackupStatus : " + "taskId=" + taskId + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<String, Object>();
        statusMap.put("status", status);
        dbTool.updateBackupTask(taskId, statusMap);
    }

}
