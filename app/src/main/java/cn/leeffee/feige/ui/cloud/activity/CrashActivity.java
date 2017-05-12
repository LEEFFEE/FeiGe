package cn.leeffee.feige.ui.cloud.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BasicActivity;

/**
 * Created by lhfei on 2017/5/3.
 */

public class CrashActivity extends BasicActivity {
    @BindView(R.id.crash_log_tv)
    EditText mView;

    @Override
    public int getLayoutId() {
        return R.layout.act_crash;
    }

    @Override
    public void onBasicLoad(@Nullable Bundle savedInstanceState) {
        File file = new File(App.getAppContext().getFilesDir().getPath(), "uspace.log");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
            if (TextUtils.isEmpty(sb.toString().trim())) {
                mView.setText("没有崩溃日志记录");
            } else {
                mView.setText(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
