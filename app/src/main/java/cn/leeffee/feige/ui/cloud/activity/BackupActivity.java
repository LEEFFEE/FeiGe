package cn.leeffee.feige.ui.cloud.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.adapter.BackupDirListAdapter;
import cn.leeffee.feige.ui.cloud.contract.ActBackupContract;
import cn.leeffee.feige.ui.cloud.db.DBTool;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.model.ActBackupModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActBackupPresenterImpl;
import cn.leeffee.feige.ui.cloud.service.BackupService;
import cn.leeffee.feige.ui.cloud.service.BackupTask;
import cn.leeffee.feige.ui.cloud.service.IBackupService;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.PropCacheManager;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.CheckableFolderListView;
import cn.leeffee.feige.widget.USpaceToolBar;


public class BackupActivity extends BaseActivity<ActBackupPresenterImpl, ActBackupModelImpl> implements ActBackupContract.View {
    @BindView(R.id.backup_toolbar)
    USpaceToolBar mToolBar;

    @BindView(R.id.backup_status_iv)
    ImageView mBackupStatus;

    @BindView(R.id.backup_device_name_tv)
    TextView mBackupDeviceName;
    @BindView(R.id.backup_dir_list_lv)
    ListView mListView;
    BackupDirListAdapter adapter;
    /**
     * 设备名
     */
    private String deviceName = Build.MODEL;
    /**
     * 远程备份路径
     */
    private String backupRemotePath = "/mybackup/Mobile/" + deviceName;
    /**
     * SD卡根路径
     */
    //private String wRootPath = "/sdcard";
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    /**
     * 文件夹可选的ListView
     */
    CheckableFolderListView mCheckableFolderListView;
    /**
     * 对话框中的标题路径
     */
    TextView tvTitle;
    /**
     * 数据库操操作
     */
    DBTool mDBTool;

    private static final String REQUEST_CODE_LIST_BACKUP_QUEUE = "listBackupQueue";

