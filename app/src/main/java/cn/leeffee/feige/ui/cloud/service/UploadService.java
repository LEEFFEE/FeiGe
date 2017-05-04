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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.activity.MainActivity;
import cn.leeffee.feige.ui.cloud.api.ApiConstants;
import cn.leeffee.feige.ui.cloud.api.ApiException;
import cn.leeffee.feige.ui.cloud.api.ApiOkHttp;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.exception.HttpException;
import cn.leeffee.feige.ui.cloud.fragment.FileTransFragment;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;

public class UploadService extends Service {
    private static final String TAG = "UploadService";
    private boolean isStop = false;
    private byte[] lock = new byte[0];

    public static final String UPLOAD_RECEIVER_ACTION = "com.uit.uspace.UPLOAD.RECEIVER.ACTION";

    public final static int UPDATE_NOTIFY_PROGRESS = 2; // 更新进度
    public final static int UPDATE_NOTIFY_COMPLETE = 3; // 更新进度条到完成状态（100%）
    public final static int UPLOAD_EXCEPTION_NOTIFY = 4; // 上传发送异常，如IO异常
    public final static int UPLOAD_CANCEL_NOTIFY = 5;

    private final static int UPLOAD_FILE_SIZE = 1024 * 8; // 缓冲池

    private final static int UPLOAD_NOTIFY_ID = 1;

    // private int TOTAL_TASK = 0;//总任务数
    // private int WAITING_TASK = 0;//正在上传任务数

    private UploadBinder mBinder;

    private NotificationManager nManager;

    private Context ctx = this;
    private DBTool dbTool = new DBTool(ctx);

    private UploadTask excTask = null; // 当前唯一执行的任务

    private UploadQueueListener listener = null;

    private int last_task_id = -1;
    private int try_times = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("UploadService onCreate ");
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBinder = new UploadBinder();

