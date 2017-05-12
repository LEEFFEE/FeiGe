package cn.leeffee.feige.ui.cloud.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.service.UploadTask;

/**
 * Created by lhfei on 2017/5/5.
 */

public class UploadListHolder extends BaseHolder<UploadTask> {
    @BindView(R.id.upload_progress_filename_tv)
    public TextView mFileName;//文件名
    @BindView(R.id.upload_progress_center_rl)
    public RelativeLayout mCenterLayout;//中的布局  包有进度条和百分比文本的相对布局
    @BindView(R.id.upload_progress_percentage_tv)
    public TextView mPercentage;//百分比
    @BindView(R.id.upload_progress_pb)
    public ProgressBar mProgressBar; //进度条
    @BindView(R.id.upload_progress_complete_size_tv)
    public TextView mCompleteSize;//已经下载完成大小
    @BindView(R.id.upload_progress_add_queue_time_tv)
    public TextView mAddQueueTime; //开始下载的时间
    @BindView(R.id.upload_progress_delete_btn)
    public Button mDelete;//删除

    //        @BindView(R.id.upload_progress_running_status)
    //        public CheckBox mRunningStatus;//运行状态
    @BindView(R.id.upload_progress_check_status)
    public CheckBox mCheckStatus;//选中状态

    public Integer taskId;
    private UploadListAdapter mAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.progress_upload;
    }

    public UploadListHolder(UploadListAdapter adapter) {
        super();
        mAdapter = adapter;
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
        UploadListHolder other = (UploadListHolder) obj;
        if (taskId == null) {
            if (other.taskId != null)
                return false;
        } else if (!taskId.equals(other.taskId))
            return false;
        return true;
    }

    @Override
    public void refreshView(final UploadTask item) {
        taskId = item.getId();
        mProgressBar.setMax(100);
        mFileName.setText(item.getName());
        mAddQueueTime.setText(item.getAddQueueTime());
        mAdapter.changeProgressStatus(this, item);

        //        if (task.getStatus() == ITransferConstants.STATUS_RUN) {
        //            mRunningStatus.setVisibility(View.VISIBLE);
        //        } else {
        //            mRunningStatus.setVisibility(View.GONE);
        //        }
        //        mRunningStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //            @Override
        //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //                if (isChecked) {
        //                    //暂停
        //                    updateUploadTask(task, ITransferConstants.STATUS_PAUSE);
        //                    try {
        //                        if (App.uploadService != null) {
        //                            App.uploadService.pauseTask(task.getId());
        //                            UploadListAdapter.this.notifyDataSetChanged();
        //                        }
        //                    } catch (Exception e) {
        //                        Log.i(TAG, e.toString(), e);
        //                    }
        //                } else {
        //                    //继续
        //                    updateUploadTask(task, ITransferConstants.STATUS_RUN);
        //                    try {
        //                        if (App.uploadService != null) {
        //                            App.uploadService.unlock();
        //                        }
        //                    } catch (Exception e) {
        //                        Log.i(TAG, e.toString(), e);
        //                    }
        //                }
        //                changeProgressStatus(holder, task);
        //            }
        //        });
        if (mAdapter.isMultiCheck()) {
            mDelete.setVisibility(View.GONE);
            mCheckStatus.setVisibility(View.VISIBLE);
            mCheckStatus.setChecked(item.isChecked());
        } else {
            mDelete.setVisibility(View.VISIBLE);
            mCheckStatus.setVisibility(View.GONE);
        }
        mCheckStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               item.setChecked(isChecked);
               mAdapter.notifyShowOnItem();
            }
        });
        // 等待或在运行的下载
        mDelete.setTag(item);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.deleteItem((UploadTask) v.getTag());
            }
        });
    }
}
