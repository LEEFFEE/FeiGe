package cn.leeffee.feige.ui.cloud.factory;

import android.app.Activity;

import java.util.HashMap;

import cn.leeffee.feige.base.BasePage;
import cn.leeffee.feige.ui.cloud.page.DownLoadListPage;
import cn.leeffee.feige.ui.cloud.page.UploadListPage;

/**
 * 生产fragment工厂
 */
public class PageFactory {

    private static HashMap<Integer, BasePage> uspacePages = new HashMap<>();

    public static BasePage createPage(int pos, Activity act) {
        // 先从集合中取, 如果没有,才创建对象, 提高性能
        BasePage page = uspacePages.get(pos);
        if (page != null) {
            return page;
        }
        switch (pos) {
            case 0:
                page = new DownLoadListPage(act, "下载");
                break;
            case 1:
                page = new UploadListPage(act, "上传");
                break;
            default:
                break;
        }
        // 将fragment保存在集合中
        uspacePages.put(pos, page);
        return page;
    }
}