    //绑定backupService,用来通知其备份
    public static boolean mBackupBind = false;
    private ServiceConnection mBackupConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ServiceConnection1", "onServiceConnected() called");
            App.backupService = (IBackupService) service;
            mBackupBind = true;
            App.backupService.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("BackupServiceConnection", "onServiceDisconnected() called");
            mBackupBind = false;
            App.downloadService = null;
        }

    };

    /**
     * 备份广播
     */
    BackupReceiver mBackupReceiver = null;

    class BackupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BackupTask bTask = null;
            try {
                bTask = (BackupTask) intent.getSerializableExtra("task");
            } catch (Exception e) {
                LogUtil.e(e.toString());
            }
            if (bTask != null && bTask.getStatus() != ITransferConstants.STATUS_CANCEL) {
                LogUtil.e("BackupActivity：Receiver [id=" + bTask.getId() + ",Status=" + bTask.getStatus());
                BackupTask tmpTask = null;
                List<BackupTask> taskList = adapter.getData();
                for (int i = 0; i < taskList.size(); i++) {
                    if (bTask.getId() == taskList.get(i).getId()) {
                        tmpTask = taskList.get(i);
                        break;
                    }
                }
                if (tmpTask != null) {
                    tmpTask.setStatus(bTask.getStatus());
                    tmpTask.setFinishTime(bTask.getFinishTime());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, BackupService.class), mBackupConn, Context.BIND_AUTO_CREATE);
        //接受备份广播
        mBackupReceiver = new BackupReceiver();
        registerReceiver(mBackupReceiver, new IntentFilter(BackupService.BACKUP_RECEIVER_ACTION));
    }

    @Override
    public int getLayoutId() {
        return R.layout.act_backup;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        mDBTool = new DBTool(App.getAppContext());
        adapter = new BackupDirListAdapter(App.getAppContext());
        mListView.setAdapter(adapter);

    }

    @Override
    protected void initToolBar() {
        mToolBar.setLeftImageVisibility(View.VISIBLE);
        mToolBar.setLeftImage(R.drawable.selector_ic_menu_back);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
    }

    @Override
    public void initView() {
        mBackupDeviceName.setText("/移动设备/" + deviceName);
        if (PropCacheManager.getInstance().isBackupOn(App.getAppContext())) {
            mBackupStatus.setImageResource(R.mipmap.open);
        } else {
            mBackupStatus.setImageResource(R.mipmap.close);
        }
        //刷新界面，填入数据
        refresh(false);
    }

    @OnClick(R.id.backup_status_iv)
    public void backupStatus() {
        if (PropCacheManager.getInstance().isBackupOn(App.getAppContext())) {
            //关闭
            PropCacheManager.getInstance().setBackupStatus(PropCacheManager.BACKUP_OFF, App.getAppContext());
            mBackupStatus.setImageResource(R.mipmap.close);
            if (App.backupService != null) {
                App.backupService.off();
            }
        } else {
            PropCacheManager.getInstance().setBackupStatus(PropCacheManager.BACKUP_ON, App.getAppContext());
            mBackupStatus.setImageResource(R.mipmap.open);
            //开启,此处已修改了开关，无需进一步处理
        }
    }

    AlertDialog mDialog;
    /**
     * 全选全不选
     */
    boolean ALL_SELECT = false;

    @OnClick(R.id.backup_add_backup_dir_rl)
    public void addBackupDir() {
        if (mDialog == null) {
            LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = lay.inflate(R.layout.dialog_backup_dir_select, null);
            tvTitle = (TextView) v.findViewById(R.id.top);
            final Button btnSelAll = (Button) v.findViewById(R.id.dialog_backup_selAll_btn);
            Button btnCancel = (Button) v.findViewById(R.id.dialog_backup_cancel_btn);
            final Button btnOk = (Button) v.findViewById(R.id.dialog_backup_ok_btn);
            FrameLayout flContent = (FrameLayout) v.findViewById(R.id.dialog_backup_content_fl);
            mCheckableFolderListView = new CheckableFolderListView(this, rootPath, new CheckableFolderListView.onSelectedListener() {
                @Override
                public void execute(int selectedCount) {
                    if (selectedCount > 0) {
                        btnOk.setText("确定(" + selectedCount + ")");
                    } else {
                        btnOk.setText("确定");
                    }
                }
            });
            btnSelAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ALL_SELECT = !ALL_SELECT;
                    if (ALL_SELECT) {
                        int size = mCheckableFolderListView.setAllSelect();
                        if (size > 0) {
                            btnSelAll.setText("非全选");
                            btnOk.setText("确定(" + size + ")");
                        }
                    } else {
                        mCheckableFolderListView.setAllNotSelect();
                        btnSelAll.setText("全选");
                        btnOk.setText("确定");
                    }
                    mCheckableFolderListView.notifyDataSetChanged();
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckableFolderListView.getCheckedFolderInfos().size() > 0) {
                        //添加备份目录进数据库
                        addBackupTasks2DB(mCheckableFolderListView.getCheckedFolderInfos());
                        mDialog.dismiss();
                        //刷新主备份界面,刷新完成后，通知backupService服务备份
                        refresh(true);
                        ToastUtil.showShort("备份目录已加入备份队列" + mCheckableFolderListView.getCheckedFolderInfos().size());
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
            mCheckableFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    USpaceFile fileInfo = mCheckableFolderListView.getFolderInfos().get(position);
                    String curPath = mCheckableFolderListView.getCurrentLocalPath();
                    if (fileInfo.isFolder() || fileInfo.isParent()) {
                        if (fileInfo.isParent()) {
                            curPath = curPath.equals("/") ? "/" : curPath.substring(0, curPath.lastIndexOf('/'));
                        } else {
                            curPath = fileInfo.getDiskPath();
                        }
                        mCheckableFolderListView.displayFolder(curPath);
                        tvTitle.setText(curPath);
                        btnOk.setText("确定");
                    }
                }
            });

            mCheckableFolderListView.setDivider(getResources().getDrawable(R.mipmap.divider_horizontal_timeline));
            mCheckableFolderListView.setDividerHeight(1);
            mCheckableFolderListView.setCacheColorHint(Color.TRANSPARENT);
            flContent.addView(mCheckableFolderListView, new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(v);
            mDialog = builder.create();
            mDialog.setCanceledOnTouchOutside(true);

            Window win = mDialog.getWindow();
            WindowManager.LayoutParams params = win.getAttributes(); // 获取对话框当前的参数值
            params.gravity = Gravity.CENTER;
            win.setAttributes(params);
        } else {
            mCheckableFolderListView.displayFolder(rootPath);
        }
        mDialog.show();
        tvTitle.setText(rootPath);
        //        if (window == null) {
        //            LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //            View v = lay.inflate(R.layout.backup_dir_select_popupwindow, null);
        //            wDirTv = (TextView) v.findViewById(R.id.top);
        //            wSelAllBtn = (Button) v.findViewById(R.id.selAll);
        //            wCancelBtn = (Button) v.findViewById(R.id.cancel);
        //            wSubmitBtn = (Button) v.findViewById(R.id.submit);
        //
        //            // 初始化listview，加载数据。
        //            wDirListview = new MyFolderListView(BackupActivity.this, wRootPath);
        //            wDirListview.setDivider(getResources().getDrawable(R.drawable.divider_horizontal_timeline));
        //            wDirListview.setDividerHeight(1);
        //            wDirListview.setCacheColorHint(Color.TRANSPARENT);
        //            wLinerLayout = (LinearLayout) v.findViewById(R.id.backup_fexplore_listview);
        //            wLinerLayout.addView(wDirListview, new WindowManager.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));
        //            wSelAllBtn.setOnClickListener(new View.OnClickListener() {
        //
        //                @Override
        //                public void onClick(View v) {
        //                    ALL_SELECT = !ALL_SELECT;
        //                    if (ALL_SELECT) {
        //                        int size = wDirListview.setAllSelect();
        //                        if (size > 0) {
        //                            wSelAllBtn.setText("非全选");
        //                            wSubmitBtn.setText("确定(" + size + ")");
        //                        }
        //                    } else {
        //                        wDirListview.setAllNotSelect();
        //                        wSelAllBtn.setText("全选");
        //                        wSubmitBtn.setText("确定");
        //                    }
        //                    wDirListview.notifyDataSetChanged();
        //                }
        //            });
        //            wSubmitBtn.setOnClickListener(new View.OnClickListener() {
        //                @Override
        //                public void onClick(View v) {
        //
        //                    if (wDirListview.getCheckedFolderInfos().size() > 0) {
        //                        //添加备份目录进数据库
        //                        addBackupTasks2DB(wDirListview.getCheckedFolderInfos());
        //                        window.dismiss();
        //                        //刷新主备份界面,刷新完成后，通知backupService服务备份
        //                        refresh(true);
        //                        toastMessage("备份目录已加入备份队列");
        //
        //                    } else {
        //                        window.dismiss();
        //                    }
        //                }
        //            });
        //            wCancelBtn.setOnClickListener(new View.OnClickListener() {
        //                @Override
        //                public void onClick(View v) {
        //                    window.dismiss();
        //                }
        //            });
        //
        //            window = new SelectDialog(ctx, v, R.style.dialog);
        //            window.setCanceledOnTouchOutside(true);
        //
        //            Window win = window.getWindow();
        //            WindowManager.LayoutParams params = win.getAttributes(); // 获取对话框当前的参数值
        //            params.gravity = Gravity.TOP;
        //            win.setAttributes(params);
        //        } else {
        //            wDirListview.toPath(wRootPath);
        //        }
        //        window.show();
        //        wDirTv.setText(wRootPath);
    }

    /**
     * 刷新界面
     *
     * @param isNeedNotify 是否需要通知backupService服务备份
     */
    private void refresh(boolean isNeedNotify) {
        Integer[] arrStatus = new Integer[]{1, 2, 3, 4, 8, 9, 10, 11, 12, 13};
        mRxManager.add(mPresenter.listBackupQueue(arrStatus, REQUEST_CODE_LIST_BACKUP_QUEUE));
        if (isNeedNotify) {
            //需要通知backService
            if (mBackupBind && App.backupService != null) {
                App.backupService.unlock();
            }
        }
    }

    @Override
    public void loadBefore(String requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_LIST_BACKUP_QUEUE:
                break;
            default:
                break;
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        switch (requestCode) {
            case REQUEST_CODE_LIST_BACKUP_QUEUE:
                adapter.setData((List<BackupTask>) result);
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
            case REQUEST_CODE_LIST_BACKUP_QUEUE:
                break;
            default:
                break;
        }
    }

    /**
     * 添加备份任务到数据库
     *
     * @param selFolders
     */
    private void addBackupTasks2DB(Set<USpaceFile> selFolders) {
        List<BackupTask> existTasks = mDBTool.listBackupQueue(ITransferConstants.STATUS_WAIT, ITransferConstants.STATUS_RUN);
        List<BackupTask> newTasks = new ArrayList<>();
        for (USpaceFile uf : selFolders) {
            BackupTask bTask = new BackupTask();
            bTask.setStatus(ITransferConstants.STATUS_WAIT);
            bTask.setTitle(getText(R.string.str_backup_dir).toString());
            bTask.setLocalPath(uf.getDiskPath());
            bTask.setRemotePath(backupRemotePath);
            if (isExist(bTask, existTasks)) {
                continue;
            } else {
                newTasks.add(bTask);
            }
        }
        if (newTasks.size() > 0) {
            mDBTool.saveBackupTasks(newTasks);
        }
    }

    private boolean isExist(BackupTask backupTask, List<BackupTask> existTasks) {
        for (BackupTask task : existTasks) {
            if (task.getLocalPath().equals(backupTask.getLocalPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackupBind) {
            unbindService(mBackupConn);
            LogUtil.i("unbinded backupService");
        }
        if (mBackupReceiver != null)
            unregisterReceiver(mBackupReceiver);
    }
}
