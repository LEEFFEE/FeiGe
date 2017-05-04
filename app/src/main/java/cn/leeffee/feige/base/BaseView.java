package cn.leeffee.feige.base;

/**
 * des:baseview
 */
public interface BaseView {
    /*******
     * 内嵌加载
     *******/
    void loadBefore(String requestCode);

    /**
     * 请求成功后调用
     *
     * @param requestCode 请求码
     * @param result      成功返回集合
     */
    void loadSuccess(String requestCode, Object result);

    /**
     * 请求失败后调用
     *
     * @param requestCode requestCode 请求码
     * @param msg         错误消息
     */
    void loadFailure(String requestCode, String msg);
}
