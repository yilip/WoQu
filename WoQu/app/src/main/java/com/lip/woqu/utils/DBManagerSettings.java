package com.lip.woqu.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**用户软件配置数据库，SharedPreferences中的东西都修改为存在这里*/
public class DBManagerSettings {
	/**Preferences 配置表*/
    public static class SharedPreferences {
		public static final String TableName = "SharedPreferences";
		public static final String KEY_Id = "id";
		public static final String KEY_key = "key";//键(int)
		public static final String KEY_value = "value";//值
		public static final String[] columns = new String[] { KEY_Id, KEY_key, KEY_value};
		public static final String Create_table = "create table if not exists "
				+ TableName + " (" + KEY_Id
				+ " integer primary key autoincrement, " + KEY_key + " integer,"
				+ KEY_value+ " text);";
		
		/**用户登录随身云后的id*/
		public static final int userUID=1;
		/**用户登录随身云后的acctk*/
		public static final int userAcctk=2;
		/**用户上次同步后的tx标签值*/
		public static final int LastSynTX=3;
		/**上次同步时间*/
		public static final int LastSynTime=4;
		/**用户名*/
		public static final int userName=5;
		/**用户密码*/
		public static final int userPwd=6;
		/**登录时计算出的4位随即编码 */
		public static final int loginDeviceNumber=7;
		/**是否自动同步*/
		public static final int IsAutoSyn=8;
		/** 保存设置是否仅在wifi下进行同步 */
		public static final int IsOnlySynWithWifi=9;
		/**同步时client的参数值*/
		public static final int synClientValue=10;
		/**最后一次成功获取的云端的数据，包含记事、日程、节日的条数*/
		public static final int lastSynYunNum=11;
		
		public static final int userImei=12;
		public static final int userImsi=13;
		public static final int CityWeatherName=18;
		public static final int CityWeatherKey=19;
        /**手机Mac地址*/
        public static final int userMac=20;
        /**用户位置信息*/
        public static final int userLocation=21;
    }
	
	private static DBManagerSettings dBManagerSettings = null;
	private DatabaseHelper mDbHelper = null;
	private SQLiteDatabase mDb = null;
	private Context ctx;

	private class DatabaseHelper extends SQLiteOpenHelper {
		public boolean isOnCreateSharedPreferencesTable=false;
		DatabaseHelper(Context context) {
			super(context, "PreSettings", null, 1);
		}
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SharedPreferences.Create_table);
			isOnCreateSharedPreferencesTable=true;
		}
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// db.execSQL("DROP TABLE IF EXISTS "+NoteContent.TableName);
			onCreate(db);
		}
	}
	private DBManagerSettings(Context ctx) {
		mDbHelper = new DatabaseHelper(ctx);
		mDb=mDbHelper.getWritableDatabase();
		this.ctx=ctx;
		if(mDbHelper.isOnCreateSharedPreferencesTable){
			mDbHelper.isOnCreateSharedPreferencesTable=false;
		}
	}
	/**获取一个实例*/
	public static DBManagerSettings getInstance(Context ctx){
		if (dBManagerSettings == null) {
			dBManagerSettings = new DBManagerSettings(ctx.getApplicationContext());
		}
		return dBManagerSettings;
	}
	/**************************SharedPreferences表操作开始 ***************************************/
	/**
	 * 插入一条数据
	 * @param key 
	 * @param value
	 * @param isEncrypt 是否加密存储
	 */
	public void insertOnePreferences(int key, String value,boolean isEncrypt) {
        try{
            ContentValues cv = new ContentValues();
            cv.put(SharedPreferences.KEY_key, key);
//            if(isEncrypt){
//                cv.put(SharedPreferences.KEY_value, EcalendarLib.getInstance().doTheEncrypt(ctx,value,1));
//            }else{
                cv.put(SharedPreferences.KEY_value, value);
//            }
            int result = mDb.update(SharedPreferences.TableName, cv, SharedPreferences.KEY_key+"=?", new String[] {key+""});
            if(result<=0){
                mDb.insert(SharedPreferences.TableName, null, cv);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
	}
	/**
	 * 查询一条数据
	 * @return 如果没有该条记录则返回null
	 * @param isEncrypt 是否加密存储
	 * */
	public String getOnePreferences(int key,boolean isEncrypt) {
		String str=null;
		try {
			str = DatabaseUtils.stringForQuery(mDb, "SELECT " + SharedPreferences.KEY_value + " FROM "
                    + SharedPreferences.TableName + " WHERE " +
                    SharedPreferences.KEY_key + "=?", new String[]{key + ""});
		} catch (Exception e) {// 查不到数据，返回0行会抛出异常
			str = null;
		}
//		if(isEncrypt&&str!=null){
//			return EcalendarLib.getInstance().doTheSecrypt(ctx,str,1);
//		}else{
			return str;
//		}
	}
	/**
	 * 删除一条数据
	 * */
	public void DelOnePreferences(int key) {
		mDb.delete(SharedPreferences.TableName, SharedPreferences.KEY_key+" LIKE ?", new String[] {key+""});
	}
	/**************************SharedPreferences表操作结束 ***************************************/
}
