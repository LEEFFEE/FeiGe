package cn.leeffee.feige.ui.cloud.fragment;

import android.content.BroadcastReceiver;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.adapter.FileListAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.FragMyFileContract;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.model.FragMyFileModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragMyFilePresenterImpl;
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
import cn.leeffee.feige.widget.MyProgressDialog;
import cn.leeffee.feige.widget.RenameMkDirAlertDialog;
import cn.leeffee.feige.widget.TopPopuWindow;
import cn.leeffee.feige.widget.USpaceToolBar;
import cn.leeffee.feige.widget.XListView;

import static android.app.Activity.RESULT_OK;
import static cn.leeffee.feige.ui.cloud.activity.UploadFileExploreActivity.REQUEST_CODE_LISTDIRONLYDIR;
import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;
import static cn.leeffee.feige.utils.FileUtil.getUserRoot;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyFileFragment#} factory method to
 * create an instance of this fragment.
 */
public class MyFileFragment extends BaseFragment<FragMyFilePresenterImpl, FragMyFileModelImpl> implements FragMyFileContract.View, AdapterView.OnItemClickListener {

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
    private static final String REQUEST_CODE_MAKE_DIR = "file_makeDir";//创建文件夹
    public static final String REQUEST_CODE_DELETE = "file_remove";//删除文件或文件夹
    public static final String REQUEST_CODE_RENAME = "file_rename";//重新命名
    public static final String REQUEST_CODE_CREATE_PUBLISH_LINK = "file_createPublishLink";//分享文件
    public static final String REQUEST_CODE_CANCEL_SHARED = "file_cancelShared";//取消分享
    public static final String REQUEST_CODE_MOVE = "file_move";//移动文件或者文件夹


