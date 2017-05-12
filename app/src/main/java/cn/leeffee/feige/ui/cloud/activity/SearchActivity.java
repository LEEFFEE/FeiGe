package cn.leeffee.feige.ui.cloud.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.adapter.FileListAdapter;
import cn.leeffee.feige.ui.cloud.api.ApiPath;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.ActSearchContract;
import cn.leeffee.feige.ui.cloud.contract.FragCloudContract;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.model.ActSearchModelImpl;
import cn.leeffee.feige.ui.cloud.model.FragCloudModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActSearchPresenterImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragCloudPresenterImpl;
import cn.leeffee.feige.ui.cloud.service.TransTool;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.FileViewer;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.utils.ValidationUtil;
import cn.leeffee.feige.widget.ChoiceRemoteDictionaryAlertDialog;
import cn.leeffee.feige.widget.RenameMkDirAlertDialog;
import cn.leeffee.feige.widget.XListView;

import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_CANCEL_SHARED;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_CREATE_PUBLISH_LINK;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_DELETE;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_LISTDIR_INIT;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_LISTDIR_REFREASH;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_MOVE;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.REQUEST_CODE_RENAME;
import static cn.leeffee.feige.ui.cloud.fragment.CloudFragment.mIsDownloadBind;
import static cn.leeffee.feige.utils.FileUtil.getPrePath;

public class SearchActivity extends BaseActivity<ActSearchPresenterImpl, ActSearchModelImpl> implements ActSearchContract.View, AdapterView.OnItemClickListener, FragCloudContract.View {

