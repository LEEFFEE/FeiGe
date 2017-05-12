package cn.leeffee.feige.ui.cloud.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.ActUploadFileExploreContract;
import cn.leeffee.feige.ui.cloud.entity.FileInfo;
import cn.leeffee.feige.ui.cloud.model.ActUploadFileExploreModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActUploadFileExplorePresenterImpl;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.CheckableFileListView;
import cn.leeffee.feige.widget.ChoiceRemoteDictionaryAlertDialog;
import cn.leeffee.feige.widget.USpaceToolBar;


public class UploadFileExploreActivity extends BaseActivity<ActUploadFileExplorePresenterImpl, ActUploadFileExploreModelImpl> implements ActUploadFileExploreContract.View, AdapterView.OnItemClickListener {

    /**
     * sd卡根路径
     */
    private static String sdCardRoot = FileUtil.getSDCardRoot();

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
    private String prefix = "上传到:";
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
            sdCardRoot = _rootPath;
        }
        isGroupUpload = intent.getBooleanExtra(AppConstants.IS_GROUP_UPLOAD, false);
        if (isGroupUpload) {
            prefix += App.getAppContext().getText(R.string.tab_group);
            btnChangePath.setEnabled(false);
        } else {
            prefix += App.getAppContext().getText(R.string.tab_cloud);
        }
    }

    @Override
    protected void initToolBar() {
        //        mToolBar.setLeftImageVisibility(View.VISIBLE);
        //        mToolBar.setLeftImage(R.drawable.selector_ic_menu_back);
        mToolBar.setRightText("取消");
        mToolBar.setRightControlOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
        //跳转到上一级
        //        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                if (mFileListView.isRoot()) {
        //                    UploadFileExploreActivity.this.finish();
        //                    AppManager.getAppManager().finishActivity();
        //                } else {
        //                    String curPath = mFileListView.getCurrentLocalPath();
        //                    String path = curPath.substring(0, curPath.lastIndexOf('/'));
        //                    display(path);
        //                }
        //            }
        //        });
    }

    @Override
    public void initView() {
        mFileListView = new CheckableFileListView(this, sdCardRoot, type);
        mFileListView.setDivider(getResources().getDrawable(R.mipmap.divider_horizontal_timeline));
        mFileListView.setDividerHeight(1);
        mFileListView.setCacheColorHint(Color.TRANSPARENT);
        mFileListView.setOnItemClickListener(this);
        mFrameLayout.addView(mFileListView);
        // mFrameLayout.addView(mFileListView, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tvCurrentLocalPath.setText(sdCardRoot);
        tvUploadPath.setText(prefix + currentRemotePath);
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
        final ChoiceRemoteDictionaryAlertDialog dialog = new ChoiceRemoteDictionaryAlertDialog(this);
        dialog.setPrefix(getText(R.string.tab_cloud).toString());
        dialog.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRemotePath = dialog.getDestPath();
                tvUploadPath.setText(prefix + currentRemotePath);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 展示当前本地路径下的内容
     *
     * @param currentLocalPath 当前路径
     */
    private void display(String currentLocalPath) {
        mFileListView.setCurrentLocalPath(currentLocalPath);
        mFileListView.setFileInfos(mFileListView.listFileInfos(currentLocalPath, type));
        if (!sdCardRoot.equals(currentLocalPath)) {
            mFileListView.getFileInfos().add(0, new FileInfo("返回上一级", 0, false, true, "", null));
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
                currentLocalPath = currentLocalPath.equals(sdCardRoot) ? sdCardRoot : currentLocalPath.substring(0, currentLocalPath.lastIndexOf('/'));
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
        //        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR_LOCAL)) {
        //            mLoadingLayout.setVisibility(View.GONE);
        //            ToastUtil.showShort(msg);
        //        }
    }

    @Override
    public void loadBefore(String requestCode) {
        //        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR_LOCAL)) {
        //            mLoadingLayout.setVisibility(View.VISIBLE);
        //        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        //        if (requestCode.equals(REQUEST_CODE_LISTDIRONLYDIR_LOCAL)) {
        //            dirAdapter.setData((List<USpaceFile>) result);
        //            dirAdapter.notifyDataSetChanged();
        //            mLoadingLayout.setVisibility(View.GONE);
        //        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String curPath = mFileListView.getCurrentLocalPath();
        if (keyCode == KeyEvent.KEYCODE_BACK && !curPath.equals(sdCardRoot)) {
            String path = curPath.substring(0, curPath.lastIndexOf('/'));
            display(path);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
