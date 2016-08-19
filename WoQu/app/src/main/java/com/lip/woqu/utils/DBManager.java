package com.lip.woqu.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lip.woqu.bean.UploadNoteBean;

import java.util.Observable;
import java.util.concurrent.Semaphore;


/**
 * DATABASE_VERSION:28 在oncreate里新增了触发器，主要用于在处理-预加载-数据时，始终确保isSyn字段为1，不让该数据同步上去.
 * DATABASE_VERSION:29 新+了一张C7广告的表.
 * DATABASE_VERSION:30 V5版本开发，修改如下表内容
 *                   （a.ecalendarNoteBookGroup表中添加了type，people，is_show,is_notice字段
 *                     b.ecalendarTableData中添加了sub_catid,format_versioncode,update_time
 *                     c.data字段中新增advances的jsonarray的数据（多次提醒的提前提醒时间数组(秒数),如[300,600]）
 *                     d.lineType=8 记录数据格式，每条记录都可以设置提醒））
 *
 */
public class DBManager extends Observable {
	/** 预加载输在存在otherData字段的标识 */
	public static final String mark_preLoadData = "preLoadData_";
	private static DatabaseHelper mDbHelper = null;;
	private static SQLiteDatabase mDb = null;
	private static DBManager dBManagerInstance = null;
	private static final String DATABASE_NAME = "eCalendar.db";
	private static final int DATABASE_VERSION = 30;
	private Context mCtx;



	public static class UploadImage {
		public static final String TableName = "UploadImage";
		public static final String KEY_ID = "id";
		// 源图片或音频的路径
		public static final String KEY_imagepath = "imagepath";
		// 上传成功的标志：0添加，1成功，2失败
		public static final String KEY_flag = "flag";
		// 上传成功的网络地址
		public static final String KEY_neturl = "neturl";
		// 上传成功的size
		public static final String KEY_size = "size";
		// 上传成功的mid
		public static final String KEY_mid = "media_id";
		public static String[] columns = new String[] { KEY_ID, KEY_imagepath,
				KEY_flag, KEY_neturl, KEY_size, KEY_mid };
		public static final String CreateTable = "create table if not exists "
				+ TableName + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_imagepath
				+ " text," + KEY_flag + " integer, " + KEY_neturl + " text,"
				+ KEY_size + " text," + KEY_mid + " text" + ");";
	}

    /*消息表*/
    public static class Messages {
        public static final String TableName = "Messages";
        public static final String KEY_ID = "id";
        public static final String KEY_uuid = "uuid";//消息id
        public static final String KEY_isread = "isread";// 是否已读
        public static final String KEY_msg_type = "msg_type";//消息类型（系统消息/普通消息）
        public static final String KEY_data = "data";//内容
        public static final String KEY_time = "time";// 日期
        public static final String KEY_push_type = "push_type";// 推送类型（1.针对所有人/2.针对个人）

        public static final String[] columns = new String[] { KEY_ID, KEY_uuid,
                KEY_isread, KEY_msg_type,KEY_push_type,KEY_data,KEY_time};
        public static final String Create_table = "create table if not exists "
                + TableName + " (" + KEY_ID + " integer primary key autoincrement, "  + KEY_uuid
                + " text not null, "+ KEY_isread + " integer not null, "
                + KEY_msg_type + " integer not null, "
                + KEY_push_type + " integer default 1, "
                + KEY_data + " text, "
                + KEY_time + " long not null );";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(UploadImage.CreateTable);
            db.execSQL(Messages.Create_table);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }



	private DBManager(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new DatabaseHelper(mCtx);
	}

