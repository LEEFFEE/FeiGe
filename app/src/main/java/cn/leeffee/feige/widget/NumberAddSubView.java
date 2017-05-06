package cn.leeffee.feige.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.TintTypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.leeffee.feige.R;


/**
 * 数字加减
 */
public class NumberAddSubView extends LinearLayout implements View.OnClickListener {

    public static final String TAG="NumberAddSubView";
    public static final int DEFAULT_MAX=1000;
    private TextView mTvNum;
    private Button mBtnAdd;
    private Button mBtnSub;
    private OnButtonClickListener onButtonClickListener;
    private LayoutInflater mInflater;
    private  int value;
    private int minValue;
    private int maxValue=DEFAULT_MAX;
    public NumberAddSubView(Context context) {
        this(context, null);
    }

    public NumberAddSubView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberAddSubView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        initView();
        if(attrs !=null){
            final TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                    R.styleable.NumberAddSubView, defStyleAttr, 0);
            int val =  a.getInt(R.styleable.NumberAddSubView_value,0);
            setValue(val);

            int maxVal = a.getInt(R.styleable.NumberAddSubView_maxValue,0);
            if(maxVal!=0)
                setMaxValue(maxVal);

            int minVal = a.getInt(R.styleable.NumberAddSubView_minValue,0);
            setMinValue(minVal);

            Drawable etBackground = a.getDrawable(R.styleable.NumberAddSubView_editBackground);
            if(etBackground!=null)
                setEditTextBackground(etBackground);


             Drawable buttonAddBackground = a.getDrawable(R.styleable.NumberAddSubView_buttonAddBackground);
             if(buttonAddBackground!=null)
                 setButtonAddBackgroud(buttonAddBackground);

            Drawable buttonSubBackground = a.getDrawable(R.styleable.NumberAddSubView_buttonSubBackground);
            if(buttonSubBackground!=null)
                setButtonSubBackgroud(buttonSubBackground);
            a.recycle();
        }
    }


    private void initView(){
        View view = mInflater.inflate(R.layout.widet_num_add_sub,this,true);
        mTvNum = (TextView) view.findViewById(R.id.etxt_num);
        mTvNum.setInputType(InputType.TYPE_NULL);
        mTvNum.setKeyListener(null);

        mBtnAdd = (Button) view.findViewById(R.id.btn_add);
        mBtnSub = (Button) view.findViewById(R.id.btn_sub);
        mBtnAdd.setOnClickListener(this);
        mBtnSub.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_add){
            numAdd();
            if(onButtonClickListener !=null){
                onButtonClickListener.onButtonAddClick(v,this.value);
            }
        }
        else if(v.getId()==R.id.btn_sub){
                numSub();
            if(onButtonClickListener !=null){
                onButtonClickListener.onButtonSubClick(v,this.value);
            }
        }
    }

    private void numAdd(){
        getValue();
        if(this.value<=maxValue)
            this.value=+this.value+1;
        mTvNum.setText(value+"");
    }


    private void numSub(){
        getValue();
        if(this.value>minValue)
            this.value=this.value-1;
        mTvNum.setText(value+"");
    }

    public int getValue(){
        String value = mTvNum.getText().toString();
        if(value !=null && !"".equals(value))
            this.value = Integer.parseInt(value);
        return this.value;
    }

    public void setValue(int value) {
        mTvNum.setText(value+"");
        this.value = value;
    }




    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }


    public void setEditTextBackground(Drawable drawable){

        mTvNum.setBackgroundDrawable(drawable);

    }


    public void setEditTextBackground(int drawableId){

      setEditTextBackground(getResources().getDrawable(drawableId));

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setButtonAddBackgroud(Drawable background){
        this.mBtnAdd.setBackground(background);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setButtonSubBackgroud(Drawable background){
        this.mBtnSub.setBackground(background);
    }


    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface  OnButtonClickListener{

         void onButtonAddClick(View view, int value);
         void onButtonSubClick(View view, int value);

    }


}
