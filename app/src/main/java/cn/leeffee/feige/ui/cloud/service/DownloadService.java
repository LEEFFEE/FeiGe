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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.activity.MainActivity;
import cn.leeffee.feige.ui.cloud.api.ApiConstants;
import cn.leeffee.feige.ui.cloud.api.ApiException;
import cn.leeffee.feige.ui.cloud.api.ApiOkHttp;
import cn.leeffee.feige.ui.cloud.constants.AppCode;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.exception.HttpException;
import cn.leeffee.feige.ui.cloud.exception.IClientExceptionCode;
import cn.leeffee.feige.ui.cloud.fragment.FileTransFragment;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;


public class DownloadService extends Service {

    private static final String TAG = "DownLoadService";
    private boolean isStop = false;
    private byte[] lock = new byte[0];

    public final static int UPDATE_NOTIFY_PROGRESS = 2; // 更新进度
    public final static int UPDATE_NOTIFY_COMPLETE = 3; // 更新进度条到完成状态（100%）
    public final static int DOWNLOAD_EXCEPTION_NOTIFY = 4; // 下载发送异常，如IO异常
    public final static int DOWNLOAD_CANCEL_NOTIFY = 5;
    private final static int DOWNLOAD_FILE_SIZE = 1024 * 8; // 缓冲池
    private DownloadBinder mBinder;
    private NotificationManager nManager;

    private DownloadTask excTask = null;

    public static final String DOWNLOAD_RECEIVER_ACTION = "com.uit.uspace.DOWNLOAD.RECEIVER.ACTION";

    private Context ctx = this;
    private DBTool dbTool = new DBTool(ctx);
    //  private YopanApi uspaceApi;

    private final static int DOWNLOAD_NOTIFY_ID = 2;

    private DownloadTaskListener listener = null;

    /**
     * 上一次执行的任务id
     */
    private int last_task_id = -1;

    private String DOWNLOAD_SHARED_FILE_URL = getBaseUrl() + "/uspace/shared!downSharedFile.action?path={0}&code={1}";

    private String DOWNLOAD_SHARED_FOLDER_URL = getBaseUrl() + "/uspace/shared!downSharedFolder.action?path={0}&code={1}";

    /**
     * 获取基本url  如https://172.16.60.202/
     *
     * @return
     */
    public String getBaseUrl() {
        return PropertyUtil.getInstance().getBaseUrl();
        // return Configuration.getScheme(false) + Configuration.getAppServer();
    }

    /**
     * 尝试请求网络次数
     */
    private int try_times = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("DownloadService onCreate ");
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBinder = new DownloadBinder();
        //  uspaceApi = YopanApplication.yopanApi;

