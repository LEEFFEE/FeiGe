package cn.leeffee.feige.ui.cloud.presenter;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

import cn.leeffee.feige.ui.cloud.contract.ActSearchContract;
import cn.leeffee.feige.ui.cloud.entity.ApiFile;
import cn.leeffee.feige.ui.cloud.entity.BaseResponse;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.DateUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by lhfei on 2017/04/24
 */

public class ActSearchPresenterImpl extends ActSearchContract.Presenter {

    private int times=1;

    /**
     * 搜索
     *
     * @param keyword     关键字
     * @param requestCode
     * @return
     */
    public Disposable listSearchFiles(final String keyword, final String requestCode) {
        mView.loadBefore(requestCode);
        return mModel.listSearchFiles(keyword, mToken).subscribe(new Consumer<BaseResponse<List<ApiFile>>>() {
            @Override
            public void accept(@NonNull BaseResponse<List<ApiFile>> res) throws Exception {
                if (res.getErrorCode() == 0) {
                    List<ApiFile> apiFiles = res.getResult();
                    List<USpaceFile> fileList = new ArrayList<>();
                    if (apiFiles != null && apiFiles.size() > 0) {
                        for (int i = 0; i < apiFiles.size(); i++) {
                            ApiFile _item = apiFiles.get(i);
                            USpaceFile _file = new USpaceFile();
                            _file.setName(_item.getName());
                            _file.setSize(_item.getSize());
                            _file.setDiskPath(_item.getDiskPath());
                            _file.setModifyTime(DateUtil.string2Timestamp(_item.getModifyTime(), "yyyy-MM-dd hh:mm:ss"));
                            _file.setFolder(_item.isFolder());
                            String sharedId = _item.getSharedId();
                            if (!TextUtils.isEmpty(sharedId) && !"null".equalsIgnoreCase(sharedId)) {
                                _file.setShared(true);
                                _file.setExtractionCode(sharedId);
                            } else {
                                _file.setShared(false);
                            }
                            if (!_file.isFolder()) {
                                _file.setVersion(_item.getVsesionNumber());
                            }
                            _file.setSearchFile(true);
                            fileList.add(_file);
                        }
                    }
                    times = 1;
                    mView.loadSuccess(requestCode, fileList);

                } else if (checkCode(res.getErrorCode())) {
                    if (times < 2) {
                        reLogin(requestCode);
                        listSearchFiles(keyword, requestCode);
                        times++;
                    } else {
                        times = 1;
                        mView.loadFailure(requestCode, "请稍后重试");
                    }
                } else {
                    mView.loadFailure(requestCode, res.getErrorMessage());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                mView.loadFailure(requestCode, "加载失败");
            }
        });
    }
}