    @BindView(R.id.search_back)
    ImageView mBack;
    @BindView(R.id.search_keyword)
    EditText mKeyword;
    @BindView(R.id.search_list_lv)
    XListView mListView;
    //    @BindView(R.id.search_cancel)
    //    TextView mCancel;
    @BindView(R.id.search_current_dir_tv)
    TextView mCurrentDir;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.search_empty_tv)
    TextView tvEmptyFiles;
    private static final String REQUEST_CODE_SEARCH_FILE_INIT = "listSearchDir_init";//搜索文件 初始化
    private static final String REQUEST_CODE_SEARCH_FILE_REFRESH = "listSearchDir_refresh";//搜索文件 刷新
    FileListAdapter mAdapter;
    private MenuInflater mMenuInflater;
    /**
     * 网盘上的当前路径
     */
    private String currentRemotePath = ROOT_PATH;

    @Override
    public int getLayoutId() {
        return R.layout.act_search;
    }

    FragCloudPresenterImpl mFilePresenter;
    TransTool mTransTool;

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        FragCloudModelImpl model = new FragCloudModelImpl();
        mFilePresenter = new FragCloudPresenterImpl();
        mFilePresenter.setVM(this, model);
        mTransTool = new TransTool();
    }

    @Override
    public void initView() {
        mCurrentDir.setVisibility(View.GONE);
        mMenuInflater = new MenuInflater(App.getAppContext());
        mAdapter = new FileListAdapter(App.getAppContext());
        mListView.setAdapter(mAdapter);
        mListView.setPullLoadEnable(false);//设置加载更多是否可用
        mListView.setXListViewListener(new MyIXListViewListener());
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                USpaceFile file = (USpaceFile) mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position - 1);//减去头布局
                if (!file.isParent() && !file.isFolder()) {
                    menu.setHeaderTitle(R.string.menu_title_file_opt);
                    mMenuInflater.inflate(R.menu.uspace_item_file_menu, menu);
                    MenuItem sharedItem = menu.findItem(R.id.uspace_menu_publish_file);
                    MenuItem unsharedItem = menu.findItem(R.id.uspace_menu_unpublish_file);
                    sharedItem.setVisible(!file.isShared());
                    unsharedItem.setVisible(file.isShared());
                    if (!FileViewer.canOpen(file.getName())) {
                        menu.findItem(R.id.uspace_menu_open_file).setVisible(false);
                    }
                } else if (!file.isParent() && file.isFolder()) {
                    menu.setHeaderTitle(R.string.menu_title_folder_opt);
                    mMenuInflater.inflate(R.menu.uspace_item_folder_menu, menu);
                    MenuItem sharedItem = menu.findItem(R.id.uspace_menu_publish_folder);
                    MenuItem unsharedItem = menu.findItem(R.id.uspace_menu_unpublish_folder);
                    sharedItem.setVisible(!file.isShared());
                    unsharedItem.setVisible(file.isShared());
                }
            }
        });
        mKeyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchSubmit();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 提交搜索
     */
    private void searchSubmit() {
        keyword = mKeyword.getText().toString().trim();
        if (keyword.length() != 0) {
            searchLevel = 0;
            isSearchPage = true;
            mRxManager.add(mPresenter.listSearchFiles(keyword, REQUEST_CODE_SEARCH_FILE_INIT));
        } else {
            ToastUtil.showShort("请输入搜索文件名");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        USpaceFile file = (USpaceFile) mListView.getAdapter().getItem(position);
        switch (item.getItemId()) {
            case R.id.uspace_menu_rename_file:
            case R.id.uspace_menu_rename_folder:
                rename(file);
                break;
            case R.id.uspace_menu_delete_file:
            case R.id.uspace_menu_delete_folder:
                removeFile(file);
                break;
            case R.id.uspace_menu_move_file:
            case R.id.uspace_menu_move_folder:
                moveTo(file);
                break;
            case R.id.uspace_menu_download_file:
                downloadFile(file);
                break;
            case R.id.uspace_menu_open_file:
                openFile(file);
                break;
            case R.id.uspace_menu_publish_file:
            case R.id.uspace_menu_publish_folder:
                shareFile(file);
                break;
            case R.id.uspace_menu_unpublish_file:
            case R.id.uspace_menu_unpublish_folder:
                cancelShareFile(file);
                break;
        }
        return true;
    }

    @Override
    public void loadBefore(String requestCode) {
        /**
         * 加载前隐藏数据为空的提示
         */
        tvEmptyFiles.setVisibility(View.GONE);
        switch (requestCode) {
            case REQUEST_CODE_LISTDIR_INIT:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_SEARCH_FILE_INIT:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_SEARCH_FILE_REFRESH:
                break;
            case REQUEST_CODE_RENAME:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_DELETE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            //            case REQUEST_CODE_LISTDIRONLYDIR:
            //                mDialogLoadingLayout.setVisibility(View.VISIBLE);
            //                break;
            case REQUEST_CODE_MOVE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
        }
        //根据条件判断是否隐藏对应的操作
        if (currentRemotePath.equalsIgnoreCase(ROOT_PATH)) {
            mCurrentDir.setVisibility(View.GONE);
        } else {
            mCurrentDir.setVisibility(View.VISIBLE);
            mCurrentDir.setText(currentRemotePath);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        List<USpaceFile> fileList = new ArrayList<>();
        if (result != null && result instanceof List) {
            fileList = (List<USpaceFile>) result;
            if (fileList.size() == 0) {
                tvEmptyFiles.setVisibility(View.VISIBLE);
                tvEmptyFiles.setText(R.string.folder_empty_tips);
            }
        }
        switch (requestCode) {
            case REQUEST_CODE_LISTDIR_INIT:
                Collections.sort(fileList);
                mAdapter.setData(fileList);
                mListView.setAdapter(mAdapter);
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_LISTDIR_REFREASH:
                mAdapter.setData(fileList);
                mAdapter.notifyDataSetChanged();
                mListView.stopRefresh();
                mListView.setRefreshTime("更新时间:" + CommonUtil.getSystemTime());
                break;
            case REQUEST_CODE_SEARCH_FILE_INIT:
                mListView.setVisibility(View.VISIBLE);
                mAdapter.setData(fileList);
                if (fileList.size() == 0) {
                    tvEmptyFiles.setText("没有找到你要搜索的内容~");
                }
                mAdapter.notifyDataSetChanged();
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_SEARCH_FILE_REFRESH:
                mAdapter.setData(fileList);
                mAdapter.notifyDataSetChanged();
                mListView.stopRefresh();
                mListView.setRefreshTime("更新时间:" + CommonUtil.getSystemTime());
                break;
            case REQUEST_CODE_RENAME:
                ToastUtil.showShort("重命名成功");
                reLoad();
                break;
            case REQUEST_CODE_DELETE:
                ToastUtil.showShort("删除成功");
                reLoad();
                break;
            //            case REQUEST_CODE_LISTDIRONLYDIR:
            //                mDirListAdapter.setData(fileList);
            //                mDirListAdapter.notifyDataSetChanged();
            //                mDialogLoadingLayout.setVisibility(View.GONE);
            //                break;
            case REQUEST_CODE_MOVE:
                ToastUtil.showShort("移动成功");
                reLoad();
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                ToastUtil.showShort("共享成功");
                mAdapter.notifyDataSetChanged();
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                ToastUtil.showShort("取消共享成功");
                mAdapter.notifyDataSetChanged();
                mLoadingLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
            case REQUEST_CODE_LISTDIR_INIT:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_LISTDIR_REFREASH:
                ToastUtil.showShort(msg);
                mListView.stopRefresh();
                break;
            case REQUEST_CODE_SEARCH_FILE_INIT:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_SEARCH_FILE_REFRESH:
                ToastUtil.showShort(msg);
                mListView.stopRefresh();
                break;
            case REQUEST_CODE_RENAME:
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_DELETE:
                ToastUtil.showShort(msg);
                break;
            //            case REQUEST_CODE_LISTDIRONLYDIR:
            //                mDialogLoadingLayout.setVisibility(View.GONE);
            //                ToastUtil.showShort(msg);
            //                break;
            case REQUEST_CODE_MOVE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
        }
    }

    /**
     * 搜索之后假设的当前路径
     */
    String searchCurrentPath = ROOT_PATH;
    /**
     * 搜索层级目录
     */
    int searchLevel = 0;
    String keyword;
    /**
     * 是否搜索界面
     */
    boolean isSearchPage = false;

    @OnClick(R.id.search_cancel)
    public void searchCancel() {
        AppManager.getAppManager().finishActivity();
    }

    @OnClick(R.id.search_back)
    public void searchBack() {
        if (currentRemotePath.equals(ROOT_PATH)) {
            isSearchPage = false;
            AppManager.getAppManager().finishActivity();
        } else {
            if (searchCurrentPath.equals(currentRemotePath)) {
                searchLevel = 0;
                currentRemotePath = ROOT_PATH;
                isSearchPage = true;
                mRxManager.add(mPresenter.listSearchFiles(keyword, REQUEST_CODE_SEARCH_FILE_INIT));
            } else {
                backToParent();
            }
        }

    }

    /**
     * 重新加载
     */
    private void reLoad() {
        if (isSearchPage) {
            mRxManager.add(mPresenter.listSearchFiles(keyword, REQUEST_CODE_SEARCH_FILE_INIT));
        } else {
            mRxManager.add(mFilePresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        USpaceFile uf = (USpaceFile) mAdapter.getItem(position - 1);//去掉头布局
        if (uf.isFolder()) {
            currentRemotePath = uf.getDiskPath();
            if (isSearchPage && searchLevel == 0) {
                searchCurrentPath = currentRemotePath;
                isSearchPage = false;
            }
            searchLevel++;
            emptyListView();
            mRxManager.add(mFilePresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
        } else {
            if (FileUtil.isFileExist(uf)) {
                openFile(uf);
            } else {
                downloadFile(uf);
            }
        }
    }

    /**
     * 重命名文件  或者文件夹
     *
     * @param file
     */
    private void rename(USpaceFile file) {
        final RenameMkDirAlertDialog dialogRename = new RenameMkDirAlertDialog(this, "重命名");
        final String oldName = file.getName();
        dialogRename.setFileName(oldName);
        final String renameDir;
        if (isSearchPage) {
            renameDir = getPrePath(file.getDiskPath());
        } else {
            renameDir = currentRemotePath;
        }
        if (file.isFolder()) {
            dialogRename.setSelection(0, oldName.length());
        } else {
            if (oldName.contains(".")) {
                dialogRename.setSelection(0, oldName.lastIndexOf("."));
            } else {
                dialogRename.setSelection(0, oldName.length());
            }
        }
        dialogRename.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = dialogRename.getFileName();
                if (newName.trim().length() == 0) {
                    ToastUtil.showShort(R.string.error_folder_name_empty);
                    return;
                } else if (!ValidationUtil.isFileNameLegal(newName)) {
                    ToastUtil.showShort("文件名不能含有 \\ : / * < > ? | \"");
                    return;
                }
                if (oldName.equals(newName.trim())) {
                    dialogRename.dismiss();
                } else if (newName.trim().length() > 50) {
                    ToastUtil.showShort("文件夹名字太长");
                } else {
                    mRxManager.add(mFilePresenter.rename(renameDir, oldName, newName, REQUEST_CODE_RENAME));
                    dialogRename.dismiss();
                }
            }
        });
        dialogRename.show();
    }

    /**
     * 移动文件
     *
     * @param file
     */
    private void moveTo(final USpaceFile file) {
        if (mTransTool.isFileDownloading(file)) {
            ToastUtil.showShort("该文件正在下载中，不可以移动！");
            return;
        }
        final ChoiceRemoteDictionaryAlertDialog dialog = new ChoiceRemoteDictionaryAlertDialog(this);
        //  dialog.setPrefix("/");
        final List<ApiPath> list = new ArrayList<>();
        list.add(new ApiPath(file.getDiskPath(), file.getVersion() == null ? 0 : file.getVersion()));
        dialog.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTransTool.validateMove(file, dialog.getDestPath())) {
                    mRxManager.add(mFilePresenter.moveTo(list, dialog.getDestPath(), REQUEST_CODE_MOVE));
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file
     */
    private void removeFile(final USpaceFile file) {
        new android.app.AlertDialog.Builder(this).setTitle(file.getName())
                .setMessage(R.string.msg_uspace_delete_confirm)
                .setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRxManager.add(mFilePresenter.remove(file, REQUEST_CODE_DELETE));
                    }
                }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 预下载处理
     *
     * @param file
     */
    private void downloadFile(final USpaceFile file) {
        if (!NetWorkUtil.isNetConnected(App.getAppContext())) {
            ToastUtil.showShort(R.string.network_not_available);
            return;
        }
        if (mPresenter.mFileUtil == null) {
            ToastUtil.showShort(App.getAppResources().getString(R.string.sdcard_not_available));
            return;
        }
        String msg = getString(R.string.msg_uspace_download_file_confirm);
        boolean confirm = SPUtil.getBoolean(AppConfig.DOWNLOAD_CONFIRM);
        if (FileUtil.isFileExist(file)) {
            msg = MessageFormat.format(getText(R.string.msg_uspace_downfile_replace_confirm).toString(), StringUtil.getFileSize(file.getSize()));
        } else {
            if (file.getSize() >= PropertyUtil.getInstance().getDownloadFileThreshold()) {
                msg = MessageFormat.format(getText(R.string.msg_uspace_download_file_exceed_threshold_confirm).toString(), StringUtil.getFileSize(file.getSize()));
                confirm = true;
            }
        }
        if (confirm) {
            new android.app.AlertDialog.Builder(this).setTitle(file.getName()).setMessage(msg).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTransTool.download(file, mIsDownloadBind);
                }
            }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        } else {
            mTransTool.download(file, mIsDownloadBind);
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    private void openFile(final USpaceFile file) {
        if (FileUtil.isFileExist(file)) {
            if (!FileViewer.canOpen(file.getName())) {
                return;
            }
            String filePath = FileUtil.getUserRoot() + file.getDiskPath();
            Intent intent = FileViewer.getOpenFileIntent(filePath);
            if (intent != null) {
                startActivity(intent);
            } else {
                String msg = MessageFormat.format(getString(R.string.msg_uspace_cannot_open_file), file.getName());
                ToastUtil.showShort(msg);
            }
        } else {
            new android.app.AlertDialog.Builder(this).setTitle(file.getName()).setMessage(R.string.msg_uspace_download_file_before_open).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTransTool.download(file, mIsDownloadBind);
                }
            }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    /**
     * 分享文件/共享文件
     *
     * @param file
     */
    private void shareFile(USpaceFile file) {
        mRxManager.add(mFilePresenter.createPublishLink(file, REQUEST_CODE_CREATE_PUBLISH_LINK));
    }

    /**
     * 取消分享
     *
     * @param file
     */
    private void cancelShareFile(USpaceFile file) {
        mRxManager.add(mFilePresenter.cancelSharedFiles(file, REQUEST_CODE_CANCEL_SHARED));
    }

    /**
     * 跳转到上一级
     */
    private void backToParent() {
        currentRemotePath = getPrePath(currentRemotePath);
        emptyListView();
        mRxManager.add(mFilePresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
    }

    /**
     * 清空ListView数据
     */
    private void emptyListView() {
        mAdapter.getData().clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                searchBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * XListView下拉刷新，加载更多监听
     */
    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            if (isSearchPage) {
                mRxManager.add(mPresenter.listSearchFiles(keyword, REQUEST_CODE_SEARCH_FILE_REFRESH));
            } else {
                mRxManager.add(mFilePresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_REFREASH));
            }
        }

        @Override
        public void onLoadMore() {
            // getMoreDataFromNet();
        }
    }
}
