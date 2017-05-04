package cn.leeffee.feige.ui.cloud.presenter;


import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.ActBackupContract;
import cn.leeffee.feige.ui.cloud.service.BackupTask;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by lhfei on 2017/04/10
 */

public class ActBackupPresenterImpl extends ActBackupContract.Presenter {

    public Disposable listBackupQueue(Integer[] arrStatus, final String requestCode) {
        mView.loadBefore(requestCode);
        return Observable.just(arrStatus).map(new Function<Integer[], List<BackupTask>>() {
            @Override
            public List<BackupTask> apply(@NonNull Integer[] ints) throws Exception {
                return mDBTool.listBackupQueue(ints);
            }
        }).subscribe(new Consumer<List<BackupTask>>() {
            @Override
            public void accept(@NonNull List<BackupTask> backupTasks) throws Exception {
                mView.loadSuccess(requestCode, backupTasks);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }
}