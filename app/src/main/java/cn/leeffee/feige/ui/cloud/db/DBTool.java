package cn.leeffee.feige.ui.cloud.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.leeffee.feige.ui.cloud.constants.AppConfig;
import cn.leeffee.feige.ui.cloud.service.BackupTask;
import cn.leeffee.feige.ui.cloud.service.DownloadTask;
import cn.leeffee.feige.ui.cloud.service.ITransferConstants;
import cn.leeffee.feige.ui.cloud.service.UploadTask;
import cn.leeffee.feige.utils.SPUtil;

/**
 * @author lvhf
 */
public class DBTool {
    private DBHelper dbHelper;
    private final static String TAG = "DBTool";
    private static byte[] lock = new byte[0];
    private static byte[] sig = new byte[0];
    private String currentUserName;
    private String userFilter;
    public final static String CACHE_DIR = "cacheDir"; // 缓存目录
    public final static String WIFI_STATUS = "wifiStatus";
    public final static String BACKUP_STATUS = "backupStatus";

    private DBTool() {
    }

    public DBTool(Context context) {
        //		dbHelper = new DBHelper(context);
        dbHelper = DBHelper.getInstance(context);
        currentUserName = SPUtil.getString(AppConfig.ACCOUNT);
        userFilter = " userName='" + currentUserName + "'";
    }

