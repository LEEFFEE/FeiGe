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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.cloud.activity.GroupLogActivity;
import cn.leeffee.feige.ui.cloud.activity.UploadFileExploreActivity;
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.adapter.FileListAdapter;
import cn.leeffee.feige.ui.cloud.adapter.GroupListAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.FragMyGroupContract;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.entity.USpaceGroup;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.model.FragMyGroupModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragMyGroupPresenterImpl;
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
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.utils.ValidationUtil;
import cn.leeffee.feige.widget.RenameMkDirAlertDialog;
import cn.leeffee.feige.widget.TopPopuWindow;
import cn.leeffee.feige.widget.USpaceToolBar;
import cn.leeffee.feige.widget.XListView;

import static android.app.Activity.RESULT_OK;
import static cn.leeffee.feige.ui.cloud.activity.UploadFileExploreActivity.REQUEST_CODE_LISTDIRONLYDIR;
import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;
import static cn.leeffee.feige.utils.CommonUtil.getRealPathFromURI;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyGroupFragment#} factory method to
 * create an instance of this fragment.
 */
public class MyGroupFragment extends BaseFragment<FragMyGroupPresenterImpl, FragMyGroupModelImpl> implements FragMyGroupContract.View, AdapterView.OnItemClickListener {

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

    @BindView(R.id.my_group_toolbar)
    USpaceToolBar mToolBar;
    /**
     * 展示当前路径信息
     */
    @BindView(R.id.my_group_current_path_tv)
    TextView mCurrentPath;
    @BindView(R.id.my_group_list_lv)
    XListView mListView;
    /**
     * ListView为空时提示
     */
    @BindView(R.id.my_group_empty_tv)
    TextView mEmptyTips;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    private String current_group_id = "";
    private String current_group_name = "";
    private String currentDisplayPath = ROOT_PATH;
    private int current_group_status;//等于2为删除状态
    private static final String REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT = "findshareGroupByUser_init";
    private static final String REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_REFRESH = "findshareGroupByUser_refresh";
    private static final String REQUEST_CODE_LIST_GROUP_FILE_INIT = "listDir_group_init";
    private static final String REQUEST_CODE_LIST_GROUP_FILE_REFRESH = "listDir_group_refresh";
    private static final String REQUEST_CODE_MAKE_GROUP_DIR = "makeDir_group";
    private static final String REQUEST_CODE_REMOVE = "remove_group";
    private static final String REQUEST_CODE_COPY_TO_PRIVATE_SPACE = "copy2PrivateSpace_group";//复制到个人（私人）空间

    /**
     * 群组文件信息适配器
     */
    BaseAdapter adapter;

    /**
     * 上传类型选择对话框
     */
    TopPopuWindow mTopPopuWindow;
    /**
     * 拍照文件的本地路径
     */
    private File mImageFile;
    /**
     * 新建文件夹对话框
     */
    RenameMkDirAlertDialog dialogNewFolder;

    /**
     * 上传下载业务处理
     */
    TransTool mTransTool;
    /**
     * 长按菜单监听
     */
    MenuInflater mMenuInflater;

