package cn.leeffee.feige.ui.cloud.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.cloud.adapter.TransPagerAdapter;
import cn.leeffee.feige.ui.cloud.contract.FragFileTransContract;
import cn.leeffee.feige.ui.cloud.model.FragFileTransModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.FragFileTransPresenterImpl;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileTransFragment#} factory method to
 * create an instance of this fragment.
 */
public class FileTransFragment extends BaseFragment<FragFileTransPresenterImpl, FragFileTransModelImpl> implements FragFileTransContract.View {
    public static int DOWNLOAD = 1;
    public static int UPLOAD = 2;
    @BindView(R.id.trans_tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.trans_viewPager)
    ViewPager mViewPager;
    //  List<BasePage> mPagerList;

    @Override
    protected int getLayoutResource() {
        return R.layout.frag_file_trans;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected void initView() {
        TransPagerAdapter adapter = new TransPagerAdapter(getActivity());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来
        // mTabLayout.setTabsFromPagerAdapter(adapter);
    }

    /**
     * 设置当前要显示的page  下载或上传
     *
     * @param transType
     */
    public void setCurrentPage(int transType) {
        int position = 0;
        if (transType == FileTransFragment.DOWNLOAD) {
            position = 0;
        } else if (transType == FileTransFragment.UPLOAD) {
            position = 1;
        }
        mViewPager.setCurrentItem(position, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
}
