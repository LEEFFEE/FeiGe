package cn.leeffee.feige.ui.cloud.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.ActUploadFileExploreContract;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.model.ActUploadFileExploreModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActUploadFileExplorePresenterImpl;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.CheckableFileListView;
import cn.leeffee.feige.widget.USpaceToolBar;


public class UploadFileExploreActivity extends BaseActivity<ActUploadFileExplorePresenterImpl, ActUploadFileExploreModelImpl> implements ActUploadFileExploreContract.View, AdapterView.OnItemClickListener {

    /**
     * 根路径
     */
    private static String rootPath = "/sdcard";

    /**
     * 上传网盘的那个路径
     */
    private String currentRemotePath;
    /**
     * 是否是群组中的上传
     */
    private boolean isGroupUpload = false;
    /**
     * 上传目录前缀显示文本
     */
    private String prefix = "上传目录：/我的云盘";
    /**
     * 选择的类型  音乐 视频 文档 所有
     */
    private int type;
    @BindView(R.id.upload_file_explore_fl)
    FrameLayout mFrameLayout;//用来装载 可选择的FileListView
    @BindView(R.id.upload_file_explore_current_local_path_tv)
    TextView tvCurrentLocalPath;//当前的路径

    //    @BindView(R.id.upload_file_explore_home_iv)
    //    ImageView ivHome;//根目录
    //    @BindView(R.id.upload_file_explore_go_back_iv)
    //    ImageView ivGoBack;//返回  后退

    @BindView(R.id.upload_file_explore_toolbar)
    USpaceToolBar mToolBar;

    @BindView(R.id.upload_file_explore_upload_path_tv)
    TextView tvUploadPath;//上传到云盘的路径

    @BindView(R.id.upload_file_explore_upload_menus)
    LinearLayout mUploadMenus;//底部上传菜单
    @BindView(R.id.upload_file_explore_change_path_btn)
    Button btnChangePath;//更改要上传到网盘的哪个目录
    @BindView(R.id.upload_file_explore_select_all_btn)
    Button btnSelectAll;//全选
    @BindView(R.id.upload_file_explore_upload_btn)
    Button btnUpload;//上传

    /**
     * 可选择的FileListView
     */
    private CheckableFileListView mFileListView;

    /*=======================对话框相关=============================*/
    /**
     * 更改要上传到网盘哪个目录的对话框
     */
    AlertDialog mDialog;
    /**
     * 对话框中的 要上传到的目录
     */
    TextView tvRemoteDir;
    /**
     * 对话框中的要更改的路径,最终选定后赋值给currentRemotePath
     */
    private String destPath;
    /**
     * 加载前进度显示
     */
    LinearLayout mLoadingLayout;
    /**
     * 更改目录对话框中是适配器
     */
    DirListAdapter dirAdapter;

    public static final String REQUEST_CODE_LISTDIRONLYDIR = "listDirOnlyDir";

    @Override
    public int getLayoutId() {
        return R.layout.act_upload_file_explore;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        Intent intent = getIntent();
        currentRemotePath = intent.getStringExtra(AppConstants.CURRENT_REMOTE_PATH);
        type = intent.getIntExtra(AppConstants.UPLOAD_TYPE, 0);
        String _rootPath = intent.getStringExtra(AppConstants.BUNDLE_KEY_ROOT_PATH);
        if (_rootPath != null) {
            rootPath = _rootPath;
        }
        isGroupUpload = intent.getBooleanExtra(AppConstants.IS_GROUP_UPLOAD, false);
    }

