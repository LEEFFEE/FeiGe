package cn.leeffee.feige.ui.cloud.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.manager.UpdateManager;
import cn.leeffee.feige.ui.cloud.activity.AboutActivity;
import cn.leeffee.feige.ui.cloud.activity.BackupActivity;
import cn.leeffee.feige.ui.cloud.activity.CrashActivity;
import cn.leeffee.feige.ui.cloud.activity.FeedbackActivity;
import cn.leeffee.feige.ui.cloud.activity.LoginActivity;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.FragSettingContract;
import cn.leeffee.feige.ui.cloud.entity.ApiAccountProp;
import cn.leeffee.feige.ui.cloud.model.FragSettingModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragSettingPresenterImpl;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.PropCacheManager;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.ChoiceLocalDictionaryAlertDialog;
import cn.leeffee.feige.widget.MyProgressDialog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends BaseFragment<FragSettingPresenterImpl, FragSettingModelImpl> implements FragSettingContract.View {

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

    @BindView(R.id.more_crash_log_rl)
    RelativeLayout mCrashLog;

    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    private final static String REQUEST_CODE_GET_ACCOUNT_PROPERTY = "user_getAccountProperty";//

    private final static String REQUEST_CODE_LOCAL_LIST_DIR = "local_list_dir";//列出本地目录
    private final static String REQUEST_CODE_CLEAR_CACHE = "local_clear_cache";//清空缓存
    private final static String REQUEST_CODE_CHANGE_PASSWORD = "local_change_password";//更改密码
    /**
     * 版本更新管理
     */
    private UpdateManager updateMgr;

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_tab_setting;
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
        //        if (PropertyUtil.ENTERPRISE_USPACE.equals(versionType)) {
        //            btnSignIn.setVisibility(View.GONE);
        //        } else {
        //            btnSignIn.setVisibility(View.VISIBLE);
        //        }
        //  new LoadSpaceTask(SettingActivity.this).execute();
        mCrashLog.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        mRxManager.add(mPresenter.getAccountProperty(REQUEST_CODE_GET_ACCOUNT_PROPERTY));
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

    @OnClick(R.id.more_cache_dir_ll)
    public void choiceCacheDir() {
        final ChoiceLocalDictionaryAlertDialog dialog = new ChoiceLocalDictionaryAlertDialog(getActivity());
        dialog.setPrefix("修改至:");
        dialog.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = SPUtil.getString(AppConfig.ACCOUNT);
                String cacheDir = dialog.getDestPath() + File.separator + PropertyUtil.getInstance().getRoot() + File.separator + account;
                PropCacheManager.getInstance().setCacheDir(cacheDir, account, getContext());
                tvCacheDir.setText(dialog.getDestPath());
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @OnClick(R.id.more_feedback_rl)
    public void feedback() {
        startActivity(FeedbackActivity.class);
    }
    @OnClick(R.id.more_crash_log_rl)
    public void crashLog() {
        startActivity(CrashActivity.class);
    }

    @OnClick(R.id.more_backup_rl)
    public void backup() {
        startActivity(BackupActivity.class);
    }

    @OnClick(R.id.more_clear_cache_rl)
    public void clearCache() {
        new android.support.v7.app.AlertDialog.Builder(getActivity()).setTitle("提示").setMessage("您确认要清空缓存文件吗？").setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.clearCache(REQUEST_CODE_CLEAR_CACHE);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
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
     * 修改密码
     */
    @OnClick(R.id.more_change_password_rl)
    public void changePassword() {
        changePasswordDialog();
    }

    /**
     * 修改密码对话框
     */
    protected void changePasswordDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setCancelable(false);
        View view = View.inflate(getContext(), R.layout.dialog_change_password, null);
        final EditText mOldPassword = (EditText) view.findViewById(R.id.dialog_old_password_et);
        final EditText mNewPassword = (EditText) view.findViewById(R.id.dialog_new_password_et);
        final EditText mNewPasswordAgain = (EditText) view.findViewById(R.id.dialog_new_password_again_et);

        Button btn_dialog_ok = (Button) view.findViewById(R.id.dialog_ok_btn);
        Button btn_dialog_cancel = (Button) view.findViewById(R.id.dialog_cancel_btn);
        final AlertDialog dialog = b.create();
        btn_dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = mOldPassword.getText().toString().trim();
                String newPassword = mNewPassword.getText().toString().trim();
                String newPasswordAgain = mNewPasswordAgain.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    ToastUtil.showShort("原始密码不能为空");
                    return;
                } else if (TextUtils.isEmpty(newPassword)) {
                    ToastUtil.showShort("新密码不能为空");
                    return;
                } else if (!newPassword.equals(newPasswordAgain)) {
                    ToastUtil.showShort("两次输入的新密码不一致");
                    return;
                } else {
                    mRxManager.add(mPresenter.changePassword(oldPassword, newPassword, REQUEST_CODE_CHANGE_PASSWORD));
                    dialog.dismiss();
                }
            }
        });
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // viewSpacingLeft : 距离对话框左内边框的距离
        // viewSpacingTop : 距离对话框顶内边框的距离
        // viewSpacingRight :距离对话框右内边框的距离
        // viewSpacingBottom : 距离对话框底内边框的距离
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
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
        switch (requestCode) {
//            case REQUEST_CODE_LOCAL_LIST_DIR:
//                changeDirAlertDialog.showLoading();
//                break;
            case REQUEST_CODE_CLEAR_CACHE:
                break;
            case REQUEST_CODE_CHANGE_PASSWORD:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        switch (requestCode) {
//            case REQUEST_CODE_LOCAL_LIST_DIR:
//                List<USpaceFile> data = (List<USpaceFile>) result;
//                dirAdapter.getData().addAll(data);
//                // mDirListAdapter.setData(data);
//                dirAdapter.notifyDataSetChanged();
//                changeDirAlertDialog.hideLoading();
//                break;
            case REQUEST_CODE_GET_ACCOUNT_PROPERTY:
                ApiAccountProp prop = (ApiAccountProp) result;
                String usedStr = StringUtil.getFileSize(prop.getUsedSpace());//已经使用空间
                String totalStr = StringUtil.getFileSize(prop.getHardLimit() * 1024 * 1024);//总空间
                String space = MessageFormat.format(getString(R.string.tmpl_space), new Object[]{usedStr, totalStr});
                tvSpace.setText(space);
                if ("lvhf@uit.com.cn".equalsIgnoreCase(prop.getEmail())) {
                    mCrashLog.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_CODE_CLEAR_CACHE:
                ToastUtil.showShort((String) result);
            case REQUEST_CODE_CHANGE_PASSWORD:
                ToastUtil.showShort((String) result);
                mLoadingLayout.setVisibility(View.GONE);
            default:
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
//            case REQUEST_CODE_LOCAL_LIST_DIR:
//                changeDirAlertDialog.hideLoading();
//                ToastUtil.showShort(msg);
//                break;
            case REQUEST_CODE_GET_ACCOUNT_PROPERTY:
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CLEAR_CACHE:
                ToastUtil.showShort(msg);
                break;
            case REQUEST_CODE_CHANGE_PASSWORD:
                ToastUtil.showShort(msg);
                mLoadingLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
