package cn.leeffee.feige.ui.cloud.page;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BasePage;
import cn.leeffee.feige.ui.cloud.adapter.DownloadListAdapter;
import cn.leeffee.feige.ui.cloud.contract.PageDownloadListContract;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.model.PageDownloadListModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.PageDownloadListPresenterImpl;
import cn.leeffee.feige.ui.cloud.service.DownloadService;
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.FileViewer;
import cn.leeffee.feige.utils.ToastUtil;

import static cn.leeffee.feige.ui.cloud.service.ITransferConstants.NET_EXCEPTION;


/**
 * Created by lhfei on 2017/4/11.下载列表pager
 */

public class DownLoadListPage extends BasePage<PageDownloadListPresenterImpl, PageDownloadListModelImpl> implements PageDownloadListContract.View, AdapterView.OnItemClickListener {

    private static final String TAG = "DownLoadListPage";
    @BindView(R.id.trans_download_list_lv)
    ListView mListView;
    @BindView(R.id.trans_download_empty_tv)
    TextView mEmpty;
    @BindView(R.id.trans_download_loading)
    LinearLayout mLoading;
    private List<DownloadTask> mTasks;
    private DownloadListAdapter mAdapter;
    private static final String REQUEST_CODE_LIST_DOWNLOAD_QUEUE = "db_listDownloadQueue";
    private FileUtil mFileUtil;
    private DownloadReceiver mDownloadReceiver;


    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadTask task = null;
            try {
                task = (DownloadTask) intent.getSerializableExtra("task");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (mAdapter != null) {
                View view = null;
                if (task != null && task.getStatus() != ITransferConstants.STATUS_CANCEL) {// 5取消下载（未完成, 删除已下载的文件.tmp）
                    DownloadTask mTask = null;
                    DownloadListAdapter.ViewHolder tag = new DownloadListAdapter.ViewHolder();
                    tag.taskId = task.getId();
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        if (task.getId() == mAdapter.getItem(i).getId()) {
                            mTask = mAdapter.getItem(i);
                            break;
                        }
                    }
                    if (mTask != null) {
                        mTask.setStatus(task.getStatus());
                        mTask.setPercent(task.getPercent());
                        mTask.setDownloadLength(task.getDownloadLength());
                    }
                    view = mListView.findViewWithTag(tag);
                    if (null != view) {
                        DownloadListAdapter.ViewHolder holder = (DownloadListAdapter.ViewHolder) view.getTag();
                        mAdapter.changeProgressStatus(holder, task);
                        //  LogUtil.e("下载的findViewWithTag 能找到View");
                    }
                }
                if (task != null) {
                    finallyMsg(task);
                }
            }
            int msgType = intent.getIntExtra(ITransferConstants.SERVER_MESSAGE, -1);
            if (msgType == NET_EXCEPTION) {
                ToastUtil.showShort("网络异常，请检查网络！");
            }
        }

    }

    public DownLoadListPage(Activity act) {
        super(act);
    }

    public DownLoadListPage(Activity act, String title) {
        super(act, title);
    }

    @Override
    protected void initPresenter() {
        mAdapter = new DownloadListAdapter(mAct);
        mListView.setAdapter(mAdapter);
        mPresenter.setVM(this, mModel);
        mDownloadReceiver = new DownloadReceiver();
        App.getAppContext().registerReceiver(mDownloadReceiver, new IntentFilter(DownloadService.DOWNLOAD_RECEIVER_ACTION));
        try {
            mFileUtil = FileUtil.newInstance();
        } catch (ClientIOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View initView() {
        return LayoutInflater.from(App.getAppContext()).inflate(R.layout.trans_download_list, null);
    }

    @Override
    public void initData() {
        Integer arrStatus[] = new Integer[]{1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13};
        mPresenter.listDownloadQueue(arrStatus, REQUEST_CODE_LIST_DOWNLOAD_QUEUE);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final DownloadTask task = mTasks.get(position);

        int status = task.getStatus();
        // 状态值参见 ITransferConstants
        if (status == 4 || status == 7 || status == 8 || status == 9 || status == 10 || status == 11 || status == 12 || status == 13) {
            ToastUtil.showShort(R.string.msg_uspace_file_download_exception);
            return;
        }

        if (task.getStatus() == 1 || task.getStatus() == 2) {
            ToastUtil.showShort(R.string.msg_uspace_file_not_download_over);
            return;
        }
        if (!FileViewer.canOpen(task.getName())) {
            ToastUtil.showShort(R.string.please_install_support_soft);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        builder.setTitle(R.string.strMenu);
        builder.setItems(new String[]{App.getAppContext().getText(R.string.menu_title_open_file).toString()}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (task.getType() == DownloadTask.SHARED_FILE_TYPE) {
                        if (FileUtil.isFileExist(task.getSavePath())) {
                            Intent intent = FileViewer.openFileIntent(task.getSavePath());
                            if (intent != null) {
                                App.getAppContext().startActivity(intent);
                            }
                        } else {
                            ToastUtil.showShort(R.string.file_not_exist);
                        }
                    } else {
                        if (FileUtil.isFileExist(task.getSavePath())) {
                            Intent intent = FileViewer.openFileIntent(task.getSavePath());
                            if (intent != null) {
                                App.getAppContext().startActivity(intent);
                            }
                        } else {
                            ToastUtil.showShort(R.string.file_not_exist);
                        }
                    }
                }
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void loadBefore(String requestCode) {
        if (REQUEST_CODE_LIST_DOWNLOAD_QUEUE.equals(requestCode)) {
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (REQUEST_CODE_LIST_DOWNLOAD_QUEUE.equals(requestCode)) {
            mTasks = (List<DownloadTask>) result;
            mAdapter.setData(mTasks);
            mAdapter.notifyDataSetChanged();
            isShowEmpty(mTasks);
            mLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (REQUEST_CODE_LIST_DOWNLOAD_QUEUE.equals(requestCode)) {
            mLoading.setVisibility(View.GONE);
        }
    }

    /**
     * Fragment销毁时先执行
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //        if (mDownloadReceiver != null) {
        //            App.getAppContext().unregisterReceiver(mDownloadReceiver);
        //        }
    }

    /**
     * 数据是否为空提示
     *
     * @param list
     */
    public void isShowEmpty(List list) {
        if (list != null && list.size() > 0) {
            mEmpty.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 下载结束提示消息
     *
     * @param task 当前下载的任务
     */
    public void finallyMsg(DownloadTask task) {
        switch (task.getStatus()) {
            case ITransferConstants.FILE_NOT_EXIST:
                ToastUtil.showShort("下载[" + task.getName() + "]失败，请重新下载");
                break;
            case ITransferConstants.TRANSFER_FAIL_ERROR:
                ToastUtil.showShort("下载[" + task.getName() + "]失败，请重新下载");
                break;
            case ITransferConstants.SERVER_RESPONSE_PARSE_ERROR:
                ToastUtil.showShort("下载[" + task.getName() + "]失败，请重新下载");
                break;
            case ITransferConstants.SERVER_RESPONSE_ERROR:
                ToastUtil.showShort("下载[" + task.getName() + "]失败，请重新下载");
                break;
            case ITransferConstants.UNKNOWN_ERROR:
                ToastUtil.showShort("下载[" + task.getName() + "]失败，请重新下载");
                break;
            case ITransferConstants.SDCARD_IO_EXCEPTION_ERROR:
                ToastUtil.showShort("sdcard 存储错误，下载[" + task.getName() + "]失败");
                break;
            case ITransferConstants.GET_CODE_ERROR:
                ToastUtil.showShort("提取码错误，下载[" + task.getName() + "]失败");
                break;
            case ITransferConstants.STATUS_CANCEL:
                ToastUtil.showShort("已停止下载[" + task.getName() + "]");
                break;
            case NET_EXCEPTION:
                ToastUtil.showShort("网络异常，停止下载[" + task.getName() + "]");
                break;
        }
    }
}
