package cn.leeffee.feige.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhfei on 2017/3/17.
 */

public class UploadTypeUtil {
    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws Exception
     */
    public static ArrayList<String> getAllMusicUrl(Context context) {
        ArrayList<String> musics = null;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/mpeg", "audio/x-ms-wma"}, null);

        musics = new ArrayList<String>();

        if (cursor.moveToFirst()) {
            do {
                // 文件路径
                if (cursor.getString(0) != null) {
                    musics.add(cursor.getString(0));
                }
            } while (cursor.moveToNext());

            cursor.close();

        }
        return musics;
    }

    /**
     * 获取sd卡所有的视频文件
     *
     * @return
     * @throws Exception
     */
    public static ArrayList<String> getAllVideoUrl(Context context) {
        ArrayList<String> videos = null;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DATA}, null,
                null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);

        videos = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            do {
                // 文件路径
                if (cursor.getString(0) != null) {
                    videos.add(cursor.getString(0));
                }
            } while (cursor.moveToNext());

            cursor.close();

        }
        return videos;
    }

    /**
     * 存储卡获取 指定文件
     * @param context
     * @param extension
     * @return
     */
    public static List<String> getSpecificTypeFiles(Context context, String[] extension){
        List<String> fileInfoList = new ArrayList<String>();

        //内存卡文件的Uri
        Uri fileUri= MediaStore.Files.getContentUri("external");
        //筛选列，这里只筛选了：文件路径和含后缀的文件名
        String[] projection=new String[]{
                MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE
        };

        //构造筛选条件语句
        String selection="";
        for(int i=0;i<extension.length;i++)
        {
            if(i!=0)
            {
                selection=selection+" OR ";
            }
            selection=selection+ MediaStore.Files.FileColumns.DATA+" LIKE '%"+extension[i]+"'";
        }
        //按时间降序条件
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED;

        Cursor cursor = context.getContentResolver().query(fileUri, projection, selection, null, sortOrder);
        if(cursor != null){
            while (cursor.moveToNext()){
                try{
                    String data = cursor.getString(0);
                    fileInfoList.add(data);
                }catch (Exception e){
                    Log.i("FileUtils", "------>>>" + e.getMessage());
                }

            }
        }
        Log.i("UploadTypeUtil", "getSize ===>>> " + fileInfoList.size());
        return fileInfoList;
    }
}
