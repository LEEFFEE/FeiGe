package cn.leeffee.feige.ui.cloud.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import cn.leeffee.feige.ui.cloud.factory.PageFactory;


/**
 * Created by lhfei on 2017/4/11.
 */

//ViewPager适配器
public class TransPagerAdapter extends PagerAdapter {
    private Activity mAct;

    public TransPagerAdapter(Activity act) {
        mAct = act;
    }

    @Override
    public int getCount() {
        return 2;//页卡数
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;//官方推荐写法
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(PageFactory.createPage(position, mAct).getView());//添加页卡
        PageFactory.createPage(position, mAct).initData();
        return PageFactory.createPage(position, mAct).getView();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);//删除页卡
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PageFactory.createPage(position, mAct).getTitle();//页卡标题
    }

}
