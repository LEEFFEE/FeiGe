package cn.leeffee.feige.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cn.leeffee.feige.R;


/**
 * Created by lhfei on 2017/4/6.
 */

public class RenameMkDirAlertDialog extends AlertDialog {
    private Context mContext;
    private View contentView;
    TextView tvTitle;
    EditText etFileName;
    Button btnOk;
    Button btnCancel;

    public RenameMkDirAlertDialog(@NonNull Context context, String title) {
        super(context);
        this.mContext = context;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_uspace_alert, null);
        initView(title);
        initListener();
    }

    private void initView(String title) {
        tvTitle = (TextView) contentView.findViewById(R.id.dialog_title_tv);
        etFileName = (EditText) contentView.findViewById(R.id.dialog_filename_et);
        btnOk = (Button) contentView.findViewById(R.id.dialog_ok_btn);
        btnCancel = (Button) contentView.findViewById(R.id.dialog_cancel_btn);

        tvTitle.setText(title);
        setCancelable(true);
        setView(contentView);
        setCanceledOnTouchOutside(true);
    }

    private void initListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RenameMkDirAlertDialog.this.dismiss();
            }
        });
    }

    public String getFileName() {
        return etFileName.getText().toString();
    }

    public void setFileName(String fileName) {
        etFileName.setText(fileName);
    }

    public void setOkClickListener(View.OnClickListener listener) {
        btnOk.setOnClickListener(listener);
    }

    public void setSelection(int start, int stop) {
        etFileName.setSelection(start, stop);
    }
}
