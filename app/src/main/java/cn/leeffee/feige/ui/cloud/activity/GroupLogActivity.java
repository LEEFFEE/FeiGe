package cn.leeffee.feige.ui.cloud.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseActivity;
import cn.leeffee.feige.manager.AppManager;
import cn.leeffee.feige.ui.cloud.adapter.GroupLogAdapter;
import cn.leeffee.feige.ui.cloud.contract.ActGroupLogContract;
import cn.leeffee.feige.ui.cloud.entity.ApiGroupLog;
import cn.leeffee.feige.ui.cloud.model.ActGroupLogModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.ActGroupLogPresenterImpl;
import cn.leeffee.feige.utils.CommonUtil;
import cn.leeffee.feige.utils.ToastUtil;
import cn.leeffee.feige.widget.USpaceToolBar;
import cn.leeffee.feige.widget.XListView;


/**
 * Created by lhfei on 2017/4/18.群组日志信息
 */

public class GroupLogActivity extends BaseActivity<ActGroupLogPresenterImpl, ActGroupLogModelImpl> implements ActGroupLogContract.View {
    @BindView(R.id.group_log_group_name_tv)
    TextView mGroupName;
    @BindView(R.id.group_log_list_lv)
    XListView mListView;
    @BindView(R.id.group_log_empty_tv)
    TextView mEmpty;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    @BindView(R.id.group_log_toolbar)
    USpaceToolBar mToolBar;
    GroupLogAdapter adapter;
    private String current_group_id;
    private String current_group_name;
    private int current_page_index = 1;
    /**
     * 每页大小
     */
    private int PAGE_SIZE = 20;
    private static final String REQUEST_CODE_GROUP_LOG_INIT = "queryShareGroupLogs_init";
    private static final String REQUEST_CODE_GROUP_LOG_REFRESH = "queryShareGroupLogs_refresh";
    private static final String REQUEST_CODE_GROUP_LOG_LOAD_MORE = "queryShareGroupLogs_load_more";

    @Override
    public int getLayoutId() {
        return R.layout.act_group_log;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(true);
        adapter = new GroupLogAdapter(App.getAppContext());
        mListView.setAdapter(adapter);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                current_page_index = 1;
                mRxManager.add(mPresenter.listGroupLogs(current_group_id, current_page_index, PAGE_SIZE, REQUEST_CODE_GROUP_LOG_REFRESH));
            }

            @Override
            public void onLoadMore() {
                current_page_index += 1;
                mRxManager.add(mPresenter.listGroupLogs(current_group_id, current_page_index, PAGE_SIZE, REQUEST_CODE_GROUP_LOG_LOAD_MORE));
            }
        });
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
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        current_group_id = intent.getStringExtra("groupId");
        current_group_name = intent.getStringExtra("groupName");
        mGroupName.setText("群组[" + current_group_name + "]的日志");
        mRxManager.add(mPresenter.listGroupLogs(current_group_id, current_page_index, PAGE_SIZE, REQUEST_CODE_GROUP_LOG_INIT));
    }

    @Override
    public void loadBefore(String requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_GROUP_LOG_INIT:
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_GROUP_LOG_REFRESH:
                break;
            case REQUEST_CODE_GROUP_LOG_LOAD_MORE:
                break;
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        switch (requestCode) {
            case REQUEST_CODE_GROUP_LOG_INIT:
                adapter.setData((List<ApiGroupLog>) result);
                adapter.notifyDataSetChanged();
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_GROUP_LOG_REFRESH:
                List<ApiGroupLog> res = (List<ApiGroupLog>) result;
                if (res == null || res.size() == 0) {
                    mEmpty.setVisibility(View.VISIBLE);
                } else {
                    mEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    mListView.stopRefresh();
                    mListView.setRefreshTime("更新时间:" + CommonUtil.getSystemTime());
                }
                break;
            case REQUEST_CODE_GROUP_LOG_LOAD_MORE:
                List<ApiGroupLog> resMore = (List<ApiGroupLog>) result;
                if (resMore == null || resMore.size() == 0) {
                    ToastUtil.showShort("没有更多了!");
                } else {
                    adapter.getData().addAll(resMore);
                    adapter.notifyDataSetChanged();
                }
                mListView.stopLoadMore();
                break;
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        switch (requestCode) {
            case REQUEST_CODE_GROUP_LOG_INIT:
                ToastUtil.showShort(msg);
                mLoadingLayout.setVisibility(View.GONE);
                break;
            case REQUEST_CODE_GROUP_LOG_REFRESH:
                ToastUtil.showShort(msg);
                mListView.stopRefresh();
                break;
            case REQUEST_CODE_GROUP_LOG_LOAD_MORE:
                ToastUtil.showShort(msg);
                mListView.stopLoadMore();
                break;
        }
    }
}