    public String queryCacheDir(String account) {
        String dir = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select value from cache where key='" + CACHE_DIR + "' AND account='" + account + "'";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                dir = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            // db.close();
        }
        return dir;
    }

    public void saveCacheDir(String dir, String account) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                String sql = "delete from cache where key='" + CACHE_DIR + "' AND account='" + account + "'";
                db.execSQL(sql);
                sql = "insert into cache(key, value, account) values (?,?,?)";
                Object[] objs = new Object[]{CACHE_DIR, dir, account};
                db.execSQL(sql, objs);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                if (db != null) {
                    db.endTransaction();
                    //db.close();
                }
            }
        }
    }

    public void saveCacheWifiSetting(String opt, String account) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                String sql = "delete from cache where key='" + WIFI_STATUS + "' AND account='" + account + "'";
                db.execSQL(sql);
                sql = "insert into cache(key, value, account) values (?,?,?)";
                Object[] objs = new Object[]{WIFI_STATUS, opt, account};
                db.execSQL(sql, objs);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //db.close();
            }
        }
    }

    public String queryCacheWifiSetting(String account) {
        String dir = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select value from cache where key='" + WIFI_STATUS + "' AND account='" + account + "'";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                dir = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //db.close();
        }
        return dir;
    }

    public void saveCacheBackupSetting(String opt, String account) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                String sql = "delete from cache where key='" + BACKUP_STATUS + "' AND account='" + account + "'";
                db.execSQL(sql);
                sql = "insert into cache(key, value, account) values (?,?,?)";
                Object[] objs = new Object[]{BACKUP_STATUS, opt, account};
                db.execSQL(sql, objs);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                // db.close();
            }
        }
    }

    public String queryCacheBackupSetting(String account) {
        String dir = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select value from cache where key='" + BACKUP_STATUS + "' AND account='" + account + "'";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                dir = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //db.close();
        }
        return dir;
    }

    /**
     * 获取下载队列
     *
     * @param status
     * @return
     */
    public List<DownloadTask> listDownloadQueue(Integer... status) {
        List<DownloadTask> list = new ArrayList<DownloadTask>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String filter = "";
            for (Integer s : status) {
                filter += " status=" + s + " OR ";
            }
            filter = filter.substring(0, filter.lastIndexOf("OR"));

            String sql = "select id, name, type, path, savePath, version, offset, addQueueTime, finishTime, fileLength, downloadLength, status, code, isGroupFile, groupId, ownId from downloadQueue where " + userFilter + " AND (" + filter + ") order by addQueueTime DESC ";
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                DownloadTask task = new DownloadTask();
                task.setId(cursor.getInt(0));
                task.setName(cursor.getString(1));
                task.setType(cursor.getInt(2));
                task.setPath(cursor.getString(3));
                task.setSavePath(cursor.getString(4));

                task.setVersion(cursor.getInt(5));
                task.setOffset(cursor.getLong(6));
                task.setAddQueueTime(cursor.getString(7));
                task.setFinishTime(cursor.getString(8));
                task.setFileLength(cursor.getLong(9));
                task.setDownloadLength(cursor.getLong(10));
                task.setStatus(cursor.getInt(11));
                task.setCode(cursor.getString(12));
                task.setIsGroupFile(cursor.getInt(13));
                task.setGroupId(cursor.getString(14));
                task.setOwnId(cursor.getString(15));

                list.add(task);
            }
            Collections.sort(list, new TaskComparator());
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return list;
    }

    public void saveDownloadTask(DownloadTask task) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                String sql = "insert into downloadQueue(name, type, path, code, savePath, version, offset, status, fileLength, userName, isGroupFile, groupId, ownId) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                Object[] objs = new Object[]{task.getName(), task.getType(), task.getPath(), task.getCode(), task.getSavePath(), task.getVersion(), task.getOffset(), task.getStatus(), task.getFileLength(), currentUserName, task.getIsGroupFile(), task.getGroupId(), task.getOwnId()};
                db.execSQL(sql, objs);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //			db.close();
            }
        }
    }

    public List<UploadTask> listUploadQueue(Integer... status) {
        List<UploadTask> list = new ArrayList<UploadTask>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String filter = "";
            for (Integer s : status) {
                filter += " status=" + s + " OR ";
            }
            filter = filter.substring(0, filter.lastIndexOf("OR"));

            String sql = "select id, remotePath, status, localPath, version, offset, addQueueTime, url, name, uploadLength, finishTime,fileLength, isGroupFile, groupId from uploadQueue where " + userFilter + " AND (" + filter + ") order by addQueueTime DESC";
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                UploadTask task = new UploadTask();
                task.setId(cursor.getInt(0));
                task.setRemotePath(cursor.getString(1));
                task.setStatus(cursor.getInt(2));
                task.setLocalPath(cursor.getString(3));
                task.setVersion(cursor.getInt(4));
                task.setOffset(cursor.getLong(5));
                task.setAddQueueTime(cursor.getString(6));
                task.setUrl(cursor.getString(7));
                task.setName(cursor.getString(8));
                task.setUploadLength(cursor.getLong(9));
                task.setFinishTime(cursor.getString(10));
                task.setFileLength(cursor.getLong(11));
                task.setIsGroupFile(cursor.getInt(12));
                task.setGroupId(cursor.getString(13));

                list.add(task);
            }
            Collections.sort(list, new TaskComparator());
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return list;
    }

    //	public synchronized void saveUploadTask(UploadTask task) {
    //		SQLiteDatabase db = null;
    //		try {
    //			db = dbHelper.getWritableDatabase();
    //			db.beginTransaction();
    //			String sql = "insert into uploadQueue(name,remotePath,status,localPath,version,offset,url,fileLength,userName, isGroupFile, groupId) values (?,?,?,?,?,?,?,?,?,?,?)";
    //			db.execSQL(sql, new Object[] { task.getName(), task.getRemotePath(), task.getStatus(), task.getLocalPath(), task.getVersion(), task.getOffset(), task.getUrl(), task.getFileLength(), currentUserName, task.getIsGroupFile(), task.getGroupId() });
    //			db.setTransactionSuccessful();
    //		} catch (Exception e) {
    //			Log.i(TAG, e.toString(), e);
    //		} finally {
    //			db.endTransaction();
    //			db.close();
    //		}
    //	}

    public void saveUploadTask(List<UploadTask> taskList) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                for (UploadTask task : taskList) {
                    String sql = "insert into uploadQueue(name,remotePath,status,localPath,version,offset,url,fileLength,userName, isGroupFile, groupId) values (?,?,?,?,?,?,?,?,?,?,?)";
                    db.execSQL(sql, new Object[]{task.getName(), task.getRemotePath(), task.getStatus(), task.getLocalPath(), task.getVersion(), task.getOffset(), task.getUrl(), task.getFileLength(), currentUserName, task.getIsGroupFile(), task.getGroupId()});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                if (db != null) {
                    db.endTransaction();
                    // db.close();
                }
            }
        }
    }

    /**
     * 实时更新每条线程已经下载的文件长度
     *
     * @param id
     * @param map
     */
    public void updateDownloadTask(int id, Map<String, Object> map) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                List<Object> params = new ArrayList<Object>();
                String sql = "update downloadQueue set ";
                if (map.get("downloadLength") != null) {
                    sql += " downloadLength = ?, ";
                    params.add(map.get("downloadLength"));
                }

                if (map.get("fileLength") != null) {
                    sql += " fileLength = ?, ";
                    params.add(map.get("fileLength"));
                }

                if (map.get("status") != null) {
                    sql += " status = ?, ";
                    params.add(map.get("status"));
                }

                if (map.get("finishTime") != null) {
                    sql += " finishTime = ?, ";
                    params.add(map.get("finishTime"));
                }
                sql = sql.substring(0, sql.lastIndexOf(","));

                params.add(id);
                db.execSQL(sql + " where id = ? " + " AND " + userFilter, params.toArray());
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //				db.close();
            }
        }
    }

    /**
     * 判断是否存在于队列
     *
     * @param type
     * @param urlOrFileName
     * @return
     */
    public boolean isExistInDownloadQueue(int type, String urlOrFileName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select id from downloadQueue where (status=1 OR status=2) AND " + userFilter;
            if (type == DownloadTask.OWN_FILE_TYPE) {
                sql += " AND savePath=\"" + urlOrFileName + "\"";
            } else {
                sql += " AND code=\"" + urlOrFileName + "\" ";
            }
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                return true;
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return false;
    }

    public boolean isDownloadTaskStop(Integer id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select status from downloadQueue where id = " + id + " AND " + userFilter;
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                if (cursor.getInt(0) == 1 || cursor.getInt(0) == 2) {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return true;
    }

    public boolean isUploadTaskStop(Integer id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select status from uploadQueue where id = " + id + " AND " + userFilter;
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                if (cursor.getInt(0) == 1 || cursor.getInt(0) == 2) {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return true;
    }

    public boolean isExistInUploadQueue(String url) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select id from uploadQueue where (status=1 OR status=2)" + " AND " + userFilter + " AND localPath=\"" + url + "\"";
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                return true;
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return false;
    }

    public void updateUploadTask(int id, Map<String, Object> map) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                List<Object> params = new ArrayList<Object>();
                String sql = "update uploadQueue set ";

                if (map.get("status") != null) {
                    sql += " status = ?, ";
                    params.add(map.get("status"));
                }

                if (map.get("uploadLength") != null) {
                    sql += " uploadLength = ?, ";
                    params.add(map.get("uploadLength"));
                }

                if (map.get("fileLength") != null) {
                    sql += " fileLength = ?, ";
                    params.add(map.get("fileLength"));
                }

                if (map.get("finishTime") != null) {
                    sql += " finishTime = ?, ";
                    params.add(map.get("finishTime"));
                }
                sql = sql.substring(0, sql.lastIndexOf(","));
                params.add(id);
                db.execSQL(sql + " where id = ? " + " AND " + userFilter, params.toArray());
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //				db.close();
            }
        }
    }

    public void initUploadQueue(Integer... status) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                List<Object> params = new ArrayList<Object>();
                String sql = "update uploadQueue set status = 5 where " + userFilter;

                for (Integer s : status) {
                    sql += " AND status = " + s;
                }
                db.execSQL(sql, params.toArray());
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //			db.close();
            }
        }
    }

    public boolean initDownloadQueue(Integer... status) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                List<Object> params = new ArrayList<Object>();
                String sql = "update downloadQueue set status = 5 where " + userFilter;

                for (Integer s : status) {
                    sql += " AND status = " + s;
                }
                db.execSQL(sql, params.toArray());
                db.setTransactionSuccessful();
                return true;
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //			db.close();
            }
            return false;
        }
    }

    public int countUpload() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "select count(*) from uploadQueue where (status=1 OR status=2) AND " + userFilter;
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return 0;
    }

    public void emptyUploadQueue() {
        synchronized (lock) {
            SQLiteDatabase db = null;

            try {
                db = dbHelper.getWritableDatabase();
                String sql = "DELETE FROM uploadQueue";
                db.execSQL(sql);
            } catch (SQLException e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                if (db != null) {
                    //				db.close();
                }
            }
        }
    }

    public void emptyDownloadQueue() {
        synchronized (lock) {
            SQLiteDatabase db = null;

            try {
                db = dbHelper.getWritableDatabase();
                String sql = "DELETE FROM downloadQueue";
                db.execSQL(sql);
            } catch (SQLException e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                if (db != null) {
                    //				db.close();
                }
            }
        }
    }

    public void emptyNotice() {
        synchronized (lock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                String sql1 = "DELETE FROM attachment";
                String sql2 = "DELETE FROM noticeMessage";
                db.execSQL(sql1);
                db.execSQL(sql2);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                if (db != null) {
                    db.endTransaction();
                    //				db.close();
                }
            }
        }
    }

    public List<BackupTask> listBackupQueue(Integer... status) {
        List<BackupTask> list = new ArrayList<BackupTask>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String filter = "";
            for (Integer s : status) {
                filter += " status=" + s + " OR ";
            }
            filter = filter.substring(0, filter.lastIndexOf("OR"));
            String sql = "";
            if (filter.equals("")) {
                sql = "select id, userName, title, localPath, status, remotePath, addQueueTime, finishTime from backupQueue where " + userFilter + " order by addQueueTime DESC";
            } else {
                sql = "select id, userName, title, localPath, status, remotePath, addQueueTime, finishTime from backupQueue where " + userFilter + " AND (" + filter + ") order by addQueueTime DESC";
            }
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                BackupTask task = new BackupTask();
                task.setId(cursor.getInt(0));
                task.setUserName(cursor.getString(1));
                task.setTitle(cursor.getString(2));
                task.setLocalPath(cursor.getString(3));
                task.setStatus(cursor.getInt(4));
                task.setRemotePath(cursor.getString(5));
                task.setAddQueueTime(cursor.getString(6));
                task.setFinishTime(cursor.getString(7));
                list.add(task);
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        } finally {
            cursor.close();
            //			db.close();
        }
        return list;
    }

    public void saveBackupTasks(List<BackupTask> taskList) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                for (BackupTask task : taskList) {
                    String sql = "delete from backupQueue where localPath='" + task.getLocalPath() + "' AND userName='" + currentUserName + "'";
                    db.execSQL(sql);
                    sql = "insert into backupQueue(userName, title, localPath, status, remotePath) values (?,?,?,?,?)";
                    db.execSQL(sql, new Object[]{currentUserName, task.getTitle(), task.getLocalPath(), task.getStatus(), task.getRemotePath()});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //				db.close();
            }
        }
    }

    public void updateBackupTask(int id, Map<String, Object> map) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                List<Object> params = new ArrayList<Object>();
                String sql = "update backupQueue set ";
                if (map.get("status") != null) {
                    sql += " status = ?, ";
                    params.add(map.get("status"));
                }
                if (map.get("localPath") != null) {
                    sql += " localPath = ?, ";
                    params.add(map.get("localPath"));
                }

                if (map.get("remotePath") != null) {
                    sql += " remotePath = ?, ";
                    params.add(map.get("remotePath"));
                }
                if (map.get("finishTime") != null) {
                    sql += " finishTime = ?, ";
                    params.add(map.get("finishTime"));
                }
                sql = sql.substring(0, sql.lastIndexOf(","));
                params.add(id);
                db.execSQL(sql + " where id = ? " + " AND " + userFilter, params.toArray());
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //				db.close();
            }
        }
    }

    public void deleteBackupTask(int id) {
        synchronized (lock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                String sql = "delete from backupQueue where id=" + id + " AND " + userFilter;
                db.execSQL(sql);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
                //				db.close();
            }
        }
    }


    private class TaskComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof UploadTask && o2 instanceof UploadTask) {
                UploadTask u1 = (UploadTask) o1;
                UploadTask u2 = (UploadTask) o2;
                if (u1.getStatus() == ITransferConstants.STATUS_RUN) {
                    return -1;
                }
                if (u2.getStatus() == ITransferConstants.STATUS_RUN) {
                    return 1;
                }
                if (u1.getStatus() == ITransferConstants.STATUS_WAIT) {
                    return -1;
                }
                if (u2.getStatus() == ITransferConstants.STATUS_WAIT) {
                    return 1;
                }
            } else if (o1 instanceof DownloadTask && o2 instanceof DownloadTask) {
                DownloadTask d1 = (DownloadTask) o1;
                DownloadTask d2 = (DownloadTask) o2;
                if (d1.getStatus() == ITransferConstants.STATUS_RUN) {
                    return -1;
                }
                if (d2.getStatus() == ITransferConstants.STATUS_RUN) {
                    return 1;
                }
                if (d1.getStatus() == ITransferConstants.STATUS_WAIT) {
                    return -1;
                }
                if (d2.getStatus() == ITransferConstants.STATUS_WAIT) {
                    return 1;
                }
            }
            return 0;
        }

    }

}
