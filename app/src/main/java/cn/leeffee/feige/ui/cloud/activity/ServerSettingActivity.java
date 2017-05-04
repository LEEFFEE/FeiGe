package cn.leeffee.feige.ui.cloud.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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
import cn.leeffee.feige.widget.USpaceToolBar;

public class ServerSettingActivity extends BasicActivity {

    @BindView(R.id.server_host_et)
    EditText etServerHost;
    @BindView(R.id.server_port_et)
    EditText etServerPort;
    @BindView(R.id.server_setting_toolbar)
    USpaceToolBar mToolBar;

    @Override
    public int getLayoutId() {
        return R.layout.act_server_setting;
    }

    @Override
    public void onBasicLoad(@Nullable Bundle savedInstanceState) {
        String server = PropertyUtil.getServer();
        if (server != null && !server.equals("")) {
            etServerHost.setText(server);
        }
        etServerPort.setText("80");
        mToolBar.setLeftImage(R.mipmap.ic_menu_back);
        mToolBar.setLeftImageVisibility(View.VISIBLE);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
    }
    @OnClick(R.id.ok_btn)
    public void ok(View view) {
        String serverHost = etServerHost.getText().toString();
        if (TextUtils.isEmpty(serverHost)) {
            ToastUtil.showShort("服务器地址不能为空");
        } else {
            SPUtil.putString(AppConfig.SERVER, serverHost);
            PropertyUtil.setServer(serverHost);
            //删除ApiClient对应的实例
            ApiClient.delete(HostType.HOST_USPACE);
            finish();
        }
    }
}
