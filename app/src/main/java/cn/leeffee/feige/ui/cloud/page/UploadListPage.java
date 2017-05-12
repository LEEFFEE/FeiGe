package cn.leeffee.feige.ui.cloud.page;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.base.BasePage;
import cn.leeffee.feige.ui.cloud.adapter.FileTransBaseAdapter;
import cn.leeffee.feige.ui.cloud.adapter.UploadListAdapter;
import cn.leeffee.feige.ui.cloud.adapter.UploadListHolder;
import cn.leeffee.feige.ui.cloud.contract.PageUploadListContract;
import cn.leeffee.feige.ui.cloud.model.PageUploadListModelImpl;
import cn.leeffee.feige.ui.cloud.presenter.PageUploadListPresenterImpl;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.ui.cloud.service.UploadService;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
import cn.leeffee.feige.utils.ToastUtil;

/**
 * Created by lhfei on 2017/4/11.
 */

public class UploadListPage extends BasePage<PageUploadListPresenterImpl, PageUploadListModelImpl> implements PageUploadListContract.View {
    private static final String TAG = "UploadListPage";
    @BindView(R.id.trans_upload_list_lv)
    ListView mListView;
    @BindView(R.id.trans_upload_empty_tv)
    TextView mEmpty;
    @BindView(R.id.trans_upload_loading)
    LinearLayout mLoading;
    //    @BindView(R.id.trans_upload_bottom_menu)
    //    LinearLayout mBottomMenu;
    private List<UploadTask> mTasks;
    //    private UploadListAdapter mAdapter;
    private UploadListAdapter mAdapter;
    private static final String REQUEST_CODE_LIST_UPLOAD_QUEUE = "db_listUploadQueue";
    private UploadReceiver mUploadReceiver;

    class UploadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UploadTask task = null;
            try {
                task = (UploadTask) intent.getSerializableExtra("task");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            View view;
            if (task != null && task.getStatus() != 5) {
                Log.i(TAG, "Receiver [id=" + task.getId() + ",Status=" + task.getStatus() + ",Percent=" + task.getPercent() + "]");
                UploadTask mTask = null;
                UploadListHolder tag = new UploadListHolder(null);
                tag.taskId = task.getId();
                view = mListView.findViewWithTag(tag);
                for (int i = 0; i < mTasks.size(); i++) {
                    if (task.getId() == mTasks.get(i).getId()) {
                        mTask = mTasks.get(i);
                        break;
                    }
                }
                if (mTask != null) {
                    mTask.setStatus(task.getStatus());
                    mTask.setPercent(task.getPercent());
                    mTask.setUploadLength(task.getUploadLength());
                }
                if (null != view) {
                    // UploadListAdapter.ViewHolder holder = (UploadListAdapter.ViewHolder) view.getTag();
                    UploadListHolder holder = (UploadListHolder) view.getTag();
                    mAdapter.changeProgressStatus(holder, task);
                }
            }
            if (task != null)
                finallyMsg(task);

            int msgType = intent.getIntExtra(ITransferConstants.SERVER_MESSAGE, -1);
            if (msgType == ITransferConstants.NET_EXCEPTION) {
                ToastUtil.showShort("网络异常，请检查网络！");
            }
        }
    }

    public UploadListPage(Fragment frag) {
        super(frag);
    }

    public UploadListPage(Fragment frag, String title) {
        super(frag, title);
    }

    @Override
    public View createView() {
        return LayoutInflater.from(App.getAppContext()).inflate(R.layout.trans_upload_list, null);
    }

    @Override
    protected void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    protected void initView() {
        //        mAdapter = new UploadListAdapter(mAct);
        mAdapter = new UploadListAdapter(mFrag);
        mListView.setAdapter(mAdapter);
        // mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        //            @Override
        //            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //                ToastUtil.showShort("onItemLongClick");
        //                boolean multiMode = mAdapter.changeMode();
        //                changeMenu(multiMode);
        //                return true;
        //            }
        //        });
        mTasks = new ArrayList<>();
        mUploadReceiver = new UploadReceiver();
        App.getAppContext().registerReceiver(mUploadReceiver, new IntentFilter(UploadService.UPLOAD_RECEIVER_ACTION));
    }

    @Override
    public void initData() {
        Integer arrStatus[] = new Integer[]{1, 2, 3, 4, 10, 11, 12, 13};
        mPresenter.listUploadQueue(arrStatus, REQUEST_CODE_LIST_UPLOAD_QUEUE);
    }
    /**
     * 数据是否为空提示
     *
     * @param list
     */
    @Override
    public void isShowEmpty(List list) {
        if (list != null && list.size() > 0) {
            mEmpty.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public FileTransBaseAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 上传结束提示消息
     *
     * @param task 当前上传的任务
     */
    private void finallyMsg(UploadTask task) {
        switch (task.getStatus()) {
            case ITransferConstants.TRANSFER_FAIL_ERROR:
                ToastUtil.showShort("上传文件[" + task.getName() + "]失败");
                break;
            case ITransferConstants.FILE_NOT_EXIST:
                ToastUtil.showShort("上传文件[" + task.getName() + "]不存在");
                break;
            case ITransferConstants.SERVER_RESPONSE_ERROR:
                ToastUtil.showShort("上传[" + task.getName() + "]失败，请重新上传");
                break;
            case ITransferConstants.STATUS_CANCEL:
                ToastUtil.showShort("已停止上传[" + task.getName() + "]");
                break;
            case ITransferConstants.NET_EXCEPTION:
                ToastUtil.showShort("网络异常，停止上传[" + task.getName() + "]");
                break;
            case ITransferConstants.STATUS_FINISH:
                ToastUtil.showShort("[" + task.getName() + "]上传完成");
                break;
        }
    }

    @Override
    public void loadBefore(String requestCode) {
        if (requestCode.equals(REQUEST_CODE_LIST_UPLOAD_QUEUE)) {
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadSuccess(String requestCode, Object result) {
        if (requestCode.equals(REQUEST_CODE_LIST_UPLOAD_QUEUE)) {
            mTasks = (List<UploadTask>) result;
            mAdapter.setData(mTasks);
            mAdapter.notifyDataSetChanged();
            isShowEmpty(mTasks);
            mLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadFailure(String requestCode, String msg) {
        if (requestCode.equals(REQUEST_CODE_LIST_UPLOAD_QUEUE)) {
            mLoading.setVisibility(View.GONE);
        }
    }
}
