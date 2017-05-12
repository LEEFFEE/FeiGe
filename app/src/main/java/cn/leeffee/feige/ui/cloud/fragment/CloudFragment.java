package cn.leeffee.feige.ui.cloud.fragment;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.cloud.activity.SearchActivity;
import cn.leeffee.feige.ui.cloud.activity.UploadFileExploreActivity;
import cn.leeffee.feige.ui.cloud.adapter.FileListAdapter;
import cn.leeffee.feige.ui.cloud.api.ApiConstants;
import cn.leeffee.feige.ui.cloud.api.ApiPath;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.FragCloudContract;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.model.FragCloudModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragCloudPresenterImpl;
import cn.leeffee.feige.ui.cloud.service.DownloadService;
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import cn.leeffee.feige.ui.cloud.service.IDownloadService;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.ui.cloud.service.IUploadService;
import cn.leeffee.feige.ui.cloud.service.TransTool;
import cn.leeffee.feige.ui.cloud.service.UploadService;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
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
import cn.leeffee.feige.widget.TopPopuWindow;
import cn.leeffee.feige.widget.USpaceToolBar;
import cn.leeffee.feige.widget.XListView;

import static android.app.Activity.RESULT_OK;
import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;
import static cn.leeffee.feige.utils.FileUtil.getUserRoot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudFragment#} factory method to
 * create an instance of this fragment.
 */
public class CloudFragment extends BaseFragment<FragCloudPresenterImpl, FragCloudModelImpl> implements FragCloudContract.View, AdapterView.OnItemClickListener {

    /**
     * 打开sd卡目录 请求码
     */
    private static final int REQUEST_SDCARD_LIBRARY = 100;
    /**
     * 打开相册 请求码
     */
    private static final int REQUEST_PHOTO_LIBRARY = 200;
    /**
     * 打开相机拍照 请求码
     */
    private static final int REQUEST_IMAGE_CAPTURE = 300;
    @BindView(R.id.my_files_listView)
    XListView mListView;
    @BindView(R.id.my_files_toolbar)
    USpaceToolBar mToolBar;
    @BindView(R.id.my_files_current_dir_tv)
    TextView tvCurrentRemoteDir;
    @BindView(R.id.my_files_empty_tv)
    TextView tvEmptyFiles;
    //正在加载中 弹出框
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;


    /**
     * 搜索标题布局
     */
    //    @BindView(R.id.my_files_search_header_ll)
    //    LinearLayout mSearchHeaderLayout;
    //    @BindView(R.id.my_files_search_keyword)
    //    USpaceEditText mSearchKeyword;
    //    @BindView(R.id.my_files_search_history_fl)
    //    FrameLayout mSearchHistory;
    /**
     * 网盘上的当前路径
     */
    private String currentRemotePath = ROOT_PATH;
    public static final String REQUEST_CODE_LISTDIR_INIT = "file_listDir_init";//首次初始化
    public static final String REQUEST_CODE_LISTDIR_REFREASH = "file_listDir_refresh";//下拉刷新
    public static final String REQUEST_CODE_MAKE_DIR = "file_makeDir";//创建文件夹
    public static final String REQUEST_CODE_DELETE = "file_remove";//删除文件或文件夹
    public static final String REQUEST_CODE_RENAME = "file_rename";//重新命名
    public static final String REQUEST_CODE_CREATE_PUBLISH_LINK = "file_createPublishLink";//分享文件
    public static final String REQUEST_CODE_CANCEL_SHARED = "file_cancelShared";//取消分享
    public static final String REQUEST_CODE_MOVE = "file_move";//移动文件或者文件夹
    public static final String REQUEST_CODE_COPY_TO = "file_copy_to";//复制文件或者文件夹到


    private FileListAdapter mAdapter;
    /**
     * 拍照文件的本地路径
     */
    private File mImageFile;

