package cn.leeffee.feige.ui.cloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;

/**
 * Created by lhfei on 2017/3/30.
 */

public class UploadMenusAdapter extends BaseAdapter {
    private List<HashMap<String, Object>> uploadMenuData = new ArrayList<>();
    private LayoutInflater inflater;

    public UploadMenusAdapter(Context context) {
        initUploadMenus();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return uploadMenuData.size();
    }

    @Override
    public HashMap<String, Object> getItem(int position) {
        return uploadMenuData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        final HashMap<String, Object> item = uploadMenuData.get(position);
        if (view == null) {
            view = inflater.inflate(R.layout.upload_menus_item, null);
            holder = new ViewHolder();
            holder.item = (TextView) view.findViewById(R.id.text_item);
            holder.icon = (ImageView) view.findViewById(R.id.image_item);

            int key = (Integer) item.get("index");
            switch (key) {
                case AppConstants.UPLOAD_CAPTURE:
                    holder.icon.setBackgroundResource(R.mipmap.shoot);
                    break;
                case AppConstants.UPLOAD_PICS:
                    holder.icon.setBackgroundResource(R.mipmap.picture);
                    break;
                case AppConstants.UPLOAD_DOCS:
                    holder.icon.setBackgroundResource(R.mipmap.document);
                    break;
                case AppConstants.UPLOAD_MUSICS:
                    holder.icon.setBackgroundResource(R.mipmap.music);
                    break;
                case AppConstants.UPLOAD_VIDEOS:
                    holder.icon.setBackgroundResource(R.mipmap.video);
                    break;
                case AppConstants.UPLOAD_ALL:
                    holder.icon.setBackgroundResource(R.mipmap.other);
                    break;
            }
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.item.setText((String) item.get("name"));

        //			int pHight = gridView.getHeight();
        //		GridView.LayoutParams params = new GridView.LayoutParams(LayoutParams.FILL_PARENT, (pHight - 12) / 3);
        //			view.setLayoutParams(params);
        return view;
    }

    static class ViewHolder {
        TextView item;
        ImageView icon;
    }

    private void initUploadMenus() {
        HashMap<String, Object> capture = new HashMap<>();
        capture.put("name", "即拍即传");
        capture.put("index", AppConstants.UPLOAD_CAPTURE);
        uploadMenuData.add(capture);

        HashMap<String, Object> pics = new HashMap<>();
        pics.put("name", "图片");
        pics.put("index", AppConstants.UPLOAD_PICS);
        uploadMenuData.add(pics);

        HashMap<String, Object> docs = new HashMap<>();
        docs.put("name", "文档");
        docs.put("index", AppConstants.UPLOAD_DOCS);
        uploadMenuData.add(docs);

        HashMap<String, Object> musics = new HashMap<>();
        musics.put("name", "音乐");
        musics.put("index", AppConstants.UPLOAD_MUSICS);
        uploadMenuData.add(musics);

        HashMap<String, Object> videos = new HashMap<>();
        videos.put("name", "视频");
        videos.put("index", AppConstants.UPLOAD_VIDEOS);
        uploadMenuData.add(videos);

        HashMap<String, Object> all = new HashMap<>();
        all.put("name", "所有文件");
        all.put("index", AppConstants.UPLOAD_ALL);
        uploadMenuData.add(all);
    }
}
