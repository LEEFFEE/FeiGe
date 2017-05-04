package cn.leeffee.feige.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import cn.leeffee.feige.R;


/**
 * Created by lhfei on 2017/4/7.
 */

public class ChangeDirAlertDialog extends AlertDialog {
    LinearLayout mLoadingLayout;
    TextView mTitle;
    Button btnOk;
    Button btnCancel;
    ListView mListView;

    public ChangeDirAlertDialog(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater lay = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = lay.inflate(R.layout.dialog_change_dir, null);
        mLoadingLayout = (LinearLayout) v.findViewById(R.id.loading_layout);
        mTitle = (TextView) v.findViewById(R.id.dialog_title_dir_tv);
        btnOk = (Button) v.findViewById(R.id.dialog_ok_btn);
        btnCancel = (Button) v.findViewById(R.id.dialog_cancel_btn);
        mListView = (ListView) v.findViewById(R.id.dialog_dir_list_lv);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDirAlertDialog.this.dismiss();
            }
        });
        setView(v);
        Window win = this.getWindow();
        WindowManager.LayoutParams params = win.getAttributes(); // 获取对话框当前的参数值
        params.gravity = Gravity.CENTER;
        win.setAttributes(params);
    }

    public void setOkClickListener(View.OnClickListener listener) {
        btnOk.setOnClickListener(listener);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        mListView.setOnItemClickListener(itemClickListener);
    }

    public void setContentTitle(String title) {
        mTitle.setText(title);
    }

    public String getContentTitle() {
        return mTitle.getText().toString();
    }

    public void setDataAdapter(BaseAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public void hideLoading() {
        mLoadingLayout.setVisibility(View.GONE);
    }

    public void showLoading() {
        mLoadingLayout.setVisibility(View.VISIBLE);
    }
}
