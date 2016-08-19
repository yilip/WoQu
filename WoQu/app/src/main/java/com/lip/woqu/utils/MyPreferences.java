/**
 *
 */
package com.lip.woqu.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.lip.woqu.ApplicationManager;

import org.json.JSONObject;
import java.util.Random;

/**
 * 该类已修改为使用SQLite实现存储(稳定刷新及时)
 *
 * @author ljg 2011-09-2
 */
public class MyPreferences {

    private static MyPreferences instance;
    private Context ctx;
    private DBManagerSettings mDBManagerSettings = null;

    public static MyPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new MyPreferences(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 这里的构造方法只提供给当内容涉及到Myservice的相关配置时调用
     */
    private MyPreferences(Context context) {
        this.ctx = context;
        mDBManagerSettings = DBManagerSettings.getInstance(context);
    }

    /**
     * 获取用户IMEI
     */
    public String getUserImei() {
        String temp = mDBManagerSettings.getOnePreferences(
                DBManagerSettings.SharedPreferences.userImei, false);
        if (TextUtils.isEmpty(temp)) {
            TelephonyManager telephoneManager = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephoneManager.getDeviceId();
            if (TextUtils.isEmpty(imei)) {
                Random random = new Random();
                imei = "01";
                for (int i = 0; i < 13; i++) {
                    imei += random.nextInt(10);
                }
            }
            mDBManagerSettings.insertOnePreferences(
                    DBManagerSettings.SharedPreferences.userImei, imei, false);
            return imei;
        }
        return temp;
    }
    /**
     * 获取用户Imsi,没有时返回
     */
    public String getUserImsi() {
        String temp = mDBManagerSettings.getOnePreferences(
                DBManagerSettings.SharedPreferences.userImsi, false);
        if (TextUtils.isEmpty(temp)) {
            TelephonyManager telephoneManager = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = telephoneManager.getSubscriberId();
            if (TextUtils.isEmpty(imsi)) {
                Random random = new Random();
                imsi = "46001";
                for (int i = 0; i < 10; i++) {
                    imsi += random.nextInt(10);
                }
            }
            mDBManagerSettings.insertOnePreferences(
                    DBManagerSettings.SharedPreferences.userImsi, imsi, false);
            return imsi;
        }
        return temp;
    }
    /**
     * 获取用户MAC
     */
    public String getUserMac() {
        String temp = mDBManagerSettings.getOnePreferences(
                DBManagerSettings.SharedPreferences.userMac, false);
        if (TextUtils.isEmpty(temp)) {
            WifiManager wifiManager = (WifiManager) ApplicationManager.ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            temp = wifiInfo.getMacAddress();
            mDBManagerSettings.insertOnePreferences(
                    DBManagerSettings.SharedPreferences.userMac, temp, false);
        }
        return temp;
    }

    /**
     * 设置天气当前城市名称和key
     */
    public void setCity(String cityName, String cityKey) {
        mDBManagerSettings.insertOnePreferences(
                DBManagerSettings.SharedPreferences.CityWeatherName, cityName,
                false);
        mDBManagerSettings.insertOnePreferences(
                DBManagerSettings.SharedPreferences.CityWeatherKey, cityKey,
                false);
    }

    /**
     * 获取天气当前城市名称
     */
    public String getCityName() {
        String temp = mDBManagerSettings.getOnePreferences(
                DBManagerSettings.SharedPreferences.CityWeatherName, false);
        if (temp == null) {
            return "";
        }
        return temp;
    }//

    /**
     * 获取天气当前城市key
     */
    public String getCityKey() {
        String temp = mDBManagerSettings.getOnePreferences(
                DBManagerSettings.SharedPreferences.CityWeatherKey, false);
        if (temp == null) {
            return "";
        }
        return temp;
    }// 101010100


    /**设置软件定位信息
     * @param city1 地级市名字
     * @param cityKey1 地级市城市key
     * @param city2 区县级名字
     * @param cityKey2 区县级城市key
     * @param lat 维度
     * @param lon 经度
     * @param address 地址描述
     * */
    public void setLocation(String city1,String cityKey1,String city2,String cityKey2,
                       String lat,String lon,String address){
        try{
            JSONObject obj=new JSONObject();
            obj.put("city1",city1);
            obj.put("cityKey1",cityKey1);
            obj.put("city2",city2);
            obj.put("cityKey2",cityKey2);
            obj.put("lat",lat);
            obj.put("lon",lon);
            obj.put("address",address);
            mDBManagerSettings.insertOnePreferences(
                    DBManagerSettings.SharedPreferences.userLocation, obj.toString(), false);
        }catch (Exception e){e.printStackTrace();}
    }
    /**获取软件定位信息
     * Json中各参数含义：
     * city1 地级市名字
     * cityKey1 地级市城市key
     * city2 区县级名字
     * cityKey2 区县级城市key
     * lat 维度
     * lon 经度
     * address 地址描述
     * */
    public JSONObject getLocation(){
        JSONObject obj=null;
        try{
            String temp = mDBManagerSettings
                    .getOnePreferences( DBManagerSettings.SharedPreferences.userLocation,false);
            if(!TextUtils.isEmpty(temp)){
                obj=new JSONObject(temp);
            }else{
                obj=new JSONObject();
            }
        }catch (Exception e){e.printStackTrace();}
        return obj;
    }


}
