package cn.leeffee.feige.ui.cloud.factory;


import java.util.HashMap;

import cn.leeffee.feige.base.BaseFragment;
import cn.leeffee.feige.ui.cloud.fragment.FileTransFragment;
import cn.leeffee.feige.ui.cloud.fragment.MoreFragment;
import cn.leeffee.feige.ui.cloud.fragment.MyFileFragment;
import cn.leeffee.feige.ui.cloud.fragment.MyGroupFragment;

/**
 * 生产fragment工厂
 */
public class FragmentFactory {

    private static HashMap<Integer, BaseFragment> uspaceFragment = new HashMap<>();

    public static BaseFragment createFragment(int pos) {
        // 先从集合中取, 如果没有,才创建对象, 提高性能
        BaseFragment fragment = uspaceFragment.get(pos);
        if (fragment != null) {
            return fragment;
        }
        switch (pos) {
            case 0:
                fragment = new MyFileFragment();
                break;
            case 1:
                fragment = new MyGroupFragment();
                break;
            case 2:
                fragment = new FileTransFragment();
                break;
            case 3:
                fragment = new MoreFragment();
                break;
            default:
                break;
        }
        // 将fragment保存在集合中
        uspaceFragment.put(pos, fragment);
        return fragment;
    }
}