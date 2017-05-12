package cn.leeffee.feige.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.leeffee.feige.App;
import cn.leeffee.feige.R;
import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.constants.AppConstants;
import cn.leeffee.feige.ui.cloud.entity.USpaceFile;
import cn.leeffee.feige.ui.cloud.exception.ClientIOException;
import cn.leeffee.feige.ui.cloud.exception.IClientExceptionCode;

import static cn.leeffee.feige.ui.cloud.constants.AppConstants.ROOT_PATH;


/**
 * @author lvhf 20170418
 */
public class FileUtil {
    private final static int BUFFER_SIZE = 1024 * 8;
    private static String SDCardRoot;
    //private File appRoot; // 初始化为 : /mnt/sdcard/eCloud
    private static FileUtil mFileUtil = null;
    private static String mLocalPath;

    public static String getSDCardRoot() {
        try {
            SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            SDCardRoot = "/sdcard";
        }
        return SDCardRoot;
    }

    /**
     * 构造函数
     *
     * @param appRootName 根目录名  YoPan或USpace
     */
    private FileUtil(String appRootName) {
        SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        mLocalPath = SDCardRoot + File.separator + appRootName + File.separator;
    }

    public static FileUtil newInstance() throws ClientIOException {
        if (validateSDCard()) {
            if (mFileUtil == null) {
                mFileUtil = new FileUtil(PropertyUtil.getInstance().getRoot());
            }
        } else {
            throw new ClientIOException(IClientExceptionCode.SDCARD_NOT_AVAILABLE_ERROR, "sdcard 不可用错误");
        }
        return mFileUtil;
    }

    public static void rebuild() {
        mFileUtil = null;
    }

    /**
     * 创建本地根目录
     *
     * @return 成功返回根路径  失败返回null
     */
    public String makeUSpaceLocalRoot() {
        File file = new File(mLocalPath);
        return file.mkdirs() ? mLocalPath : null;
    }

    /**
     * 创建本地共享目录
     *
     * @return 成功返回创建的路径  失败返回null
     */
    public String makeUSpaceSharedRoot() {
        String root = mLocalPath + PropertyUtil.getInstance().getLocalSharedRoot() + File.separator;
        File file = new File(root);
        return file.mkdirs() ? root : null;
    }

    public String getUSpaceLocalRoot() {
        return mLocalPath;
    }

    //    public File getInitAppRoot() {
    //        return this.appRoot;
    //    }

    /**
     * 根据组名获取组文件根目录
     *
     * @param groupName
     * @return
     */
    public static String getUSpaceLocalGroupRoot(String groupName) {
        String localRoot = getUserRoot();
        String path = localRoot + File.separator + AppConstants.TITLE_MY_GROUP + File.separator + groupName;
        return path;
    }

