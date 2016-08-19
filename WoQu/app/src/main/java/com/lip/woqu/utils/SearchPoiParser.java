package com.lip.woqu.utils;

import android.content.Context;
import android.os.Handler;

import com.lip.woqu.bean.BaseBean;
import com.lip.woqu.bean.MapPoiBean;
import com.lip.woqu.bean.MapPoiListBean;
import com.lip.woqu.utils.net.NetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;



/** 搜索获取poi列表 auther LJG */
public class SearchPoiParser extends DataToObject {
	Context c;

	public SearchPoiParser(Context c) {
		super(c);
		this.c = c;
	}

	@Override
	public BaseBean getMsgDBandNetwork(Handler handler, String interfaceType,
			Hashtable<String, String> data) throws Exception {
		
		return null;
	}

	@Override
	public BaseBean getMsgFromCacheDB(Handler handler, String interfaceType,
			Hashtable<String, String> data) throws Exception {
		
		return null;
	}

	@Override
	/**该方法中interfaceType 则为请求的基础地址*/
	public MapPoiListBean getMsgFromNetwork(Handler handler,
			String interfaceType, Hashtable<String, String> data)
			throws Exception {
		NetManager mynetmanager = NetManager.getInstance();
		String in = mynetmanager.doGetAsString(interfaceType, data);
		MapPoiListBean bean = getParserResult(in);
		return bean;
	}

	@Override
	public XmlHandlerBase implXmlHander() {
		
		return null;
	}

	public MapPoiListBean getParserResult(String in) {
		MapPoiListBean poiListBean = new MapPoiListBean();
		JSONObject obj = null;
		JSONArray array = null;
		try {
			obj = new JSONObject(in);
			array = obj.getJSONArray("poi");
			if (array == null)
				return poiListBean;
			int length = array.length();
			if (length >= 20) {
				poiListBean.isHaveNextPage = true;
			} else {
				poiListBean.isHaveNextPage = false;
			}
			for (int i = 0; i < length; i++) {
				MapPoiBean bean = new MapPoiBean();
				obj = (JSONObject) array.get(i);
				bean.PoiName = obj.getString("name");
				bean.poiAddress = obj.getString("address");
				double[] location = UtilsManager.JieMiLocation(Double
                        .valueOf(obj.getString("x")), Double.valueOf(obj
                        .getString("y")), 20);
				bean.lat = location[0];
				bean.lon = location[1];
				UtilsManager.println(bean.toString());
				poiListBean.list.add(bean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return poiListBean;
	}

}
