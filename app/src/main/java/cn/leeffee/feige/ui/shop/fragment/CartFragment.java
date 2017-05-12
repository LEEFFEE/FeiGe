package cn.leeffee.feige.ui.shop.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.shop.adapter.CartAdapter;
import cn.leeffee.feige.ui.shop.adapter.DividerItemDecoration;
import cn.leeffee.feige.ui.shop.entity.ShoppingCart;
import cn.leeffee.feige.ui.shop.tool.CartProvider;
import cn.leeffee.feige.ui.shop.widget.ShopToolBar;


public class CartFragment extends BaseFragment implements View.OnClickListener {

    public static final int ACTION_EDIT = 1;
    public static final int ACTION_CAMPLATE = 2;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.checkbox_all)
    CheckBox mCheckBox;

    @BindView(R.id.txt_total)
    TextView mTextTotal;

    @BindView(R.id.btn_order)
    Button mBtnOrder;

    @BindView(R.id.btn_del)
    Button mBtnDel;

    @BindView(R.id.toolbar)
    ShopToolBar mToolbar;

    private CartAdapter mAdapter;
    private CartProvider cartProvider;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_cart;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void initView() {
        cartProvider = new CartProvider(getContext());
        changeToolbar();
        showData();
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.btn_del)
    public void delCart() {
        mAdapter.delCart();
    }


    private void showData() {
        List<ShoppingCart> carts = cartProvider.getAll();
        mAdapter = new CartAdapter(getContext(), carts, mCheckBox, mTextTotal);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
    }


    public void refData() {
        mAdapter.clear();
        List<ShoppingCart> carts = cartProvider.getAll();
        mAdapter.addData(carts);
        mAdapter.showTotalPrice();
    }


    public void changeToolbar() {
        mToolbar.hideSearchView();
        mToolbar.showTitleView();
        mToolbar.setTitle("购物车");
        mToolbar.getRightButton().setVisibility(View.VISIBLE);
        mToolbar.setRightButtonText("编辑");
        mToolbar.getRightButton().setOnClickListener(this);
        mToolbar.getRightButton().setTag(ACTION_EDIT);
    }


    private void showDelControl() {
        mToolbar.getRightButton().setText("完成");
        mTextTotal.setVisibility(View.GONE);
        mBtnOrder.setVisibility(View.GONE);
        mBtnDel.setVisibility(View.VISIBLE);
        mToolbar.getRightButton().setTag(ACTION_CAMPLATE);

        mAdapter.checkAll_None(false);
        mCheckBox.setChecked(false);
    }

    private void hideDelControl() {

        mTextTotal.setVisibility(View.VISIBLE);
        mBtnOrder.setVisibility(View.VISIBLE);


        mBtnDel.setVisibility(View.GONE);
        mToolbar.setRightButtonText("编辑");
        mToolbar.getRightButton().setTag(ACTION_EDIT);

        mAdapter.checkAll_None(true);
        mAdapter.showTotalPrice();

        mCheckBox.setChecked(true);
    }


    @Override
    public void onClick(View v) {
        int action = (int) v.getTag();
        if (ACTION_EDIT == action) {
            showDelControl();
        } else if (ACTION_CAMPLATE == action) {
            hideDelControl();
        }
    }
}
