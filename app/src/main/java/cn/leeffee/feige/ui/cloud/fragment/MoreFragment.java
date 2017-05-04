package cn.leeffee.feige.ui.cloud.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.SplashActivity;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.activity.AboutActivity;
import cn.leeffee.feige.ui.cloud.activity.BackupActivity;
import cn.leeffee.feige.ui.cloud.activity.FeedbackActivity;
import cn.leeffee.feige.ui.cloud.activity.LoginActivity;
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.FragMoreContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.model.FragMoreModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragMorePresenterImpl;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.PropCacheManager;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.ChangeDirAlertDialog;
import cn.leeffee.feige.widget.MyProgressDialog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoreFragment#} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends BaseFragment<FragMorePresenterImpl, FragMoreModelImpl> implements FragMoreContract.View {

    @BindView(R.id.more_sign_in_btn)
    Button btnSignIn;//签到
    @BindView(R.id.more_account_tv)
    TextView tvAccount;//账户
    @BindView(R.id.more_space_tv)
    TextView tvSpace;//空间
    @BindView(R.id.more_wifi_cb)
    CheckBox cbWifi;//仅仅在WiFi环境下下载、上传、备份
    @BindView(R.id.more_small_files_tips_cb)
    CheckBox cbSmallFilesTips;//小文件下载是否提示
    @BindView(R.id.more_cache_dir_tv)
    TextView tvCacheDir;
    private final static String REQUEST_CODE_GET_ACCOUNT_PROPERTY = "user_getAccountProperty";

    private final static String REQUEST_CODE_LOCAL_LIST_DIR = "local_list_dir";//列出本地目录

    /**
     * 版本更新管理
     */
    // private UpdateManager updateMgr;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_more;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected void initView() {
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        tvAccount.setText(account);
        boolean isWifiOn = PropCacheManager.getInstance().isWifiOn(App.getAppContext());
        if (isWifiOn) {
            cbWifi.setChecked(true);
        } else {
            cbWifi.setChecked(false);
        }
        String dir = FileUtil.getUserRoot();
        tvCacheDir.setText(dir);
        btnSignIn.setVisibility(View.GONE);//签到领取空间
        //        String versionType = PropertyUtil.getInstance().getProperty("uspace.version.type");
        //
        //        if (PropertyUtil.ENTERPRISE_USPACE.equals(versionType)) {
        //            btnSignIn.setVisibility(View.GONE);
        //        } else {
        //            btnSignIn.setVisibility(View.VISIBLE);
        //        }
        //  new LoadSpaceTask(SettingActivity.this).execute();
        mPresenter.getAccountProperty(REQUEST_CODE_GET_ACCOUNT_PROPERTY);
    }

    @OnClick(R.id.more_wifi_cb)
    public void wifiClick() {
        if (cbWifi.isChecked()) {
            PropCacheManager.getInstance().setWifiStatus(PropCacheManager.WIFI_ON, App.getAppContext());
        } else {
            PropCacheManager.getInstance().setWifiStatus(PropCacheManager.WIFI_OFF, App.getAppContext());
        }
    }

    @OnClick(R.id.more_small_files_tips_cb)
    public void smallFilesTipsClick() {
        if (cbSmallFilesTips.isChecked()) {
            SPUtil.putBoolean(AppConfig.DOWNLOAD_CONFIRM, true);
        } else {
            SPUtil.putBoolean(AppConfig.DOWNLOAD_CONFIRM, false);
        }
    }

    /**
     * 更改目录对话框
     */
    ChangeDirAlertDialog changeDirAlertDialog;
    /**
     * 当前本地路径
     */
    String currentLocalPath;
    /**
     * 更改目录对话框适配器
     */
    DirListAdapter dirAdapter;

    private final String CHANGE_DIR_TIP = "修改缓存目录至：";

    private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();

    @OnClick(R.id.more_cache_dir_ll)
    public void cacheClick() {
        //currentLocalPath = "/sdcard";
        currentLocalPath = SDCARD;
        if (changeDirAlertDialog == null) {
            changeDirAlertDialog = new ChangeDirAlertDialog(getActivity());
            dirAdapter = new DirListAdapter(App.getAppContext());
            changeDirAlertDialog.setDataAdapter(dirAdapter);
            changeDirAlertDialog.setOkClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String account = SPUtil.getString(AppConfig.ACCOUNT);
                    PropCacheManager.getInstance().setCacheDir(currentLocalPath, account, getContext());
                    tvCacheDir.setText(currentLocalPath);
                    changeDirAlertDialog.dismiss();
                }
            });
            changeDirAlertDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    USpaceFile fileInfo = (USpaceFile) dirAdapter.getItem(position);
                    currentLocalPath = fileInfo.getDiskPath();
                    if (fileInfo.isParent()) {
                        currentLocalPath = currentLocalPath.substring(0, currentLocalPath.lastIndexOf('/'));
                        if ("".equals(currentLocalPath)) {
                            currentLocalPath = SDCARD;
                        }
                    }
                    dirAdapter.getData().clear();
                    if (!currentLocalPath.equals(SDCARD)) {
                        dirAdapter.getData().add(0, new USpaceFile(App.getAppResources().getText(R.string.strBackToParent).toString(), 0, false, true, currentLocalPath, false));
                    }
                    changeDirAlertDialog.setContentTitle(CHANGE_DIR_TIP + currentLocalPath);
                    mRxManager.add(mPresenter.listLocalDir(currentLocalPath, REQUEST_CODE_LOCAL_LIST_DIR));
                }
            });
        }
        changeDirAlertDialog.setContentTitle(CHANGE_DIR_TIP + "/我的文件");
        mRxManager.add(mPresenter.listLocalDir(currentLocalPath, REQUEST_CODE_LOCAL_LIST_DIR));
        changeDirAlertDialog.show();
    }

    @OnClick(R.id.more_feedback_rl)
    public void feedback() {
        startActivity(new Intent(App.getAppContext(), FeedbackActivity.class));
    }

    @OnClick(R.id.more_backup_rl)
    public void backup() {
        startActivity(new Intent(App.getAppContext(), BackupActivity.class));
    }

    /**
     * 检查更新客户端信息
     */
    MyProgressDialog pDialog;

    @OnClick(R.id.more_check_update_btn)
    public void checkUpdate() {
        //
        //        if (updateMgr == null) {
        //            updateMgr = new UpdateManager(getActivity());
        //        }
        //        if (pDialog == null) {
        //            String title = getString(R.string.str_version);
        //            String message = getString(R.string.msg_check_version);
        //            pDialog = new MyProgressDialog(getActivity());
        //            pDialog.setTitle(title);
        //            pDialog.setMessage(message);
        //        }
        //
        //        updateMgr.checkUpdate(false);
        //        pDialog.dismiss();

        /*请求及响应信息*/
        //  /uspace/system
        //jsonParams={method:"getLatestClient",params:{type:1}}
        //  {"errorCode":0,"errorMessage":null,"latestClient":"success","result":null}
        ToastUtil.showShort("没有检查到新版本！");
    }

    /**
     * 关于软件
     */
    @OnClick(R.id.more_about_rl)
    public void about() {
        startActivity(AboutActivity.class);
    }

    /**
     * 回到首页
     */
    @OnClick(R.id.more_go_splash_rl)
    public void goSplash() {
        startActivity(SplashActivity.class);
        AppManager.getAppManager().finishActivity();
    }

    /**
     * 注销
     */
    @OnClick(R.id.more_logout_rl)
    public void logout() {
        new AlertDialog.Builder(getActivity()).setMessage(R.string.msg_confirm_logout).setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent1 = new Intent(getActivity(), LoginActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                AppManager.getAppManager().finishAllActivity();
                CommonUtil.stopTransferService(App.getAppContext());
                String loginServer = SPUtil.getString(AppConfig.SERVER);
                String loginAccount = SPUtil.getString(AppConfig.ACCOUNT);
                mPresenter.destroyLoginInfo(true);
                SPUtil.putString(AppConfig.SERVER, loginServer);
                SPUtil.putString(AppConfig.ACCOUNT, loginAccount);
                getActivity().finish();
                startActivity(intent1);
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void loadBefore(String requestCode) {
        if (REQUEST_CODE_LOCAL_LIST_DIR.equals(requestCode)) {
            changeDirAlertDialog.showLoading();
        }

    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (REQUEST_CODE_GET_ACCOUNT_PROPERTY.equals(requestCode)) {
            ApiAccountProp prop = (ApiAccountProp) result;
            String usedStr = StringUtil.getFileSize(prop.getUsedSpace());//已经使用空间
            String totalStr = StringUtil.getFileSize(prop.getHardLimit() * 1024 * 1024);//总空间
            String space = MessageFormat.format(getString(R.string.tmpl_space), new Object[]{usedStr, totalStr});
            tvSpace.setText(space);
        } else if (REQUEST_CODE_LOCAL_LIST_DIR.equals(requestCode)) {
            List<USpaceFile> data = (List<USpaceFile>) result;
            dirAdapter.getData().addAll(data);
            // mDirListAdapter.setData(data);
            dirAdapter.notifyDataSetChanged();
            changeDirAlertDialog.hideLoading();
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (REQUEST_CODE_GET_ACCOUNT_PROPERTY.equals(requestCode)) {
            ToastUtil.showShort(msg);
        } else if (REQUEST_CODE_LOCAL_LIST_DIR.equals(requestCode)) {
            changeDirAlertDialog.hideLoading();
            ToastUtil.showShort(msg);
        }
    }

    //    class DirListAdapter extends BaseAdapter {
    //        private class ViewHolder {
    //            ImageView icon;
    //            TextView fnameText;
    //        }
    //
    //        private Context context;
    //
    //        private LayoutInflater inflater = null;
    //
    //        private List<FileInfo> data = null;
    //
    //        public DirListAdapter(Context ctx) {
    //            this.context = ctx;
    //            inflater = LayoutInflater.from(context);
    //            data = new ArrayList<>();
    //        }
    //
    //        @Override
    //        public int getCount() {
    //            return data.size();
    //        }
    //
    //        @Override
    //        public Object getItem(int position) {
    //            return data.get(position);
    //        }
    //
    //        @Override
    //        public long getItemId(int position) {
    //            return position;
    //        }
    //
    //        public void setData(List<FileInfo> data) {
    //            this.data.clear();
    //            this.data.addAll(data);
    //        }
    //
    //        public List<FileInfo> getData() {
    //            return data;
    //        }
    //
    //        @Override
    //        public View getView(int position, View convertView, ViewGroup parent) {
    //            FileInfo fileInfo = data.get(position);
    //            ViewHolder holder;
    //            if (convertView == null) {
    //                convertView = inflater.inflate(R.layout.dir_list_item, null);
    //                holder = new ViewHolder();
    //                holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
    //                holder.fnameText = (TextView) convertView.findViewById(R.id.file_name);
    //                convertView.setTag(holder);
    //            } else {
    //                holder = (ViewHolder) convertView.getTag();
    //            }
    //            holder.icon.setImageResource(fileInfo.isParent ? R.mipmap.uspace_back_folder : R.mipmap.uspace_default_folder);
    //            // holder.icon.setImageResource(R.mipmap.uspace_default_folder);
    //            holder.fnameText.setText(fileInfo.filename);
    //
    //            return convertView;
    //        }
    //    }

}
