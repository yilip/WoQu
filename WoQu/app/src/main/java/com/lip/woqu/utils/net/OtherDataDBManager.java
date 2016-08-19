package com.lip.woqu.utils.net;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lip.woqu.utils.MidData;
import com.lip.woqu.utils.ZipManager;

import java.io.File;

/**该类关闭软件解压到用户SD卡上的数据库，里面存储着天气城市、黄历、解梦等数据*/
public class OtherDataDBManager {
	public String S_jianxi = "jx", S_ganzhi = "gz", S_yi = "yi", S_ji = "ji";
	private String[] columns = new String[] { S_jianxi, S_ganzhi, S_ji, S_yi };

	/**海外国家*/
	private String foreignCountryTable="f_city";
	private String[] foreignCountryColumns = new String[] { "AreaId_1", "CITY_NAME_", "COUNTRY__1" };
	/** 省份 */
	private String proTableName = "prov";
	private String proId = "id", proName = "provName", proSort = "sort";
	private String[] proColumns = new String[] { proId, proName, proSort };
	/** 地级市 */
	private String districtTableName = "district";
	private String districtId = "id", districtName = "districtName",
			districtKey = "districtKey", districtProId = "proId",
			districtSort = "sort", areaKey = "areaKey";
	private String[] districtColumns = new String[] { districtId, districtName,
			districtKey, districtProId, districtSort, areaKey };
	/** 县级市 */
	private String cityTableName = "city";
	private String cityId = "id", cityName = "cityName", cityKey = "cityKey",
			cityDistrictId = "districtId", citySort = "sort",
			cityIsHot = "isHotCity", pycityName = "pycityName", pyshort = "pyshort";
	private String[] cityColumns = new String[] { cityId, cityName, cityKey,
			cityDistrictId, citySort, cityIsHot, pycityName, pyshort };

	/** 数据库版本 */
	private String versionTableName = "version";
	private String versionId = "id", versionCode = "versioncode";
	private String[] versionColumns = new String[] { versionId, versionCode };

    private static OtherDataDBManager mOtherDataDBManager=null;
	private static SQLiteDatabase db;

    public static OtherDataDBManager getInstance(Context ctx){
        if(mOtherDataDBManager==null||db==null){
            mOtherDataDBManager=new OtherDataDBManager(ctx);
        }
        return mOtherDataDBManager;
    }
	private OtherDataDBManager(Context ctx) {
		File file = new File(MidData.appDir + "etouch_ecalendar.db");
		if (file.exists()) {
			if (ZipManager.isZipExtractSuccess) {
				try {
					db = SQLiteDatabase.openDatabase(MidData.appDir + "etouch_ecalendar.db", null,
                            SQLiteDatabase.OPEN_READWRITE);
				} catch (Exception e) {
					db=null;
				}
			}else {
				db=null;
			}
		}
	}
	public void close() {
		if (db != null) {
			db.close();
		}
        db=null;
        mOtherDataDBManager=null;
	}


	
    /** 获取海外国家列表 */
	public Cursor getForeignCountryList() {
		if (db != null) {
			return db.query(foreignCountryTable, foreignCountryColumns, null, null, "COUNTRY__1", null,
					null);
		}
		return null;
	}
	
	/** 获取海外城市列表 */
	public Cursor getForeignCityList(String country) {
		if (db != null) {
			return db.query(foreignCountryTable, foreignCountryColumns, "COUNTRY__1=?", new String[]{country}, null, null,
					null);
		}
		return null;
	}

    
	/** 获取省份列表 */
	public Cursor getProvList() {
		if (db != null) {
			return db.query(proTableName, proColumns, null, null, null, null,
					proSort + " DESC");
		}
		return null;
	}

	/** 根据省份id获取地级市 */
	public Cursor getDistrictListByProId(int proId) {
		if (db != null) {
			return db.query(districtTableName, districtColumns, districtProId
					+ "=?", new String[] { String.valueOf(proId) }, null, null,
					districtKey + " ASC");
		}
		return null;
	}

