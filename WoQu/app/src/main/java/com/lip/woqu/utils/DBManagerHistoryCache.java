package com.lip.woqu.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 单独抽一个db库存缓存数据，以免多进程多线程引起的db.locked异常
 * */
public class DBManagerHistoryCache {

	/** 缓存表 */
	static class historyCache {
		public static final String TableName = "historyCache";
		public static final String KEY_ROWId = "id";
		public static final String KEY_IdKey = "IdKey";// 名字
		public static final String KEY_content = "content";//
		public static final String KEY_date = "date";
		public static final String[] columns = new String[] { KEY_ROWId,
				KEY_IdKey, KEY_content, KEY_date };
		public static final String Create_table = "CREATE TABLE " + TableName
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_IdKey
				+ " TEXT NOT NULL, " + KEY_content + " TEXT NOT NULL,"
				+ KEY_date + " LONG NOT NULL " + ");";
	}

	

	private static DatabaseHelper mDbHelper = null;;
	private static SQLiteDatabase mDb = null;;
	private static DBManagerHistoryCache dBManagerInstance = null;
	private static final String DATABASE_NAME = "history_cache.db";
	private static final int DATABASE_VERSION = 1;
	private Context mCtx;

	private class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(historyCache.Create_table);
		}
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}

	private DBManagerHistoryCache(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new DatabaseHelper(mCtx);
	}

	public static DBManagerHistoryCache open(Context ctx) throws SQLException {
		if (dBManagerInstance != null) {
			if (mDb == null) {
				mDb = mDbHelper.getWritableDatabase();
			}
		} else {
			dBManagerInstance = new DBManagerHistoryCache(ctx.getApplicationContext());
			mDb = mDbHelper.getWritableDatabase();
		}
		return dBManagerInstance;
	}

	/** 获取Database句柄实例 */
	public SQLiteDatabase getTheSQLiteDatabase() {
		return mDb;
	}

	public void close() {
		mDb.close();
		mDbHelper.close();
		dBManagerInstance = null;
	}

	/************************** 缓存表操作开始 ***************************************/
	/**
	 * 向缓存表更新或插入一条数据
	 */
	public void insertToCache(String IdKey, String content, long date) {
		ContentValues cv = new ContentValues();
		cv.put(historyCache.KEY_IdKey, IdKey);
		cv.put(historyCache.KEY_content, content);
		cv.put(historyCache.KEY_date, date);
		int result = mDb.update(historyCache.TableName, cv,
				historyCache.KEY_IdKey + " LIKE ? ", new String[] { IdKey });
		if (result <= 0) {
			mDb.insert(historyCache.TableName, null, cv);
		}
	}

	/**
	 * 更新缓存表中的一条数据
	 */
	public int updateToCache(String IdKey, String content, long date) {
		ContentValues cv = new ContentValues();
		cv.put(historyCache.KEY_IdKey, IdKey);
		cv.put(historyCache.KEY_content, content);
		cv.put(historyCache.KEY_date, date);
		return mDb.update(historyCache.TableName, cv, historyCache.KEY_IdKey
				+ " LIKE ? ", new String[] { IdKey });
	}

	/** 更新缓存表中的一条数据的修改时间 */
	public int updateToCacheModifyTime(String IdKey, long date) {
		ContentValues cv = new ContentValues();
		cv.put(historyCache.KEY_IdKey, IdKey);
		cv.put(historyCache.KEY_date, date);
		return mDb.update(historyCache.TableName, cv, historyCache.KEY_IdKey
				+ " LIKE ? ", new String[] { IdKey });
	}

	/*** 根据key获取一条缓存 */
	public synchronized Cursor getCache(String IdKey) {
		Cursor cur = null;
		cur = mDb.query(historyCache.TableName, historyCache.columns,
				historyCache.KEY_IdKey + " LIKE ? ", new String[] { IdKey },
				null, null, null);
		return cur;
	}

	/**
	 * 根据key删除一条缓存
	 */
	public void deleteCache(String IdKey) {
		mDb.delete(historyCache.TableName, historyCache.KEY_IdKey + " LIKE ? ",
				new String[] { IdKey });
	}

	/************************** 缓存表操作结束 ***************************************/

}
