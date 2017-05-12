package cn.leeffee.feige.ui.cloud.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.listener.FragmentBackListener;
import cn.leeffee.feige.ui.cloud.adapter.MainFragmentAdapter;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.contract.ActMainContract;
import cn.leeffee.feige.ui.cloud.factory.FragmentFactory;
import cn.leeffee.feige.ui.cloud.fragment.TransFragment;
import cn.leeffee.feige.ui.cloud.model.ActMainModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActMainPresenterImpl;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.SPUtil;
import cn.leeffee.feige.utils.StringUtil;
import cn.leeffee.feige.widget.MyProgressDialog;
import cn.leeffee.feige.widget.ViewPagerFixed;

import static cn.leeffee.feige.ui.cloud.activity.LoginActivity.REQUEST_CODE_LOGIN;


public class MainActivity extends BaseActivity<ActMainPresenterImpl, ActMainModelImpl> implements ActMainContract.View {
    @BindView(R.id.main_rg)
    RadioGroup rgMain;

    @BindView(R.id.main_viewpager)
    ViewPagerFixed mViewPager;
    /**
     * 当前tab标签索引
     */
    private int currentPosition = 0;

    /**
     * Fragment回退键监听
     */
    private FragmentBackListener backListener;
    /**
     * 是否与Fragment交互
     */
    private boolean isInterception = false;

    private Bundle mSavedInstanceState;

    public FragmentBackListener getBackListener() {
        return backListener;
    }

    public void setBackListener(FragmentBackListener backListener) {
        this.backListener = backListener;
    }

    public boolean isInterception() {
        return isInterception;
    }

    public void setInterception(boolean isInterception) {
        this.isInterception = isInterception;
    }


    @Override
    public int getLayoutId() {
        return R.layout.act_cloud_main;
    }

    @Override
    public void onBasicLoad(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    public void initView() {
        MainFragmentAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);
        int position = getIntent().getIntExtra(AppConstants.POSITION_FRAGMENT, -1);
        if (position == AppConstants.POSITION_TRANS_FRAGMENT) {//通知跳转到文件传输界面
            int transType = getIntent().getIntExtra("taskType", TransFragment.DOWNLOAD);
            currentPosition = AppConstants.POSITION_TRANS_FRAGMENT;
            SwitchTo(position, transType);
        } else {
            String token = SPUtil.getString(AppConfig.TOKEN);
            if (StringUtil.isEmpty(token)) {
                String mUsername = SPUtil.getString(AppConfig.ACCOUNT);
                String mPassword = StringUtil.decrypt(SPUtil.getString(AppConfig.PASSWORD));
                if (!StringUtil.isEmpty(mUsername) && !StringUtil.isEmpty(mPassword)) {
                    mPresenter.login(REQUEST_CODE_LOGIN);
                } else {
                    // TODO
//                    ToastUtil.showShort("请先登录！");
                    //                    startActivity(LoginActivity.class);
                    //                    this.finish();
                }
            } else {
                if (mSavedInstanceState != null) {
                    currentPosition = mSavedInstanceState.getInt(AppConstants.POSITION_FRAGMENT);
                }
                loginSuccess();
            }
        }
    }

    /**
     * 登录成功
     */
    private void loginSuccess() {
        CommonUtil.startTransferService(App.getAppContext());
    }

