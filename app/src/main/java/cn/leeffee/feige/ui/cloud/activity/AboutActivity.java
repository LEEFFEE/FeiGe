package cn.leeffee.feige.ui.cloud.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.MessageFormat;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.utils.DateUtil;

/**
 * Created by lhfei on 2017/4/10.
 */

public class AboutActivity extends BaseActivity {
    //    @BindView(R.id.about_toolbar)
    //    USpaceToolBar mToolBar;
    @BindView(R.id.about_back_iv)
    ImageView mBack;
    @BindView(R.id.about_version_tv)
    TextView mVersion;
    @BindView(R.id.about_copyright_tv)
    TextView mCopyright;
    @BindView(R.id.about_website_tv)
    TextView mWebsite;

    @Override
    public int getLayoutId() {
        return R.layout.act_about;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void initToolBar() {
        //        mToolBar.setLeftImageVisibility(View.VISIBLE);
        //        mToolBar.setLeftImage(R.mipmap.ic_menu_back);
        //        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                AboutActivity.this.finish();
        //            }
        //        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
    }

    @Override
    public void initView() {
        mCopyright.setText(MessageFormat.format(getString(R.string.str_copyright), DateUtil.getCurrentTime("yyyy")));
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = getString(R.string.app_name) + " " + info.versionName;
            mVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AboutActivity", e.getMessage(), e);
        }

        mWebsite.setVisibility(View.GONE);
        // String versionType = PropertyUtil.getInstance().getProperty("uspace.version.type");
        //
        //        if (Configuration.ENTERPRISE_USPACE.equals(versionType)) {
        //            mWebsite.setVisibility(View.GONE);
        //        } else {
        //            mWebsite.setVisibility(View.VISIBLE);
        //        }
    }
}
