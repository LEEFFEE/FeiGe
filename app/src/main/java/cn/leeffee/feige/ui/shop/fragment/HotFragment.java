package cn.leeffee.feige.ui.shop.fragment;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cjj.MaterialRefreshLayout;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.shop.activity.WareDetailActivity;
import cn.leeffee.feige.ui.shop.adapter.BaseAdapter;
import cn.leeffee.feige.ui.shop.adapter.HWAdatper;
import cn.leeffee.feige.ui.shop.constant.Contants;
import cn.leeffee.feige.ui.shop.entity.Page;
import cn.leeffee.feige.ui.shop.entity.Wares;
import cn.leeffee.feige.ui.shop.tool.Pager;


public class HotFragment extends BaseFragment implements Pager.OnPageListener<Wares> {
    private HWAdatper mAdatper;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.refresh_view)
    MaterialRefreshLayout mRefreshLaout;

    @Override
    public void load(List<Wares> datas, int totalPage, int totalCount) {

        mAdatper = new HWAdatper(getContext(), datas);

        mAdatper.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Wares wares = mAdatper.getItem(position);

                Intent intent = new Intent(getActivity(), WareDetailActivity.class);

                intent.putExtra(Contants.WARE, wares);
                startActivity(intent);


            }
        });


        mRecyclerView.setAdapter(mAdatper);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public void refresh(List<Wares> datas, int totalPage, int totalCount) {
        mAdatper.refreshData(datas);

        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void loadMore(List<Wares> datas, int totalPage, int totalCount) {

        mAdatper.loadMoreData(datas);
        mRecyclerView.scrollToPosition(mAdatper.getDatas().size());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_hot;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void initView() {
        Pager pager = Pager.newBuilder()
                .setUrl(Contants.API.WARES_HOT)
                .setLoadMore(true)
                .setOnPageListener(this)
                .setPageSize(20)
                .setRefreshLayout(mRefreshLaout)
                .build(getContext(), new TypeToken<Page<Wares>>() {
                }.getType());


        pager.request();
    }
}
