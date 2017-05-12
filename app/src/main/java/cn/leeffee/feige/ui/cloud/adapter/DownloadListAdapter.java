package cn.leeffee.feige.ui.cloud.adapter;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.factory.PageFactory;
import cn.leeffee.feige.ui.cloud.fragment.TransFragment;
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import cn.leeffee.feige.ui.cloud.service.IDownloadService;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * Created by lhfei on 2017/4/11.
 */

public class DownloadListAdapter extends FileTransBaseAdapter<DownloadTask> {
    private static final String TAG = "DownloadListAdapter";
    private Fragment mFrag;

    public DownloadListAdapter(Fragment frag) {
        super();
        mFrag = frag;
    }

    @Override
    public BaseHolder<DownloadTask> getHolder(int position) {
        return new DownloadListHolder(this);
    }

    private void deleteDownloadTask(DownloadTask task) {
        try {
            getData().remove(task);
            mDBTool.deleteDownloadTasks(task.getId());
            ToastUtil.showShort("已删除下载[" + task.getName() + "]记录。");
        } catch (Exception e) {
            ToastUtil.showShort("删除[" + task.getName() + "]失败！");
        }
    }

    /**
     * * 改变条目进度状态
     *
     * @param holder
     * @param task
     */
    public void changeProgressStatus(DownloadListHolder holder, DownloadTask task) {
        if (task.getType() == DownloadTask.SHARED_FOLDER_TYPE) {
            holder.mCompleteSize.setText(StringUtil.getFileSize(task.getDownloadLength()));
        } else {
            holder.mCompleteSize.setText(StringUtil.getFileSize(task.getDownloadLength()) + "/" + StringUtil.getFileSize(task.getFileLength()));
        }
        holder.mProgressBar.setProgress(task.getPercent());
        holder.mCenterLayout.setVisibility(View.VISIBLE);
        switch (task.getStatus()) {
            case ITransferConstants.FILE_NOT_EXIST://13
                holder.mPercentage.setText("文件下载失败");
                break;
            case ITransferConstants.TRANSFER_FAIL_ERROR://12
                holder.mPercentage.setText("文件下载失败");
                break;
            case 11:
                holder.mPercentage.setText("未知解析错误");
                break;
            case ITransferConstants.SERVER_RESPONSE_ERROR://10
                holder.mPercentage.setText("服务器响应错误");
                break;
            case 9:
                holder.mPercentage.setText("未知异常");
                break;
            case 8:
                holder.mPercentage.setText("sdcard 存储错误");
                break;
            case 7:
                holder.mPercentage.setText("提取码错误");
                break;
            case ITransferConstants.STATUS_CANCEL://5
                holder.mPercentage.setText("已停止下载");
                break;
            case ITransferConstants.NET_EXCEPTION://4
                holder.mPercentage.setText("网络异常，停止下载");
                break;
            case ITransferConstants.STATUS_FINISH://3
                holder.mPercentage.setText("完成");
                holder.mProgressBar.setProgress(100);
                holder.mCenterLayout.setVisibility(View.GONE);
                holder.mCompleteSize.setText("下载完成，大小为：" + StringUtil.getFileSize(task.getDownloadLength()));
                break;
            case ITransferConstants.STATUS_WAIT:
                holder.mPercentage.setText("等待中...");
                holder.mProgressBar.setProgress(0);
                break;
            default:
                holder.mProgressBar.setProgress(task.getPercent());
                holder.mPercentage.setText(task.getPercent() + "%");
                break;
        }
    }

    /**
     * 删除选中的条目
     */
    @Override
    public void delete() {
        List<DownloadTask> tasks = getSelectedTasks();
        for (DownloadTask task : tasks) {
            if (task.getStatus() == ITransferConstants.STATUS_RUN) {
                try {
                    IDownloadService service = App.downloadService;
                    if (service != null) {
                        service.cancelTask(task.getId());// 中断上传过程
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString(), e);
                }
            }
            getData().remove(task);
            mDBTool.deleteDownloadTasks(task.getId());
        }
        notifyShow();
    }

    /**
     * 通知界面刷新
     */
    public void notifyShow() {
        this.notifyDataSetChanged();
        PageFactory.createPage(TransFragment.DOWNLOAD, mFrag).isShowEmpty(getData());
        notifyShowOnItem();
    }
    /**
     * 触发checkbox时界面刷新
     */
    public void notifyShowOnItem() {
        ((TransFragment) mFrag).onSelectedCountChange(getSelectedTasks().size());
    }
    /**
     * 删除条目
     *
     * @param task
     */
    public void deleteItem(final DownloadTask task) {
        new AlertDialog.Builder(mFrag.getActivity()).setTitle("提示").setMessage("您确认要将所选文件从任务队列中删除吗？").setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (task.getStatus() == ITransferConstants.NET_EXCEPTION ||
                        task.getStatus() == 3 || task.getStatus() == 1 ||
                        task.getStatus() == 7 || task.getStatus() == 8 ||
                        task.getStatus() == 9 || task.getStatus() == 10 ||
                        task.getStatus() == 11 || task.getStatus() == 12 ||
                        task.getStatus() == 13) {
                    deleteDownloadTask(task);// 删除记录
                } else { // service 中终止任务、并更改记录状态
                    try {
                        IDownloadService service = App.downloadService;
                        if (service != null) {
                            service.cancelTask(task.getId());// 中断下载过程
                            deleteDownloadTask(task);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                notifyShow();
                Log.e(TAG, "\t\t\t\t-->getView: Id=" + task.getId() + ", Name=" + task.getName() + ", Status=" + task.getStatus());
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
