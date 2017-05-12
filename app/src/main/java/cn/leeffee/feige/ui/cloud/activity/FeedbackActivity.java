package cn.leeffee.feige.ui.cloud.activity;

import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.contract.ActFeedbackContract;
import cn.leeffee.feige.ui.cloud.model.ActFeedbackModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActFeedbackPresenterImpl;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.MyProgressDialog;
import cn.leeffee.feige.widget.USpaceToolBar;


public class FeedbackActivity extends BaseActivity<ActFeedbackPresenterImpl, ActFeedbackModelImpl> implements ActFeedbackContract.View {

    @BindView(R.id.feedback_toolbar)
    USpaceToolBar mToolBar;
    @BindView(R.id.feedback_et)
    EditText mEditText;

    public static final String REQUEST_CODE_ADD_SUGGESTION = "system_addSuggestion";

    @Override
    public int getLayoutId() {
        return R.layout.act_feedback;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected void initToolBar() {
        mToolBar.setLeftImage(R.drawable.selector_ic_menu_back);
        mToolBar.setLeftImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity();
            }
        });
        mToolBar.setRightImage(android.R.drawable.ic_menu_set_as);
        mToolBar.setRightImageOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditText.getText().toString();
                if (content == null || content.length() == 0) {
                    ToastUtil.showShort(getString(R.string.error_suggestion_content_empty));
                } else {
                    mRxManager.add(mPresenter.addSuggestion(content, REQUEST_CODE_ADD_SUGGESTION));
                    // new SendSuggestionTask(NewSuggestionActivity.this).execute();
                }
            }
        });
    }

    @Override
    public void initView() {

    }

    MyProgressDialog pDialog;

    @Override
    public void loadBefore(String requestCode) {
        if (REQUEST_CODE_ADD_SUGGESTION.equals(requestCode)) {
            if (pDialog == null) {
                pDialog = new MyProgressDialog(this);
            }
            final String strTitle = getString(R.string.strPostDialogTitle);
            final String strMsg = getString(R.string.strPostDialogBody);
            pDialog.setTitle(strTitle);
            pDialog.setMessage(strMsg);
            pDialog.show();
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (REQUEST_CODE_ADD_SUGGESTION.equals(requestCode)) {
            ToastUtil.showShort("发送成功！");
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            this.finish();
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (REQUEST_CODE_ADD_SUGGESTION.equals(requestCode)) {
            ToastUtil.showShort(msg);
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}
