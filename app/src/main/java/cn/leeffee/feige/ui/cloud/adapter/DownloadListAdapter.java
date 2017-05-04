package cn.leeffee.feige.ui.cloud.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import cn.leeffee.feige.ui.cloud.service.IDownloadService;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * Created by lhfei on 2017/4/11.
 */

public class DownloadListAdapter extends BaseAdapter {
    private static final String TAG = "DownloadListAdapter";
    private List<DownloadTask> data;
    private Activity mAct;
    private DBTool mDBTool;

    public void setData(List<DownloadTask> data) {
        this.data = data;
    }

    public DownloadListAdapter(Activity act) {
        mAct = act;
        data = new ArrayList<>();
        mDBTool = new DBTool(act);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public DownloadTask getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final DownloadTask task = data.get(position);
        if (convertView == null) {
            convertView = View.inflate(App.getAppContext(), R.layout.progress_download, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.taskId = task.getId();
        holder.mProgressBar.setMax(100);
        holder.mFileName.setText(task.getName());
        changeProgressStatus(holder, task);
        holder.mAddQueueTime.setText(task.getAddQueueTime());
        // 等待或在运行的下载
        holder.mCancel.setTag(task);
        holder.mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(mAct).setTitle("提示").setMessage("您确认要将所选文件从任务队列中删除吗？").setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DownloadTask task = (DownloadTask) v.getTag();
                        if (task.getStatus() == ITransferConstants.NET_EXCEPTION) {
                            updateDownloadTask(task, ITransferConstants.STATUS_CANCEL);
                        } else if (task.getStatus() == 3 || task.getStatus() == 1 || task.getStatus() == 7 || task.getStatus() == 8 || task.getStatus() == 9 || task.getStatus() == 10 || task.getStatus() == 11 || task.getStatus() == 12 || task.getStatus() == 13) {
                            updateDownloadTask(task, ITransferConstants.STATUE_DELETE);// 删除记录
                        } else { // service 中终止任务、并更改记录状态
                            try {
                                IDownloadService service = App.downloadService;
                                if (service != null) {
                                    if (service.cancelTask(task.getId())) {// 中断下载过程
                                        data.remove(task);
                                        DownloadListAdapter.this.notifyDataSetChanged();
                                        ToastUtil.showShort("已删除下载[" + task.getName() + "]记录。");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                //Log.i(TAG, e.toString(), e);
                            }
                        }
                        Log.e(TAG, "\t\t\t\t-->getView: Id=" + task.getId() + ", Name=" + task.getName() + ", Status=" + task.getStatus());
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

    private void updateDownloadTask(DownloadTask task, int type) {
        try {
            //  Log.i(TAG, "updateStatus : " + "taskId=" + task.getId() + ", status=" + type);
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("status", type);
            mDBTool.updateDownloadTask(task.getId(), statusMap);
            data.remove(task);
            this.notifyDataSetChanged();
            ToastUtil.showShort("已删除下载[" + task.getName() + "]记录。");
            ((DownLoadListPage) PageFactory.createPage(0, mAct)).isShowEmpty(data);
        } catch (Exception e) {
            ToastUtil.showShort("取消下载[\" + task.getName() + \"]失败！");
        }
    }


    public static class ViewHolder {
        @BindView(R.id.download_progress_filename_tv)
        public TextView mFileName;//文件名
        @BindView(R.id.download_progress_center_rl)
        public RelativeLayout mCenterLayout;//中的布局  包有进度条和百分比文本的相对布局
        @BindView(R.id.download_progress_percentage_tv)
        public TextView mPercentage;//百分比
        @BindView(R.id.download_progress_pb)
        public ProgressBar mProgressBar; //进度条
        @BindView(R.id.download_progress_cancel_btn)
        public Button mCancel;//取消下载
        @BindView(R.id.download_progress_complete_size_tv)
        public TextView mCompleteSize;//已经下载完成大小
        @BindView(R.id.download_progress_add_queue_time_tv)
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
    public void changeProgressStatus(ViewHolder holder, DownloadTask task) {
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
}
