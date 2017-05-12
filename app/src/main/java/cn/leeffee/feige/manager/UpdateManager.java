package cn.leeffee.feige.manager;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.ToastUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class UpdateManager {
    private final static String TAG = "UpdateManager";

    private Context mContext;

    // 提示语
    private String updateMsgTmpl = "当前版本{0},发现新版本{1},是否更新?";

    private String updateMsg = "";

    // 返回的安装包url
    private String apkUrlTmpl = PropertyUtil.getInstance().getBaseUrl() + "/uspace/{0}";

    private String apkUrl = "";

    private Dialog noticeDialog;

    private Dialog downloadDialog;

    /* 下载包安装路径 */
    private static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PropertyUtil.getInstance().getRoot() + File.separator + ".update";

    private String saveFileName = "";

    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private TextView mProgressTv;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int DOWN_EXCEPTION = 3;

    private static final int DOWN_EXCEPTION_FILE_NOT_FOUND = 4;

    private int progress;

    private Thread downLoadThread;

    private boolean interceptFlag = false;

    private boolean isAutoUpdate = true;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    mProgressTv.setText(progress + "%");
                    break;
                case DOWN_OVER:
                    downloadDialog.dismiss();
                    installApk();
                    break;
                case DOWN_EXCEPTION:
                    downloadDialog.dismiss();
                    ToastUtil.showShort("版本更新失败！");
                    mCallback.callback();
                    break;
                case DOWN_EXCEPTION_FILE_NOT_FOUND:
                    downloadDialog.dismiss();
                    ToastUtil.showShort("更新文件不存在！");
                    mCallback.callback();
                    break;
                default:
                    break;
            }
        }
    };

    private UpdateCallback mCallback;

    //    public UpdateManager(Context context, UpdateCallback callback) {
    //        this.mContext = context;
    //        this.mCallback = callback;
    //    }
    public UpdateManager(Context context) {
        this.mContext = context;
    }

    //    public void manualCheckUpdate() {
    //        isAutoUpdate = false;
    //        if (!CommonUtil.isNetworkAvaliable(mContext)) {
    //            ToastUtil.showShort("网络不可用，请检查网络设置！");
    //            //  mCallback.callback();
    //        } else {
    //            new GetLatestVersionTask().execute();
    //        }
    //    }

    /**
     * 检查更新
     *
     * @param isAutoUpdate 是否自动更新
     */
    public void checkUpdate(boolean isAutoUpdate) {
        this.isAutoUpdate = isAutoUpdate;
        if (!NetWorkUtil.isNetConnected(mContext)) {
            ToastUtil.showShort("网络不可用，请检查网络设置！");
            //  mCallback.callback();
        } else {
            //new GetLatestVersionTask().execute();
            String jsonParams = "{method:'getLatestClient',params:{type:1}}";
            ApiClient.getDefault(HostType.HOST_USPACE).getLatestClient(ApiClient.NO_NEED_CACHE,jsonParams).observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BaseResponse<String>>() {
                @Override
                public void accept(@NonNull BaseResponse<String> res) throws Exception {
                    //                    if (client != null) {
                    //                        PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    //                        int oldVersion = info.versionCode;
                    //                        int newVersion = client.getVersionCode();
                    //                        if (newVersion > oldVersion) {
                    //                            updateMsg = MessageFormat.format(updateMsgTmpl, new Object[]{info.versionName, client.getVersionName()});
                    //                            apkUrl = MessageFormat.format(apkUrlTmpl, client.getUpdateUrl());
                    //                            saveFileName = savePath + File.separator + client.getFileName();
                    //                            showNoticeDialog(client.getForceUpdate());
                    //                            if (!isAutoUpdate) {
                    //                                // mCallback.callback();
                    //                            }
                    //                        } else {
                    //                            // mCallback.callback();
                    //                            if (!isAutoUpdate) {
                    //                                ToastUtil.showShort(R.string.msg_no_new_version);
                    //                            }
                    //                        }
                    //                    } else {
                    //                        // mCallback.callback();
                    //                        if (!isAutoUpdate) {
                    //                            ToastUtil.showShort(R.string.msg_no_new_version);
                    //                        }
                    //                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                }
            });

        }
    }

    //    // 外部接口让主Activity调用
    //    public void checkUpdateInfo() {
    //        isAutoUpdate = true;
    //        if (!CommonUtil.isNetworkAvaliable(mContext)) {
    //            ToastUtil.showShort("网络不可用，请检查网络设置！");
    //            // mCallback.callback();
    //            return;
    //        }
    //        new GetLatestVersionTask().execute();
    //    }

    private void showNoticeDialog(boolean forceUpdate) {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        if (!forceUpdate) {
            builder.setNegativeButton("以后再说", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // mCallback.callback();
                }
            });
        }
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mProgressTv = (TextView) v.findViewById(R.id.progressTv);
        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
                // mCallback.callback();
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private Runnable mDownApkRunnable = new Runnable() {
        FileOutputStream fos = null;
        InputStream is = null;

        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载.
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                if (e instanceof FileNotFoundException) {
                    mHandler.sendEmptyMessage(DOWN_EXCEPTION_FILE_NOT_FOUND);
                } else {
                    mHandler.sendEmptyMessage(DOWN_EXCEPTION);
                }
            } finally {
                interceptFlag = true;
                try {
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 下载apk
     */

    private void downloadApk() {
        downLoadThread = new Thread(mDownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        if (isAutoUpdate) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(i);
        } else {
            mContext.startActivity(i);
        }
        // mCallback.callback();
    }

    public interface UpdateCallback {
        void callback();
    }

    //    class GetLatestVersionTask extends AsyncTask<Void, String, AppClient> {
    //        @Override
    //        protected AppClient doInBackground(Void... params) {
    //            try {
    //                return YopanApplication.yopanApi.getLatestClient();
    //            } catch (Exception e) {
    //                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
    //                Log.e(TAG, e.getMessage(), e);
    //                publishProgress(msg);
    //            }
    //            return null;
    //        }
    //
    //        @Override
    //        protected void onProgressUpdate(String... values) {
    //            super.onProgressUpdate(values);
    //            ToastUtil.showShort(values[0]);
    //        }
    //
    //        @Override
    //        protected void onPostExecute(AppClient client) {
    //            try {
    //                if (client != null) {
    //                    PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
    //                    int oldVersion = info.versionCode;
    //                    int newVersion = client.getVersionCode();
    //                    if (newVersion > oldVersion) {
    //                        updateMsg = MessageFormat.format(updateMsgTmpl, new Object[]{info.versionName, client.getVersionName()});
    //                        apkUrl = MessageFormat.format(apkUrlTmpl, client.getUpdateUrl());
    //                        saveFileName = savePath + File.separator + client.getFileName();
    //                        showNoticeDialog(client.getForceUpdate());
    //                        if (!isAutoUpdate) {
    //                            // mCallback.callback();
    //                        }
    //                    } else {
    //                        // mCallback.callback();
    //                        if (!isAutoUpdate) {
    //                            ToastUtil.showShort(R.string.msg_no_new_version);
    //                        }
    //                    }
    //                } else {
    //                    // mCallback.callback();
    //                    if (!isAutoUpdate) {
    //                        ToastUtil.showShort(R.string.msg_no_new_version);
    //                    }
    //                }
    //            } catch (Exception e) {
    //                Log.e(TAG, e.getMessage(), e);
    //                // mCallback.callback();
    //            }
    //        }
    //
    //    }
}
