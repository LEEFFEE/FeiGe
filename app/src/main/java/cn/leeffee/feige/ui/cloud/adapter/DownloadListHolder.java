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
import cn.leeffee.feige.ui.cloud.service.DownloadTask;

/**
 * Created by lhfei on 2017/5/5.
 */

public class DownloadListHolder extends BaseHolder<DownloadTask> {
    @BindView(R.id.download_progress_filename_tv)
    public TextView mFileName;//文件名
    @BindView(R.id.download_progress_center_rl)
    public RelativeLayout mCenterLayout;//中的布局  包有进度条和百分比文本的相对布局
    @BindView(R.id.download_progress_percentage_tv)
    public TextView mPercentage;//百分比
    @BindView(R.id.download_progress_pb)
    public ProgressBar mProgressBar; //进度条
    @BindView(R.id.download_progress_delete_btn)
    public Button mDelete;//删除
    @BindView(R.id.download_progress_complete_size_tv)
    public TextView mCompleteSize;//已经下载完成大小
    @BindView(R.id.download_progress_add_queue_time_tv)
    public TextView mAddQueueTime; //开始下载的时间
    @BindView(R.id.download_progress_check_status)
    public CheckBox mCheckStatus;//选中状态
    public Integer taskId;

    private DownloadListAdapter mAdapter;

    public DownloadListHolder(DownloadListAdapter adapter) {
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
        DownloadListHolder other = (DownloadListHolder) obj;
        if (taskId == null) {
            if (other.taskId != null)
                return false;
        } else if (!taskId.equals(other.taskId))
            return false;
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.progress_download;
    }

    @Override
    public void refreshView(final DownloadTask task) {
        taskId = task.getId();
        mProgressBar.setMax(100);
        mFileName.setText(task.getName());
        mAddQueueTime.setText(task.getAddQueueTime());
        mAdapter.changeProgressStatus(this, task);
        if (mAdapter.isMultiCheck()) {
            mDelete.setVisibility(View.GONE);
            mCheckStatus.setVisibility(View.VISIBLE);
            mCheckStatus.setChecked(task.isChecked());
        } else {
            mDelete.setVisibility(View.VISIBLE);
            mCheckStatus.setVisibility(View.GONE);
        }
        mCheckStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                task.setChecked(isChecked);
                mAdapter.notifyShowOnItem();
            }
        });
        // 等待或在运行的下载
        mDelete.setTag(task);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.deleteItem((DownloadTask) v.getTag());
            }
        });
    }
}
