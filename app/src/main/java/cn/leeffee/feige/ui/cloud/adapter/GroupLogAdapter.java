package cn.leeffee.feige.ui.cloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;

/**
 * Created by lhfei on 2017/4/19.
 */

public class GroupLogAdapter extends BaseAdapter {
    public static class ViewHolder {
        @BindView(R.id.group_log_message_tv)
        TextView tvLog;
        @BindView(R.id.group_log_time_tv)
        TextView tvTime;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }

    private List<ApiGroupLog> data;

    public List<ApiGroupLog> getData() {
        return data;
    }

    public void setData(List<ApiGroupLog> data) {
        this.data = data;
    }

    private LayoutInflater inflater = null;

    public GroupLogAdapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);
        data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ApiGroupLog groupLog = data.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_logs_item, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //			holder.IconSex.setImageResource(file.isParent() ? R.drawable.uspace_back_folder : R.drawable.uspace_default_folder);
        holder.tvLog.setText(groupLog.getMessage());
        holder.tvTime.setText(groupLog.getLogTime().replaceAll("T", " "));
        return convertView;
    }
}