        listener = new UploadQueueListener();
        listener.start();
    }

    class UploadQueueListener extends Thread {
        @Override
        public void run() {
            while (!isStop) {
                try {
                    if (excTask != null) {
                        Log.i(TAG, "--> exc [id=" + excTask.getId() + ", status=" + excTask.getStatus() + "]");
                        if (excTask.getStatus() == ITransferConstants.STATUS_WAIT || excTask.getStatus() == ITransferConstants.STATUS_RUN) {
                            excTask.setStatus(ITransferConstants.STATUS_RUN);
                            updateStatus(excTask.getId(), ITransferConstants.STATUS_RUN);
                            uploadForBP();
                        }
                    } else {
                        List<UploadTask> list = dbTool.listUploadQueue(ITransferConstants.STATUS_WAIT, ITransferConstants.STATUS_RUN);
                        if (list.size() > 0) {
                            excTask = list.get(0);
                            try_times = 3;
                        } else {
                            synchronized (lock) {
                                Log.i(TAG, "\t\t====== uploadService thread locked ======");
                                lock.wait();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString(), e);
                }
                Log.i(TAG, "UploadService[" + Thread.currentThread() + "]");
            }

            // 保存现场
            if (last_task_id != -1) {
                updateStatus(last_task_id, ITransferConstants.STATUS_WAIT);
                last_task_id = -1;
            }
        }
    }

    /**
     * 上传具体方法
     **/
    private synchronized void upload() {
        FileInputStream fis = null;
        DataOutputStream dos = null;
        BufferedReader reader = null;

        Notification notify = newNotification();
        try {
            File file = new File(excTask.getLocalPath());// 保证上传文件存在
            if (!file.exists()) {
                excTask.setStatus(ITransferConstants.FILE_NOT_EXIST);
                throw new ApiException(ITransferConstants.FILE_NOT_EXIST);
            }
            fis = new FileInputStream(file);
            String strUrl = getUrl();
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setChunkedStreamingMode(UPLOAD_FILE_SIZE);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.write(("Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + file.getName() + "\"" + end).getBytes("UTF-8"));
            dos.writeBytes(end);
            dos.flush();
            byte[] buffer = new byte[UPLOAD_FILE_SIZE];
            int count = 0;
            long length = 0;
            long total = file.length();
            excTask.setFileLength(total);// 文件大小
            recordFileSize();

            while (!isStop && excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = fis.read(buffer)) != -1) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new HttpException(ITransferConstants.WIFI_EXCEPTION);
                }
                dos.write(buffer, 0, count);
                dos.flush();
                length += count;
                handle(notify, length, total);
            }
            if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                updateStatus(excTask.getId(), ITransferConstants.STATUS_CANCEL);
                nManager.cancel(UPLOAD_NOTIFY_ID);
                updateCompleteSize();
            }
            if (!isStop && length == total) {
                dos.writeBytes(end);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                dos.flush();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String result = reader.readLine();
                updateCompleteSize();
                if ("1".equals(result)) {
                    updateNotify2Complete(notify);
                } else {// {"errorCode":100,"errorMessage":"Unexpected system error. Please contact your administrator!","result":null}
                    throw new ApiException(ITransferConstants.SERVER_RESPONSE_ERROR);
                }
            }
            excTask = null;
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
            exceptionProcessor(notify, e);
        } finally {
            readerClose(reader);
            ioClose(fis, dos);
            if (isStop) {
                nManager.cancel(UPLOAD_NOTIFY_ID);
            }
        }
    }

    /**
     * 支持断点上传
     */
    private synchronized void uploadForBP() {
        FileInputStream fis = null;
        DataOutputStream dos = null;
        BufferedReader reader = null;
        RandomAccessFile raFile = null;
        Notification notify = newNotification();
        try {
            File file = new File(excTask.getLocalPath());// 保证上传文件存在
            if (!file.exists()) {
                excTask.setStatus(ITransferConstants.FILE_NOT_EXIST);
                throw new ApiException(ITransferConstants.FILE_NOT_EXIST);
            }
            //预处理，获取已上传的文件大小
            String token = SPUtil.getString(AppConfig.TOKEN);
            if (StringUtil.isEmpty(token)) {
                token = ApiOkHttp.login();
            }
            String remoteFullFilePath;
            if (excTask.getRemotePath().equals("/")) {
                remoteFullFilePath = excTask.getRemotePath() + excTask.getName();
            } else {
                remoteFullFilePath = excTask.getRemotePath() + "/" + excTask.getName();
            }
            long total = file.length();
            //获取已经文件已经上传的大小
            long uploadedSize;
            if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
                uploadedSize = ApiOkHttp.uploadShareGroupPreProcess(remoteFullFilePath, total, excTask.getVersion(), 1, excTask.getGroupId(), token);
            } else {
                uploadedSize = ApiOkHttp.uploadPreProcess(remoteFullFilePath, total, excTask.getVersion(), 1, token);
            }
            Log.i(TAG, "remoteFile:" + excTask.getRemotePath() + ",uploaded-size:" + uploadedSize);

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
            conn.setRequestProperty(ApiConstants.REQUEST_PARAMS_KEY, getParamsForBPUpload(remoteFullFilePath, uploadedSize, total, token));
            //指定流的大小，当内容达到这个值的时候就把流输出
            conn.setChunkedStreamingMode(UPLOAD_FILE_SIZE);
            dos = new DataOutputStream(conn.getOutputStream());
            raFile = new RandomAccessFile(file, "rw");
            raFile.seek(uploadedSize);
            byte[] buffer = new byte[UPLOAD_FILE_SIZE];
            int count = 0;
            long length = uploadedSize;
            excTask.setFileLength(total);// 文件大小
            recordFileSize();
         //   android.os.Debug.waitForDebugger();
            while (!isStop && excTask.getStatus() == ITransferConstants.STATUS_RUN && (count = raFile.read(buffer)) != -1) {
                if (!NetWorkUtil.isWifiEnvOK(this)) {
                    throw new HttpException(ITransferConstants.WIFI_EXCEPTION);
                }
                dos.write(buffer, 0, count);
                dos.flush();
                length += count;
                handle(notify, length, total);
            }
            if (excTask.getStatus() == ITransferConstants.STATUS_CANCEL) {
                updateStatus(excTask.getId(), ITransferConstants.STATUS_CANCEL);
                nManager.cancel(UPLOAD_NOTIFY_ID);
                updateCompleteSize();
            }
            if (!isStop && length == total) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String retString = reader.readLine();
                JSONObject jsonObj = new JSONObject(retString);
                updateCompleteSize();
                //LogUtil.e(jsonObj.toString());
                if (isBPUploadSuccess(jsonObj)) {
                    updateNotify2Complete(notify);
                } else {
                    throw new ApiException(ITransferConstants.SERVER_RESPONSE_ERROR);
                }
            }
            excTask = null;
            return;
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
            modifyStatusByExcept(e);
            try_times--;
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            readerClose(reader);
            ioClose(fis, dos);
            if (isStop) {
                nManager.cancel(UPLOAD_NOTIFY_ID);
            }
        }
        //三次完成后检查网络
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
            if (code == ITransferConstants.SERVER_RESPONSE_ERROR) {
                excTask.setStatus(ITransferConstants.SERVER_RESPONSE_ERROR);
            } else if (code == ITransferConstants.FILE_NOT_EXIST) {
                excTask.setStatus(ITransferConstants.FILE_NOT_EXIST);
            } else {
                excTask.setStatus(ITransferConstants.TRANSFER_FAIL_ERROR);
            }
        } else {
            excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
        }
    }

    /**
     * 上传失败,广播异常
     */
    private void exceptionProcessor(Notification notify, Exception e) {
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
            if (code == ITransferConstants.SERVER_RESPONSE_ERROR) {
                excTask.setStatus(ITransferConstants.SERVER_RESPONSE_ERROR);
            } else if (code == ITransferConstants.FILE_NOT_EXIST) {
                excTask.setStatus(ITransferConstants.FILE_NOT_EXIST);
            } else {
                excTask.setStatus(ITransferConstants.TRANSFER_FAIL_ERROR);
            }
        } else {
            excTask.setStatus(ITransferConstants.UNKNOWN_ERROR);
        }
        updateStatus(excTask.getId(), excTask.getStatus());
        sendBroadCast();
        updateNoticeProgress(notify);
    }

    private String getUrlForBP() throws HttpException, ApiException {
        String strUrl = "";
        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
            strUrl = getBaseUrl() + ApiConstants.GROUP_URL;
        } else {
            strUrl = getBaseUrl() + ApiConstants.FILE_URL;
        }
        return strUrl;
    }

    /**
     * 注意：此处filePath需要转换成ISO-8859-1给服务器，服务器再转回来，否则中文乱码
     *
     * @param remoteFullFilePath
     * @param offset
     * @param fileLength
     * @param token
     * @return
     */
    private String getParamsForBPUpload(String remoteFullFilePath, long offset, long fileLength, String token) {
        String _pvalue = "";
//        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
//            _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + StringUtil.getStringByUTF8(remoteFullFilePath) + "\",version:" + excTask.getVersion() + "},offset:" + offset + ",fileLength:" + fileLength + ",groupId:\"" + excTask.getGroupId() + "\",userId:''},token:\"" + token + "\"}";
//        } else {
//            _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + StringUtil.getStringByUTF8(remoteFullFilePath) + "\",version:" + excTask.getVersion() + "},offset:" + offset + ",fileLength:" + fileLength + ",userId:''},token:\"" + token + "\"}";
//        }
        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
            _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + remoteFullFilePath + "\",version:" + excTask.getVersion() + "},offset:" + offset + ",fileLength:" + fileLength + ",groupId:\"" + excTask.getGroupId() + "\",userId:''},token:\"" + token + "\"}";
        } else {
            _pvalue = "{method:\"uploadFile\",params:{filePath:{path:\"" + remoteFullFilePath + "\",version:" + excTask.getVersion() + "},offset:" + offset + ",fileLength:" + fileLength + ",userId:''},token:\"" + token + "\"}";
        }
        return _pvalue;
    }

    private String getUrl() throws HttpException, ApiException {
        // 登录状态下的 token
        String token = SPUtil.getString(AppConfig.TOKEN);
        if (StringUtil.isEmpty(token)) {
            token = ApiOkHttp.login();
        }
        String strUrl = "";
        String params = "?path=" + URLEncoder.encode(excTask.getRemotePath()) + "&token=" + token + "&uploadType=0";

        if (excTask.getIsGroupFile() == AppConstants.GROUP_FILE) {
            strUrl = getBaseUrl() + ApiConstants.UPLOAD_GROUP_FILE_URL + params + "&groupId=" + excTask.getGroupId();
        } else {
            strUrl = getBaseUrl() + ApiConstants.UPLOAD_FILE_URL + params;
        }
        return strUrl;
    }

    private int handle(Notification notify, long length, long total) {
        excTask.setUploadLength(length);
        int percent = excTask.getPercent();
        int nowPercent = (int) (((float) length / (float) total) * 100);
        if (nowPercent > percent && nowPercent < 100) {// 如果百分比有变动则更新进度条
            percent = nowPercent;
            excTask.setPercent(percent);
            if (percent < 3 || (percent % 3 == 0)) {
                updateCompleteSize();//记录到数据库
                sendBroadCast();// 广播到 FileTransFragment
                updateNoticeProgress(notify);
            }
        }
        return percent;
    }

    private void updateNotify2Complete(Notification notify) {
        if (excTask.getStatus() == ITransferConstants.STATUS_RUN) {
            excTask.setStatus(ITransferConstants.STATUS_FINISH);
            excTask.setPercent(100);// 完成HTTP协议，完成所有上传！
            updateStatus(excTask.getId(), ITransferConstants.STATUS_FINISH);
            sendBroadCast();// 广播到 Activity
            updateNoticeProgress(notify);
        }
    }

    public String getBaseUrl() {
        return PropertyUtil.getInstance().getScheme(false) + PropertyUtil.getServer();
    }

    private void updateStatus(Integer taskId, Integer status) {
        Log.i(TAG, "updateStatus : " + "taskId=" + taskId + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("status", status);
        dbTool.updateUploadTask(taskId, statusMap);
    }

    private void updateNoticeProgress(Notification notify) {
        //        int what = excTask.getStatus();
        //        int size = excTask.getPercent();
        //        String completeSize = StringUtil.getFileSize(excTask.getUploadLength()) + "/" + StringUtil.getFileSize(excTask.getFileLength());
        //        notify.contentView.setTextViewText(R.id.progress_notification_add_queue_time_tv, excTask.getAddQueueTime());
        //        switch (what) {
        //            case ITransferConstants.STATUS_RUN:
        //                notify.contentView.setProgressBar(R.id.progress_notification_pb, 100, size, false);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, size + "%");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.STATUS_FINISH:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.contentView.setProgressBar(R.id.progress_notification_pb, 100, 100, false);// 强制发100%通知
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "完成");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.SERVER_RESPONSE_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "服务器响应错误，请重新上传");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.NET_EXCEPTION:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "网络异常，停止上传");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.WIFI_EXCEPTION:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "WIFI异常，停止上传");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.FILE_NOT_EXIST:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "上传文件不存在");
        //                notify.contentView.setTextViewText(R.id.progress_notification_complete_size_tv, completeSize);
        //                break;
        //            case ITransferConstants.UNKNOWN_ERROR:
        //                notify.flags = Notification.FLAG_AUTO_CANCEL;
        //                notify.icon = android.R.drawable.stat_notify_error;
        //                notify.contentView.setImageViewResource(R.id.progress_notification_icon_iv, android.R.drawable.stat_sys_warning);
        //                notify.contentView.setTextViewText(R.id.progress_notification_percentage_tv, "未知异常，请重新上传");
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
        //            nManager.notify(UPLOAD_NOTIFY_ID, notify);
        //        }
    }

    private void sendBroadCast() {
        Intent intent = new Intent(UPLOAD_RECEIVER_ACTION);
        intent.putExtra("taskType", FileTransFragment.UPLOAD);
        intent.putExtra("task", excTask);
        sendBroadcast(intent);
        Log.i(TAG, "sendBroadcast[" + "id=" + excTask.getId() + ", status=" + excTask.getStatus() + ", percent=" + excTask.getPercent() + "]");
    }

    private Notification newNotification() {
        String tickText = excTask.getName();
        // 通过通知来进入上传列表
        Intent intent = new Intent(UploadService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("taskType", FileTransFragment.UPLOAD);
        intent.putExtra(AppConstants.POSITION_FRAGMENT, AppConstants.POSITION_FILETRANS_FRAGMENT);

        Notification notification = new Notification(R.mipmap.upload_title, tickText, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.contentView = new RemoteViews(getPackageName(), R.layout.progress_notification);
        notification.contentView.setProgressBar(R.id.progress_notification_pb, 100, 0, false);
        notification.contentView.setTextViewText(R.id.progress_notification_filename_tv, tickText);

        final Context cxt = getApplicationContext();
        notification.contentIntent = PendingIntent.getActivity(cxt, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return notification;
    }

    private void handleMsg(int type) {
        Intent intent = new Intent(UPLOAD_RECEIVER_ACTION);
        intent.putExtra(ITransferConstants.SERVER_MESSAGE, type);
        intent.putExtra("taskType", FileTransFragment.UPLOAD);
        sendBroadcast(intent);
        Log.i(TAG, "send Msg[***** " + type + " *****]");
    }

    @Override
    public void onDestroy() {
        isStop = true;// 自然结束
        if (excTask != null) {
            last_task_id = excTask.getId();
            excTask.setStatus(ITransferConstants.STATUS_WAIT);// 结束当前任务
            excTask = null;
        }
        nManager.cancel(UPLOAD_NOTIFY_ID);
        Log.i(TAG, "************* uploadService destroy *****************");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return this.mBinder;
    }

    private void readerClose(BufferedReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            Log.i(TAG, "BufferedReader close error : " + e.toString());
            e.printStackTrace();
        }

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
            Log.i(TAG, "io close error : " + e.toString());
            e.printStackTrace();
        }
    }

    // 该接口提供对外访问接口
    class UploadBinder extends Binder implements IUploadService {
        @Override
        public void unlock() {
            synchronized (lock) {
                Log.i(TAG, "\t\t=== begin to notify uploadService thread ===");
                lock.notify();
                try_times = 3;
            }
        }

        @Override
        public boolean cancelTask(int id) {
            boolean flag = false;
            if (getExcTask() != null && getExcTask().getId() == id) {// 正在执行的任务
                getExcTask().setStatus(ITransferConstants.STATUS_CANCEL);
                Log.i(TAG, "############ Id=" + getExcTask().getId() + ",Name=" + getExcTask().getName() + "############");
                flag = true;
            } else {
                updateStatus(id, ITransferConstants.STATUS_CANCEL);// 通过对象锁保证这一刻不被查到
                flag = true;
            }
            return flag;
        }

        @Override
        public UploadTask getExcTask() {
            return UploadService.this.getExcTask();
        }

        @Override
        public boolean cancelAllTask() throws RemoteException {
            if (getExcTask() != null) {
                getExcTask().setStatus(ITransferConstants.STATUS_CANCEL);
            }
            return true;
        }
    }

    public UploadTask getExcTask() {
        return excTask;
    }

    private void updateCompleteSize() {
        Log.i(TAG, "updateCompleteSize[" + "taskId=" + excTask.getId() + ", uploadLength=" + excTask.getUploadLength() + "]");
        Map<String, Object> map = new HashMap<>();
        map.put("uploadLength", excTask.getUploadLength());
        dbTool.updateUploadTask(excTask.getId(), map);
    }

    private void recordFileSize() {
        Log.i(TAG, "recordFileSize[" + "taskId=" + excTask.getId() + ", fileLength=" + excTask.getFileLength() + "]");
        Map<String, Object> map = new HashMap<>();
        map.put("fileLength", excTask.getFileLength());
        dbTool.updateUploadTask(excTask.getId(), map);
    }

    /**
     * 判断断点续传是否成功
     * 成功：{"errorCode":0,"errorMessage":null,"result":1}
     * 失败：{"errorCode":60007,"errorMessage":"Failed to move file","result":null}
     *
     * @param jsonObj
     * @return
     */
    private boolean isBPUploadSuccess(JSONObject jsonObj) {
        try {
            int retVer = jsonObj.getInt("result");
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}