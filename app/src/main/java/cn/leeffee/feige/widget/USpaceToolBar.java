package cn.leeffee.feige.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leeffee.feige.R;


/**
 * Created by lhfei on 2017/3/20.
 */

public class USpaceToolBar extends Toolbar {

    /**
     * 左边第一张图片
     */
    @BindView(R.id.toolbar_left_image_iv)
    ImageView leftImage;
    /**
     * 左边第二张图片
     */
    @BindView(R.id.toolbar_left_second_image_iv)
    ImageView leftSecondImage;
    /**
     * 右边第一张图片
     */
    @BindView(R.id.toolbar_right_control_fl)
    FrameLayout rightControl;
    @BindView(R.id.toolbar_right_image_iv)
    ImageView rightImage;
    @BindView(R.id.toolbar_right_text_tv)
    TextView rightText;
    /**
     * 右边第二张图片
     */
    @BindView(R.id.toolbar_right_second_image_iv)
    ImageView rightSecondImage;
    /**
     * 中间文字标题
     */
    @BindView(R.id.toolbar_center_title_tv)
    TextView tvCenterTitle;

    private View mView;
    private LayoutInflater mInflater;
    String centerTitle;

    public USpaceToolBar(Context context) {
        this(context, null);
    }

    public USpaceToolBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public USpaceToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.USpaceToolBar);
        if (typedArray != null) {
            centerTitle = typedArray.getString(R.styleable.USpaceToolBar_center_title);
            typedArray.recycle();
        }
        initView();
    }

    private void initView() {
        if (mView == null) {
            mInflater = LayoutInflater.from(getContext());
            mView = mInflater.inflate(R.layout.toolbar, null);
            ButterKnife.bind(this, mView);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(mView, lp);
        }
        leftImage.setBackgroundDrawable(null);
        tvCenterTitle.setText(centerTitle);
        rightImage.setBackgroundDrawable(null);
        rightText.setText("");
    }

    /*=================左边第一个按钮===================*/
    public void setLeftImage(int resId) {
        setLeftImageVisibility(VISIBLE);
        this.leftImage.setBackgroundResource(resId);
    }

    public void setLeftImageOnClick(OnClickListener listener) {
        this.leftImage.setOnClickListener(listener);
    }

    public void setLeftImageVisibility(int visibility) {
        this.leftImage.setVisibility(visibility);
    }

    /*=================左边第二个按钮===================*/
    public void setLeftSecondImage(int resId) {
        setLeftSecondImageVisibility(VISIBLE);
        this.leftSecondImage.setBackgroundResource(resId);
    }

    public void setLeftSecondImageOnClick(OnClickListener listener) {
        this.leftSecondImage.setOnClickListener(listener);
    }

    public void setLeftSecondImageVisibility(int visibility) {
        this.leftSecondImage.setVisibility(visibility);
    }

    /*=================右边第一个按钮===================*/

    /**
     * 设置右边第一个控件的背景图
     *
     * @param resId 要设置的图片的资源id
     */
    public void setRightImage(int resId) {
        setRightControlVisibility(VISIBLE);//设置控件可见
        this.rightText.setVisibility(GONE);
        this.rightImage.setBackgroundResource(resId);
    }

    /**
     * 设置右边第一个控件的文字
     *
     * @param content 要设置的文字内容
     */
    public void setRightText(CharSequence content) {
        setRightControlVisibility(VISIBLE);//设置控件可见
        this.rightImage.setVisibility(GONE);
        this.rightText.setText(content);
    }

    public void setRightImageOnClick(OnClickListener listener) {
        setRightControlOnClick(listener);
    }

    public void setRightTextOnClick(OnClickListener listener) {
        setRightControlOnClick(listener);
    }

    public void setRightControlOnClick(OnClickListener listener) {
        this.rightControl.setOnClickListener(listener);
    }


    public void setRightImageVisibility(int visibility) {
        setRightControlVisibility(visibility);
    }

    public void setRightTextVisibility(int visibility) {
        setRightControlVisibility(visibility);
    }

    public void setRightControlVisibility(int visibility) {
        this.rightControl.setVisibility(visibility);
    }

    /*=================右边第二个按钮===================*/
    public void setRightSecondImage(int resId) {
        setRightSecondImageVisibility(VISIBLE);
        this.rightSecondImage.setBackgroundResource(resId);
    }

    public void setRightSecondImageOnClick(OnClickListener listener) {
        this.rightSecondImage.setOnClickListener(listener);
    }

    public void setRightSecondImageVisibility(int visibility) {
        this.rightSecondImage.setVisibility(visibility);
    }

    /*=================中心标题===================*/
    public void setCenterTitle(String centerTitle) {
        this.tvCenterTitle.setText(centerTitle);
    }
}