    private FileListAdapter mAdapter;
    /**
     * 新建文件夹对话框
     */
    RenameMkDirAlertDialog dialogNewFolder;
    /**
     * 重新命名
     */
    RenameMkDirAlertDialog dialogRename;
    /**
     * 上传类型选择对话框
     */
    TopPopuWindow mTopPopuWindow;
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
     * 进度对话框
     */
    MyProgressDialog pDialog;
    /**
     * 下载服务是否已经绑定
     */
    public static boolean mIsDownloadBind = false;
    private ServiceConnection mDownloadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("mDownloadConn", "onServiceConnected() called");
            App.downloadService = (IDownloadService) service;
            mIsDownloadBind = true;
            App.downloadService.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("mDownloadConn", "onServiceDisconnected() called");
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
            Log.i("mUploadConn", "onServiceConnected() called");
            App.uploadService = (IUploadService) service;
            mIsUploadBind = true;
            App.uploadService.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("mUploadConn", "onServiceDisconnected() called");
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
                //                String parentPath = path.substring(0, path.lastIndexOf("/"));
                //                if ("".equals(parentPath)) {
                //                    parentPath = "/";
                //                }

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().bindService(new Intent(getContext(), DownloadService.class), mDownloadConn, Context.BIND_AUTO_CREATE);
        getActivity().bindService(new Intent(getContext(), UploadService.class), mUploadConn, Context.BIND_AUTO_CREATE);
        mDownloadReceiver = new DownloadReceiver();
        getActivity().registerReceiver(mDownloadReceiver, new IntentFilter(DownloadService.DOWNLOAD_RECEIVER_ACTION));
        mUploadReceiver = new UploadReceiver();
        getActivity().registerReceiver(mUploadReceiver, new IntentFilter(UploadService.UPLOAD_RECEIVER_ACTION));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_my_file;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        mDBTool = new DBTool(App.getAppContext());
        mTransTool = new TransTool();
    }

    @Override
    public void initToolbar() {
        mToolBar.setLeftImage(R.mipmap.ic_menu_back);
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
                startActivity(new Intent(App.getAppContext(), SearchActivity.class));
            }
        });
        mToolBar.setRightImage(R.drawable.btn_style_upload);
        // mToolBar.setRightImage(R.mipmap.icon_upload);
        mToolBar.setRightImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTopPopuWindow == null) {
                    mTopPopuWindow = new TopPopuWindow(getActivity());
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
                } else if (!mTopPopuWindow.isShowing()) {
                    mTopPopuWindow.show();
                }
            }
        });

        mToolBar.setRightSecondImage(R.drawable.btn_style_mkdir);
        // mToolBar.setRightSecondImage(R.mipmap.icon_folder_new);
        mToolBar.setRightSecondImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogNewFolder == null) {
                    dialogNewFolder = new RenameMkDirAlertDialog(getActivity(), "新建文件夹");
                    dialogNewFolder.setOkClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String dirName = dialogNewFolder.getFileName();
                            if (dirName.trim().length() == 0) {
                                ToastUtil.showShort(R.string.error_folder_name_empty);
                            } else if (!ValidationUtil.isFileNameLegal(dirName)) {
                                ToastUtil.showShort("文件名不能含有 \\ : / * < > ? | \"");
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
                } else if (!dialogNewFolder.isShowing()) {
                    dialogNewFolder.show();
                }
            }
        });
    }

    @Override
    protected void initView() {
        mMenuInflater = new MenuInflater(App.getAppContext());
        mRxManager.add(mPresenter.listDir(currentRemotePath, REQUEST_CODE_LISTDIR_INIT));
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (isVisible) {
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
        return false;
    }

    /**
     * 取消分享
     *
     * @param file
     */
    private void cancelShareFile(USpaceFile file) {
        mRxManager.add(mPresenter.cancelSharedFiles(file, REQUEST_CODE_CANCEL_SHARED));
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
                    mRxManager.add(mPresenter.listDirOnlyDir(moveToRemotePath, REQUEST_CODE_LISTDIRONLYDIR));
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    mRxManager.add(mPresenter.move(srcFile.getDiskPath(), moveToRemotePath, REQUEST_CODE_MOVE));
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
        mRemoteMoveToDir.setText("/我的云盘");
        mRxManager.add(mPresenter.listDirOnlyDir("/", REQUEST_CODE_LISTDIRONLYDIR));
    }


    /**
     * 重命名文件  或者文件夹
     *
     * @param file
     */
    private void rename(USpaceFile file) {
        if (dialogRename == null) {
            dialogRename = new RenameMkDirAlertDialog(getActivity(), "重命名");
        }
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
                if (pDialog == null) {
                    pDialog = new MyProgressDialog(getActivity());
                }
                String strDeleteTitle = getString(R.string.strPostDialogTitle);
                String strDeleteMsg = getString(R.string.msg_uspace_delete_file);
                pDialog.setTitle(strDeleteTitle);
                pDialog.setMessage(strDeleteMsg);
                pDialog.show();
                break;
            case REQUEST_CODE_RENAME:
                if (pDialog == null) {
                    pDialog = new MyProgressDialog(getActivity());
                }
                String strRenameTitle = getString(R.string.strPostDialogTitle);
                String strRenameMsg = getString(R.string.msg_uspace_rename);
                pDialog.setTitle(strRenameTitle);
                pDialog.setMessage(strRenameMsg);
                pDialog.show();
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                if (pDialog == null) {
                    pDialog = new MyProgressDialog(getActivity());
                }
                String strPublishTitle = getString(R.string.strPostDialogTitle);
                String strPublishMsg = getString(R.string.msg_uspace_publish_file);
                pDialog.setTitle(strPublishTitle);
                pDialog.setMessage(strPublishMsg);
                pDialog.show();
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                if (pDialog == null) {
                    pDialog = new MyProgressDialog(getActivity());
                }
                String strCancelSharedTitle = getString(R.string.strPostDialogTitle);
                String strCancelSharedMsg = getString(R.string.msg_uspace_unpublish_file);
                pDialog.setTitle(strCancelSharedTitle);
                pDialog.setMessage(strCancelSharedMsg);
                pDialog.show();
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_MOVE:
                if (pDialog == null) {
                    pDialog = new MyProgressDialog(getActivity());
                }
                String strMoveTitle = getString(R.string.strPostDialogTitle);
                String strMoveMsg = "正在移动";
                pDialog.setTitle(strMoveTitle);
                pDialog.setMessage(strMoveMsg);
                pDialog.show();
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
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort("删除成功");
                reLoad();
                break;
            case REQUEST_CODE_RENAME:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort("重命名成功");
                reLoad();
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                mAdapter.notifyDataSetChanged();
                ToastUtil.showShort("分享成功");
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                mAdapter.notifyDataSetChanged();
                ToastUtil.showShort("取消分享成功");
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDirListAdapter.setData(fileList);
                mDirListAdapter.notifyDataSetChanged();
                mDialogLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_MOVE:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort("移动成功");
                reLoad();
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
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_RENAME:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CREATE_PUBLISH_LINK:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CANCEL_SHARED:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_MOVE:
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                ToastUtil.showShort(msg);
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
        intent.putExtra(AppConstants.BUNDLE_KEY_ROOT_PATH, "/sdcard");
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
    public void onDestroy() {
        super.onDestroy();
        if (mIsDownloadBind)
            getActivity().unbindService(mDownloadConn);
        if (mDownloadReceiver != null)
            getActivity().unregisterReceiver(mDownloadReceiver);
        if (mIsUploadBind)
            getActivity().unbindService(mUploadConn);
        if (mUploadReceiver != null)
            getActivity().unregisterReceiver(mUploadReceiver);
    }
}