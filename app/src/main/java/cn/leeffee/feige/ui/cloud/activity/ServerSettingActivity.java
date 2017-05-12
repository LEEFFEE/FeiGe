package cn.leeffee.feige.ui.cloud.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BasicActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.api.ApiClient;
import cn.leeffee.feige.ui.cloud.api.HostType;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.utils.PropertyUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.utils.ValidationUtil;
import cn.leeffee.feige.widget.USpaceEditText;
import cn.leeffee.feige.widget.USpaceToolBar;

public class ServerSettingActivity extends BasicActivity {

    @BindView(R.id.server_host_et)
    USpaceEditText etServerHost;
    @BindView(R.id.server_port_et)
    EditText etServerPort;
    @BindView(R.id.server_setting_toolbar)
    USpaceToolBar mToolBar;
    @BindView(R.id.server_setting_rg)
    RadioGroup mGroup;

    @Override
    public int getLayoutId() {
        return R.layout.act_server_setting;
    }

    @Override
    public void onBasicLoad(@Nullable Bundle savedInstanceState) {
        String server = PropertyUtil.getInstance().getServer();
        if (!TextUtils.isEmpty(server)) {
            etServerHost.setText(server);
        }
        String port = PropertyUtil.getInstance().getPort();
        if (!TextUtils.isEmpty(port)) {
            etServerPort.setText(port);
        }
        mToolBar.setLeftImage(R.drawable.selector_ic_menu_back);
        mToolBar.setLeftImageVisibility(View.VISIBLE);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
    }

    @OnClick({R.id.server_setting_ip_rb, R.id.server_setting_domain_rb})
    public void radioChange(View v) {
        switch (v.getId()) {
            case R.id.server_setting_ip_rb:
                etServerPort.setEnabled(true);
                etServerHost.setHint("如：172.16.60.202");
                break;
            case R.id.server_setting_domain_rb:
                etServerHost.setText("");
                etServerHost.setHint("如：www.baidu.com");
                etServerPort.setEnabled(false);
                etServerPort.setText("80");
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.ok_btn)
    public void ok(View view) {
        String serverHost = etServerHost.getText().toString().trim();
        String serverPort = etServerPort.getText().toString().trim();
        if (TextUtils.isEmpty(serverHost)) {
            ToastUtil.showShort("服务器地址不能为空!");
        } else if (TextUtils.isEmpty(serverPort)) {
            ToastUtil.showShort("端口号不能为空!");
        } else if (!ValidationUtil.validatePort(Integer.parseInt(serverPort))) {
            ToastUtil.showShort("端口号非法!");
        } else {
            if (mGroup.getCheckedRadioButtonId() == R.id.server_setting_ip_rb && !ValidationUtil.validateIpAddress(serverHost)) {
                ToastUtil.showShort("IP地址不合法");
                return;
            }
            if (mGroup.getCheckedRadioButtonId() == R.id.server_setting_domain_rb && !ValidationUtil.validateDomain(serverHost)) {
                ToastUtil.showShort("域名不合法");
                return;
            }
            SPUtil.putString(AppConfig.SERVER, serverHost);
            SPUtil.putString(AppConfig.PORT, serverPort);
            // PropertyUtil.setServer(serverHost);
            //删除ApiClient对应的实例
            ApiClient.delete(HostType.HOST_USPACE);
            AppManager.getAppManager().finishActivity();
        }
    }
}
