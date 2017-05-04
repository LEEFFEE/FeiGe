package cn.leeffee.feige.ui.cloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;

/**
 * Created by lhfei on 2017/3/21.
 */

public class DirListAdapter extends BaseAdapter {
    static class ViewHolder {
        @BindView(R.id.folder_icon)
        ImageView folderIcon;
        @BindView(R.id.folder_name)
        TextView folderName;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }

    private LayoutInflater inflater = null;

    private List<USpaceFile> data = null;

    public DirListAdapter(Context ctx) {
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

    public void setData(List<USpaceFile> data) {
        this.data.clear();
        this.data.addAll(data);
    }

    public List<USpaceFile> getData() {
        return data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        USpaceFile fileInfo = data.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dir_list_item, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.folderIcon.setImageResource(fileInfo.isParent() ? R.mipmap.uspace_back_folder : R.mipmap.uspace_default_folder);
        holder.folderName.setText(fileInfo.getName());
        return convertView;
    }
}