    @Override
    protected void initToolBar() {
        mToolBar.setLeftImageVisibility(View.VISIBLE);
        mToolBar.setLeftImage(R.mipmap.ic_menu_back);
        //跳转到上一级
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileListView.isRoot()) {
                    UploadFileExploreActivity.this.finish();
                    AppManager.getAppManager().finishActivity();
                } else {
                    String curPath = mFileListView.getCurrentLocalPath();
                    String path = curPath.substring(0, curPath.lastIndexOf('/'));
                    display(path);
                }
            }
        });
    }

    @Override
    public void initView() {
        mFileListView = new CheckableFileListView(this, rootPath, type);
        mFileListView.setDivider(getResources().getDrawable(R.mipmap.divider_horizontal_timeline));
        mFileListView.setDividerHeight(1);
        mFileListView.setCacheColorHint(Color.TRANSPARENT);
        mFileListView.setOnItemClickListener(this);
        mFrameLayout.addView(mFileListView);
        // mFrameLayout.addView(mFileListView, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tvCurrentLocalPath.setText(rootPath);
    }
    //    /**
    //     * 回到根目录
    //     */
    //    @OnClick(R.id.upload_file_explore_home_iv)
    //    public void goHome(View v) {
    //        if (mFileListView.isRoot()) {
    //            this.finish();
    //        } else {
    //            String curPath = mFileListView.getCurrentLocalPath();
    //            String path = curPath.substring(0, curPath.lastIndexOf('/'));
    //            display(path);
    //        }
    //    }

    /**
     * 全选或者全不选
     */
    private boolean allSelect = false;

    @OnClick(R.id.upload_file_explore_select_all_btn)
    public void selectAll(View v) {
        allSelect = !allSelect;
        if (allSelect) {
            int size = mFileListView.setAllSelect();
            if (size > 0) {
                btnSelectAll.setText("全不选");
                btnUpload.setText("上传(" + size + ")");
            }
        } else {
            mFileListView.setAllNotSelect();
            btnSelectAll.setText("全选");
            btnUpload.setText("上传");
        }
        mFileListView.notifyDataSetChanged();
    }

    /**
     * 选中条目后上传
     *
     * @param v
     */
    @OnClick(R.id.upload_file_explore_upload_btn)
    public void upload(View v) {
        if (mFileListView.getCheckedFileInfos().size() > 0) {
            Set<FileInfo> fileset = mFileListView.getCheckedFileInfos();
            Intent intent = getIntent();
            Bundle bundle = new Bundle();
            FileInfo[] files = new FileInfo[fileset.size()];
            bundle.putSerializable(AppConstants.BUNDLE_KEY_UPLOAD_FILES, fileset.toArray(files));
            intent.putExtras(bundle);
            intent.putExtra("currentRemotePath", currentRemotePath);
            this.setResult(RESULT_OK, intent);
            AppManager.getAppManager().finishActivity();
        } else {
            ToastUtil.showShort("请选择要上传的文件");
        }
    }

    /**
     * 更改要上传到服务器的哪个目录
     */
    @OnClick(R.id.upload_file_explore_change_path_btn)
    public void changeDir() {
        destPath = "/";
        if (mDialog == null) {
            LayoutInflater Inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = Inflater.inflate(R.layout.dialog_change_dir, null);

            mLoadingLayout = (LinearLayout) v.findViewById(R.id.loading_layout);
            tvRemoteDir = (TextView) v.findViewById(R.id.dialog_title_dir_tv);
            // 初始化按钮
            v.findViewById(R.id.dialog_ok_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentRemotePath = destPath;
                    tvUploadPath.setText(prefix + currentRemotePath);
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
            dirAdapter = new DirListAdapter(mContext);
            dirListView.setAdapter(dirAdapter);
            dirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    USpaceFile fileInfo = (USpaceFile) dirAdapter.getItem(position);
                    String path = fileInfo.getDiskPath();
                    String dir = "/我的云盘";
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
                    destPath = path;
                    mPresenter.listDirOnlyDir(destPath, REQUEST_CODE_LISTDIRONLYDIR);
                }
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
        mPresenter.listDirOnlyDir("/", REQUEST_CODE_LISTDIRONLYDIR);
        //  new ListDirOnlyDirTask(UploadFileExploreActivity.this).execute("/");
    }

    /**
     * 展示当前本地路径下的内容
     *
     * @param currentLocalPath 当前路径
     */
    private void display(String currentLocalPath) {
        mFileListView.setCurrentLocalPath(currentLocalPath);
        mFileListView.setFileInfos(mFileListView.listFileInfos(currentLocalPath, type));
        if (!rootPath.equals(currentLocalPath)) {
            mFileListView.getFileInfos().add(0, new FileInfo("返回上一级", 0, false, true, "", null));
            mToolBar.setLeftImageVisibility(View.GONE);
        } else {
            mToolBar.setLeftImageVisibility(View.VISIBLE);
        }
        mFileListView.notifyDataSetChanged();
        tvCurrentLocalPath.setText(currentLocalPath);
        tvUploadPath.setText(prefix + currentRemotePath);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo fileInfo = mFileListView.getFileInfos().get(position);
        if (!fileInfo.isFile) {
            String currentLocalPath = mFileListView.getCurrentLocalPath();
            if (fileInfo.isParent) {
                currentLocalPath = currentLocalPath.equals("/") ? "/" : currentLocalPath.substring(0, currentLocalPath.lastIndexOf('/'));
            } else {
                currentLocalPath = fileInfo.filepath;
            }
            display(currentLocalPath);
        } else {
            CheckBox checkBox = ((CheckBox) view.findViewById(R.id.file_explorer_file_cbox));
            checkBox.setChecked(!checkBox.isChecked());
            if (checkBox.isChecked()) {
                mFileListView.getCheckedFileInfos().add(fileInfo);
            } else {
                mFileListView.getCheckedFileInfos().remove(fileInfo);
            }
            int size = mFileListView.getCheckedFileInfos().size();
            if (size > 0) {
                btnUpload.setText("上传(" + size + ")");
            } else {
                btnUpload.setText("上传");
            }
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR)) {
            mLoadingLayout.setVisibility(View.GONE);
            ToastUtil.showShort(msg);
        }
    }

    @Override
    public void loadBefore(String requestCode) {
        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR)) {
            mLoadingLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR)) {
            dirAdapter.setData((List<USpaceFile>) result);
            dirAdapter.notifyDataSetChanged();
            mLoadingLayout.setVisibility(View.GONE);
        }
    }
}
