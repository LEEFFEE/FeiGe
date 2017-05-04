/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      CheckableFileListView.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-13 下午12:59:33
 * 版本：           
 *
 */
package cn.leeffee.feige.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.utils.DateUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.UploadTypeUtil;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lvhf
 */
public class CheckableFileListView extends ListView {
    /**
     * 当前路径 本地文件路径
     */
    private String currentLocalPath;

    // Root directory set by customer.
    private String root;

    /**
     * 当前目录所有文件列表
     */
    private List<FileInfo> mFileInfos;

    /**
     * 当前目录选中的文件列表
     */
    private Set<FileInfo> mCheckedFileInfos = null;

    public void setFileInfos(List<FileInfo> fileInfos) {
        mFileInfos = fileInfos;
    }

    public List<FileInfo> getFileInfos() {
        return mFileInfos;
    }

    public String getCurrentLocalPath() {
        return currentLocalPath;
    }

    public void setCurrentLocalPath(String currentLocalPath) {
        this.currentLocalPath = currentLocalPath;
    }

    public String[] docTypes = new String[]{"txt", "doc", "docx", "xls", "xlsx", "wps", "htm", "pdf", "ppt"};
    public String[] videoTypes = new String[]{"avi", "wma", "rmvb", "rm", "flash", "mp4", "mid", "3gp", "mp5"};
    public String[] musicTypes = new String[]{"wav", "mp3", "cda", "wma", "mp4", "vqf", "flv", "cd", "rm"};

    public List<String> docTypesList = new ArrayList<>();
    public List<String> videoTypesList = new ArrayList<>();
    public List<String> musicTypesList = new ArrayList<>();

    private CheckableFileListViewAdapter adapter;

    //  protected ICallBack callback;

    Disposable disposable;

