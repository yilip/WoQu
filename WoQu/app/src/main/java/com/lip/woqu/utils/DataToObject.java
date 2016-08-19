package com.lip.woqu.utils;

import android.content.Context;
import android.os.Handler;

import com.lip.woqu.bean.BaseBean;

import java.util.Hashtable;


public abstract class DataToObject {
	public DataToObject(Context c) {
	}

	public abstract BaseBean getMsgFromNetwork(Handler handler,
			String interfaceType, Hashtable<String, String> data)
			throws Exception;

	public abstract BaseBean getMsgFromCacheDB(Handler handler,
			String interfaceType, Hashtable<String, String> data)
			throws Exception;

	public abstract BaseBean getMsgDBandNetwork(Handler handler,
			String interfaceType, Hashtable<String, String> data)
			throws Exception;

	public abstract XmlHandlerBase implXmlHander();
}
