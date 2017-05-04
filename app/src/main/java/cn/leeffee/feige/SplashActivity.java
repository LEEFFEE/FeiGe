package cn.leeffee.feige;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.OnClick;
import cn.leeffee.feige.base.BasicActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.activity.LoginActivity;
import cn.leeffee.feige.utils.ToastUtil;

public class SplashActivity extends BasicActivity {

    Class mClass;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public void onBasicLoad(@Nullable Bundle savedInstanceState) {

    }

    @OnClick({R.id.splash_cloud, R.id.splash_follow, R.id.splash_news, R.id.splash_player, R.id.splash_shop})
    public void checkChange(View v) {
        switch (v.getId()) {
            case R.id.splash_cloud:
                mClass = LoginActivity.class;
                break;
            case R.id.splash_follow:
                mClass = cn.leeffee.feige.ui.follow.activity.MainActivity.class;
                break;
            case R.id.splash_news:
                break;
            case R.id.splash_player:
                break;
            case R.id.splash_shop:
                break;
        }
    }

    @OnClick(R.id.splash_start)
    public void start() {
        if (mClass != null) {
            startActivity(mClass);
            AppManager.getAppManager().finishActivity();
        } else {
            ToastUtil.showShort("请选择你要进入的频道");
        }
    }
}