    public File createUserFileInSDCard(File file) throws ClientIOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
        }
        System.out.println(file);
        return file;
    }

    public File createAbsoluteFileInSDCard(String absoluteFileName) throws ClientIOException {
        File file = new File(absoluteFileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
        }
        return file;
    }

    public File createUspaceFileInSDCard(String diskPath) throws ClientIOException {
        File file = new File(diskPath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
        }
        return file;
    }

    /**
     * 创建服务于断点续传的临时文件(以.tmp.1结尾)
     *
     * @param diskPath
     * @return
     * @throws ClientIOException
     */
    public File createBPTmpFileInSDCard(String diskPath) throws ClientIOException {
        File file = new File(diskPath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "创建文件父目录失败");
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
            }
        }
        return file;
    }

    /**
     * 获取断点续传临时文件大小，获取偏移量
     *
     * @param diskPath
     * @return
     */
    public long getBPTmpFileSizeInSDCard(String diskPath) {
        File file = new File(diskPath);
        if (!file.exists()) {
            return 0L;
        } else {
            return file.length();
        }
    }

    public File createSharedFileInSDCard(String filename) throws ClientIOException {
        File file = new File(filename);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
        }
        return file;
    }

    /**
     * 判断文件路径是否存在于SDCard
     *
     * @param
     * @return
     * @throws
     */
    public Boolean isPathExist(String path) {
        File file = new File(SDCardRoot + path);
        System.out.println("isAppRootExist = " + (file.exists() == true ? "Yes" : "NO"));
        return file.exists();
    }

    public Integer writeSharedFile2SDCard(String filename, InputStream in) throws ClientIOException {
        File file = null;
        OutputStream out = null;

        try {
            file = createSharedFileInSDCard(filename);
            out = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                System.out.println("read in fuffer count = " + count + ",buffer total length = " + buffer.length);
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "网络流写入错误");
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件流关闭异常");
            }
        }
        return file != null ? 0 : -1;
    }

    public Integer writeUspaceFile2SDCard(String diskPath, InputStream in) throws ClientIOException {
        File file = null;
        OutputStream out = null;

        try {
            file = createUspaceFileInSDCard(diskPath);
            out = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                System.out.println("read in fuffer count = " + count + ",buffer total length = " + buffer.length);
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "网络流写入错误");
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (Exception e) {
                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件流关闭异常");
            }
        }
        return file != null ? 0 : -1;
    }

    //    public File writeBitmap2SDCard(String fileName, Bitmap bm) throws ClientIOException {
    //        File file = createAbsoluteFileInSDCard(fileName);
    //        try {
    //            System.out.print("yuanshi:"+bm.getByteCount());
    //            ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //
    //            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    //            InputStream is = new ByteArrayInputStream(bos.toByteArray());
    //            System.out.print("yuanshi:"+bos.size());
    //
    //            bos.flush();
    //            bos.close();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        return file;
    //    }
    public File writeBitmap2SDCard(String fileName, Bitmap bitmap) throws ClientIOException {
        File file = createAbsoluteFileInSDCard(fileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] photoBytes = baos.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(photoBytes);
            fos.close();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "sdcard 存储错误");
        }
        return file;
    }

    //	public File saveBitmap2Png(String savePath, Bitmap bitmap) throws ClientIOException {
    //		File file = createAbsoluteFileInSDCard(savePath);
    //		FileOutputStream out = null;
    //		try {
    //			out = new FileOutputStream(file);
    //			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
    //				out.flush();
    //			}
    //			return file;
    //		} catch (IOException e) {
    //			throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "sdcard 存储错误");
    //		} finally {
    //			if (out != null) {
    //				try {
    //					out.close();
    //				} catch (IOException e) {
    //					e.printStackTrace();
    //				}
    //			}
    //		}
    //	}

    public File saveBitmap2Jpeg(String savePath, Bitmap bitmap) throws ClientIOException {
        File file = createAbsoluteFileInSDCard(savePath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
            }
            return file;
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "sdcard 存储错误");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //	public File saveBitmap(String savePath, Bitmap bitmap) throws ClientIOException {
    //		File file = createAbsoluteFileInSDCard(savePath);
    //		File parentFile = file.getParentFile();
    //		File[] childFiles = parentFile.listFiles();
    //		for (File f : childFiles) {
    //			f.delete();
    //		}
    //		FileOutputStream out = null;
    //		try {
    //			out = new FileOutputStream(file);
    //			int size = bitmap.getHeight() * bitmap.getRowBytes();
    //			ByteBuffer dst = ByteBuffer.allocate(size);
    //			bitmap.copyPixelsToBuffer(dst);
    //			out.write(dst.array());
    //			out.flush();
    //			return file;
    //		} catch (IOException e) {
    //			throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "sdcard 存储错误");
    //		} finally {
    //			if (out != null) {
    //				try {
    //					out.close();
    //				} catch (IOException e) {
    //					e.printStackTrace();
    //				}
    //			}
    //		}
    //	}

    //	public Bitmap imgResize(String savePath, int width, int height) {
    //		BitmapFactory.Options opts = new BitmapFactory.Options();
    //		opts.inJustDecodeBounds = true;
    //		BitmapFactory.decodeFile(savePath, opts);
    //		if (opts.outWidth >= opts.outHeight) {
    //			height = (int) Math.round((opts.outHeight * width * 1.0 / opts.outWidth));
    //		} else {
    //			width = (int) Math.round((opts.outWidth * height * 1.0 / opts.outHeight));
    //		}
    //		opts.inJustDecodeBounds = false;
    //		opts.inSampleSize = 2;
    //		Bitmap bitmap = BitmapFactory.decodeFile(savePath, opts);
    //		return Bitmap.createScaledBitmap(bitmap , width, height, false);
    //	}

    //	public Bitmap imgResize(String savePath, int width, int height) {
    //		BitmapFactory.Options opts = new BitmapFactory.Options();
    //		opts.inJustDecodeBounds = true;
    //		BitmapFactory.decodeFile(savePath, opts);
    //		float ratio = 0f;
    //		if (opts.outWidth >= opts.outHeight) {
    //			ratio = (float) opts.outWidth / width;
    //			height = (int) Math.round(opts.outHeight * 1.0 / ratio);
    //		} else {
    //			ratio = (float) opts.outHeight / height;
    //			width = (int) Math.round( opts.outHeight * 1.0 / ratio);
    //		}
    ////		opts.inJustDecodeBounds = false;
    ////		opts.inSampleSize = 2;
    ////		Bitmap bitmap = BitmapFactory.decodeFile(savePath, opts);
    ////		return Bitmap.createScaledBitmap(bitmap, width, height, false);
    //
    //		BitmapFactory.Options newOpts = new BitmapFactory.Options();
    //		if (ratio < 2) {
    //			ratio = 2;
    //		}
    //		newOpts.inSampleSize = (int) ratio;
    //		newOpts.inJustDecodeBounds = false;
    //		newOpts.outHeight = height;
    //		newOpts.outWidth = width;
    //		return BitmapFactory.decodeFile(savePath, newOpts);
    //
    ////		BitmapFactory.Options opts = new BitmapFactory.Options();
    ////		BitmapFactory.decodeFile(savePath, opts);
    ////		int srcWidth = opts.outWidth;
    ////		int srcHeight = opts.outHeight;
    ////		int destWidth = 0;
    ////		int destHeight = 0;
    ////		double ratio = 0.0;// 缩放的比例
    ////		// 按比例计算缩放后的图片大小，maxLength是长或宽允许的最大长度
    ////		if (srcWidth >= srcHeight) {
    ////			ratio = srcWidth / width;
    ////			destWidth = width;
    ////			destHeight = (int) (srcHeight / ratio);
    ////		} else {
    ////			ratio = srcHeight / height;
    ////			destHeight = height;
    ////			destWidth = (int) (srcWidth / ratio);
    ////		}
    ////		BitmapFactory.Options newOpts = new BitmapFactory.Options();
    ////		newOpts.inSampleSize = (int) ratio + 1;
    ////		newOpts.inJustDecodeBounds = false;
    ////		newOpts.outHeight = destHeight;
    ////		newOpts.outWidth = destWidth;
    ////		return BitmapFactory.decodeFile(savePath, newOpts);
    //	}

    //	public Bitmap imgResize(Bitmap bmp, String savePath, int width, int height) throws ClientIOException {
    //		File file = saveBitmap2sdcard(savePath, bmp);
    //		return imgResize(file.toString(), width, height);
    //	}