        listener = new DownloadTaskListener();
        listener.start();
    }

    class DownloadTaskListener extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                while (!isStop) {
                    try {
                        if (excTask != null) {
                            Log.d(TAG, "--> exc [id=" + excTask.getId() + ", status=" + excTask.getStatus() + "]");
                            if (excTask.getStatus() == ITransferConstants.STATUS_WAIT || excTask.getStatus() == ITransferConstants.STATUS_RUN) {
                                excTask.setStatus(ITransferConstants.STATUS_RUN);
                                updateStatus(excTask.getId(), ITransferConstants.STATUS_RUN);
                                download();
                            }
                        } else {
                            List<DownloadTask> list = dbTool.listDownloadQueue(ITransferConstants.STATUS_WAIT, ITransferConstants.STATUS_RUN);
                            if (list.size() > 0) {
                                excTask = list.get(0);
                                try_times = 3;
                            } else {
                                synchronized (lock) {
                                    Log.d(TAG, "\t\t=== downloadService thread locked ===");
                                    lock.wait();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Download Service = " + e.toString());
                    }
                    Log.d(TAG, "DownloadService[" + Thread.currentThread() + "]");
                }
            }

            if (last_task_id != -1) {
                updateStatus(last_task_id, ITransferConstants.STATUS_WAIT);
                last_task_id = -1;
            }
        }
    }


    private void downloadSharedFile(int type) {
        if (type == DownloadTask.OWN_FILE_TYPE) {
            return;
        }
        Notification notify = newNotification();
        String code = excTask.getCode();
        String path = excTask.getPath();
        String strUrl = "";
        if (type == DownloadTask.SHARED_FILE_TYPE) {
            strUrl = MessageFormat.format(getBaseUrl() + DOWNLOAD_SHARED_FILE_URL, URLEncoder.encode(path), code);
        } else if (type == DownloadTask.SHARED_FOLDER_TYPE) {
            strUrl = MessageFormat.format(getBaseUrl() + DOWNLOAD_SHARED_FOLDER_URL, URLEncoder.encode(path), code);
        }
        HttpURLConnection urlConn = null;
        InputStream fis = null;
        OutputStream fos = null;
        File tmp = null;
        try {
            tmp = FileUtil.newInstance().createSharedFileInSDCard(excTask.getSavePath() + ".tmp");
            fos = new FileOutputStream(tmp);
            URL url = new URL(strUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            if (urlConn.getResponseCode() != 200) {
                throw new Exception("服务器响应错误，错误码：" + urlConn.getResponseCode());
            }
            fis = urlConn.getInputStream();
            long total = excTask.getFileLength();
            byte[] buffer = new byte[DOWNLOAD_FILE_SIZE];
            long length = 0;
            int count = 0;
            recordFileSize();
            while (excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = fis.read(buffer)) > 0) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new Exception("wifi 异常");
                }
                try {
                    fos.write(buffer, 0, count);
                    fos.flush();
                } catch (Exception e) {
                    throw new ClientIOException(IClientExceptionCode.SDCARD_NOT_AVAILABLE_ERROR, "sdcard 存储错误");
                }
                length += count;
                handle(notify, length, total);
            }
            lastCheck(notify, tmp, type);
            updateCompleteSize();
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
            exceptionProcessor(notify, tmp, e);
        } finally {
            ioClose(fis, fos);
            if (isStop) {
                nManager.cancel(DOWNLOAD_NOTIFY_ID);
            }
            excTask = null;
        }
    }

    /**
     * 处理通知
     *
     * @param notify
     * @param length
     * @param total
     * @return
     */
    private int handle(Notification notify, long length, long total) {
        excTask.setDownloadLength(length);
        int percent = excTask.getPercent();
        int nowPercent = (int) (((float) length / (float) total) * 100);

        if (nowPercent > percent && nowPercent < 100) {// 如果百分比有变动则更新进度条
            percent = nowPercent;
            excTask.setPercent(percent); // 更新队列中Queue的已下载百分比
            if (percent < 3 || (percent % 3 == 0)) {
                updateCompleteSize();//更新数据库下载完成大小
                sendBroadCast();// 广播到 Activity
                updateNoticeProgress(notify);//更新通知
            }
        }
        return percent;
    }

    private void downloadOwnFile() {
        Notification notify = newNotification();
        updateNoticeProgress(notify);
        String token = SPUtil.getString(AppConfig.TOKEN);
        InputStream fis = null;
        OutputStream fos = null;
        File tmp = null;
        try {
            if (StringUtil.isEmpty(token)) {
                // token = uspaceApi.relogin();
            }
            tmp = FileUtil.newInstance().createUspaceFileInSDCard(excTask.getSavePath() + ".tmp");
            fos = new FileOutputStream(tmp);
            String strUrl = getUrl(token);
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            fis = conn.getInputStream();
            long total = excTask.getFileLength();
            byte[] buffer = new byte[DOWNLOAD_FILE_SIZE];
            long length = 0;
            int count = 0;
            excTask.setFileLength(total); // 文件长度
            recordFileSize();
            while (!isStop && excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = fis.read(buffer)) > 0) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new Exception("wifi 环境不ok");
                }
                try {
                    fos.write(buffer, 0, count);
                    fos.flush();
                } catch (Exception e) {
                    throw new ClientIOException(IClientExceptionCode.SDCARD_NOT_AVAILABLE_ERROR, "sdcard 存储错误");
                }
                length += count;
                handle(notify, length, total);
            }
            lastCheck(notify, tmp, DownloadTask.OWN_FILE_TYPE);
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
            // exceptionProcessor(notify, tmp, e);
        } finally {
            ioClose(fis, fos);
            updateCompleteSize();
            if (isStop) {
                nManager.cancel(DOWNLOAD_NOTIFY_ID);
            }
        }
    }

    @SuppressWarnings("static-access")
    private void downloadOwnFileForBP() {
        Notification notify = newNotification();
        //updateProgressBar(notify);
        String token = SPUtil.getString(AppConfig.TOKEN);
        InputStream fis = null;
        OutputStream fos = null;
        RandomAccessFile raFile = null;
        File tmp = null;
        try {
            if (StringUtil.isEmpty(token)) {
                token = ApiOkHttp.login();
            }
            String localTmpFilePath = excTask.getSavePath() + ".tmp" + "." + excTask.getVersion();
            tmp = FileUtil.newInstance().createBPTmpFileInSDCard(localTmpFilePath);
            long offset = FileUtil.newInstance().getBPTmpFileSizeInSDCard(localTmpFilePath);
            raFile = new RandomAccessFile(tmp, "rw");
            raFile.seek(offset);
            String strUrl = getUrlForBP(excTask.getIsGroupFile());
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
            conn.setRequestProperty(ApiConstants.REQUEST_PARAMS_KEY, getParamsForBPDownload(offset, token));
            fis = conn.getInputStream();
            long total = excTask.getFileLength();
            byte[] buffer = new byte[DOWNLOAD_FILE_SIZE];
            long length = offset;
            int count = 0;
            // excTask.setFileLength(total); // 文件长度
            recordFileSize();
            while (!isStop && excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = fis.read(buffer)) > 0) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new Exception("wifi 环境不ok");
                }
                try {
                    raFile.write(buffer, 0, count);
                } catch (Exception e) {
                    throw new ClientIOException(IClientExceptionCode.SDCARD_NOT_AVAILABLE_ERROR, "sdcard 存储错误");
                }
                length += count;
                handle(notify, length, total);
            }
            updateCompleteSize();
            lastCheck(notify, tmp, DownloadTask.OWN_FILE_TYPE);
            return;
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
            modifyStatusByExcept(e);
            try_times--;
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                    raFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ioClose(fis, fos);
            if (isStop) {
                nManager.cancel(DOWNLOAD_NOTIFY_ID);
            }
        }
        //失败未到三次，继续做。三次到后广播，并检查网络
        if (try_times > 0) {
            excTask.setStatus(ITransferConstants.STATUS_WAIT);
        } else {
            updateStatus(excTask.getId(), excTask.getStatus());
            sendBroadCast();
            updateNoticeProgress(notify);
            if (excTask.getStatus() == ITransferConstants.NET_EXCEPTION) {
                handleMsg(ITransferConstants.NET_EXCEPTION);
                while (!NetWorkUtil.isNetConnected(ctx)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Log.d(TAG, "\t\t=== isNetPrepared thread sleep error ===");
                    }
                }
                excTask.setStatus(ITransferConstants.STATUS_WAIT);
                try_times = 3;
            } else {
                excTask = null;
            }
        }
    }

    private void modifyStatusByExcept(Exception e) {
        //tmp.delete();
        if (e instanceof IOException) {
            if (e instanceof ClientIOException) {
                excTask.setStatus(ITransferConstants.SDCARD_IO_EXCEPTION_ERROR);
            } else {
                excTask.setStatus(ITransferConstants.NET_EXCEPTION);
            }
        } else if (e instanceof HttpException) {
            excTask.setStatus(ITransferConstants.NET_EXCEPTION);
        } else if (e instanceof ApiException) {
            int code = ((ApiException) e).getCode();
            if (code == AppCode.PICKUP_CODE_ERROR) {
                excTask.setStatus(ITransferConstants.GET_CODE_ERROR);
            } else {
                excTask.setStatus(ITransferConstants.SERVER_RESPONSE_PARSE_ERROR);
            }
        } else {
            excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
        }
    }

    /**
     * 下载失败,广播异常
     *
     * @param notify
     * @param tmp
     * @param e
     */
    private void exceptionProcessor(Notification notify, File tmp, Exception e) {
        //tmp.delete();
        if (e instanceof IOException) {
            if (e instanceof ClientIOException) {
                excTask.setStatus(ITransferConstants.SDCARD_IO_EXCEPTION_ERROR);
            } else {
                excTask.setStatus(ITransferConstants.NET_EXCEPTION);
            }
        } else if (e instanceof HttpException) {
            excTask.setStatus(ITransferConstants.NET_EXCEPTION);
        } else if (e instanceof ApiException) {
            int code = ((ApiException) e).getCode();
            if (code == AppCode.PICKUP_CODE_ERROR) {
                excTask.setStatus(ITransferConstants.GET_CODE_ERROR);
            } else {
                excTask.setStatus(ITransferConstants.SERVER_RESPONSE_PARSE_ERROR);
            }
        } else {
            excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
        }
        updateStatus(excTask.getId(), excTask.getStatus());
        sendBroadCast();
        updateNoticeProgress(notify);
    }

    private String getUrl(String token) {
        String strUrl = "";
        String params = "?path=" + URLEncoder.encode(excTask.getPath()) + "&token=" + token + "&version=" + excTask.getVersion();

        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
            strUrl = getBaseUrl() + ApiConstants.DOWNLOAD_GROUP_FILE_URL + params + "&groupId=" + excTask.getGroupId() + "&ownerId=" + excTask.getOwnId();
        } else {
            strUrl = getBaseUrl() + ApiConstants.DOWNLOAD_FILE_URL + params;
        }
        return strUrl;
    }

    /**
     * 获取url
     *
     * @param type 组文件或者个人文件
     * @return
     */
    private String getUrlForBP(int type) {
        String strUrl;
        if (type == AppConstants.GROUP_FILE) {
            strUrl = getBaseUrl() + ApiConstants.GROUP_URL;
        } else {
            strUrl = getBaseUrl() + ApiConstants.FILE_URL;
        }
        return strUrl;
    }

    /**
     * 注意：此处filePath需要转换成ISO-8859-1给服务器，服务器再转回来，否则中文乱码
     *
     * @param offset
     * @param token
     * @return
     */
    private String getParamsForBPDownload(long offset, String token) {
        String _pValue;
        //        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
        //            _pValue = "{method:\"downloadFile\",params:{filePath:{path:\"" + StringUtil.getStringByUTF8(excTask.getPath()) + "\",version:" + excTask.getVersion() + ",ownerId:\"" + excTask.getOwnId() + "\"}, offset:" + offset + ", userId:'', groupId:\"" + excTask.getGroupId() + "\"},token:\"" + token + "\"}";
        //        } else {
        //            _pValue = "{method:\"downloadFile\",params:{filePath:{path:\"" + StringUtil.getStringByUTF8(excTask.getPath()) + "\",version:" + excTask.getVersion() + "}, offset:" + offset + ", userId:''},token:\"" + token + "\"}";
        //        }
        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
            _pValue = "{method:\"downloadFile\",params:{filePath:{path:\"" + excTask.getPath() + "\",version:" + excTask.getVersion() + ",ownerId:\"" + excTask.getOwnId() + "\"}, offset:" + offset + ", userId:'', groupId:\"" + excTask.getGroupId() + "\"},token:\"" + token + "\"}";
        } else {
            _pValue = "{method:\"downloadFile\",params:{filePath:{path:\"" + excTask.getPath() + "\",version:" + excTask.getVersion() + "}, offset:" + offset + ", userId:''},token:\"" + token + "\"}";
        }
        return _pValue;
    }

    /**
     * 最后一步检查
     *
     * @param notify
     * @param tmp
     * @param type
     * @throws ClientIOException
     */
    private void lastCheck(Notification notify, File tmp, int type) throws ClientIOException {
        if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
            updateStatus(excTask.getId(), ITransferConstants.STATUS_CANCEL);
            nManager.cancel(DOWNLOAD_NOTIFY_ID);
        }
        if (type == DownloadTask.SHARED_FOLDER_TYPE) {
            // 下载共享文件夹时是以ZIP的方式下载的，取不到文件的总大小
            if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                tmp.delete();
            } else {
                updateNotify2Complete(notify);
                File file = FileUtil.newInstance().createSharedFileInSDCard(excTask.getSavePath());
                tmp.renameTo(file);
            }
        } else {
            if (excTask.getDownloadLength() == excTask.getFileLength()) {
                updateNotify2Complete(notify);
                File file = null;
                if (DownloadTask.OWN_FILE_TYPE == type) {
                    file = FileUtil.newInstance().createUspaceFileInSDCard(excTask.getSavePath());
                } else {
                    file = FileUtil.newInstance().createSharedFileInSDCard(excTask.getSavePath());
                }
                tmp.renameTo(file);
            } else {
                if (excTask.getStatus() != ITransferConstants.STATUS_CANCEL) {
                    failed(notify);
                }
                tmp.delete();
            }
            excTask = null;
        }
    }

    /**
     * 下载具体方法
     **/
    private void download() {
        Log.d(TAG, "download begin [" + "id=" + excTask.getId() + "name=" + excTask.getName() + ", total=" + excTask.getDownloadLength() + "]");
        if (excTask.getType() == DownloadTask.OWN_FILE_TYPE) {
            downloadOwnFileForBP();
        } else {
            downloadSharedFile(excTask.getType());
        }
    }

    private void updateNotify2Complete(Notification notify) {
        excTask.setStatus(ITransferConstants.STATUS_FINISH);
        excTask.setPercent(100);// 完成HTTP协议，完成所有下载！
        updateStatus(excTask.getId(), ITransferConstants.STATUS_FINISH);
        sendBroadCast();// 广播到 Activity
        updateNoticeProgress(notify);
    }

    private void failed(Notification notify) {
        excTask.setStatus(ITransferConstants.TRANSFER_FAIL_ERROR);
        excTask.setPercent(0);
        updateStatus(excTask.getId(), ITransferConstants.TRANSFER_FAIL_ERROR);
        sendBroadCast();// 广播到 Activity
        updateNoticeProgress(notify);
    }

    private void updateStatus(Integer taskId, Integer status) {
        Log.d(TAG, "updateStatus : " + "taskId=" + taskId + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<String, Object>();
        statusMap.put("status", status);
        dbTool.updateDownloadTask(taskId, statusMap);
    }

    //    * @param int          notifyId 需要更新的notify id
    //    * @param int          what handler更新标识
    //    * @param int          progress 进度条刻度
    //    * @param Notification notify notify实例

    //    * @param String tickText 显示在进度条上的文字标题
    //    * @param Intent intent 绑定的intent
    //    * @param int    flag notification标识:是否可以被自动清除
    //    * @param int    progressMax 最大进度条刻度

    /**
     * 创建一个新notify
     **/
    private Notification newNotification() {
        String tickText = excTask.getName();
        Intent intent = new Intent(DownloadService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("taskType", FileTransFragment.DOWNLOAD);
        intent.putExtra(AppConstants.POSITION_FRAGMENT, AppConstants.POSITION_FILETRANS_FRAGMENT);

        Notification notification = new Notification(R.mipmap.download_title, tickText, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.contentView = new RemoteViews(getPackageName(), R.layout.progress_notification);
        notification.contentView.setProgressBar(R.id.progress_notification_pb, 100, 0, false);
        notification.contentView.setTextViewText(R.id.progress_notification_filename_tv, tickText);

        final Context cxt = getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pendingIntent;
        return notification;
    }

    /**
     * 更新通知的进度信息
     **/
    private void updateNoticeProgress(Notification notify) {
        //        int status = excTask.getStatus();
        //        int size = excTask.getPercent();
        //        String completeSize = StringUtil.getFileSize(excTask.getDownloadLength()) + "/" + StringUtil.getFileSize(excTask.getFileLength());
        //        notify.contentView.setTextViewText(R.id.progress_notification_add_queue_time_tv, excTask.getAddQueueTime());
        //        switch (status) {
        //            case ITransferConstants.STATUS_RUN:
        //                notify.contentView.setProgressBar(R.id.progress_notification_pb, 100, size, false);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, size + "%");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.STATUS_FINISH:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.contentView.setProgressBar(R.id.progress_notification_pb, 100, 100, false);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "完成");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.NET_EXCEPTION:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "网络异常，停止下载");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.GET_CODE_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "提取码错误");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.SDCARD_IO_EXCEPTION_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "sdcard 存储错误");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.SERVER_RESPONSE_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "服务器响应错误");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.SERVER_RESPONSE_PARSE_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "未知解析错误");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.UNKNOWN_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "未知异常,请重新下载");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.TRANSFER_FAIL_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "文件下载失败");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            default:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "未知异常");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //        }
        //        if (!isStop) {
        //            nManager.notify(DOWNLOAD_NOTIFY_ID, notify);
        //        }
    }

    private void sendBroadCast() {
        Intent intent = new Intent(DOWNLOAD_RECEIVER_ACTION);
        intent.putExtra("taskType", FileTransFragment.DOWNLOAD);
        intent.putExtra("task", excTask);
        sendBroadcast(intent);
        Log.d(TAG, "sendBroadcast[" + "id=" + excTask.getId() + ", status=" + excTask.getStatus() + ", percent=" + excTask.getPercent() + "]");
    }

    private void handleMsg(int type) {
        Intent intent = new Intent(DOWNLOAD_RECEIVER_ACTION);
        intent.putExtra(ITransferConstants.SERVER_MESSAGE, type);
        intent.putExtra("taskType", FileTransFragment.DOWNLOAD);
        sendBroadcast(intent);
        Log.d(TAG, "send Msg[***** " + type + " *****]");
    }

    /**
     * 该类提供对外访问接口
     */
    class DownloadBinder extends Binder implements IDownloadService {
        public void unlock() {
            synchronized (lock) {
                Log.d(TAG, "========== begin to notify downloadService thread ==========");
                lock.notify();
            }
        }

        @Override
        public boolean cancelTask(int id) {
            boolean flag;
            if (getExcTask() != null && getExcTask().getId() == id) {// 正在执行的任务
                getExcTask().setStatus(ITransferConstants.STATUS_CANCEL);
                Log.d(TAG, "############ Id=" + getExcTask().getId() + ",Name=" + getExcTask().getName() + "############");
                flag = true;
            } else {
                updateStatus(id, ITransferConstants.STATUS_CANCEL);// 通过对象锁保证这一刻不被查到
                flag = true;
            }
            return flag;
        }

        @Override
        public DownloadTask getExcTask() {
            return DownloadService.this.getExcTask();
        }

        @Override
        public boolean cancelAllTask() throws RemoteException {
            if (getExcTask() != null) {
                getExcTask().setStatus(ITransferConstants.STATUS_CANCEL);
            }
            return true;
        }
    }

    @Override
    public void onDestroy() {
        isStop = true;
        if (excTask != null) {
            last_task_id = excTask.getId();
            excTask.setStatus(ITransferConstants.STATUS_WAIT);
            excTask = null;
        }
        nManager.cancel(DOWNLOAD_NOTIFY_ID);
        Log.d(TAG, "************* downloadloadService destroy *****************");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return this.mBinder;
    }

    private void ioClose(InputStream fis, OutputStream fos) {
        try {
            if (fos != null) {
                fos.close();
            }
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "io close error : " + e.toString());
            e.printStackTrace();
        }
    }

    public DownloadTask getExcTask() {
        return excTask;
    }

    private void updateCompleteSize() {
        Log.d(TAG, "updateCompleteSize[" + "taskId=" + excTask.getId() + ", downloadLength=" + excTask.getDownloadLength() + "]");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("downloadLength", excTask.getDownloadLength());
        dbTool.updateDownloadTask(excTask.getId(), map);
    }

    private void recordFileSize() {
        Log.d(TAG, "recordFileSize[" + "taskId=" + excTask.getId() + ", fileLength=" + excTask.getFileLength() + "]");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("fileLength", excTask.getFileLength());
        dbTool.updateDownloadTask(excTask.getId(), map);
    }
}
