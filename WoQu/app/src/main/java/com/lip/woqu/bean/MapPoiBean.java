package com.lip.woqu.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class MapPoiBean {

	public String PoiName = "", poiType = "", poiAddress = "";
	public double lat, lon, poiDis;
    public String poi_id = "";

	/** 数据对象转字符串 */
	public JSONObject beanToJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("PoiName", this.PoiName);
			obj.put("poiType", this.poiType);
			obj.put("poiAddress", this.poiAddress);
			obj.put("lat", this.lat);
			obj.put("lon", this.lon);
			obj.put("poiDis", this.poiDis);
            obj.put("id", this.poi_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}// end beanToString

	/** 字符串转数据对象 */
	public void jsonObjToBean(JSONObject obj) {
		if (obj == null) {
			return;
		}
		try {
			this.PoiName = obj.getString("PoiName");
			this.poiType = obj.getString("poiType");
			this.poiAddress = obj.getString("poiAddress");
			this.lat = obj.getDouble("lat");
			this.lon = obj.getDouble("lon");
			this.poiDis = obj.getDouble("poiDis");
            this.poi_id = obj.optString("id","");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}// end stringToBean
}
