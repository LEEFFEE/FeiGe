package cn.leeffee.feige.ui.shop.activity;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.ui.shop.entity.Tab;
import cn.leeffee.feige.ui.shop.fragment.CartFragment;
import cn.leeffee.feige.ui.shop.fragment.CategoryFragment;
import cn.leeffee.feige.ui.shop.fragment.HomeFragment;
import cn.leeffee.feige.ui.shop.fragment.HotFragment;
import cn.leeffee.feige.ui.shop.fragment.MineFragment;
import cn.leeffee.feige.widget.FragmentTabHost;

public class MainActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_shop_main;
    }

    private LayoutInflater mInflater;

    private FragmentTabHost mTabhost;

    private CartFragment cartFragment;

    private List<Tab> mTabs = new ArrayList<>(5);

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        initTab();
    }

    private void initTab() {

        Tab tab_home = new Tab(HomeFragment.class,R.string.home,R.drawable.selector_icon_home);
        Tab tab_hot = new Tab(HotFragment.class,R.string.hot,R.drawable.selector_icon_hot);
        Tab tab_category = new Tab(CategoryFragment.class,R.string.catagory,R.drawable.selector_icon_category);
        Tab tab_cart = new Tab(CartFragment.class,R.string.cart,R.drawable.selector_icon_cart);
        Tab tab_mine = new Tab(MineFragment.class,R.string.mine,R.drawable.selector_icon_mine);

        mTabs.add(tab_home);
        mTabs.add(tab_hot);
        mTabs.add(tab_category);
        mTabs.add(tab_cart);
        mTabs.add(tab_mine);



        mInflater = LayoutInflater.from(this);
        mTabhost = (FragmentTabHost) this.findViewById(android.R.id.tabhost);
        mTabhost.setup(this,getSupportFragmentManager(),R.id.real_tabcontent);

        for (Tab tab : mTabs){

            TabHost.TabSpec tabSpec = mTabhost.newTabSpec(getString(tab.getTitle()));

            tabSpec.setIndicator(buildIndicator(tab));

            mTabhost.addTab(tabSpec,tab.getFragment(),null);

        }

        mTabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                if(tabId.equals(getString(R.string.cart))){
                    refData();
                }

            }
        });

        mTabhost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabhost.setCurrentTab(0);


    }


    private void refData(){

        if(cartFragment == null){

            Fragment fragment =  getSupportFragmentManager().findFragmentByTag(getString(R.string.cart));
            if(fragment !=null){
                cartFragment= (CartFragment) fragment;
                cartFragment.refData();
            }
        }
        else{
            cartFragment.refData();

        }
    }


    private  View buildIndicator(Tab tab){


        View view =mInflater.inflate(R.layout.tab_indicator,null);
        ImageView img = (ImageView) view.findViewById(R.id.icon_tab);
        TextView text = (TextView) view.findViewById(R.id.txt_indicator);

        img.setBackgroundResource(tab.getIcon());
        text.setText(tab.getTitle());

        return  view;
    }


}