    boolean mIsDownloadBind = false;
    boolean mIsUploadBind = false;
    private ServiceConnection mDownloadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnection1", "onServiceConnected() called");
            App.downloadService = (IDownloadService) service;
            mIsDownloadBind = true;
            App.downloadService.unlock();// TODO
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection1", "onServiceDisconnected() called");
            mIsDownloadBind = false;
            App.downloadService = null;
        }
    };

    private ServiceConnection mUploadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnection2", "onServiceConnected() called");
            App.uploadService = (IUploadService) service;
            mIsUploadBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection2", "onServiceDisconnected() called");
            mIsUploadBind = false;
            App.uploadService = null;
        }
    };

    /**
     * 上传广播接收
     */
    private UploadReceiver mUploadReceiver;

    class UploadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UploadTask task = (UploadTask) intent.getSerializableExtra("task");
            if (task != null && task.getStatus() == ITransferConstants.STATUS_FINISH && task.getRemotePath().equalsIgnoreCase(getRealFilePath(currentDisplayPath)) && task.getIsGroupFile() == AppConstants.GROUP_FILE) {
                refresh();
            }
        }
    }

    /**
     * 下载广播接收
     */
    private DownloadReceiver mDownloadReceiver;

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadTask task = (DownloadTask) intent.getSerializableExtra("task");
            if (task != null && task.getIsGroupFile() == AppConstants.GROUP_FILE) {
                String path = task.getPath();
                String parentPath = path.substring(0, path.lastIndexOf("/"));
                if ("".equals(parentPath)) {
                    parentPath = "/";
                }

                if (task.getStatus() == ITransferConstants.STATUS_FINISH && parentPath.equalsIgnoreCase(getRealFilePath(currentDisplayPath))) {
                    String localPath = task.getSavePath();
                    String localPathParent = localPath.substring(0, localPath.lastIndexOf("/"));
                    ToastUtil.showShort("已完成下载" + task.getName() + "！\n下载目录：" + localPathParent);
                    adapter.notifyDataSetChanged();
                }
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
        return R.layout.frag_my_group;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        mListView.setPullLoadEnable(false);//设置加载更多是否可用
        mListView.setXListViewListener(new MyIXListViewListener());
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);
        mMenuInflater = new MenuInflater(getContext());
        mTransTool = new TransTool();

    }

    @Override
    protected void initToolbar() {
        mToolBar.setLeftImage(R.mipmap.ic_menu_back);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToParent();
            }
        });
        mToolBar.setRightImage(R.drawable.btn_style_upload);
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
                                    String uploadPath = FileUtil.getUserRoot() + "/" + AppConstants.TITLE_MY_GROUP + currentDisplayPath;
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

                                String remotePath = getRealFilePath(currentDisplayPath + "/" + dirName);
                                mRxManager.add(mPresenter.makeGroupDir(remotePath, current_group_id, REQUEST_CODE_MAKE_GROUP_DIR));
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
        if (currentDisplayPath.equals(ROOT_PATH)) {
            mRxManager.add(mPresenter.listGroups(REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT));
        } else {
            mRxManager.add(mPresenter.listGroupFile(getRealFilePath(currentDisplayPath), current_group_id, current_group_name, REQUEST_CODE_LIST_GROUP_FILE_INIT));
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Object o = adapter.getItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position - 1);
        mMenuInflater.inflate(R.menu.menu_group_item, menu);
        if (o instanceof USpaceGroup) {
            menu.setHeaderTitle("群组操作");
            menu.findItem(R.id.menu_group_log).setVisible(true);
        } else {
            USpaceFile file = (USpaceFile) o;
            menu.setHeaderTitle(R.string.menu_title_file_opt);
            if (!file.isParent() && !file.isFolder()) {
                menu.findItem(R.id.menu_group_download).setVisible(true);
            }
            menu.findItem(R.id.menu_group_copy).setVisible(true);
            if (current_group_status != 2) {
                menu.findItem(R.id.menu_group_delete).setVisible(true);
            }
            //            if (file.getCreaterName().equals(SPUtil.getString(AppConfig.ACCOUNT))) {
            //                menu.findItem(R.id.menu_group_delete).setVisible(true);//如果是创建者才能删除
            //            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (isVisible) {
            int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
            Object o = mListView.getAdapter().getItem(position);
            if (o instanceof USpaceGroup) {
                USpaceGroup group = (USpaceGroup) o;
                current_group_id = group.getGroupId();
                current_group_name = group.getName();
                current_group_status = group.getStatus();
                switch (item.getItemId()) {
                    case R.id.menu_group_log:
                        showGroupLog(group.getGroupId(), group.getName());
                        break;
                }
            } else {
                USpaceFile file = (USpaceFile) o;
                switch (item.getItemId()) {
                    case R.id.menu_group_download:
                        downloadFile(file);
                        break;
                    case R.id.menu_group_copy:
                        openDirChoiceDialog(new USpaceFile[]{file});
                        break;
                    case R.id.menu_group_delete:
                        deleteFiles(new USpaceFile[]{file});
                        break;
                }
            }
            return true;
        }
        return false;
    }

    AlertDialog mDialog;
    TextView tvRemoteDir;
    String copyToPath;
    DirListAdapter dirAdapter;
    private USpaceFile[] mCopyFiles;
    LinearLayout mDialogLoading;

    /**
     * 打开目录选中对话框
     *
     * @param ufs 要复制的文件数组
     */
    private void openDirChoiceDialog(USpaceFile[] ufs) {
        mCopyFiles = ufs;
        copyToPath = ROOT_PATH;
        if (mDialog == null) {
            LayoutInflater Inflater = (LayoutInflater) App.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = Inflater.inflate(R.layout.dialog_change_dir, null);
            mDialogLoading = (LinearLayout) v.findViewById(R.id.loading_layout);
            tvRemoteDir = (TextView) v.findViewById(R.id.dialog_title_dir_tv);
            // 初始化按钮
            v.findViewById(R.id.dialog_ok_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRxManager.add(mPresenter.copy2PrivateSpace(mCopyFiles, copyToPath, current_group_id, REQUEST_CODE_COPY_TO_PRIVATE_SPACE));
                    mDialog.dismiss();
                }
            });


            v.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });

            // 初始化Listview，加载数据。
            ListView dirListView = (ListView) v.findViewById(R.id.dialog_dir_list_lv);
            dirAdapter = new DirListAdapter(App.getAppContext());
            dirListView.setAdapter(dirAdapter);
            dirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    USpaceFile fileInfo = (USpaceFile) dirAdapter.getItem(position);
                    String path = fileInfo.getDiskPath();
                    String dir = "复制到：/我的云盘";
                    if (fileInfo.isParent()) {
                        path = path.substring(0, path.lastIndexOf('/'));
                        if ("".equals(path)) {
                            path = "/";
                        }
                    }
                    dirAdapter.getData().clear();
                    if (path.equals("/")) {
                        dirAdapter.getData().add(0, new USpaceFile(getText(R.string.strBackToParent).toString(), 0, true, true, path, false));
                    }
                    dir += path;
                    tvRemoteDir.setText(dir);
                    dirAdapter.notifyDataSetChanged();
                    copyToPath = path;
                    mRxManager.add(mPresenter.listDirOnlyDir(copyToPath, REQUEST_CODE_LISTDIRONLYDIR));
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
        mDialog.show();
        tvRemoteDir.setText("/我的云盘");
        mRxManager.add(mPresenter.listDirOnlyDir(ROOT_PATH, REQUEST_CODE_LISTDIRONLYDIR));
    }

    /**
     * 查看组日志
     *
     * @param groupId
     * @param groupName
     */
    private void showGroupLog(String groupId, String groupName) {
        Intent intent = new Intent(getContext(), GroupLogActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    /**
     * 刷新
     */
    private void refresh() {
        if (current_group_name.length() != 0 && currentDisplayPath.contains(current_group_name)) {
            mRxManager.add(mPresenter.listGroupFile(getRealFilePath(currentDisplayPath), current_group_id, current_group_name, REQUEST_CODE_LIST_GROUP_FILE_REFRESH));
        } else {
            mRxManager.add(mPresenter.listGroups(REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_REFRESH));
            //  new MessagePopsTask().execute();
        }
    }

    @Override
    public void loadBefore(String requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_INIT:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_REFRESH:
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_REFRESH:
                break;
            case REQUEST_CODE_REMOVE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoading.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_COPY_TO_PRIVATE_SPACE:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_MAKE_GROUP_DIR:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        //根据条件判断是否隐藏对应的操作
        if (currentDisplayPath.equalsIgnoreCase(ROOT_PATH)) {
            mCurrentPath.setVisibility(View.GONE);
            mToolBar.setLeftImageVisibility(View.GONE);

            mToolBar.setRightImageVisibility(View.GONE);
            mToolBar.setRightSecondImageVisibility(View.GONE);
        } else {
            mToolBar.setLeftImageVisibility(View.VISIBLE);
            mCurrentPath.setVisibility(View.VISIBLE);
            mCurrentPath.setText(currentDisplayPath);

            mToolBar.setRightImageVisibility(View.VISIBLE);
            mToolBar.setRightSecondImageVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        switch (requestCode) {
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT:
                adapter = new GroupListAdapter(getContext());
                mListView.setAdapter(adapter);
                List<USpaceGroup> resGroups = (List<USpaceGroup>) result;
                if (resGroups == null || resGroups.size() == 0) {
                    mEmptyTips.setText("还没有和你相关的群组~");
                    mEmptyTips.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTips.setVisibility(View.GONE);
                    ((GroupListAdapter) adapter).setData(resGroups);
                    adapter.notifyDataSetChanged();
                }
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_INIT:
                adapter = new FileListAdapter(getContext());
                mListView.setAdapter(adapter);
                List<USpaceFile> resFiles = (List<USpaceFile>) result;
                if (resFiles == null || resFiles.size() == 0) {
                    mEmptyTips.setText("这是一个空文件夹~");
                    mEmptyTips.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTips.setVisibility(View.GONE);
                    ((FileListAdapter) adapter).setData(resFiles);
                    adapter.notifyDataSetChanged();
                }

                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_REFRESH:
                ((GroupListAdapter) adapter).setData((List<USpaceGroup>) result);
                adapter.notifyDataSetChanged();
                mListView.stopRefresh();
                mListView.setRefreshTime("更新时间:" + CommonUtil.getSystemTime());
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_REFRESH:
                ((FileListAdapter) adapter).setData((List<USpaceFile>) result);
                adapter.notifyDataSetChanged();
                mListView.stopRefresh();
                mListView.setRefreshTime("更新时间:" + CommonUtil.getSystemTime());
                break;
            case REQUEST_CODE_REMOVE:
                mLoadingLayout.setVisibility(View.GONE);
                refresh();
                ToastUtil.showShort("删除成功!");
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                dirAdapter.setData((List<USpaceFile>) result);
                dirAdapter.notifyDataSetChanged();
                mDialogLoading.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_COPY_TO_PRIVATE_SPACE:
                ToastUtil.showShort("复制成功!");
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_MAKE_GROUP_DIR:
                ToastUtil.showShort((CharSequence) result);
                refresh();
                mLoadingLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT:
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_INIT:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_REFRESH:
                mListView.stopRefresh();
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_LIST_GROUP_FILE_REFRESH:
                mListView.stopRefresh();
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_REMOVE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort("删除失败，" + msg);
                break;
            case REQUEST_CODE_LISTDIRONLYDIR:
                mDialogLoading.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_COPY_TO_PRIVATE_SPACE:
                mLoadingLayout.setVisibility(View.GONE);
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_MAKE_GROUP_DIR:
                ToastUtil.showShort("创建目录失败!");
                mLoadingLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = adapter.getItem(position - 1);//去掉头布局
        if (o instanceof USpaceGroup) {
            USpaceGroup group = (USpaceGroup) o;
            current_group_id = group.getGroupId();
            current_group_name = group.getName();
            current_group_status = group.getStatus();
            currentDisplayPath += current_group_name;
            emptyListView();
            mPresenter.listGroupFile(getRealFilePath(currentDisplayPath), current_group_id, current_group_name, REQUEST_CODE_LIST_GROUP_FILE_INIT);
        } else {
            USpaceFile file = (USpaceFile) o;
            if (file.isFolder()) {
                currentDisplayPath = ROOT_PATH + current_group_name + file.getDiskPath();
                emptyListView();
                mPresenter.listGroupFile(getRealFilePath(currentDisplayPath), current_group_id, current_group_name, REQUEST_CODE_LIST_GROUP_FILE_INIT);
            } else {
                if (FileUtil.isFileExist(file)) {
                    openFile(file);
                } else {
                    downloadFile(file);
                }
            }
        }
    }

    @Override
    public boolean onBackForward(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (currentDisplayPath.equalsIgnoreCase(ROOT_PATH)) {
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
        currentDisplayPath = currentDisplayPath.substring(0, currentDisplayPath.lastIndexOf('/'));
        if ("".equals(currentDisplayPath)) {
            currentDisplayPath = ROOT_PATH;
            // new MessagePopsTask().execute();
            mPresenter.listGroups(REQUEST_CODE_FIND_SHARE_GROUP_BY_USER_INIT);
        } else {
            mPresenter.listGroupFile(getRealFilePath(currentDisplayPath), current_group_id, current_group_name, REQUEST_CODE_LIST_GROUP_FILE_INIT);
        }
    }

    /**
     * XListView下拉刷新，加载更多监听
     */
    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            refresh();
        }

        @Override
        public void onLoadMore() {
            // getMoreDataFromNet();
        }
    }

    /**
     * 清空listview数据
     */
    private void emptyListView() {
        if (adapter instanceof GroupListAdapter) {
            ((GroupListAdapter) adapter).getData().clear();
        } else {
            ((FileListAdapter) adapter).getData().clear();
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 获取组中的文件路径
     *
     * @param displayPath /组名/真实路径
     * @return
     */
    public String getRealFilePath(String displayPath) {
        String realPath = displayPath.substring(displayPath.indexOf(current_group_name) + current_group_name.length());
        if (realPath.length() == 0) {
            realPath = ROOT_PATH;
        }
        return realPath;
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
        intent.putExtra(AppConstants.CURRENT_REMOTE_PATH, currentDisplayPath);//网盘上的路径
        intent.putExtra(AppConstants.IS_GROUP_UPLOAD, true);
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
            LogUtil.e(e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SDCARD_LIBRARY && resultCode == RESULT_OK) {
            Object[] objs = (Object[]) data.getSerializableExtra(AppConstants.BUNDLE_KEY_UPLOAD_FILES);
            uploadFile(objs);
        } else if (requestCode == REQUEST_PHOTO_LIBRARY && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            String filepath = getRealPathFromURI(imgUri);
            if (filepath != null) {
                File file = new File(filepath);
                FileInfo fileInfo = new FileInfo(file.getName(), file.length(), true, false, filepath, new Timestamp(file.lastModified()));
                uploadFile(fileInfo);
            } else {
                ToastUtil.showShort(R.string.error_upload_img_file_failed);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mImageFile != null) {
                FileInfo fileInfo = new FileInfo(mImageFile.getName(), mImageFile.length(), true, false, mImageFile.getPath(), new Timestamp(mImageFile.lastModified()));
                uploadFile(fileInfo);
            } else {
                ToastUtil.showShort(R.string.error_upload_img_file_failed);
            }
        }
    }

    /**
     * 预上传处理
     *
     * @param objs
     */
    private void uploadFile(Object... objs) {
        if (!NetWorkUtil.isNetConnected(App.getAppContext())) {
            ToastUtil.showShort(R.string.network_not_available);
            return;
        }
        final List<File> existed = new ArrayList<>();
        final List<File> uploaded = new ArrayList<>();
        for (Object obj : objs) {
            FileInfo fileInfo = (FileInfo) obj;
            if (((FileListAdapter) adapter).containsFile(fileInfo.filename)) {
                existed.add(new File(fileInfo.filepath));
            }
            uploaded.add(new File(fileInfo.filepath));
        }
        if (existed.size() > 0) {
            String joinName = FileUtil.getFileJoinName(existed);
            String msg = MessageFormat.format(getText(R.string.msg_uspace_upload_file_exist_current_path_confirm).toString(), joinName);
            new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_prompt).setMessage(msg).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (uploaded.size() > 0) {
                        mTransTool.uploadFiles(getRealFilePath(currentDisplayPath), uploaded, mIsUploadBind, current_group_id);
                    }
                }
            }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 把存在的文件从上传列表中移除
                    uploaded.removeAll(existed);
                    dialog.dismiss();
                    if (uploaded.size() > 0) {
                        mTransTool.uploadFiles(getRealFilePath(currentDisplayPath), uploaded, mIsUploadBind, current_group_id);
                    }
                }
            }).show();
        } else {
            if (uploaded.size() > 0) {
                mTransTool.uploadFiles(getRealFilePath(currentDisplayPath), uploaded, mIsUploadBind, current_group_id);
            }
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    private void openFile(USpaceFile file) {
        if (!FileViewer.canOpen(file.getName())) {
            return;
        }
        String filePath = FileUtil.getUSpaceLocalGroupRoot(current_group_name) + file.getDiskPath();
        Intent intent = FileViewer.getOpenFileIntent(filePath);
        if (intent != null) {
            startActivity(intent);
        } else {
            String msg = MessageFormat.format(getString(R.string.msg_uspace_cannot_open_file), file.getName());
            ToastUtil.showShort(msg);
        }
    }

    /**
     * 预上传处理
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
     * 删除提示
     *
     * @param files
     */
    private void deleteFiles(final USpaceFile[] files) {
        new AlertDialog.Builder(getActivity()).setTitle(getFileName(files)).setMessage(R.string.msg_uspace_delete_file_confirm).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // new DeleteFileTask(act).execute(files);
                mRxManager.add(mPresenter.removeGroupFile(files, current_group_id, REQUEST_CODE_REMOVE));
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public String getFileName(USpaceFile[] files) {
        String names = "";
        for (USpaceFile file : files) {
            names += file.getName() + ",";
        }
        names = names.substring(0, names.lastIndexOf(","));
        return names;
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
