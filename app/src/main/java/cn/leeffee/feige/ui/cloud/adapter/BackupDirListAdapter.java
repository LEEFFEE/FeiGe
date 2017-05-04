package cn.leeffee.feige.ui.cloud.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import cn.leeffee.feige.ui.cloud.service.BackupTask;
import cn.leeffee.feige.ui.cloud.service.IBackupService;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.ToastUtil;

import static android.content.ContentValues.TAG;
import static cn.leeffee.feige.ui.cloud.activity.BackupActivity.mBackupBind;


/**
 * Created by lhfei on 2017/4/21.
 */

public class BackupDirListAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    List<BackupTask> taskList;
    DBTool dbTool;

    public BackupDirListAdapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);
        taskList = new ArrayList<>();
        dbTool = new DBTool(App.getAppContext());
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<BackupTask> data) {
        taskList.clear();
        taskList.addAll(data);
    }

    public List<BackupTask> getData() {
        return taskList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BackupTask taskInfo = taskList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.backup_list_item, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.taskId = taskInfo.getId();
        holder.dirName.setText(taskInfo.getTitle());
        holder.dirPath.setText(taskInfo.getLocalPath());
        int status = taskInfo.getStatus();
        switch (status) {
            case ITransferConstants.STATUS_WAIT:
                //                holder.waitText.setVisibility(View.VISIBLE);
                //                holder.runText.setVisibility(View.GONE);
                //                holder.finishText.setVisibility(View.GONE);
                //                holder.failureText.setVisibility(View.GONE);
                holder.backupStatus.setText(R.string.str_backup_wait);
                break;
            case ITransferConstants.STATUS_RUN:
                //                holder.waitText.setVisibility(View.GONE);
                //                holder.runText.setVisibility(View.VISIBLE);
                //                holder.finishText.setVisibility(View.GONE);
                //                holder.failureText.setVisibility(View.GONE);
                holder.backupStatus.setText(R.string.str_backup_running);
                break;
            case ITransferConstants.STATUS_FINISH:
                if (taskInfo.getFinishTime() != null && !taskInfo.getFinishTime().equals("")) {
                    holder.backupStatus.setText(App.getAppContext().getString(R.string.str_backup_finish) + " " + taskInfo.getFinishTime());
                } else {
                    holder.backupStatus.setText(R.string.str_backup_finish);
                }
                break;
            default:
                holder.backupStatus.setText(R.string.str_backup_failure);
                break;
        }
        final BackupTask tmpTask = taskInfo;
        holder.againBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpTask.setStatus(ITransferConstants.STATUS_WAIT);
                updateBackupStatus(tmpTask.getId(), ITransferConstants.STATUS_WAIT);
                notifyDataSetChanged();
                LogUtil.e("备份服务是否绑定:" + mBackupBind);
                if (mBackupBind && App.backupService != null) {
                    App.backupService.unlock();
                }

            }
        });
        if (status == ITransferConstants.STATUS_WAIT || status == ITransferConstants.STATUS_RUN) {
            holder.againBackup.setEnabled(false);
        } else {
            holder.againBackup.setEnabled(true);
        }
        holder.cancelBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (tmpTask.getStatus() == ITransferConstants.STATUS_WAIT || tmpTask.getStatus() == ITransferConstants.STATUS_FINISH) {
                    dbTool.deleteBackupTask(tmpTask.getId());
                    taskList.remove(tmpTask);
                    notifyDataSetChanged();
                    ToastUtil.showShort("备份目录取消成功:" + tmpTask.getLocalPath());
                } else {
                    IBackupService service = App.backupService;
                    try {
                        if (mBackupBind && service != null) {
                            if (service.cancelTask(tmpTask.getId())) {
                                taskList.remove(tmpTask);
                                notifyDataSetChanged();
                                ToastUtil.showShort("备份目录取消成功:" + tmpTask.getLocalPath());
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.e(e.toString());
                    }
                }
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        @BindView(R.id.backup_list_item_dir_name_tv)
        TextView dirName;
        @BindView(R.id.backup_list_item_dir_path_tv)
        TextView dirPath;
        @BindView(R.id.backup_list_item_backup_status_tv)
        TextView backupStatus;
        //        TextView runText;
        //        TextView finishText;
        //        TextView failureText;
        @BindView(R.id.backup_list_item_cancel_backup_btn)
        Button cancelBackup;
        @BindView(R.id.backup_list_item_again_backup_btn)
        Button againBackup;
        Integer taskId;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
            convertView.setTag(this);
            //   holder.dirName = (TextView) convertView.findViewById(R.id.backup_list_item_dir_name_tv);
            // holder.dirPath = (TextView) convertView.findViewById(R.id.backup_list_item_dir_path_tv);

            //            holder.backupStatus = (TextView) convertView.findViewById(R.id.backup_list_item_backup_status_tv);
            //            holder.cancelBackup = (Button) convertView.findViewById(R.id.backup_list_item_cancel_backup_btn);
            //            holder.againBackup = (Button) convertView.findViewById(R.id.backup_list_item_again_backup_btn);
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

    private void updateBackupStatus(Integer taskId, Integer status) {
        Log.i(TAG, "updateBackupStatus : " + "taskId=" + taskId + ", status=" + status);
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("status", status);
        dbTool.updateBackupTask(taskId, statusMap);
    }
}