//    public Integer write2SDCard(File file, InputStream in, FileHandler handler) throws IOException {
//        OutputStream out = null;
//        try {
//            file = createUserFileInSDCard(file);
//            out = new FileOutputStream(file);
//            byte[] buffer = new byte[BUFFER_SIZE];
//            int count = 0;
//            long len = 0;
//            while ((count = in.read(buffer, 0, buffer.length)) > 0 && !handler.isCancel()) {
//                len += count;
//                Log.i("write2SDCard", "read in fuffer=" + count + ", length=" + len);
//                if (handler != null) {
//                    handler.send(len);
//                }
//                out.write(buffer, 0, count);
//                out.flush();
//            }
//        } catch (IOException e) {
//            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "sdcard 存储错误");
//        } finally {
//            if (handler.isCancel() && file.exists()) {
//                file.delete();
//            }
//            try {
//                if (null != out) {
//                    out.close();
//                }
//                if (null != in) {
//                    in.close();
//                }
//            } catch (IOException e) {
//                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件写入流关闭异常");
//            }
//        }
//        return file != null ? 0 : -1;
//    }

    private static boolean validateSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static byte[] file2Byte(String filePath) throws ClientIOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));

            byte[] temp = new byte[BUFFER_SIZE];
            int size = 0;
            while ((size = in.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            in.close();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件流转换错误");
        }
        return out.toByteArray();
    }

    //    public boolean isSharedDownloadFileExist(String path) {
    //        File file = new File(path);
    //        return file.exists();
    //    }

    public File createFileInCacheDir(String filePath) throws ClientIOException {
        File file = new File(filePath);
        System.out.println(file);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件创建失败");
        }
        return file;
    }

    public Integer write2CacheDir(OutputStream out, InputStream in) throws ClientIOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                System.out.println("read in fuffer count = " + count + ",buffer total length = " + buffer.length);
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (IOException e) {
            throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "网络流写入失败");
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                throw new ClientIOException(IClientExceptionCode.CLIENT_IOEXCEPTION_ERROR, "文件流关闭错误");
            }
        }
        return 0;
    }

    public static String getFileJoinName(List<File> files) {
        StringBuffer buff = new StringBuffer();
        if (files.size() > 0) {
            buff.append(files.get(0).getName());
        }
        return buff.toString();
    }

    public String createPhotoCapturedName(String uploadPath) {
        File file = new File(uploadPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return uploadPath + (uploadPath.endsWith("/") ? "" : File.separator) + getRandomFileName() + ".jpg";
    }

    public static String getRandomFileName() {
        return DateUtil.getCurrentTime("yyyyMMddHHmmss");
    }

    /**
     * 判断是否有足够的空间供下载
     *
     * @param downloadSize
     * @return
     */
    public boolean isSDCardEnoughForDownload(long downloadSize) {
        StatFs statFs = new StatFs(SDCardRoot);

        // sd卡可用分区数
        int avCounts = statFs.getAvailableBlocks();

        // 一个分区数的大小
        long blockSize = statFs.getBlockSize();

        // sd卡可用空间
        long spaceLeft = avCounts * blockSize;

        if (spaceLeft < downloadSize) {
            return false;
        }

        return true;
    }

    public static List<File> getAllSubFiles(String filePath) {
        List<File> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return list;
        }
        if (file.isFile()) {
            list.add(file);
        } else {
            File[] subFiles = file.listFiles();
            if (subFiles != null && subFiles.length > 0) {
                for (File tmpFile : subFiles) {
                    if (tmpFile.isFile()) {
                        list.add(tmpFile);
                    } else {
                        list.addAll(getAllSubFiles(tmpFile.getPath()));
                    }
                }
            }
        }
        return list;
    }

    public static int getFileBitmapResource(USpaceFile fileInfo) {
        int resource = R.mipmap.uspace_default_file;
        String name = fileInfo.getName();
        if (fileInfo.isFolder()) {
            resource = R.mipmap.uspace_default_folder;
        } else {
            resource = getFileBitmapResource(name);
        }
        return resource;
    }

    public static int getFileBitmapResource(String name) {
        int resource = R.mipmap.uspace_default_file;
        if (FileViewer.isImage(name)) {
            resource = R.mipmap.uspace_image_file;
        } else if (FileViewer.isPdf(name)) {
            resource = R.mipmap.uspace_pdf_file;
        } else if (FileViewer.isText(name)) {
            resource = R.mipmap.uspace_txt_file;
        } else if (FileViewer.isWord(name)) {
            resource = R.mipmap.uspace_word_file;
        } else if (FileViewer.isPpt(name)) {
            resource = R.mipmap.uspace_ppt_file;
        } else if (FileViewer.isExcel(name)) {
            resource = R.mipmap.uspace_excel_file;
        } else if (FileViewer.isZip(name)) {
            resource = R.mipmap.uspace_zip_file;
        } else if (FileViewer.isMusic(name)) {
            resource = R.mipmap.uspace_music_file;
        } else if (FileViewer.isVideo(name)) {
            resource = R.mipmap.uspace_video_file;
        } else if (FileViewer.isSwf(name)) {
            resource = R.mipmap.uspace_swf_file;
        }
        return resource;
    }

    /**
     * 用户缓存路径 包括指定的账号  如../uspace/admin@uit
     *
     * @param account 指定某个账号的缓存
     * @return
     */
    public static String getUserRoot(String account) {
        String dir = PropCacheManager.getInstance().getCacheDir(account, App.getAppContext());
        if (dir == null || dir.length() == 0) {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PropertyUtil.getInstance().getRoot() + File.separator + account;
        }
        return dir;
    }

    /**
     * 用户缓存路径  默认为当前登录的账户
     *
     * @return
     */
    public static String getUserRoot() {
        String account = SPUtil.getString(AppConfig.ACCOUNT);
        return getUserRoot(account);
    }

    /**
     * 判断文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean isFileExist(USpaceFile file) {
        String path;
        if (file.getIsGroupFile() == AppConstants.GROUP_FILE) {//群组文件是否存在
            path = getUSpaceLocalGroupRoot(file.getGroupName()) + file.getDiskPath();
        } else {//个人文件是否存在
            path = getUserRoot() + file.getDiskPath();
        }
        try {
            return isFileExist(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断文件是否存在
     *
     * @param localAbsolutePath
     * @return 存在返回true 不存在返回false
     */
    public static boolean isFileExist(String localAbsolutePath) {
        File file = new File(localAbsolutePath);
        return file.exists();
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void RecursionDeleteFile(File file) {
        if (file == null) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param filePath 要删除的目录
     */
    public static void RecursionDeleteFile(String filePath) {
        RecursionDeleteFile(filePath != null ? new File(filePath) : null);
    }

    /**
     * 获取硬盘路径获取上一级路径/目录
     *
     * @param diskPath 根据diskPath 获取
     * @return
     */
    public static String getPrePath(String diskPath) {
        if (diskPath == null)
            return ROOT_PATH;
        String prePath = diskPath.substring(0, diskPath.lastIndexOf('/'));
        if ("".equals(prePath)) {
            prePath = ROOT_PATH;
        }
        return prePath;
    }

    /**
     * 列出给定目录下的本地文件夹
     *
     * @param pathname
     * @return
     */
    public static List<USpaceFile> listFolderInfos(String pathname) {
        File parent = new File(pathname);
        List<USpaceFile> uSpaceFiles = new ArrayList<>();
        if (parent != null && parent.isDirectory() && parent.listFiles() != null) {
            USpaceFile uf;
            for (File file : parent.listFiles()) {
                if (file != null) {
                    //只列出文件夹
                    if (file.isDirectory()) {
                        uf = new USpaceFile(file.getName(), file.length(), file.isDirectory(), false, file.getPath(), false);
                        uf.setModifyTime(new Timestamp(file.lastModified()));
                        uSpaceFiles.add(uf);
                    }
                }
            }
            Collections.sort(uSpaceFiles);
        }

        return uSpaceFiles;
    }
}
