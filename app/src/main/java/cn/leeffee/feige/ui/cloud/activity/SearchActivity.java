package cn.leeffee.feige.ui.cloud.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.adapter.FileListAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.ActSearchContract;
import cn.leeffee.feige.ui.cloud.contract.FragMyFileContract;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.model.ActSearchModelImpl;
import cn.leeffee.feige.ui.cloud.model.FragMyFileModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActSearchPresenterImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragMyFilePresenterImpl;
import cn.leeffee.feige.ui.cloud.service.TransTool;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.FileViewer;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.utils.ValidationUtil;
import cn.leeffee.feige.widget.RenameMkDirAlertDialog;
import cn.leeffee.feige.widget.XListView;

import static cn.leeffee.feige.ui.cloud.activity.UploadFileExploreActivity.REQUEST_CODE_LISTDIRONLYDIR;
import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_CANCEL_SHARED;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_CREATE_PUBLISH_LINK;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_DELETE;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_LISTDIR_INIT;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_LISTDIR_REFREASH;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_MOVE;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.REQUEST_CODE_RENAME;
import static cn.leeffee.feige.ui.cloud.fragment.MyFileFragment.mIsDownloadBind;
import static cn.leeffee.feige.utils.FileUtil.getPrePath;


public class SearchActivity extends BaseActivity<ActSearchPresenterImpl, ActSearchModelImpl> implements ActSearchContract.View, AdapterView.OnItemClickListener, FragMyFileContract.View {

    @BindView(R.id.search_back)
    ImageView mBack;
    @BindView(R.id.search_keyword)
    EditText mKeyword;
    @BindView(R.id.search_list_lv)
    XListView mListView;
    @BindView(R.id.search_submit)
    TextView mSubmit;
    @BindView(R.id.search_current_dir_tv)
    TextView mCurrentDir;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.search_empty_tv)
    TextView tvEmptyFiles;
    private static final String REQUEST_CODE_SEARCH_FILE_INIT = "listSearchDir_init";//搜索文件 初始化
    private static final String REQUEST_CODE_SEARCH_FILE_REFRESH = "listSearchDir_refresh";//搜索文件 刷新
    /**
     * 重新命名
     */
    RenameMkDirAlertDialog dialogRename;
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

    FragMyFilePresenterImpl mFilePresenter;
    TransTool mTransTool;

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        FragMyFileModelImpl model = new FragMyFileModelImpl();
        mFilePresenter = new FragMyFilePresenterImpl();
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        USpaceFile file = (USpaceFile) mListView.getAdapter().getItem(position);
        switch (item.getItemId()) {
            case R.id.menu_rename_file:
            case R.id.menu_rename_folder:
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
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoadingLayout.setVisibility(View.VISIBLE);
                break;
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
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDirListAdapter.setData(fileList);
                mDirListAdapter.notifyDataSetChanged();
                mDialogLoadingLayout.setVisibility(View.GONE);
                break;
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
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
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

    @OnClick(R.id.search_submit)
    public void searchSubmit() {
        keyword = mKeyword.getText().toString().trim();
        if (keyword.length() != 0) {
            searchLevel = 0;
            isSearchPage = true;
            mRxManager.add(mPresenter.listSearchFiles(keyword, REQUEST_CODE_SEARCH_FILE_INIT));
        } else {
            ToastUtil.showShort("请输入搜索文件名");
        }
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
        if (dialogRename == null) {
            dialogRename = new RenameMkDirAlertDialog(this, "重命名");
        }
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
            if (oldName.indexOf(".") != -1) {
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
     * @param srcFile
     */
    private void moveTo(USpaceFile srcFile) {
        if (mTransTool.isFileDownloading(srcFile)) {
            ToastUtil.showShort("该文件正在下载中，不可以移动！");
            return;
        }
        choiceDir(srcFile);
    }

    /**
     * 弹出要移动到哪儿的目录对话框
     */
    AlertDialog mDialog;
    /**
     * 更改要上传到服务器的哪个目录
     */
    private String moveToRemotePath;
    /**
     * 对话框中 要移动到的那个目录
     */
    TextView mRemoteMoveToDir;
    /**
     * 对话框中的加载布局
     */
    LinearLayout mDialogLoadingLayout;
    /**
     * 目录列表适配器
     */
    DirListAdapter mDirListAdapter;
    /**
     * 对话框中的确认按钮
     */
    Button mBtnOK;

    public void choiceDir(final USpaceFile srcFile) {
        moveToRemotePath = "/";
        if (mDialog == null) {
            LayoutInflater Inflater = (LayoutInflater) App.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = Inflater.inflate(R.layout.dialog_change_dir, null);
            mDialogLoadingLayout = (LinearLayout) v.findViewById(R.id.loading_layout);
            mRemoteMoveToDir = (TextView) v.findViewById(R.id.dialog_title_dir_tv);
            // 初始化按钮
            mBtnOK = (Button) v.findViewById(R.id.dialog_ok_btn);
            v.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });

            // 初始化Listview，加载数据。
            ListView dirListView = (ListView) v.findViewById(R.id.dialog_dir_list_lv);
            mDirListAdapter = new DirListAdapter(App.getAppContext());
            dirListView.setAdapter(mDirListAdapter);
            dirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    USpaceFile fileInfo = (USpaceFile) mDirListAdapter.getItem(position);
                    String path = fileInfo.getDiskPath();
                    String dir = "/我的云盘";
                    if (fileInfo.isParent()) {
                        path = path.substring(0, path.lastIndexOf('/'));
                        if ("".equals(path)) {
                            path = "/";
                        }
                    }
                    mDirListAdapter.getData().clear();
                    if ("/".equals(path)) {
                        mDirListAdapter.getData().add(0, new USpaceFile(getText(R.string.strBackToParent).toString(), 0, true, true, path, false));
                    }
                    moveToRemotePath = path;
                    dir += path;
                    mRemoteMoveToDir.setText(dir);
                    mDirListAdapter.notifyDataSetChanged();
                    mRxManager.add(mFilePresenter.listDirOnlyDir(moveToRemotePath, REQUEST_CODE_LISTDIRONLYDIR));
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(v);
            builder.setCancelable(true);
            mDialog = builder.create();
            mDialog.setCanceledOnTouchOutside(true);

            Window win = mDialog.getWindow();
            WindowManager.LayoutParams params = win.getAttributes(); // 获取对话框当前的参数值
            params.gravity = Gravity.CENTER;
            win.setAttributes(params);
        }
        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTransTool.validateMove(srcFile, moveToRemotePath)) {
                    mRxManager.add(mFilePresenter.move(srcFile.getDiskPath(), moveToRemotePath, REQUEST_CODE_MOVE));
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
        mRemoteMoveToDir.setText("/我的云盘");
        mRxManager.add(mFilePresenter.listDirOnlyDir("/", REQUEST_CODE_LISTDIRONLYDIR));
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
