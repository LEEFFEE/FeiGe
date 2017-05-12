package cn.leeffee.feige.manager;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.leeffee.feige.utils.LogUtil;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;

/**
 * 用Flowable实现的EventBus  还未完成  TODO
 * Created by lhfei on 2017/3/29.
 */

public class RxBusFlowable {
    private static RxBusFlowable instance;

    public static synchronized RxBusFlowable getInstance() {
        if (null == instance) {
            instance = new RxBusFlowable();
        }
        return instance;
    }

    private RxBusFlowable() {
    }

    @SuppressWarnings("rawtypes")
    private ConcurrentHashMap<Object, List<Subject>> subjectMapper = new ConcurrentHashMap<>();

    /**
     * 注册事件源
     *
     * @param eventName
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public <T> Flowable<T> register(@NonNull Object eventName) {
        List<Subject> subjectList = subjectMapper.get(eventName);
        if (null == subjectList) {
            subjectList = new ArrayList<>();
            subjectMapper.put(eventName, subjectList);
        }
        FlowableSubscriber<T> subject;
        //  subjectList.add(subject = PublishSubject.create());
        LogUtil.e("register" + eventName + "  size:" + subjectList.size());
        // Flowable.create().
        //FlowableSubscriber
        //  FlowableOnSubscribe
        // return subject.;
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void unregister(@NonNull Object eventName) {
        List<Subject> subjects = subjectMapper.get(eventName);
        if (null != subjects) {
            subjectMapper.remove(eventName);
        }
    }

    /**
     * 取消监听
     *
     * @param eventName
     * @param observable
     * @return
     */
    @SuppressWarnings("rawtypes")
    public RxBusFlowable unregister(@NonNull Object eventName,
                                    @NonNull Observable<?> observable) {
        if (null == observable)
            return getInstance();
        List<Subject> subjects = subjectMapper.get(eventName);
        if (null != subjects) {
            subjects.remove(observable);
            if (isEmpty(subjects)) {
                subjectMapper.remove(eventName);
                LogUtil.e("unregister" + eventName + "  size:" + subjects.size());
            }
        }
        return getInstance();
    }

    public void post(@NonNull Object content) {
        post(content.getClass().getName(), content);
    }

    /**
     * 触发事件
     *
     * @param content
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void post(@NonNull Object eventName, @NonNull Object content) {
        LogUtil.e("post" + "eventName: " + eventName);
        List<Subject> subjectList = subjectMapper.get(eventName);
        if (!isEmpty(subjectList)) {
            for (Subject subject : subjectList) {
                subject.onNext(content);
                LogUtil.e("onEvent" + "eventName: " + eventName);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection<Subject> collection) {
        return null == collection || collection.isEmpty();
    }
}
