package com.lip.woqu.bean;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


public class MapPoiListBean extends BaseBean{
	public static final int PLACE_CACHE_COUNT = 30;
	public boolean isHaveNextPage = false;
	public List<MapPoiBean> list = new ArrayList<MapPoiBean>();

    public String getCacheKey() {
		return "searchPlaceHistory";
	}

    public String getUsedCacheKey() {
        return "searchUsedPlaceHistory";
    }

	public String beanToString(){
		JSONArray array = new JSONArray();
		int size = this.list.size();
		if (size>PLACE_CACHE_COUNT) {
			this.list = this.list.subList(size-PLACE_CACHE_COUNT, size);
		}
		for (MapPoiBean poiBean : list) {
			array.put(poiBean.beanToJSONObject());
		}
		return array.toString();
	}
}
