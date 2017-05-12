package cn.leeffee.feige.widget;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import java.util.HashMap;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.adapter.UploadMenusAdapter;

/**
 * Created by lhfei on 2017/4/5.
 */

public class TopPopuWindow {
    FragmentActivity act;
    private PopupWindow popupWindow;
    View contentView;
    GridView gridView;
    UploadMenusAdapter menusAdapter;

    public TopPopuWindow(FragmentActivity act) {
        this.act = act;
        initPopupWindow();
    }

    /**
     * 初始化
     */
    private void initPopupWindow() {

        contentView = LayoutInflater.from(act).inflate(R.layout.upload_menus, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setAnimationStyle(R.style.TopPopuWindowAnimationShow);
        // 菜单背景色。加了一点透明度
        ColorDrawable dw = new ColorDrawable(0xddffffff);
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        contentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*
                 * if( popupWindow!=null && popupWindow.isShowing()){
                 * popupWindow.dismiss(); popupWindow=null; }
                 */
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
                return false;
            }
        });
        dealWithSelect();
    }


    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        gridView.setOnItemClickListener(listener);
    }

    /**
     * 处理点击事件
     */
    private void dealWithSelect() {
        //点击了关闭图标（右上角图标）
        contentView.findViewById(R.id.upload_menus_cancel_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        gridView = (GridView) contentView.findViewById(R.id.upload_menus_gv);
        menusAdapter = new UploadMenusAdapter(act);
        gridView.setAdapter(menusAdapter);
    }

    public HashMap<String, Object> getItem(int position) {
        return menusAdapter.getItem(position);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = act.getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        act.getWindow().setAttributes(lp);
    }

    public void dismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public boolean isShowing() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        }
        return false;
    }

    public void show() {
        if (popupWindow != null && !popupWindow.isShowing()) {
            //TODO 注意：这里的 R.layout.activity_main，不是固定的。你想让这个popupwindow盖在哪个界面上面。就写哪个界面的布局。这里以主界面为例
            popupWindow.showAtLocation(contentView,
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            // 设置背景半透明
            backgroundAlpha(0.7f);
        }
    }
}
