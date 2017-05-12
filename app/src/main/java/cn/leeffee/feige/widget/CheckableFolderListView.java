package cn.leeffee.feige.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.FileUtil;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CheckableFolderListView extends ListView {

    //    protected Context context;
    private String currentLocalPath = "";
    private String rootPath = "";
    private List<USpaceFile> mFolderInfos = null;
    private Set<USpaceFile> mCheckedFolderInfos = null;

    private FoldersListAdapter adapter;
    Disposable disposable;
    protected onSelectedListener callback;

    public CheckableFolderListView(Activity act, String pathname, onSelectedListener callback) {
        super(act);
        this.rootPath = this.currentLocalPath = pathname;
        mCheckedFolderInfos = new HashSet<>();
        this.callback = callback;
        //        setOnItemClickListener(new OnItemClickListener() {
        //            @Override
        //            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //                OnItemClickProcess(parent, view, position, id);
        //            }
        //        });
        //弹出对话框
        String title = act.getString(R.string.strPostDialogTitle);
        String content = "正在搜索您手机上的目录...";
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
        disposable = Flowable.just(this.currentLocalPath).map(new Function<String, List<USpaceFile>>() {
            @Override
            public List<USpaceFile> apply(@NonNull String pathName) throws Exception {
                if (mCheckedFolderInfos != null) {
                    mCheckedFolderInfos.clear();
                }
                return FileUtil.listFolderInfos(pathName);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<USpaceFile>>() {

            @Override
            public void accept(@NonNull List<USpaceFile> fileInfos) throws Exception {
                mFolderInfos = fileInfos;
                adapter = new FoldersListAdapter(getContext());
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
        // new ListLocalFoldersTask(act, context, pathname).execute();
    }

    public void displayFolder(String curPath) {
        this.currentLocalPath = curPath;
        if (mCheckedFolderInfos != null) {
            mCheckedFolderInfos.clear();
        }
        mFolderInfos = FileUtil.listFolderInfos(currentLocalPath);
        if (!rootPath.equals(currentLocalPath)) {
            mFolderInfos.add(0, new USpaceFile("返回上一级", 0, false, true, "", false));
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 文件夹所选择后（checkbox选中状态改变）触发
     */
    public interface onSelectedListener {
        void execute(int selectedCount);
    }

    //    class ListLocalFoldersTask extends GenericTask<Object, String, Boolean> {
    //        private Context context;
    //        private String pathname;
    //        private ProgressDialog pDialog;
    //        private Activity act;
    //
    //        public ListLocalFoldersTask(Activity act, Context context, String pathname) {
    //            super(act);
    //            this.act = act;
    //            this.context = context;
    //            this.pathname = pathname;
    //        }
    //
    //        @Override
    //        protected void onPreExecute() {
    //            final String strTitle = act.getString(R.string.strPostDialogTitle);
    //            final String strBody = "正在搜索您手机上的文件...";
    //            pDialog = ProgressDialog.show(act, strTitle, strBody, true, true);
    //        }
    //
    //        @Override
    //        protected void onProgressUpdate(String... values) {
    //            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
    //        }
    //
    //        @Override
    //        public Boolean _doInBackground(Object... params) throws Exception {
    //            mFolderInfos = listFolderInfos(pathname);
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
    //                adapter = new FoldersListAdapter(context);
    //                setAdapter(adapter);
    //            }
    //            pDialog.dismiss();
    //        }
    //    }

    //    /**
    //     * When item was clicked, this would be recall.
    //     */
    //    protected void OnItemClickProcess(AdapterView<?> listView, View item, int position, long id) {
    //        // Click a folder to get it open or get it close.
    //        USpaceFile fileInfo = mFolderInfos.get(position);
    //
    //        // Open folder opened, list all files under it.
    //        if (!fileInfo.isFile) {
    //            // Calculate exactly path.
    //            if (fileInfo.isParent()) {
    //                currentLocalPath = currentLocalPath.equals("/") ? "/" : currentLocalPath.substring(0, currentLocalPath.lastIndexOf('/'));
    //            } else {
    //                currentLocalPath = fileInfo.filepath;
    //
    //            }
    //
    //            mFolderInfos = listFolderInfos(currentLocalPath);
    //
    //			/*
    //             * List all files in ListView , when current path name is not root ,
    //			 * set an item named ".." to go back its parent is at first.
    //			 */
    //            if (!rootPath.equals(currentLocalPath)) {
    //                mFolderInfos.add(0, new FileInfo("返回上一级", 0, false, true, "", null));
    //            }
    //
    //            // Display its sub files.
    //            setAdapter(new FoldersListAdapter(getContext()));
    //        }
    //    }

    public String getCurrentLocalPath() {
        return currentLocalPath;
    }

    //    public List<USpaceFile> listFolderInfos(String pathname) {
    //        File parent = new File(pathname);
    //        List<USpaceFile> uSpaceFiles = new ArrayList<>();
    //        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
    //            USpaceFile uf;
    //            for (File file : parent.listFiles()) {
    //                if (file != null) {
    //                    //只列出文件夹
    //                    if (file.isDirectory()) {
    //                        uf = new USpaceFile(file.getName(), file.length(), file.isDirectory(), false, file.getPath(), false);
    //                        uf.setModifyTime(new Timestamp(file.lastModified()));
    //                        uSpaceFiles.add(uf);
    //                        // uSpaceFiles.add(new FileInfo(file.getName(), file.length(), file.isFile(), false, file.getPath(), new Timestamp(file.lastModified())));
    //                    }
    //                }
    //            }
    //            Collections.sort(uSpaceFiles);
    //        }
    //
    //        return uSpaceFiles;
    //    }

    private void onFolderCheckedChange(boolean checked, USpaceFile file) {
        if (checked) {
            mCheckedFolderInfos.add(file);
        } else {
            mCheckedFolderInfos.remove(file);
        }
        if (callback != null) {
            callback.execute(mCheckedFolderInfos.size());
        }
    }

    public Set<USpaceFile> getCheckedFolderInfos() {
        return mCheckedFolderInfos;
    }

    public List<USpaceFile> getFolderInfos() {
        return mFolderInfos;
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置全选
     *
     * @return 全选后的条目数量
     */
    public int setAllSelect() {
        for (USpaceFile file : mFolderInfos) {
            //不是文件和上一层目录
            if (file.isFolder() && !file.isParent()) {
                mCheckedFolderInfos.add(file);
            }
        }
        return mCheckedFolderInfos.size();
    }

    /**
     * 设置全不选
     *
     * @return
     */
    public void setAllNotSelect() {
        mCheckedFolderInfos.clear();
    }

    static class ViewHolder {
        // LinearLayout itemLayout;
        ImageView icon;
        TextView folderText;
        CheckBox cbox;
    }

    /**
     * Uses for setting data into file listView.
     */
    protected class FoldersListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public FoldersListAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        /**
         * The number of items in the list is determined by the number of
         * speeches in our array.
         */
        public int getCount() {
            return mFolderInfos.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         */
        public USpaceFile getItem(int position) {
            return mFolderInfos.get(position);
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
            final USpaceFile fileInfo = mFolderInfos.get(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.checkable_dir_list_item, null);
                holder = new ViewHolder();
                // holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.checkable_file_item);
                holder.icon = (ImageView) convertView.findViewById(R.id.folder_icon);
                holder.folderText = (TextView) convertView.findViewById(R.id.folder_name);
                holder.cbox = (CheckBox) convertView.findViewById(R.id.folder_exploer_file_cbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //  holder.itemLayout.setBackgroundColor(Color.WHITE);
            // Display them.
            if (!fileInfo.isFolder() && !fileInfo.isParent()) {
                holder.folderText.setText(fileInfo.getName());
                holder.icon.setImageResource(FileUtil.getFileBitmapResource(fileInfo.getName()));
                holder.cbox.setVisibility(GONE);
            } else {
                holder.icon.setImageResource(fileInfo.isParent() ? R.mipmap.uspace_back_folder : R.mipmap.uspace_default_folder);
                holder.folderText.setText(fileInfo.getName());
                if (fileInfo.isParent()) {
                    holder.cbox.setVisibility(GONE);
                } else {
                    holder.cbox.setVisibility(VISIBLE);
                    holder.cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            CheckableFolderListView.this.onFolderCheckedChange(isChecked, fileInfo);
                        }
                    });
                    holder.cbox.setChecked(mCheckedFolderInfos.contains(fileInfo));
                }
            }
            return convertView;
        }

    }
}
