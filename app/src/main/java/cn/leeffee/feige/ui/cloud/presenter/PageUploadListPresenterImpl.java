package cn.leeffee.feige.ui.cloud.presenter;

import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.PageUploadListContract;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by lhfei on 2017/04/11
 */

public class PageUploadListPresenterImpl extends PageUploadListContract.Presenter {

    public void listUploadQueue(Integer[] arrStatus, final String requestCode) {
        mView.loadBefore(requestCode);
        Flowable.just(arrStatus).map(new Function<Integer[], List<UploadTask>>() {
            @Override
            public List<UploadTask> apply(@NonNull Integer[] ints) throws Exception {
                return mDBTool.listUploadQueue(ints);
            }
        }).subscribe(new Consumer<List<UploadTask>>() {
            @Override
            public void accept(@NonNull List<UploadTask> uploadTasks) throws Exception {
                mView.loadSuccess(requestCode, uploadTasks);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                mView.loadFailure(requestCode, "加载错误");
            }
        });
    }
}