package cn.leeffee.feige.ui.cloud.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author lvhf
 */
public class DBHelper extends SQLiteOpenHelper {
	// private static final String DBNAME = "/mnt/sdcard/eCloud/eCloud.db";
	private static final String DBNAME = "USpace.db";
	private static final int VERSION = 9;//8 for enterprise

	private DBHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}
	//单实例模式，多线程访问推荐使用，数据库文件引用不需要关，程序结束时关闭db文件引用
	private static DBHelper instance;
	
	public synchronized static DBHelper getInstance(Context context){
		if (instance == null) {
			instance = new DBHelper(context);
		}
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS downloadQueue (id integer primary key autoincrement, name varchar(200), status INTEGER NOT NULL DEFAULT 1, type INTEGER NOT NULL DEFAULT 0, path varchar(500),code varchar(100), savePath varchar(500), version INTEGER, offset BIGINT NOT NULL DEFAULT 0,addQueueTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),finishTime varchar(30),  fileLength BIGINT NOT NULL DEFAULT 0, downloadLength BIGINT NOT NULL DEFAULT 0, userName varchar(20), isGroupFile INTEGER NOT NULL DEFAULT 0, groupId varchar(50), ownId varchar(50))");

		db.execSQL("CREATE TABLE IF NOT EXISTS uploadQueue (id integer primary key autoincrement, name varchar(200), remotePath varchar(200), status INTEGER NOT NULL DEFAULT 1,localPath varchar(200), version INTEGER, offset BIGINT NOT NULL DEFAULT 0, addQueueTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')), url varchar(200), userName varchar(20), uploadLength BIGINT NOT NULL DEFAULT 0, finishTime varchar(30), fileLength BIGINT NOT NULL DEFAULT 0, isGroupFile INTEGER NOT NULL DEFAULT 0, groupId varchar(50), ownId varchar(50))");

		db.execSQL("CREATE TABLE IF NOT EXISTS cache (id integer primary key autoincrement, key varchar(200), value varchar(200), account varchar(200), addTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')), loginAccount varchar(50))");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS backupQueue (id integer primary key autoincrement, userName varchar(200), title varchar(200), localPath varchar(200), status INTEGER NOT NULL DEFAULT 1, remotePath varchar(200), addQueueTime TimeStamp NOT NULL DEFAULT (datetime('now', 'localtime')), finishTime varchar(30))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS downloadQueue");
		db.execSQL("DROP TABLE IF EXISTS uploadQueue");
		db.execSQL("DROP TABLE IF EXISTS cache");
		db.execSQL("DROP TABLE IF EXISTS backupQueue");
		onCreate(db);
	}
}