    DBTool mDBTool;
    TransTool mTransTool;
    /**
     * listView上下文菜单监听
     */
    private MenuInflater mMenuInflater;
    /**
     * 下载服务是否已经绑定
     */
    public static boolean mIsDownloadBind = false;
    private ServiceConnection mDownloadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("mMyFileDownloadConn", "onServiceConnected() called");
            App.downloadService = (IDownloadService) service;
            mIsDownloadBind = true;
            App.downloadService.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("mMyFileDownloadConn", "onServiceDisconnected() called");
            mIsDownloadBind = false;
            App.downloadService = null;
        }
    };
    /**
     * 上传服务是否已经绑定
     */
    boolean mIsUploadBind = false;
    private ServiceConnection mUploadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("mMyFileUploadConn", "onServiceConnected() called");
            App.uploadService = (IUploadService) service;
            mIsUploadBind = true;
            App.uploadService.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("mMyFileUploadConn", "onServiceDisconnected() called");
            mIsUploadBind = false;
            App.uploadService = null;
        }
    };

    /**
     * 下载广播接收者
     */
    private DownloadReceiver mDownloadReceiver;

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadTask task = (DownloadTask) intent.getSerializableExtra("task");
            if (task != null && task.getIsGroupFile() != AppConstants.GROUP_FILE) {
                String path = task.getPath();
                String parentPath = FileUtil.getPrePath(path);
                if (task.getStatus() == ITransferConstants.STATUS_FINISH && parentPath.equalsIgnoreCase(currentRemotePath)) {
                    String localPath = task.getSavePath();
                    String localPathParent = localPath.substring(0, localPath.lastIndexOf("/"));
                    ToastUtil.showShort("已完成下载" + task.getName() + "！\n下载目录：" + localPathParent);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 上传广播接收者
     */
    private UploadReceiver mUploadReceiver;

    class UploadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UploadTask task = (UploadTask) intent.getSerializableExtra("task");
            if (task != null && task.getStatus() == ITransferConstants.STATUS_FINISH && task.getRemotePath().equalsIgnoreCase(currentRemotePath) && task.getIsGroupFile() != AppConstants.GROUP_FILE) {
                reLoad();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerReceiverAndBindService();
    }

    /**
     * 注册广播或者服务
     */
    private void registerReceiverAndBindService() {
        getActivity().bindService(new Intent(getContext(), DownloadService.class), mDownloadConn, Context.BIND_AUTO_CREATE);
        getActivity().bindService(new Intent(getContext(), UploadService.class), mUploadConn, Context.BIND_AUTO_CREATE);
        mDownloadReceiver = new DownloadReceiver();
        getActivity().registerReceiver(mDownloadReceiver, new IntentFilter(DownloadService.DOWNLOAD_RECEIVER_ACTION));
        mUploadReceiver = new UploadReceiver();
        getActivity().registerReceiver(mUploadReceiver, new IntentFilter(UploadService.UPLOAD_RECEIVER_ACTION));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_tab_cloud;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    public void initToolbar() {
        mToolBar.setLeftImage(R.drawable.selector_ic_menu_back);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToParent();
            }
        });
        mToolBar.setLeftSecondImage(R.drawable.tv_search_drawable_selector);
        mToolBar.setLeftSecondImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SearchActivity.class);
            }
        });
        mToolBar.setRightImage(R.drawable.btn_style_upload);
        // mToolBar.setRightImage(R.mipmap.icon_upload);
        mToolBar.setRightImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TopPopuWindow mTopPopuWindow = new TopPopuWindow(getActivity());
                mTopPopuWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, Object> map = mTopPopuWindow.getItem(position);
                        int key = (Integer) map.get("index");
                        if (key == AppConstants.UPLOAD_CAPTURE) {
                            //打开相机
                            try {
                                String uploadPath = getUserRoot() + currentRemotePath;
                                String photoName = FileUtil.newInstance().createPhotoCapturedName(uploadPath);
                                mImageFile = new File(photoName);
                                openCamera(Uri.fromFile(mImageFile));
                            } catch (ClientIOException e) {
                                e.printStackTrace();
                            }
                        } else if (key == AppConstants.UPLOAD_PICS) {
                            //打开相册
                            openAlbum();
                        } else {
                            //打开SD卡
                            openSdcardLib(key);
                        }
                        mTopPopuWindow.dismiss();
                    }
                });
                mTopPopuWindow.show();
            }
        });

        mToolBar.setRightSecondImage(R.drawable.btn_style_mkdir);
        // mToolBar.setRightSecondImage(R.mipmap.icon_folder_new);
        mToolBar.setRightSecondImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RenameMkDirAlertDialog dialogNewFolder = new RenameMkDirAlertDialog(getActivity(), "新建文件夹");
                dialogNewFolder.setOkClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String dirName = dialogNewFolder.getFileName();
                        if (dirName.trim().length() == 0) {
                            ToastUtil.showShort(R.string.error_folder_name_empty);
                        } else if (!ValidationUtil.isFileNameLegal(dirName)) {
                            ToastUtil.showShort("文件名不能含有 \\ : / * < > ? | \"");
                        } else if (dirName.trim().length() > 50) {
                            ToastUtil.showShort("文件夹名字太长");
                        } else {
                            String remotePath = ROOT_PATH.equalsIgnoreCase(currentRemotePath) ? (currentRemotePath + dirName) : (currentRemotePath + "/" + dirName);
                            mRxManager.add(mPresenter.makeDir(remotePath, REQUEST_CODE_MAKE_DIR));
                            dialogNewFolder.dismiss();
                        }
                    }
                });
                dialogNewFolder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialogNewFolder.setFileName("");
                    }
                });
                dialogNewFolder.show();
            }
        });
    }

    @Override
    protected void initView() {
        mDBTool = new DBTool(App.getAppContext());
        mTransTool = new TransTool();
        mMenuInflater = new MenuInflater(App.getAppContext());
        mAdapter = new FileListAdapter(App.getAppContext());
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
                    unsharedItem.setTitle("外链地址");
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
                    unsharedItem.setTitle("外链地址");
                    sharedItem.setVisible(!file.isShared());
                    unsharedItem.setVisible(file.isShared());
                }
            }
        });
    }

    @Override
    protected void initData() {
        reLoad();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (isVisible) {
            int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
            USpaceFile file = (USpaceFile) mListView.getAdapter().getItem(position);
            switch (item.getItemId()) {
                case R.id.uspace_menu_rename_file:
                case R.id.uspace_menu_rename_folder:
                    rename(file);
                    break;
                case R.id.uspace_menu_move_file:
                case R.id.uspace_menu_move_folder:
                    moveTo(file);
                    break;
                case R.id.uspace_menu_copy_file:
                case R.id.uspace_menu_copy_folder:
                    copyTo(file);
                    break;
                case R.id.uspace_menu_delete_file:
                case R.id.uspace_menu_delete_folder:
                    removeFile(file);
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
        return false;
    }

    /**
     * 取消分享
     *
     * @param file
     */
    private void cancelShareFile(final USpaceFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String publicLink = PropertyUtil.getInstance().getBaseUrl() + ApiConstants.PICK_PICKLINK + file.getExtractionCode();
        builder.setMessage(publicLink);
        builder.setPositiveButton("复制链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(publicLink);
                ToastUtil.showShort("复制成功，可以发给朋友们了。");
            }
        });
        builder.setNegativeButton("取消分享", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRxManager.add(mPresenter.cancelSharedFiles(file, REQUEST_CODE_CANCEL_SHARED));
            }
        });
        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 分享文件/共享文件
     *
     * @param file
     */
    private void shareFile(USpaceFile file) {
        mRxManager.add(mPresenter.createPublishLink(file, REQUEST_CODE_CREATE_PUBLISH_LINK));
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
        final ChoiceRemoteDictionaryAlertDialog dialog = new ChoiceRemoteDictionaryAlertDialog(getActivity());
        //  dialog.setPrefix("/");
        final List<ApiPath> list = new ArrayList<>();
        list.add(new ApiPath(file.getDiskPath(), file.getVersion() == null ? 0 : file.getVersion()));
        dialog.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTransTool.validateMove(file, dialog.getDestPath())) {
                    mRxManager.add(mPresenter.moveTo(list, dialog.getDestPath(), REQUEST_CODE_MOVE));
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void copyTo(final USpaceFile file) {
        final ChoiceRemoteDictionaryAlertDialog dialog = new ChoiceRemoteDictionaryAlertDialog(getActivity());
        //  dialog.setPrefix("/");
        final List<ApiPath> list = new ArrayList<>();
        list.add(new ApiPath(file.getDiskPath(), file.getVersion() == null ? 0 : file.getVersion()));
        dialog.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxManager.add(mPresenter.copyTo(list, dialog.getDestPath(), REQUEST_CODE_COPY_TO));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 重命名文件  或者文件夹
     *
     * @param file
     */
    private void rename(USpaceFile file) {
        final RenameMkDirAlertDialog dialogRename = new RenameMkDirAlertDialog(getActivity(), "重命名");
        final String oldName = file.getName();
        dialogRename.setFileName(oldName);
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
                } else if (newName.trim().length() > 50) {
                    ToastUtil.showShort("文件夹名字太长");
                } else {
                    mRxManager.add(mPresenter.rename(currentRemotePath, oldName, newName, REQUEST_CODE_RENAME));
                    dialogRename.dismiss();
                }
            }
        });
        dialogRename.show();
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file
     */
    private void removeFile(final USpaceFile file) {
        new android.app.AlertDialog.Builder(getActivity()).setTitle(file.getName())
                .setMessage(R.string.msg_uspace_delete_confirm)
                .setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRxManager.add(mPresenter.remove(file, REQUEST_CODE_DELETE));
                    }
                }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 加载数据前执行
     *
     * @param requestCode 请求码
     */
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
            case REQUEST_CODE_MAKE_DIR:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_DELETE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_RENAME:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                mLoadingLayout.setVisibility(View.VISIBLE);
            case REQUEST_CODE_MOVE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_COPY_TO:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        //根据条件判断是否隐藏对应的操作
        if (currentRemotePath.equalsIgnoreCase(ROOT_PATH)) {
            tvCurrentRemoteDir.setVisibility(View.GONE);
            mToolBar.setLeftImageVisibility(View.GONE);
        } else {
            mToolBar.setLeftImageVisibility(View.VISIBLE);
            tvCurrentRemoteDir.setVisibility(View.VISIBLE);
            tvCurrentRemoteDir.setText(currentRemotePath);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        // public void loadSuccess(String requestCode, List<USpaceFile> fileList) {
        //        mListView.stopRefresh();
        //        mListView.stopLoadMore();
        //        mListView.setRefreshTime("更新时间:" + getSystemTime());
        List<USpaceFile> fileList = new ArrayList<>();
        if (result != null && result instanceof List) {
            fileList = (List<USpaceFile>) result;
            if (fileList.size() == 0) {
                tvEmptyFiles.setVisibility(View.VISIBLE);
            }
        }
        switch (requestCode) {
            case REQUEST_CODE_LISTDIR_INIT:
                // emptyListView();
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
            case REQUEST_CODE_MAKE_DIR:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort("创建成功");
                reLoad();
                break;
            case REQUEST_CODE_DELETE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort("删除成功");
                reLoad();
                break;
            case REQUEST_CODE_RENAME:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort("重命名成功");
                reLoad();
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                mLoadingLayout.setVisibility(View.GONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final String publicLink = (String) result;
                builder.setMessage(publicLink);
                builder.setPositiveButton("复制链接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // API11开始android推荐使用android.content.ClipboardManager
                        // API11之前为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
                        ClipboardManager cm = (ClipboardManager) App.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        // 将文本内容放到系统剪贴板里。
                        cm.setText(publicLink);
                        ToastUtil.showShort("复制成功，可以发给朋友们了。");
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                mAdapter.notifyDataSetChanged();
                ToastUtil.showShort("分享成功");
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                mLoadingLayout.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showShort("取消分享成功");
                break;
            case REQUEST_CODE_MOVE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort("移动成功");
                reLoad();
                break;
            case REQUEST_CODE_COPY_TO:
                ToastUtil.showShort("复制成功");
                mLoadingLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
            case REQUEST_CODE_LISTDIR_INIT:
                ToastUtil.showShort(msg);
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_LISTDIR_REFREASH:
                ToastUtil.showShort(msg);
                mListView.stopRefresh();
                break;
            case REQUEST_CODE_MAKE_DIR:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_DELETE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_RENAME:
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
            case REQUEST_CODE_MOVE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_COPY_TO:
                ToastUtil.showShort(msg);
                mLoadingLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 打开SD卡选择
     *
     * @param uploadType 音乐、视频、文档、所有
     */
    private void openSdcardLib(int uploadType) {
        Intent intent = new Intent(getContext(), UploadFileExploreActivity.class);
        intent.putExtra(AppConstants.BUNDLE_KEY_ROOT_PATH, FileUtil.getSDCardRoot());
        intent.putExtra(AppConstants.UPLOAD_TYPE, uploadType);
        intent.putExtra(AppConstants.CURRENT_REMOTE_PATH, currentRemotePath);//网盘上的路径
        intent.putExtra(AppConstants.IS_GROUP_UPLOAD, false);
        startActivityForResult(intent, REQUEST_SDCARD_LIBRARY);
    }

    /**
     * 打开相册
     */
    protected void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
    }

    /**
     * 打开相机 拍照
     */
    protected void openCamera(Uri imageUri) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SDCARD_LIBRARY && resultCode == RESULT_OK) {
            Object[] objs = (Object[]) data.getSerializableExtra(AppConstants.BUNDLE_KEY_UPLOAD_FILES);
            String uploadPath = data.getStringExtra(AppConstants.CURRENT_REMOTE_PATH);
            uploadFile(objs, uploadPath);
        } else if (requestCode == REQUEST_PHOTO_LIBRARY && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            String filepath = CommonUtil.getRealPathFromURI(imgUri);
            if (filepath != null) {
                File file = new File(filepath);
                FileInfo fileInfo = new FileInfo(file.getName(), file.length(), true, false, filepath, new Timestamp(file.lastModified()));
                uploadFile(new Object[]{fileInfo}, currentRemotePath);
            } else {
                ToastUtil.showShort(getText(R.string.error_upload_img_file_failed).toString());
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mImageFile != null) {
                FileInfo fileInfo = new FileInfo(mImageFile.getName(), mImageFile.length(), true, false, mImageFile.getPath(), new Timestamp(mImageFile.lastModified()));
                uploadFile(new Object[]{fileInfo}, currentRemotePath);
            } else {
                ToastUtil.showShort(getText(R.string.error_upload_img_file_failed).toString());
            }
        }
    }

    /**
     * 预上传处理
     *
     * @param objs
     * @param currentPath
     */
    private void uploadFile(Object[] objs, final String currentPath) {
        if (!NetWorkUtil.isNetConnected(App.getAppContext())) {
            ToastUtil.showShort(R.string.network_not_available);
            return;
        }
        final List<File> existed = new ArrayList<>();
        final List<File> uploaded = new ArrayList<>();
        for (Object obj : objs) {
            FileInfo fileInfo = (FileInfo) obj;
            if (mAdapter.containsFile(fileInfo.filename)) {
                existed.add(new File(fileInfo.filepath));
            }
            uploaded.add(new File(fileInfo.filepath));
        }
        if (existed.size() > 0) {
            String joinName = FileUtil.getFileJoinName(existed);
            String msg = MessageFormat.format(getText(R.string.msg_uspace_upload_file_exist_current_path_confirm).toString(), joinName);
            new android.app.AlertDialog.Builder(getContext()).setTitle(R.string.dialog_title_prompt).setMessage(msg).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (uploaded.size() > 0) {
                        mTransTool.uploadFiles(currentPath, uploaded, mIsUploadBind, null);
                    }
                }
            }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 把存在的文件从上传列表中移除
                    uploaded.removeAll(existed);
                    dialog.dismiss();
                    if (uploaded.size() > 0) {
                        mTransTool.uploadFiles(currentPath, uploaded, mIsUploadBind, null);
                    }
                }
            }).show();
        } else {
            if (uploaded.size() > 0) {
                mTransTool.uploadFiles(currentPath, uploaded, mIsUploadBind, null);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        USpaceFile uf = (USpaceFile) mAdapter.getItem(position - 1);//去掉头布局
        if (uf.isFolder()) {
            currentRemotePath = uf.getDiskPath();
            emptyListView();
            mRxManager.add(mPresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
        } else {
            if (FileUtil.isFileExist(uf)) {
                openFile(uf);
            } else {
                downloadFile(uf);
            }
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
            String filePath = getUserRoot() + file.getDiskPath();
            Intent intent = FileViewer.getOpenFileIntent(filePath);
            if (intent != null) {
                startActivity(intent);
            } else {
                String msg = MessageFormat.format(getString(R.string.msg_uspace_cannot_open_file), file.getName());
                ToastUtil.showShort(msg);
            }
        } else {
            new android.app.AlertDialog.Builder(getActivity()).setTitle(file.getName()).setMessage(R.string.msg_uspace_download_file_before_open).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

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
            new android.app.AlertDialog.Builder(getActivity()).setTitle(file.getName()).setMessage(msg).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

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
     * 清空ListView数据
     */
    private void emptyListView() {
        mAdapter.getData().clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onBackForward(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (currentRemotePath.equalsIgnoreCase(ROOT_PATH)) {
                return false;
            } else {
                backToParent();
                return true;
            }
        }
        return false;
    }

    /**
     * 跳转到上一级
     */
    private void backToParent() {
        currentRemotePath = FileUtil.getPrePath(currentRemotePath);
        emptyListView();
        mRxManager.add(mPresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
    }


    /**
     * XListView下拉刷新，加载更多监听
     */
    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            mRxManager.add(mPresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_REFREASH));
        }

        @Override
        public void onLoadMore() {
            // getMoreDataFromNet();
        }
    }

    /**
     * 重新加载
     */
    private void reLoad() {
        mRxManager.add(mPresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
    }

    @Override
    public void onDestroyView() {
        if (mIsDownloadBind)
            getActivity().unbindService(mDownloadConn);
        if (mDownloadReceiver != null)
            getActivity().unregisterReceiver(mDownloadReceiver);
        if (mIsUploadBind)
            getActivity().unbindService(mUploadConn);
        if (mUploadReceiver != null)
            getActivity().unregisterReceiver(mUploadReceiver);
        super.onDestroyView();
    }
}