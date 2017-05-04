package cn.leeffee.feige.ui.cloud.presenter;


import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.PageDownloadListContract;
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
* Created by lhfei on 2017/04/11
*/

public class PageDownloadListPresenterImpl extends PageDownloadListContract.Presenter{

    public void listDownloadQueue(Integer[] arrStatus, final String requestCode) {
        mView.loadBefore(requestCode);
        Flowable.just(arrStatus).map(new Function<Integer[], List<DownloadTask>>() {
            @Override
            public List<DownloadTask> apply(@NonNull Integer[] ints) throws Exception {
                return mDBTool.listDownloadQueue(ints);
            }
        }).subscribe(new Consumer<List<DownloadTask>>() {
            @Override
            public void accept(@NonNull List<DownloadTask> downloadTasks) throws Exception {
                mView.loadSuccess(requestCode,downloadTasks);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                mView.loadFailure(requestCode,"加载错误");
            }
        });
    }
}