    public CheckableFileListView(final Activity act, String pathname, final int type) {
        super(act);
        this.root = this.currentLocalPath = pathname;
        mCheckedFileInfos = new HashSet<>();
        // this.callback = callback;

        initDocTypesList();
        initVideoTypesList();
        initMusicTypesLis();

        //        setOnItemClickListener(new OnItemClickListener() {
        //            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        //                OnItemClickProcess(arg0, arg1, arg2, arg3);
        //            }
        //        });

        //弹出对话框
        String title = act.getString(R.string.strPostDialogTitle);
        String content = "正在搜索您手机上的文件...";
        final MyProgressDialog dialog = new MyProgressDialog(act);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                    disposable = null;
                }
            }
        });
        dialog.show();
        disposable = Flowable.just(this.currentLocalPath).map(new Function<String, List<FileInfo>>() {
            @Override
            public List<FileInfo> apply(@NonNull String pathName) throws Exception {
                return listFileInfos(pathName, type);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<FileInfo>>() {

            @Override
            public void accept(@NonNull List<FileInfo> fileInfos) throws Exception {
                mFileInfos = fileInfos;
                adapter = new CheckableFileListViewAdapter(getContext());
                setAdapter(adapter);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        // new ListLocalFilesTask(act, context, pathname, type).execute();
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }
    //    @Override
    //    public void setOnItemClickListener(OnItemClickListener listener) {
    //        super.setOnItemClickListener(listener);
    //    }

    //    public interface ICallBack {
    //        void execute(CheckableFileListView listview);
    //    }

    //    class ListLocalFilesTask extends GenericTask<Object, String, Boolean> {
    //        private Context context;
    //        private String pathname;
    //        private int type;
    //        private ProgressDialog pdialog;
    //        private Activity act;
    //
    //        public ListLocalFilesTask(Activity act, Context context, String pathname, int type) {
    //            super(act);
    //            this.act = act;
    //            this.context = context;
    //            this.pathname = pathname;
    //            this.type = type;
    //        }
    //
    //        @Override
    //        protected void onPreExecute() {
    //            final String strTitle = act.getString(R.string.strPostDialogTitle);
    //            final String strBody = "正在搜索您手机上的文件...";
    //            pdialog = ProgressDialog.show(act, strTitle, strBody, true, true);
    //        }
    //
    //        @Override
    //        protected void onProgressUpdate(String... values) {
    //            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
    //        }
    //
    //        @Override
    //        public Boolean _doInBackground(Object... params) throws Exception {
    //            fileInfos = listFileInfos(pathname, type);
    //            return true;
    //        }
    //
    //        @Override
    //        public Boolean _processException(Exception e) {
    //            publishProgress(e.getMessage() != null ? e.getMessage() : e.toString());
    //            return false;
    //        }
    //
    //        @Override
    //        public void _onPostExecute(Boolean result) {
    //            if (result) {
    //                adapter = new CheckableFileListViewAdapter(context);
    //                setAdapter(adapter);
    //            }
    //            if (pdialog != null) {
    //                pdialog.dismiss();
    //                pdialog = null;
    //            }
    //        }
    //    }

    private void initMusicTypesLis() {
        for (String o : musicTypes) {
            musicTypesList.add(o);
        }
    }

    private void initVideoTypesList() {
        for (String o : videoTypes) {
            videoTypesList.add(o);
        }
    }

    private void initDocTypesList() {
        for (String o : docTypes) {
            docTypesList.add(o);
        }
    }
    /**
     * When item was clicked, this would be recall.
     */
    //    protected void OnItemClickProcess(AdapterView<?> listView, View item, int position, long time) {
    //        // Click a folder to get it open or get it close.
    //        FileInfo fileInfo = mFileInfos.get(position);
    //
    //        // Open folder opened, list all files under it.
    //        if (!fileInfo.isFile) {
    //            // Calculate exactly path.
    //            if (fileInfo.isParent) {
    //                pathname = pathname.equals("/") ? "/" : pathname.substring(0, pathname.lastIndexOf('/'));
    //            } else {
    //                pathname = fileInfo.filepath;
    //
    //            }
    //
    //            mFileInfos = listFileInfos(pathname);
    //
    //			/*
    //             * List all files in ListView , when current path name is not root ,
    //			 * set an item named ".." to go back its parent is at first.
    //			 */
    //            if (!root.equals(pathname)) {
    //                mFileInfos.add(0, new FileInfo("返回上一级", 0, false, true, "", null));
    //            }
    //
    //            // Display its sub files.
    //            setAdapter(new CheckableFileListViewAdapter(getContext()));
    //        }
    //    }

    /**
     * List all files info below this explored directory.
     *
     * @param pathname Explored directory name.
     * @return All files info below this explored directory.
     */
    protected List<FileInfo> listFileInfos(String pathname) {
        File parent = new File(pathname);
        List<FileInfo> fileInfos = new ArrayList<>();
        if (mCheckedFileInfos != null) {
            mCheckedFileInfos.clear();
        }
        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
            for (File file : parent.listFiles()) {
                if (file != null) {
                    fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
                }
            }
            Collections.sort(fileInfos);
        }

        return fileInfos;
    }

    public List<FileInfo> listFileInfos(String pathname, int type) {
        List<FileInfo> fileInfos = new ArrayList<>();
        //		List<FileInfo> fileInfos = Collections.synchronizedList(new ArrayList<FileInfo>());
        if (mCheckedFileInfos != null) {
            mCheckedFileInfos.clear();
        }
        listFileInfos(fileInfos, pathname, type);
        return fileInfos;
    }

    class SearchFileRunnable implements Runnable {
        private String path = null;
        private boolean isCancel = false;
        private List<FileInfo> fileInfos;
        private int type = 0;

        public SearchFileRunnable(List<FileInfo> fileInfos, String path, int type) {
            this.fileInfos = fileInfos;
            this.path = path;
            this.type = type;
        }

        @Override
        public void run() {
            while (!isCancel) {
                listFileInfos(fileInfos, path, type);
            }
        }
    }

    protected List<FileInfo> listFileInfos(List<FileInfo> fileInfos, String pathname, int type) {
        switch (type) {
            case AppConstants.UPLOAD_DOCS:
                List<String> allDocUrl = UploadTypeUtil.getSpecificTypeFiles(getContext(), docTypes);
                for (String docUrl : allDocUrl) {
                    File file1 = new File(docUrl);
                    fileInfos.add(new FileInfo(file1.getName(), file1.length(), true, false, file1.getPath(), new Timestamp(file1.lastModified())));
                }
                break;
            case AppConstants.UPLOAD_MUSICS:
                List<String> musicUrls = UploadTypeUtil.getAllMusicUrl(getContext());
                for (String musicUrl : musicUrls) {
                    File file1 = new File(musicUrl);
                    fileInfos.add(new FileInfo(file1.getName(), file1.length(), true, false, file1.getPath(), new Timestamp(file1.lastModified())));
                }
                break;

            case AppConstants.UPLOAD_VIDEOS:
                List<String> allVideoUrl = UploadTypeUtil.getAllVideoUrl(getContext());
                for (String videoUrl : allVideoUrl) {
                    File file1 = new File(videoUrl);
                    fileInfos.add(new FileInfo(file1.getName(), file1.length(), true, false, file1.getPath(), new Timestamp(file1.lastModified())));

                }
                break;
            case AppConstants.UPLOAD_ALL:
                File parent = new File(pathname);
                if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
                    for (File file : parent.listFiles()) {
                        if (file != null) {
                            fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
                        }
                    }
                }
                break;
        }
        Collections.sort(fileInfos);
        return fileInfos;
    }
    //    protected List<FileInfo> listFileInfos(List<FileInfo> fileInfos, String pathname, int type) {
    //        File parent = new File(pathname);
    //        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
    //            for (File file : parent.listFiles()) {
    //                if (file != null) {
    //                    switch (type) {
    //                        case IConstants.DOCS:
    ////                            if (file.isFile()) {
    ////                                String path = file.getPath();
    ////                                String postfix = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
    ////                                if (docTypesList.contains(postfix)) {
    ////                                    fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
    ////                                }
    ////                            } else {
    ////                                listFileInfos(fileInfos, file.getPath(), type);
    ////                            }
    //                            ArrayList<String> allDocUrl = UploadTypeUtil.getAllDocUrl(getContext());
    //                            for (String docUrl : allDocUrl) {
    //                                File file1 = new File(docUrl);
    //                                fileInfos.add(new FileInfo(file1.getName(), file1.length(), file1.isFile(), false, file1.getPath(), new Timestamp(file1.lastModified())));
    //                            }
    //                            break;
    //                        case IConstants.MUSICS:
    //                            //                            if (file.isFile()) {
    //                            //                                String path = file.getPath();
    //                            //                                String postfix = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
    //                            //                                if (musicTypesList.contains(postfix)) {
    //                            //                                    fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
    //                            //                                }
    //                            //                            } else {
    //                            //                                listFileInfos(fileInfos, file.getPath(), type);
    //                            //                            }
    //                            ArrayList<String> musicUrls = UploadTypeUtil.getAllMusicUrl(getContext());
    //                            for (String musicUrl : musicUrls) {
    //                                File file1 = new File(musicUrl);
    //                                fileInfos.add(new FileInfo(file1.getName(), file1.length(), file1.isFile(), false, file1.getPath(), new Timestamp(file1.lastModified())));
    //                            }
    //                            break;
    //                        case IConstants.VIDEOS:
    ////                            if (file.isFile()) {
    ////                                String path = file.getPath();
    ////                                String postfix = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
    ////                                if (videoTypesList.contains(postfix)) {
    ////                                    fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
    ////                                }
    ////                            } else {
    ////                                listFileInfos(fileInfos, file.getPath(), type);
    ////                            }
    //                            ArrayList<String> allVideoUrl = UploadTypeUtil.getAllVideoUrl(getContext());
    //                            for (String videoUrl : allVideoUrl) {
    //                                File file1 = new File(videoUrl);
    //                                fileInfos.add(new FileInfo(file1.getName(), file1.length(), file1.isFile(), false, file1.getPath(), new Timestamp(file1.lastModified())));
    //
    //                            }
    //                            break;
    //                        case IConstants.ALL:
    //                            fileInfos.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
    //                            break;
    //                    }
    //                }
    //            }
    //            Collections.sort(fileInfos);
    //        }
    //        return fileInfos;
    //    }

    /**
     * Refresh ListView items.
     */
    public void refresh() {
        mFileInfos = listFileInfos(currentLocalPath);

        // Display its sub files.
        ((BaseAdapter) getAdapter()).notifyDataSetChanged();
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public boolean isRoot() {
        return this.root.equalsIgnoreCase(currentLocalPath);
    }

    /**
     * 全选
     *
     * @return 选中的数量
     */
    public int setAllSelect() {
        for (FileInfo file : mFileInfos) {
            if (file.isFile) {
                mCheckedFileInfos.add(file);
            }
        }
        return mCheckedFileInfos.size();
    }

    /**
     * 全部选
     */
    public void setAllNotSelect() {
        mCheckedFileInfos.clear();
    }

    //
    //    private void onFileCheckedChange(boolean checked, FileInfo file) {
    //        if (checked) {
    //            mCheckedFileInfos.add(file);
    //        } else {
    //            mCheckedFileInfos.remove(file);
    //        }
    ////        if (callback != null) {
    ////            callback.execute(this);
    ////        }
    //    }

    public Set<FileInfo> getCheckedFileInfos() {
        return mCheckedFileInfos;
    }

    static class ViewHolder {
        ImageView icon;
        LinearLayout itemLayout;
        RelativeLayout fileLayout;
        TextView fnameText;
        TextView fsizeText;
        TextView ftimeText;
        CheckBox cbox;
        TextView floderText;
    }

    /**
     * Uses for setting data into file listView.
     */
    protected class CheckableFileListViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public CheckableFileListViewAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        /**
         * The number of items in the list is determined by the number of
         * speeches in our array.
         */
        public int getCount() {
            return mFileInfos.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            // Retrieve file information at assigned position.
            final FileInfo fileInfo = mFileInfos.get(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.checkable_file_list_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.file_explorer_file_icon);
                holder.fileLayout = (RelativeLayout) convertView.findViewById(R.id.file_explorer_file_layout);
                holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.checkable_file_item);
                holder.fnameText = (TextView) convertView.findViewById(R.id.file_explorer_filename_text);
                holder.fsizeText = (TextView) convertView.findViewById(R.id.file_explorer_filesize_text);
                holder.ftimeText = (TextView) convertView.findViewById(R.id.file_explorer_filedate_text);
                holder.floderText = (TextView) convertView.findViewById(R.id.file_explorer_flodername_text);
                holder.cbox = (CheckBox) convertView.findViewById(R.id.file_explorer_file_cbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.itemLayout.setBackgroundColor(Color.WHITE);
            // Display them.
            if (fileInfo.isFile) {
                holder.floderText.setVisibility(GONE);
                holder.icon.setImageResource(FileUtil.getFileBitmapResource(fileInfo.filename));
                holder.fileLayout.setVisibility(VISIBLE);
                holder.fnameText.setText(fileInfo.filename);
                holder.fsizeText.setText(StringUtil.getFileSize(fileInfo.size));
                holder.ftimeText.setText(DateUtil.timestamp2String(fileInfo.lastModifiedTime, DateUtil.DATE_YYYY_MM_DD_HH_MM_SS));
                holder.cbox.setVisibility(VISIBLE);
                //                holder.cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                //                    @Override
                //                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //                        //  CheckableFileListView.this.onFileCheckedChange(isChecked, fileInfo);
                //                        if (isChecked) {
                //                            mCheckedFileInfos.add(fileInfo);
                //                        } else {
                //                            mCheckedFileInfos.remove(fileInfo);
                //                        }
                //                    }
                //                });
                holder.cbox.setChecked(mCheckedFileInfos.contains(fileInfo));
            } else {
                holder.icon.setImageResource(fileInfo.isParent ? R.mipmap.uspace_back_folder : R.mipmap.uspace_default_folder);
                holder.floderText.setVisibility(VISIBLE);
                holder.floderText.setText(fileInfo.filename);
                holder.fileLayout.setVisibility(GONE);
                holder.cbox.setVisibility(GONE);
            }
            return convertView;
        }
    }

}
