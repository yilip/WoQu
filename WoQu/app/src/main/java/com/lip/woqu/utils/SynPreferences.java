/**
 * 
 */
package com.lip.woqu.utils;

import android.content.Context;


/**
 * @author ljg 2011-09-2
 * @author LJG 2013-06-27 修改为数据库实现
 */
public class SynPreferences {
	
	private static SynPreferences mSynPreferences=null;
	
	private DBManagerSettings mDBManagerSettings=null;

	public static SynPreferences getInstance(Context context) {
		if (mSynPreferences == null)
			mSynPreferences = new SynPreferences(context);
		return mSynPreferences;
	}

	private SynPreferences(Context context) {
		mDBManagerSettings=DBManagerSettings.getInstance(context);
	}

	/** 设置同步uid */
	public void setUID(String uid) {
		mDBManagerSettings.insertOnePreferences(DBManagerSettings.SharedPreferences.userUID, uid,true);
	}

	/** 获取同步uid */
	public String getUID() {
		String temp=mDBManagerSettings.getOnePreferences(DBManagerSettings.SharedPreferences.userUID,true);
		if(temp==null){
			return "";
		}
		return temp;
	}

	/** 设置Acctk */
	public void setAcctk(String acctk) {
		mDBManagerSettings.insertOnePreferences(DBManagerSettings.SharedPreferences.userAcctk, acctk,true);
	}

	/** 获取Acctk */
	public String getAcctk() {
		String temp=mDBManagerSettings.getOnePreferences(DBManagerSettings.SharedPreferences.userAcctk,true);
		if(temp==null){
			return "";
		}
		return temp;
	}

	/** 存入登录时计算出的4位随即编码 */
	public void setloginDeviceNumber(String loginDeviceNumber) {
		mDBManagerSettings.insertOnePreferences(DBManagerSettings.SharedPreferences.loginDeviceNumber, loginDeviceNumber,false);
	}

	/** 读取登录是需要使用的手机型号+4位随即编码 由第一次登录时算出 终身不变 */
	public String getloginDeviceNumber() {
		String temp=mDBManagerSettings.getOnePreferences(DBManagerSettings.SharedPreferences.loginDeviceNumber,false);
		if(temp==null){
			return "";
		}
		return temp;
	}

}
