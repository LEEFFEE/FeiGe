package cn.leeffee.feige.ui.cloud.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.factory.PageFactory;
import cn.leeffee.feige.ui.cloud.fragment.TransFragment;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.ui.cloud.service.IUploadService;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * Created by lhfei on 2017/5/5.
 */

public class UploadListAdapter extends FileTransBaseAdapter<UploadTask> {
    private static final String TAG = "UploadListAdapter";
    private Fragment mFrag;

    public UploadListAdapter(Fragment frag) {
        super();
        mFrag = frag;
    }

    @Override
    public BaseHolder<UploadTask> getHolder(int position) {
        return new UploadListHolder(this);
    }

    /**
     * 点击取消时 更新上传任务信息
     */
    public void deleteUploadTask(UploadTask task) {
        try {
            mDBTool.deleteUploadTasks(task.getId());
            getData().remove(task);
            ToastUtil.showShort("已删除上传[" + task.getName() + "]记录。");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            ToastUtil.showShort("删除上传[" + task.getName() + "]失败！");
        }
    }

    /**
     * * 改变条目进度状态
     *
     * @param holder
     * @param task
     */
    public void changeProgressStatus(UploadListHolder holder, UploadTask task) {
        holder.mProgressBar.setProgress(task.getPercent());
        holder.mCenterLayout.setVisibility(View.VISIBLE);
        switch (task.getStatus()) {
            case ITransferConstants.STATUS_PAUSE:
                holder.mPercentage.setText("暂停中...");
                break;
            case ITransferConstants.TRANSFER_FAIL_ERROR:
                holder.mPercentage.setText("上传文件失败");
                break;
            case ITransferConstants.FILE_NOT_EXIST:
                holder.mPercentage.setText("上传文件不存在");
                break;
            case ITransferConstants.SERVER_RESPONSE_ERROR:
                holder.mPercentage.setText("服务器响应错误，请重新上传");
                break;
            case ITransferConstants.STATUS_CANCEL:
                holder.mPercentage.setText("已停止上传");
                break;
            case ITransferConstants.NET_EXCEPTION:
                holder.mPercentage.setText("网络异常，停止上传");
                break;
            case ITransferConstants.STATUS_FINISH:
                holder.mPercentage.setText("完成");
                holder.mProgressBar.setProgress(100);
                holder.mCenterLayout.setVisibility(View.GONE);
                holder.mCompleteSize.setText("上传完成，大小为：" + StringUtil.getFileSize(task.getUploadLength()));
                break;
            case ITransferConstants.STATUS_WAIT:
                holder.mPercentage.setText("等待中...");
                break;
            default:
                holder.mPercentage.setText(task.getPercent() + "%");
                holder.mCompleteSize.setText(StringUtil.getFileSize(task.getUploadLength()) + "/" + StringUtil.getFileSize(task.getFileLength()));
                break;
        }
    }

    /**
     * 删除选中的条目
     */
    @Override
    public void delete() {
        List<UploadTask> tasks = getSelectedTasks();
        for (UploadTask task : tasks) {
            if (task.getStatus() == ITransferConstants.STATUS_RUN) {
                try {
                    IUploadService service = App.uploadService;
                    if (service != null) {
                        service.cancelTask(task.getId());// 中断上传过程
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString(), e);
                }
            }
            getData().remove(task);
            mDBTool.deleteUploadTasks(task.getId());
        }
        notifyShow();
    }

    /**
     * 通知界面刷新
     */
    public void notifyShow() {
        this.notifyDataSetChanged();
        PageFactory.createPage(TransFragment.UPLOAD, mFrag).isShowEmpty(getData());
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
    public void deleteItem(final UploadTask task) {
        new AlertDialog.Builder(mFrag.getActivity()).setTitle("提示").setMessage("确认删除吗？").setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (task.getStatus() == ITransferConstants.NET_EXCEPTION ||
                        task.getStatus() == ITransferConstants.STATUS_PAUSE ||
                        task.getStatus() == ITransferConstants.STATUS_FINISH ||
                        task.getStatus() == ITransferConstants.STATUS_WAIT ||
                        task.getStatus() == ITransferConstants.SERVER_RESPONSE_ERROR ||
                        task.getStatus() == ITransferConstants.SERVER_RESPONSE_PARSE_ERROR ||
                        task.getStatus() == ITransferConstants.TRANSFER_FAIL_ERROR ||
                        task.getStatus() == ITransferConstants.FILE_NOT_EXIST) {
                    deleteUploadTask(task);// 删除记录
                } else { // service 中终止任务、并更改记录状态
                    try {
                        IUploadService service = App.uploadService;
                        if (service != null) {
                            service.cancelTask(task.getId()); // 中断上传过程
                            deleteUploadTask(task);
                        }
                    } catch (Exception e) {
                        Log.i(TAG, e.toString(), e);
                    }
                }
                notifyShow();
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
