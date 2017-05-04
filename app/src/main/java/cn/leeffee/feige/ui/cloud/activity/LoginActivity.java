package cn.leeffee.feige.ui.cloud.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.contract.ActLoginContract;
import cn.leeffee.feige.ui.cloud.model.ActLoginModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActLoginPresenterImpl;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.utils.ToastUtil;

public class LoginActivity extends BaseActivity<ActLoginPresenterImpl, ActLoginModelImpl> implements ActLoginContract.View {

    @BindView(R.id.login_account_et)
    EditText loginAccountEt = null;
    @BindView(R.id.login_pwd_et)
    EditText passwordEt = null;

    private String loginAccount = null;
    private String password = null;
    ProgressDialog progressDialog;

    private static final String REQUEST_CODE_LOGIN = "REQUEST_CODE_LOGIN";

    @Override
    public int getLayoutId() {
        return R.layout.act_login;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    public void initView() {
        initData();
    }

    private void initData() {
        if (TextUtils.isEmpty(PropertyUtil.getServer())) {
            loginAccountEt.setText("");
            passwordEt.setText("");
            ToastUtil.showShort("请先设置服务器");
            return;
        }
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        loginAccountEt.setText(account);
        String password = StringUtil.decrypt(SPUtil.getString(AppConfig.PASSWORD));
        passwordEt.setText(password);
    }

    @OnClick(R.id.login_login_btn)
    public void loginClick(View v) {
        if (TextUtils.isEmpty(PropertyUtil.getServer())) {
            loginAccountEt.setText("");
            passwordEt.setText("");
            ToastUtil.showShort("请先设置服务器");
            return;
        }
        loginAccount = loginAccountEt.getText().toString();
        password = passwordEt.getText().toString();
        mPresenter.login(loginAccount, password, REQUEST_CODE_LOGIN);
    }

    //    public String login() throws Exception {
    //        String loginAccount = SPUtil.getString(AppConfig.ACCOUNT);
    //        String password = SPUtil.getString(AppConfig.PASSWORD);
    //        String json = "jsonParams={method:'userAuthentication',params:{abc:1, userName:'" + loginAccount + "',password:'" + password + "'}}";
    //        BaseResponse<String> res = OkHttpUtil.getInstance().<String>postSync(PropertyUtil.getInstance().getBaseUrl() + File.separator + ApiConstants.USER_URL, json);
    //        if (res.getErrorCode() == 0) {
    //            LogUtil.e(res.getResult());
    //            return res.getResult();
    //        }
    //        LogUtil.e(res.getErrorMessage());
    //        return null;
    //    }

    @OnClick(R.id.login_server_setting_tv)
    public void serverSetting(View v) {
        startActivity(new Intent(this, ServerSettingActivity.class));
    }


    @Override
    public void loadBefore(String requestCode) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            progressDialog = new ProgressDialog(mContext, R.style.dialog);
            progressDialog.setMessage(mContext.getText(R.string.logining));
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mPresenter.unSubscribe();
                }
            });
            progressDialog.show();
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            startActivity(new Intent(this, MainActivity.class));
            AppManager.getAppManager().finishActivity();
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            ToastUtil.showShort(msg);
        }
    }
}
