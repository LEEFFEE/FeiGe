package cn.leeffee.feige.ui.shop.fragment;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.shop.activity.WareListActivity;
import cn.leeffee.feige.ui.shop.adapter.HomeCatgoryAdapter;
import cn.leeffee.feige.ui.shop.adapter.decoration.CardViewtemDecortion;
import cn.leeffee.feige.ui.shop.constant.Contants;
import cn.leeffee.feige.ui.shop.entity.Banner;
import cn.leeffee.feige.ui.shop.entity.Campaign;
import cn.leeffee.feige.ui.shop.entity.HomeCampaign;
import cn.leeffee.feige.ui.shop.http.BaseCallback;
import cn.leeffee.feige.ui.shop.http.OkHttpHelper;
import cn.leeffee.feige.ui.shop.http.SpotsCallBack;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends BaseFragment {

    @BindView(R.id.slider)
    SliderLayout mSliderLayout;


    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private HomeCatgoryAdapter mAdatper;


    private static final String TAG = "HomeFragment";


    private Gson mGson = new Gson();

    private List<Banner> mBanner;


    private OkHttpHelper httpHelper = OkHttpHelper.getInstance();

    private void requestImages() {
        String url = "http://112.124.22.238:8081/course_api/banner/query?type=1";

        httpHelper.get(url, new SpotsCallBack<List<Banner>>(getContext()) {


            @Override
            public void onSuccess(Response response, List<Banner> banners) {

                mBanner = banners;
                initSlider();
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });


    }


    private void initRecyclerView() {
        httpHelper.get(Contants.API.CAMPAIGN_HOME, new BaseCallback<List<HomeCampaign>>() {
            @Override
            public void onBeforeRequest(Request request) {

            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

            @Override
            public void onResponse(Response response) {

            }

            @Override
            public void onSuccess(Response response, List<HomeCampaign> homeCampaigns) {

                initData(homeCampaigns);
            }


            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });

    }


    private void initData(List<HomeCampaign> homeCampaigns) {
        mAdatper = new HomeCatgoryAdapter(homeCampaigns, getActivity());
        mAdatper.setOnCampaignClickListener(new HomeCatgoryAdapter.OnCampaignClickListener() {
            @Override
            public void onClick(View view, Campaign campaign) {
                Intent intent = new Intent(getActivity(), WareListActivity.class);
                intent.putExtra(Contants.COMPAINGAIN_ID, campaign.getId());
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mAdatper);

        mRecyclerView.addItemDecoration(new CardViewtemDecortion());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }


    private void initSlider() {


        if (mBanner != null) {

            for (Banner banner : mBanner) {


                TextSliderView textSliderView = new TextSliderView(this.getActivity());
                textSliderView.image(banner.getImgUrl());
                textSliderView.description(banner.getName());
                textSliderView.setScaleType(BaseSliderView.ScaleType.Fit);
                mSliderLayout.addSlider(textSliderView);

            }
        }


        mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);

        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.RotateUp);
        mSliderLayout.setDuration(3000);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSliderLayout.stopAutoCycle();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_home;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void initView() {
        requestImages();
        initRecyclerView();
    }
}
