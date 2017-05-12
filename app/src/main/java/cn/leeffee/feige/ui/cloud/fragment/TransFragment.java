package cn.leeffee.feige.ui.cloud.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.base.BasePage;
import cn.leeffee.feige.ui.cloud.activity.MainActivity;
import cn.leeffee.feige.ui.cloud.factory.PageFactory;
import cn.leeffee.feige.ui.cloud.adapter.TransPagerAdapter;
import cn.leeffee.feige.ui.cloud.contract.FragTransContract;
import cn.leeffee.feige.ui.cloud.model.FragTransModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragTransPresenterImpl;
import cn.leeffee.feige.widget.USpaceToolBar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransFragment#} factory method to
 * create an instance of this fragment.
 */
public class TransFragment extends BaseFragment<FragTransPresenterImpl, FragTransModelImpl> implements FragTransContract.View {
    public final static int DOWNLOAD = 0;
    public final static int UPLOAD = 1;
    public static int current_position = DOWNLOAD;
    @BindView(R.id.trans_toolbar)
    USpaceToolBar mToolBar;
    @BindView(R.id.trans_tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.trans_viewPager)
    ViewPager mViewPager;
    @BindView(R.id.trans_bottom_menu_fl)
    LinearLayout mBottomMenu;
    @BindView(R.id.trans_selAll_btn)
    Button mSelAll;
    @BindView(R.id.trans_delete_btn)
    Button mDelete;
    //  List<BasePage> mPagerList;
    /**
     * 当前page
     */
    BasePage currentPage;

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_tab_trans;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected void initToolbar() {
        mToolBar.setRightText("取消");
        mToolBar.setRightImage(R.mipmap.menu_edit);
        mToolBar.setRightControlOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage.getAdapter().isEmpty() && !multiMode) {
                    return;
                }
                toggleMode();
            }
        });
    }

    /**
     * 多选模式
     */
    boolean multiMode;

    /**
     * 切换模式
     */
    private void toggleMode() {
        mToolBar.toggleRightControl();
        multiMode = currentPage.getAdapter().changeMode();
        ((MainActivity) getActivity()).setGroupVisibility(multiMode ? View.GONE : View.VISIBLE);
        mBottomMenu.setVisibility(multiMode ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initView() {
        TransPagerAdapter adapter = new TransPagerAdapter(this);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来
        // mTabLayout.setTabsFromPagerAdapter(adapter);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (multiMode) {
                    toggleMode();
                }
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                current_position = tab.getPosition();
                setCurrentPage(current_position);
                initData();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // setCurrentPage(position);
    }

    @Override
    protected void initData() {
        currentPage = PageFactory.createPage(current_position, this);
        currentPage.initData();
    }

    /**
     * 全选
     */
    @OnClick(R.id.trans_selAll_btn)
    public void selectAll() {
        if (currentPage.getAdapter().isEmpty()) {
            return;
        }
        if ("全选".equals(mSelAll.getText().toString().trim())) {
            mSelAll.setText("全不选");
            currentPage.getAdapter().selectAll();
        } else {
            mSelAll.setText("全选");
            currentPage.getAdapter().selectNoAll();
        }
    }

    /**
     * 删除
     */
    @OnClick(R.id.trans_delete_btn)
    public void delete() {
        currentPage.getAdapter().delete();
    }

    /**
     * 由adapter来回调
     *
     * @param count
     */
    public void onSelectedCountChange(int count) {
        if (currentPage.getAdapter().isEmpty() && multiMode) {
            toggleMode();
        }
        if (count == 0) {
            mSelAll.setText("全选");
            mDelete.setText("删除");
            mDelete.setEnabled(false);
        } else if (count == currentPage.getAdapter().getCount()) {
            mSelAll.setText("全不选");
            mDelete.setText("删除(" + count + ")");
            mDelete.setEnabled(true);
        } else {
            mSelAll.setText("全选");
            mDelete.setText("删除(" + count + ")");
            mDelete.setEnabled(true);
        }
    }

    /**
     * 设置当前要显示的page  下载或上传
     *
     * @param transType
     */
    public void setCurrentPage(int transType) {
        if (transType == TransFragment.DOWNLOAD) {
            current_position = TransFragment.DOWNLOAD;
        } else if (transType == TransFragment.UPLOAD) {
            current_position = TransFragment.UPLOAD;
        }
        mViewPager.setCurrentItem(current_position, false);
    }

    @Override
    public void loadBefore(String requestCode) {

    }

    @Override
    public void loadSuccess(String requestCode, Object result) {

    }

    @Override
    public void loadFailure(String requestCode, String msg) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PageFactory.destroyAllPage();
    }

    @Override
    public boolean onBackForward(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (multiMode) {
                toggleMode();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
