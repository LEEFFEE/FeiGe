package cn.leeffee.feige.ui.cloud.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.DateUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.StringUtil;

/**
 * Created by lhfei on 2017/3/24.
 */

public class FileListAdapter extends BaseAdapter {
    static class ViewHolder {
        //        holder.icon = (ImageView) convertView.findViewById(R.id.dropbox_file_icon);
        //        holder.folderShareIcon = (ImageView) convertView.findViewById(R.id.uspace_folder_share_icon);
        //        holder.fileShareIcon = (ImageView) convertView.findViewById(R.id.uspace_file_share_icon);
        //        holder.downloadIcon = (ImageView) convertView.findViewById(R.id.uspace_file_download_icon);
        //        holder.fileLayout = (RelativeLayout) convertView.findViewById(R.id.dropbox_file_layout);
        //        holder.fnameText = (TextView) convertView.findViewById(R.id.dropbox_filename_text);
        //        holder.fsizeText = (TextView) convertView.findViewById(R.id.dropbox_filesize_text);
        //        holder.fdateText = (TextView) convertView.findViewById(R.id.dropbox_filedate_text);
        //        holder.floderText = (TextView) convertView.findViewById(R.id.dropbox_flodername_text);
        //        convertView.setTag(holder);
        @BindView(R.id.file_item_icon)
        ImageView fileIcon;
        //        @BindView(R.id.uspace_folder_share_icon)
        //        ImageView folderShareIcon;
        @BindView(R.id.file_item_share_icon)
        ImageView fileShareIcon;
        @BindView(R.id.file_item_download_icon)
        ImageView downloadIcon;
        @BindView(R.id.file_item_file_layout)
        RelativeLayout fileLayout;
        @BindView(R.id.file_item_filename)
        TextView fileName;
        @BindView(R.id.file_item_fileSize)
        TextView fileSize;
        @BindView(R.id.file_item_file_location)
        TextView fileLocation;
        @BindView(R.id.file_item_file_upload_date)
        TextView fileUploadDate;
        //        @BindView(R.id.file_item_folder_name)
        //        TextView folderName;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            view.setTag(this);
        }
    }

    private LayoutInflater inflater = null;

    private List<USpaceFile> data = null;

    public FileListAdapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);
        data = new ArrayList<>();
    }

    public boolean containsFile(String fileName) {
        for (USpaceFile _file : data) {
            if (_file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    public USpaceFile getByPath(String path) {
        for (USpaceFile _file : data) {
            if (_file.getDiskPath().equals(path)) {
                return _file;
            }
        }
        return null;
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
        USpaceFile uf = data.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.uspace_file_list_item, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (uf.isFolder()) {
            holder.fileIcon.setImageResource(uf.isParent() ? R.mipmap.uspace_back_folder : R.mipmap.uspace_default_folder);
            holder.downloadIcon.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.GONE);
            //  holder.fileLocation.setVisibility(View.GONE);
            // holder.folderName.setVisibility(View.VISIBLE);
            // holder.folderName.setText(uf.getName());
            //  holder.fileLayout.setVisibility(View.GONE);
        } else {
            holder.fileIcon.setImageResource(FileUtil.getFileBitmapResource(uf));
            // holder.folderShareIcon.setVisibility(View.GONE);
            if (FileUtil.isFileExist(uf)) {
                holder.downloadIcon.setVisibility(View.VISIBLE);
            } else {
                holder.downloadIcon.setVisibility(View.GONE);
            }
            //  holder.folderName.setVisibility(View.GONE);
            //  holder.fileLayout.setVisibility(View.VISIBLE);
            //  holder.fileName.setText(uf.getName());
            // holder.fileLocation.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.fileSize.setText(StringUtil.getFileSize(uf.getSize()));
            //            if (uf.getIsGroupFile() == AppConstants.GROUP_FILE) {
            //                holder.fileUploadDate.setText(uf.getCreaterName() + "    " + DateUtil.timestamp2String(uf.getModifyTime(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
            //            } else {
            //                holder.fileUploadDate.setText(DateUtil.timestamp2String(uf.getModifyTime(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
            //            }
        }
        holder.fileName.setText(uf.getName());
        if (uf.getIsGroupFile() == AppConstants.GROUP_FILE) {
            if (!TextUtils.isEmpty(uf.getCreaterName())) {
                holder.fileUploadDate.setText(uf.getCreaterName() + "\t" + DateUtil.timestamp2String(uf.getModifyTime(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
            } else {
                holder.fileUploadDate.setText(DateUtil.timestamp2String(uf.getModifyTime(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
            }
        } else {
            holder.fileUploadDate.setText(DateUtil.timestamp2String(uf.getModifyTime(), DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
        }
        if (uf.isShared()) {
            holder.fileShareIcon.setVisibility(View.VISIBLE);
        } else {
            holder.fileShareIcon.setVisibility(View.GONE);
        }
        if (uf.isSearchFile()) {
            holder.fileLocation.setVisibility(View.VISIBLE);
            holder.fileLocation.setText("路径：" + uf.getDiskPath());
        } else {
            holder.fileLocation.setVisibility(View.GONE);
        }
        return convertView;
    }
}
