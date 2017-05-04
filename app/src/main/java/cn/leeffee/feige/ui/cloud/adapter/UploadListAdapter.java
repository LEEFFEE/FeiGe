package cn.leeffee.feige.ui.cloud.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.factory.PageFactory;
import cn.leeffee.feige.ui.cloud.page.DownLoadListPage;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.ui.cloud.service.IUploadService;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * Created by lhfei on 2017/4/11.
 */

public class UploadListAdapter extends BaseAdapter {
    private static final String TAG = "UploadListAdapter";
    private List<UploadTask> data;
    private Activity mAct;
    private DBTool mDBTool;

    public void setData(List<UploadTask> data) {
        this.data = data;
    }

    public UploadListAdapter(Activity act) {
        mAct = act;
        data = new ArrayList<>();
        mDBTool = new DBTool(act);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UploadTask getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final UploadTask task = data.get(position);
        if (convertView == null) {
            convertView = View.inflate(App.getAppContext(), R.layout.progress_upload, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.taskId = task.getId();
        holder.mProgressBar.setMax(100);
        holder.mFileName.setText(task.getName());
        holder.mAddQueueTime.setText(task.getAddQueueTime());
        changeProgressStatus(holder, task);
        // 等待或在运行的下载
        holder.mCancel.setTag(task);
        holder.mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new android.app.AlertDialog.Builder(mAct).setTitle("提示").setMessage("确认删除吗？").setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UploadTask task = (UploadTask) v.getTag();
                        if (task.getStatus() == ITransferConstants.NET_EXCEPTION) {
                            updateUploadTask(task, ITransferConstants.STATUS_CANCEL);
                        } else if (task.getStatus() == ITransferConstants.STATUS_FINISH ||
                                task.getStatus() == ITransferConstants.STATUS_WAIT ||
                                task.getStatus() == ITransferConstants.SERVER_RESPONSE_ERROR ||
                                task.getStatus() == ITransferConstants.SERVER_RESPONSE_PARSE_ERROR ||
                                task.getStatus() == ITransferConstants.TRANSFER_FAIL_ERROR ||
                                task.getStatus() == ITransferConstants.FILE_NOT_EXIST) {
                            updateUploadTask(task, ITransferConstants.STATUE_DELETE);// 删除记录
                        } else { // service 中终止任务、并更改记录状态
                            try {
                                IUploadService service = App.uploadService;
                                if (service != null) {
                                    if (service.cancelTask(task.getId())) {// 中断上传过程
                                        data.remove(task);
                                        UploadListAdapter.this.notifyDataSetChanged();
                                        ToastUtil.showShort("已删除上传[" + task.getName() + "]记录。");
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(TAG, e.toString(), e);
                            }
                        }
                    }
                }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        return convertView;
    }

    /**
     * 点击取消时 更新上传任务信息
     *
     * @param task
     * @param type
     */
    private void updateUploadTask(UploadTask task, int type) {
        try {
            Log.i(TAG, "updateStatus : " + "taskId=" + task.getId() + ", status=" + type);
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("status", type);
            mDBTool.updateUploadTask(task.getId(), statusMap);
            data.remove(task);
            notifyDataSetChanged();
            ToastUtil.showShort("已删除上传[" + task.getName() + "]记录。");
            ((DownLoadListPage) PageFactory.createPage(1, mAct)).isShowEmpty(data);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            ToastUtil.showShort("取消上传[" + task.getName() + "]失败！");
        }
    }


    public static class ViewHolder {
        @BindView(R.id.upload_progress_filename_tv)
        public TextView mFileName;//文件名
        @BindView(R.id.upload_progress_center_rl)
        public RelativeLayout mCenterLayout;//中的布局  包有进度条和百分比文本的相对布局
        @BindView(R.id.upload_progress_percentage_tv)
        public TextView mPercentage;//百分比
        @BindView(R.id.upload_progress_pb)
        public ProgressBar mProgressBar; //进度条
        @BindView(R.id.upload_progress_cancel_btn)
        public Button mCancel;//取消下载
        @BindView(R.id.upload_progress_complete_size_tv)
        public TextView mCompleteSize;//已经下载完成大小
        @BindView(R.id.upload_progress_add_queue_time_tv)
        public TextView mAddQueueTime; //开始下载的时间
        public Integer taskId;

        public ViewHolder() {

        }

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            view.setTag(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ViewHolder other = (ViewHolder) obj;
            if (taskId == null) {
                if (other.taskId != null)
                    return false;
            } else if (!taskId.equals(other.taskId))
                return false;
            return true;
        }
    }

    /**
     * * 改变条目进度状态
     *
     * @param holder
     * @param task
     */
    public void changeProgressStatus(ViewHolder holder, UploadTask task) {
        holder.mProgressBar.setProgress(task.getPercent());
        holder.mCenterLayout.setVisibility(View.VISIBLE);
        switch (task.getStatus()) {
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
}
