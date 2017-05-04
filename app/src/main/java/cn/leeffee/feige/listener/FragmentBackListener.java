package cn.leeffee.feige.listener;

import android.view.KeyEvent;

/**
 * Created by lhfei on 2017/4/1.
 */

public interface FragmentBackListener {
    boolean onBackForward(int keyCode, KeyEvent event);
}
