package cn.leeffee.feige.manager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * 用于管理单个presenter的RxBus的事件和Rxjava相关代码的生命周期处理
 * Created by lhfei on 2017/3/29.
 */

public class RxManager {
    public RxBus mRxBus = RxBus.getInstance();
    //管理rxbus订阅
    private Map<Object, Observable<?>> mObservables = new HashMap<>();
    /*管理Observables 和 Subscribers订阅*/
    private CompositeDisposable disposables = new CompositeDisposable();

    /**
     * RxBus注入监听
     *
     * @param eventName
     */
    public <T> void on(Object eventName, DisposableObserver<T> observer) {
        Observable<T> mObservable = mRxBus.register(eventName);
        mObservables.put(eventName, mObservable);
        /*订阅管理*/
        disposables.add(mObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer));
    }

    /**
     * 单纯的Observables 和 Subscribers管理
     *
     * @param m
     */
    public void add(Disposable m) {
        /*订阅管理*/
        disposables.add(m);
    }

    /**
     * 移除并且dispose() 掉
     *
     * @param m
     */
    public void remove(Disposable m) {
        disposables.remove(m);
    }

    /**
     * 单个presenter生命周期结束，取消订阅和所有rxbus观察
     */
    public void clear() {
        disposables.clear();// 取消所有订阅
        for (Map.Entry<Object, Observable<?>> entry : mObservables.entrySet()) {
            mRxBus.unregister(entry.getKey(), entry.getValue());// 移除rxbus观察
        }
    }

    //发送rxbus
    public void post(Object eventName, Object content) {
        mRxBus.post(eventName, content);
    }
}
