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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leeffee.feige.App;
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
        // context.getApplicationContext().startService(new Intent(context, BackupService.class));
    }

    public static void stopTransferService(Context context) {
        context.getApplicationContext().stopService(new Intent(context, DownloadService.class));
        context.getApplicationContext().stopService(new Intent(context, UploadService.class));
        //  context.getApplicationContext().stopService(new Intent(context, BackupService.class));
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

    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void setNoTitleBar(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * 设置全屏
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 取消全屏
     *
     * @param activity
     */
    public static void cancelFullScreen(Activity activity) {
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * @return 获取sd卡的可用空间
     */
    public static long getAvailableSd() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long availableBlocks = sf.getAvailableBlocks();
        long blockSize = sf.getBlockSize();
        return availableBlocks * blockSize;
    }

    /**
     * @return 获取Rom的可用空间
     */
    public static long getAvailableRom() {
        File path = Environment.getDataDirectory();
        StatFs sf = new StatFs(path.getPath());
        long availableBlocks = sf.getAvailableBlocks();
        long blockSize = sf.getBlockSize();
        return availableBlocks * blockSize;
    }

    /**
     * @return 获取Ram的可用空间
     */
    public static long getAvailableRam(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }

    /**
     * @return 获取Ram的总空间
     */
    public static long getTotalRam(Context context) {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(outInfo);
            return outInfo.totalMem;
        } else {
            File file = new File("/proc/meminfo");
            StringBuffer sb = new StringBuffer();
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                // 获取数字
                char[] charArray = line.toCharArray();
                for (char c : charArray) {
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    }
                }
                String string = sb.toString();
                // 转化成long
                long parseLong = Long.parseLong(string);
                return parseLong * 1024;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return 0;
        }
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /**
     * Drawable → Bitmap
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    /*
         * Bitmap → Drawable
		 */
    @SuppressWarnings("deprecation")
    public static Drawable bitmap2Drawable(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        BitmapDrawable bd = new BitmapDrawable(bm);
        bd.setTargetDensity(bm.getDensity());
        return new BitmapDrawable(bm);
    }
}