    @OnClick({R.id.main_cloud_rb, R.id.main_group_rb, R.id.main_trans_rb, R.id.main_setting_rb})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.main_cloud_rb:
                currentPosition = AppConstants.POSITION_CLOUD_FRAGMENT;
                break;
            case R.id.main_group_rb:
                currentPosition = AppConstants.POSITION_GROUP_FRAGMENT;
                break;
            case R.id.main_trans_rb:
                currentPosition = AppConstants.POSITION_TRANS_FRAGMENT;
                break;
            case R.id.main_setting_rb:
                currentPosition = AppConstants.POSITION_SETTING_FRAGMENT;
                break;
            default:
                break;
        }
        mViewPager.setCurrentItem(currentPosition, false);
    }

    /**
     * 切换页签
     *
     * @param position
     * @param type     附加字段  如某个fragment中需要选中某个page页
     */
    private void SwitchTo(int position, int type) {
        switch (position) {
            case AppConstants.POSITION_CLOUD_FRAGMENT:
                rgMain.check(R.id.main_cloud_rb);
                break;
            case AppConstants.POSITION_GROUP_FRAGMENT:
                rgMain.check(R.id.main_group_rb);
                break;
            case AppConstants.POSITION_TRANS_FRAGMENT://文件传输
                rgMain.check(R.id.main_trans_rb);
                TransFragment fragment = (TransFragment) FragmentFactory.createFragment(AppConstants.POSITION_TRANS_FRAGMENT);
                fragment.setCurrentPage(type);//TransFragment.DOWNLOAD;TransFragment.UPLOAD
                LogUtil.e("type:"+type);
                break;
            //            case AppConstants.POSITION_FILESEARCHF_RAGMENT:
            //                rgMain.check(R.id.main_file_search_rb);
            //                break;
            case AppConstants.POSITION_SETTING_FRAGMENT:
                rgMain.check(R.id.main_setting_rb);
                break;
            default:
                break;
        }
        mViewPager.setCurrentItem(position, false);
        // invalidateOptionsMenu();//销毁ActionBar
    }

    /**
     * 菜单显示隐藏动画
     *
     * @param showOrHide
     */
    private void startAnimation(boolean showOrHide) {
        //        final ViewGroup.LayoutParams layoutParams = tabLayout.getLayoutParams();
        //        ValueAnimator valueAnimator;
        //        ObjectAnimator alpha;
        //        if (!showOrHide) {
        //            valueAnimator = ValueAnimator.ofInt(tabLayoutHeight, 0);
        //            alpha = ObjectAnimator.ofFloat(tabLayout, "alpha", 1, 0);
        //        } else {
        //            valueAnimator = ValueAnimator.ofInt(0, tabLayoutHeight);
        //            alpha = ObjectAnimator.ofFloat(tabLayout, "alpha", 0, 1);
        //        }
        //        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        //            @Override
        //            public void onAnimationUpdate(ValueAnimator valueAnimator) {
        //                layoutParams.height = (int) valueAnimator.getAnimatedValue();
        //                tabLayout.setLayoutParams(layoutParams);
        //            }
        //        });
        //        AnimatorSet animatorSet = new AnimatorSet();
        //        animatorSet.setDuration(500);
        //        animatorSet.playTogether(valueAnimator, alpha);
        //        animatorSet.start();
    }

    /**
     * 监听全屏视频时返回键
     */
    //
    //    //利用时间差方法完成两次返回键退出，防止误操作。
    //
    //    //退出时间
    //
    //    private long currentBackPressedTime = 0;
    //    //退出间隔
    //
    //    private static final int BACK_PRESSED_INTERVAL = 2000;
    //
    //    //重写onBackPressed()方法,继承自退出的方法
    //
    //    @Override
    //
    //    public void onBackPressed() {
    //        //判断时间间隔
    //
    //        if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
    //
    //            currentBackPressedTime = System.currentTimeMillis();
    //
    //            Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
    //
    //        } else {
    //
    //            //退出
    //
    //            finish();
    //
    //        }
    //    }

    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean flag = FragmentFactory.createFragment(currentPosition).onBackForward(keyCode, event);
            if (flag) {
                return true;
            }
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 奔溃前保存位置
        outState.putInt(AppConstants.POSITION_FRAGMENT, currentPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // UpdateFunGO.onResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //UpdateFunGO.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ChangeModeController.onDestory();
    }

    MyProgressDialog pDialog;

    @Override
    public void loadBefore(String requestCode) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            if (pDialog == null)
                pDialog = new MyProgressDialog(App.getAppContext());
            String strTitle = getString(R.string.strPostDialogTitle);
            pDialog.setTitle(strTitle);
            String strMsg = getString(R.string.msg_login_to_uspace);
            pDialog.setMessage(strMsg);
            pDialog.show();
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            SPUtil.putString(AppConfig.TOKEN, (String) result);
            //  SwitchTo(AppConstants.POSITION_MYFILE_FRAGMENT,0);
            loginSuccess();
            pDialog.dismiss();
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (requestCode.equals(REQUEST_CODE_LOGIN)) {
            // TODO: 2017/4/12
            pDialog.dismiss();
        }
    }
    public void setGroupVisibility(int visibility){
        rgMain.setVisibility(visibility);
    }
}
