package cn.leeffee.feige.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.adapter.DirListAdapter;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.utils.FileUtil;
import cn.leeffee.feige.utils.ToastUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lhfei on 2017/4/7.
 */

public class ChoiceLocalDictionaryAlertDialog extends AlertDialog implements AdapterView.OnItemClickListener {
    LinearLayout mLoadingLayout;
    TextView mTitle;
    Button btnOk;
    Button btnCancel;
    ListView mListView;

    DirListAdapter adapter;
    //  FragCloudPresenterImpl filePresenter;
    private String prefix = "";
    private String destPath = FileUtil.getSDCardRoot();

    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置前缀
     *
     * @param prefix 不以 "/" 结尾
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        mTitle.setText(prefix);
    }

    public String getDestPath() {
        return destPath;
    }
//
//    public void setDestPath(String destPath) {
//        this.destPath = destPath;
//    }

    public void setOkClickListener(View.OnClickListener listener) {
        btnOk.setOnClickListener(listener);
    }

    public DirListAdapter getDirListAdapter() {
        return adapter;
    }

    public void setDirListAdapter(DirListAdapter adapter) {
        this.adapter = adapter;
    }

    public ChoiceLocalDictionaryAlertDialog(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater lay = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = lay.inflate(R.layout.dialog_change_dir, null);
        mLoadingLayout = (LinearLayout) v.findViewById(R.id.loading_layout);
        mTitle = (TextView) v.findViewById(R.id.dialog_title_dir_tv);
        btnOk = (Button) v.findViewById(R.id.dialog_ok_btn);
        btnCancel = (Button) v.findViewById(R.id.dialog_cancel_btn);
        mListView = (ListView) v.findViewById(R.id.dialog_dir_list_lv);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceLocalDictionaryAlertDialog.this.dismiss();
            }
        });
        setView(v);
        Window win = this.getWindow();
        WindowManager.LayoutParams params = win.getAttributes(); // 获取对话框当前的参数值
        params.gravity = Gravity.CENTER;
        win.setAttributes(params);

        adapter = new DirListAdapter(App.getAppContext());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        FileUtil.listFolderInfos(destPath);
        load();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        USpaceFile fileInfo = (USpaceFile) adapter.getItem(position);
        String path = fileInfo.getDiskPath();
        if (fileInfo.isParent()) {
            path = path.substring(0, path.lastIndexOf('/'));
            if ("".equals(path)) {
                path = "/";
            }
        }
        adapter.getData().clear();
        if (!path.equals(FileUtil.getSDCardRoot())) {
            adapter.getData().add(0, new USpaceFile(App.getAppContext().getText(R.string.strBackToParent).toString(), 0, true, true, path, false));
        }
        destPath = path;
        mTitle.setText(prefix + path);
        adapter.notifyDataSetChanged();
        load();
    }

    private void load() {
        Observable.create(new ObservableOnSubscribe<List<USpaceFile>>() {

            @Override
            public void subscribe(ObservableEmitter<List<USpaceFile>> e) throws Exception {
                e.onNext(FileUtil.listFolderInfos(destPath));
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<USpaceFile>>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull List<USpaceFile> uSpaceFiles) throws Exception {
                adapter.getData().addAll(uSpaceFiles);
                adapter.notifyDataSetChanged();
                mLoadingLayout.setVisibility(View.GONE);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                ToastUtil.showShort("加载失败");
                mLoadingLayout.setVisibility(View.GONE);
            }
        });
    }
}
