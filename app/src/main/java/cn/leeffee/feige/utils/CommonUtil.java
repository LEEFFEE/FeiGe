/*    
 * Copyright (c) 2012 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      Common.java
 * 作者:     Jacky Wang
 * 创建日期： 2012-1-6 下午04:55:16
 * 版本：           
 *
 */
package cn.leeffee.feige.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leeffee.feige.App;
import cn.leeffee.feige.ui.cloud.service.BackupService;
import cn.leeffee.feige.ui.cloud.service.DownloadService;
import cn.leeffee.feige.ui.cloud.service.UploadService;

/**
 * @author lvhf
 */
public class CommonUtil {
    /**
     * 开启服务
     *
     * @param context
     */
    public static void startTransferService(Context context) {
        context.getApplicationContext().startService(new Intent(context, DownloadService.class));
        context.getApplicationContext().startService(new Intent(context, UploadService.class));
        context.getApplicationContext().startService(new Intent(context, BackupService.class));
    }

    public static void stopTransferService(Context context) {
        context.getApplicationContext().stopService(new Intent(context, DownloadService.class));
        context.getApplicationContext().stopService(new Intent(context, UploadService.class));
        context.getApplicationContext().stopService(new Intent(context, BackupService.class));
    }

    /**
     * 得到系统时间
     *
     * @return 返回HH:mm:ss 格式字符串
     */
    public static String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 从URI中获取图片的真实路径
     *
     * @param imageUri 图片URI
     * @return 真实路径
     */
    public static String getRealPathFromURI(Uri imageUri) {
        String imagePath = null;
        if (Build.VERSION.SDK_INT >= 19) {
            if (DocumentsContract.isDocumentUri(App.getAppContext(), imageUri)) {
                //如果是document类型的uri,则通过document id处理
                String docId = DocumentsContract.getDocumentId(imageUri);
                if ("com.android.providers.media.documents".equals(imageUri.getAuthority())) {
                    String id = docId.split(":")[1];//解析出数字格式的id
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.downloads.documents".equals(imageUri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
                //如果是content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(imageUri, null);
            } else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
                //如果是file类型的Uri,直接获取图片路径即可
                imagePath = imageUri.getPath();
            }
        } else {
            imagePath = getImagePath(imageUri, null);
        }
        return imagePath;
    }

    /**
     * 获取图片的真实路径
     *
     * @param uri       URI
     * @param selection 选择条件
     * @return
     */
    private static String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection老获取真实的图片路径
        Cursor cursor = App.getAppContext().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
