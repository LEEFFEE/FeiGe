package cn.leeffee.feige.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.leeffee.feige.R;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.manager.RxManager;

/**
 * Created by lhfei on 2017/3/28.
 */

public abstract class BasicActivity extends AppCompatActivity {
    public Context mContext;
    Unbinder bind;
    public RxManager mRxManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRxManager = new RxManager();
        doBeforeSetContentView();
        setContentView(getLayoutId());
        bind = ButterKnife.bind(this);
        mContext = this;
        onBasicLoad(savedInstanceState);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.uspace_colorPrimary);
    }

    /*********************
     * 子类实现
     *****************************/
    //获取布局文件
    public abstract int getLayoutId();

    //基本activity加载完成时执行
    public abstract void onBasicLoad(@Nullable Bundle savedInstanceState);


    /**
     * 设置layout前配置
     */
    private void doBeforeSetContentView() {
        //设置昼夜主题
        // initTheme();
        // 把actvity放到application栈中管理
        AppManager.getAppManager().addActivity(this);
        // 无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 默认着色状态栏
        // SetStatusBarColor();

    }
    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /**
     * 通过Class跳转界面
     **/
    public void startActivityForResult(Class<?> cls, int requestCode) {
        startActivityForResult(cls, null, requestCode);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxManager.clear();
        if (bind != null)
            bind.unbind();
        AppManager.getAppManager().finishActivity(this);
    }
}