	public static DBManager open(Context ctx) throws SQLException {
		if (dBManagerInstance != null) {
			if (mDb == null) {
				mDb = mDbHelper.getWritableDatabase();
			}
		} else {
			dBManagerInstance = new DBManager(ctx.getApplicationContext());
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


	/** 可重入锁,对资源列表进行同步 , 用来测试的，暂时不要删 */
	// private final ReentrantLock lock = new ReentrantLock();
	private Semaphore semaphore = new Semaphore(1, true);// 第一个参数表示信号量个数，一个信号量表示一个时刻只能有一个线程获取信号量，释放后另一个线程才能获取信号量，第二个参数表示公平方式，FIFO，谁先拿到谁先占用，后面的等待

	public void release() {
		semaphore.release();
	}


    /**
     * 向图片下载表中插入一条数据
     *
     * @param bean
     */
    public void inserOneDataToUploadImage(UploadNoteBean bean) {
        ContentValues cv = new ContentValues();
        cv.put(UploadImage.KEY_imagepath, bean.imagePath);
        cv.put(UploadImage.KEY_flag, bean.flag);
        cv.put(UploadImage.KEY_neturl, bean.neturl);
        cv.put(UploadImage.KEY_size, bean.size);
        cv.put(UploadImage.KEY_mid, bean.mid);
        mDb.insert(UploadImage.TableName, null, cv);
    }
    /**
     * 通过path获取一条数据
     *
     * @param path
     * @return
     */
    public Cursor getOneDataFromUploadImage(String path) {
        Cursor cur = null;
        cur = mDb.query(UploadImage.TableName, UploadImage.columns,
                UploadImage.KEY_imagepath + " like ?", new String[] { path },
                null, null, null, null);
        return cur;
    }


    //-----------------------------------------------------------------------------
    //------------------------------**********消息**********------------------------
    //-----------------------------------------------------------------------------

    /**
     * 检测当前消息是否已经存在,消息是否已读
     * @param uuid
     * @return 0当前消息是否已经存在,1当前消息是否已读
     */
    public boolean[] checkMsgIsExist(String uuid){
        Cursor cur = null;
        cur = mDb.query(Messages.TableName,
                new String[]{Messages.KEY_isread+""}, Messages.KEY_uuid+" like ? ", new String[] { uuid },null, null, null);
        int num=0;
        int isRead=0;
        boolean isExist=false;
        if (cur!=null){
            num=cur.getCount();
            isExist=num!=0;
            if (isExist){
                if (cur.moveToFirst()){
                    isRead=cur.getInt(0);
                }
            }
            cur.close();
        }
        return new boolean[]{isExist,isRead==1};
    }

    /**获取未读消息数*/
    public int getUnReadMsgNum() {
        Cursor cur = null;
        cur = mDb.query(Messages.TableName,
                new String[]{Messages.KEY_ID+""}, Messages.KEY_isread+" = 0", null, null, null, null);
        int num=0;
        if (cur!=null){
            num=cur.getCount();
        }
        return num;
    }

    /**插入一条消息
     * @param uuid
     * @param isread
     * @param msg_type
     *     UserMessage=1;//http超链接
     *     SystemMessage=2;//消息列表
     * @param push_type 广播类型
     *     broadcast_all=1;//所有人
     *     broadcast_one=2;//个人
     * @param data
     */
    public void insertOneDataToMessages(String uuid,int isread,int msg_type,int push_type,String data) {
        boolean[] isExist=checkMsgIsExist(uuid);
        if (!isExist[1]) {//是否未读(如果不存在，肯定是未读)
            long count=updateChangeOneDataToMessages(uuid,isread);
            if (count <= 0){
                ContentValues cv = new ContentValues();
                cv.put(Messages.KEY_uuid, uuid);
                cv.put(Messages.KEY_isread, isread);
                cv.put(Messages.KEY_time, System.currentTimeMillis());
                cv.put(Messages.KEY_msg_type, msg_type);
                cv.put(Messages.KEY_push_type, push_type);
                cv.put(Messages.KEY_data, data);
                mDb.insert(Messages.TableName, null, cv);
            }
        }
    }

    /**修改一条消息*/
    private long updateChangeOneDataToMessages(String uuid,int isread) {
        ContentValues cv = new ContentValues();
        cv.put(Messages.KEY_isread, isread);
        return mDb.update(Messages.TableName, cv, Messages.KEY_uuid + " LIKE ? AND "+ Messages.KEY_isread+" <1",
                new String[] { uuid });
    }


    /**将所有的消息置为已读*/
    public long updateAllMsgDataHasRead() {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Messages.KEY_isread, 1);
            return mDb.update(Messages.TableName, cv, Messages.KEY_isread+" < ?",
                    new String[] { 1+"" });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return 0;
        }
    }

    /**删除时间间隔查过20天的已读消息*/
    public void checkDeleteAllUnReadMessages() {
        try {
            mDb.delete(Messages.TableName, Messages.KEY_isread + " = 1 AND "+ Messages.KEY_time+" < ?",
                    new String[]{(System.currentTimeMillis()-1728000000)+""});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**删除某条消息*/
    public void deleteOneMessages(String uuid) {
        try {
            mDb.delete(Messages.TableName, Messages.KEY_uuid + " LIKE ?",
                    new String[]{uuid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**清空所有消息*/
    public void deleteAllMessages() {
        try {
            mDb.delete(Messages.TableName, Messages.KEY_time+" > ?",
                    new String[]{-1+""});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**获取所有系统消息(需要排除删帖类型的系统消息，因为接口会读取)*/
//    public Cursor getAllNeedShowSysMsg() {
//        try {
//            return mDb.query(Messages.TableName,
//                    Messages.columns, Messages.KEY_msg_type+" = "+ AllPushManager.pushInfo.d.SystemMessage
//                            +" AND "+ Messages.KEY_push_type+" = "+AllPushManager.pushInfo.f.broadcast_all
//                    , null, null, null, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            return null;
//        }
//    }

}