    /** 根据地级市名获取对应的citycode */
    public Cursor getCitycodeByCityName(String proId) {
        if (db != null) {
            return db.query(districtTableName, districtColumns, districtName + "=?",
                    new String[] { String.valueOf(proId) }, null, null, districtKey + " ASC");
        }
        return null;
    }

	/** 根据地级市id获取县级市列表 */
	public Cursor getCityListByDistrictId(int districtId) {
		if (db != null) {
			return db.query(cityTableName, cityColumns, cityDistrictId + "=?",
					new String[] { String.valueOf(districtId) }, null, null,
					cityKey + " ASC");
		}
		return null;
	}

	/** 根据城市Key获取城市的地级市、区id,未找到则返回-1 */
	public int getDistrictIdByCityKey(String cityKey) {
		int id = -1;
		if (db != null) {
			Cursor cur = db.query(cityTableName, cityColumns, this.cityKey
					+ " = ?", new String[] { String.valueOf(cityKey) },
					null, null, null);
			if (cur != null && cur.moveToFirst()) {
				id = cur.getInt(3);
			}
			if (cur != null) {
				cur.close();
			}
		}
		return id;
	}
    /** 根据城市Key获取城市的地级市、区Key */
    public String getDistrictKeyByCityKey(String thecityKey) {
        String key="";
        if (db != null) {
            Cursor cur=db.rawQuery("select district.districtKey FROM district,city WHERE  city.cityKey LIKE ? AND district.id==city.districtId"
                    ,new String[]{thecityKey});
            if (cur != null && cur.moveToFirst()) {
                key = cur.getString(0);
            }
            if (cur != null) {
                cur.close();
            }
        }
        return key;
    }
	
	/** 根据城市名字，返回该条城市数据*/
	public Cursor getCityByCityName(String cityName) {
		Cursor cur=null;
		if (db != null) {
			cur = db.query(cityTableName, cityColumns, this.cityName
					+ " like ? or " + this.pycityName + " like ? or "
					+ this.pyshort + " like ?", new String[] { "%"
					+ String.valueOf(cityName) + "%","%"
					+ String.valueOf(cityName) + "%","%"
					+ String.valueOf(cityName) + "%" }, null, null, null);
		}
		return cur;
	}
	
	/** 根据城市名字，返回该条城市数据(只用于自动定位)*/
	public Cursor getCityByCityName4AutoLoc(String cityName) {
		Cursor cur=null;
		if (db != null) {
			cur = db.query(cityTableName, cityColumns, this.cityName
					+ " = ?", new String[]{cityName}, null, null, null);
		}
		return cur;
	}
    /** 根据区县名字以及城市所属市级城市id，返回该条城市数据(只用于自动定位)*/
    public Cursor getCityByCityName4AutoLoc(String cityName, String cityDistrictId) {
        Cursor cur=null;
        if (db != null) {
            cur = db.query(cityTableName, cityColumns, this.cityName
                    + "=? AND " + this.cityDistrictId + "=?", new String[] {
                    cityName, cityDistrictId }, null, null, null);
        }
        return cur;
    }
	/** 获取热门城市列表 */
	public Cursor getHotCityList() {
		if (db != null) {
			return db.query(cityTableName, cityColumns, cityIsHot + "!=?",
					new String[] { "0" }, null, null, citySort + " ASC");
		}
		return null;
	}

    /** 获取数据库版本 */
    public int getDbVersion() {
        int versionCode = 0;
        try {
            if (db != null) {
                Cursor cur = db.query(versionTableName, versionColumns, null, null,null, null, null);
                if (cur != null && cur.moveToFirst()) {
                    versionCode = cur.getInt(1);
                }
                if (cur != null) {
                    cur.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

	/** 获取所有地级市列表 */
	public Cursor getAllDistrictList() {
		if (db != null) {
			return db.query(districtTableName, districtColumns, null, null,
					null, null, districtSort + " DESC");
		}
		return null;
	}

	/** 根据地级市名字搜索该条记录 */
	public Cursor getTheDistrictByName(String theDistrictName) {
		if (db != null) {
			return db.query(districtTableName, districtColumns, districtName
					+ " like ?", new String[] { theDistrictName }, null, null,null);
		}
		return null;
	}
}
