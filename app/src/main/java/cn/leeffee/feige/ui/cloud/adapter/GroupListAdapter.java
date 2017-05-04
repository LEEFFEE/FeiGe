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
import cn.leeffee.feige.ui.cloud.entity.USpaceGroup;
import cn.leeffee.feige.utils.StringUtil;

/**
 * Created by lhfei on 2017/4/17.
 */

public class GroupListAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private List<USpaceGroup> data = null;
    private static final int WAIT_DELETE = 2;

    static class ViewHolder {
        @BindView(R.id.dropbox_file_icon)
        ImageView icon;
//        @BindView(R.id.uspace_folder_share_icon)
//        ImageView folderShareIcon;
        @BindView(R.id.icn_group_expire)
        ImageView icnGroupExpire;
//        @BindView(R.id.uspace_file_share_icon)
//        ImageView fileShareIcon;
//        @BindView(R.id.uspace_file_download_icon)
//        ImageView downloadIcon;
//        @BindView(R.id.dropbox_file_layout)
//        RelativeLayout fileLayout;
        @BindView(R.id.dropbox_filename_text)
        TextView filename;
        @BindView(R.id.dropbox_filesize_text)
        TextView spaceInfo;

        //        holder.icon = (ImageView) view.findViewById(R.id.dropbox_file_icon);
        //        holder.folderShareIcon = (ImageView) view.findViewById(R.id.uspace_folder_share_icon);
        //        holder.icnGroupExpire = (ImageView) view.findViewById(R.id.icn_group_expire);
        //        holder.fileShareIcon = (ImageView) view.findViewById(R.id.uspace_file_share_icon);
        //        holder.downloadIcon = (ImageView) view.findViewById(R.id.uspace_file_download_icon);
        //        holder.fileLayout = (RelativeLayout) view.findViewById(R.id.dropbox_file_layout);
        //        holder.name = (TextView) view.findViewById(R.id.dropbox_filename_text);
        //        holder.spaceInfo = (TextView) view.findViewById(R.id.dropbox_filesize_text);
        //        view.setTag(holder);

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }

    public GroupListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public boolean contains(String fileName) {
        for (USpaceGroup group : data) {
            if (group.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
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

    public void setData(List<USpaceGroup> result) {
        this.data.clear();
        this.data.addAll(result);
    }

    public List<USpaceGroup> getData() {
        return data;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        USpaceGroup group = data.get(position);
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.uspace_group_list_item, null);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.icon.setImageResource(R.mipmap.avatar_groups);
//        holder.fileShareIcon.setVisibility(View.GONE);
//        holder.downloadIcon.setVisibility(View.GONE);
//        holder.folderShareIcon.setVisibility(View.GONE);
        if (group.getStatus() == WAIT_DELETE) {
            holder.icnGroupExpire.setVisibility(View.VISIBLE);
        } else {
            holder.icnGroupExpire.setVisibility(View.GONE);
        }
        holder.filename.setText(group.getName());
        holder.spaceInfo.setText("空间：" + StringUtil.getFileSize(group.getUsedSpace()) + "/" + StringUtil.getFileSize(group.getHardLimit()));
        return view;
    }
}
