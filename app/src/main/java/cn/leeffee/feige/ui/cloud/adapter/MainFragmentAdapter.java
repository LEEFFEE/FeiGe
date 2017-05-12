package cn.leeffee.feige.ui.cloud.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.factory.FragmentFactory;


/**
 * Created by lhfei on 2017/3/27.
 */

public class MainFragmentAdapter extends FragmentPagerAdapter {
    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentFactory.createFragment(position);
    }

    @Override
    public int getCount() {
        return AppConstants.MAIN_SIZE_FRAGMENT;
    }
}
