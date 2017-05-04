package cn.leeffee.feige.widget;


import android.app.ProgressDialog;
import android.content.Context;

import cn.leeffee.feige.R;


/**
 * Created by lhfei on 2017/3/31.
 */

public class MyProgressDialog extends ProgressDialog {
    public MyProgressDialog(Context context) {
        this(context, R.style.dialog);
    }

    public MyProgressDialog(Context context, int theme) {
        super(context, theme);
        setCancelable(true);
        setIndeterminate(true);
    }

    //    progressDialog = new ProgressDialog(mContext, dialog);
    //    progressDialog.setMessage(mContext.getText(R.string.logining));
    //    progressDialog.setCancelable(true);
    //    progressDialog.setIndeterminate(true);
    //    progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
    //        @Override
    //        public void onDismiss(DialogInterface dialog) {
    //            mPresenter.unSubscribe();
    //        }
    //    });
    //    progressDialog.show();

}
