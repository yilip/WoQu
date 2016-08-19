package com.lip.woqu.utils.net;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.lip.woqu.utils.MyPreferences;
import com.lip.woqu.utils.UtilsManager;

import java.util.Random;


/**
 * Created by etouch on 15/1/7.
 * 定位类，用于定位当前位置，位置通过回调方式返回；
 * (每次定位到当前位置后，均将经纬度、位置描述、地级市名，地级市key,区县名、区县key存储在MyPreference中)
 */
public class LocationUtils implements
        AMapLocationListener, View.OnClickListener {

    private static LocationUtils mLocationUtils=null;


    public static LocationUtils getInstance(Context ctx){
        if(mLocationUtils==null){
            mLocationUtils=new LocationUtils(ctx.getApplicationContext());
        }
        return mLocationUtils;
    }

    private Context ctx;
    private Random mRandom = new Random();
    private LocationManagerProxy mLocationManagerProxy;
    private LocationUtilsListener locationUtilsListener=null;

    private String lon="",lat="",address="",city1="",city2="",cityKey1="",cityKey2="";

    private LocationUtils(Context ctx){
        this.ctx=ctx;
        init();
    }

    public void startLocation(LocationUtilsListener listener){
        this.locationUtilsListener=listener;
        // 注意更换定位时间后，需要先将定位请求删除，再进行定位请求
        mLocationManagerProxy.removeUpdates(this);
        int randomTime = mRandom.nextInt(1000);
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000 + randomTime,
                15, this);
        mLocationManagerProxy.setGpsEnable(false);
    }

    /**
     * 初始化定位
     */
    private void init() {
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy.getInstance(ctx);
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);
    }


    public void onPause() {
        // 移除定位请求
        mLocationManagerProxy.removeUpdates(this);
        // 销毁定位
        mLocationManagerProxy.destroy();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation == null){
            if(locationUtilsListener!=null){
                locationUtilsListener.onGetFail();
            }
            return;
        }
        if (amapLocation != null
                && amapLocation.getAMapException().getErrorCode() == 0) {
            city1=amapLocation.getCity();
            if(!TextUtils.isEmpty(city1)){city1=city1.replace("市", "");}
            city2=amapLocation.getDistrict();
            if(!TextUtils.isEmpty(city2)){city2=city2.replace("区", "").replace("县", "");}
            if(!TextUtils.isEmpty(city1)||!TextUtils.isEmpty(city2)){
                cityKey1="";cityKey2="";
                lon= String.valueOf(amapLocation.getLongitude());
                lat= String.valueOf(amapLocation.getLatitude());
                address=amapLocation.getAddress();

                UtilsManager.println(lat + "," + lon + " " + city1 + " " + city2 + " " + address);
                OtherDataDBManager db =OtherDataDBManager.getInstance(ctx);
                Cursor cur;
                int cityId1=0;
                if(!TextUtils.isEmpty(city1)){
                    /**先获取地级市的key*/
                    cur = db.getTheDistrictByName(city1);
                    if (cur != null && cur.moveToFirst()) {
                        cityId1=cur.getInt(0);
                        cityKey1 = cur.getString(2);
                    }
                    if (cur != null){cur.close();}
                }
                if(!TextUtils.isEmpty(city2)){
                    /**获取区县的key*/
                    if (cityId1>0) {
                        cur = db.getCityByCityName4AutoLoc(city2, cityId1+"");
                    } else {
                        cur = db.getCityByCityName4AutoLoc(city2);
                    }
                    if (cur != null && cur.moveToFirst()) {
                        cityKey2 = cur.getString(2);
                    }
                    if (cur != null){cur.close();}
                    if(TextUtils.isEmpty(cityKey2)){
                        city2="";
                    }
                }
                if(TextUtils.isEmpty(city1)){
                    city1=city2;
                }
                if(TextUtils.isEmpty(city2)){
                    city2=city1;
                }
                if(TextUtils.isEmpty(cityKey1)){
                    cityKey1=cityKey2;
                }
                if(TextUtils.isEmpty(cityKey2)){
                    cityKey2=cityKey1;
                }
            }
            try {// 关闭位置监听
                if (mLocationManagerProxy != null) {
                    mLocationManagerProxy.removeUpdates(this);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                handler.sendEmptyMessage(1);
//                if(!TextUtils.isEmpty(cityKey2)){
//                    handler.sendEmptyMessage(1);
//                }else{
//                    if(locationUtilsListener!=null){
//                        locationUtilsListener.onGetFail();
//                    }
//                }
            }
        } else {
            if(locationUtilsListener!=null){
                locationUtilsListener.onGetFail();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(View view) {

    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    MyPreferences.getInstance(ctx).setLocation(city1,cityKey1,city2,cityKey2,lat,lon,address);
                    if(locationUtilsListener!=null){
                        locationUtilsListener.onGetLocationSuccess(city1,cityKey1,city2,cityKey2,lat,lon,address);
                    }
                    break;
            }
        }
    };

    public interface LocationUtilsListener{
        /**
         * @param city1 地级市名字
         * @param cityKey1 地级市城市key
         * @param city2 区县级名字
         * @param cityKey2 区县级城市key
         * @param lat 维度
         * @param lon 经度
         * @param address 地址描述
         * */
        public void onGetLocationSuccess(String city1, String cityKey1, String city2, String cityKey2,
                                         String lat, String lon, String address);
        public void onGetFail();
    }
}
