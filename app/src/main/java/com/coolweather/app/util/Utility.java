package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wangzhichao on 2017/6/3.
 * 用于解析和处理数据
 */
public class Utility {
    /**
     * 01|北京,02|上海,03|天津,04|重庆,05|黑龙江,06|吉林,07|辽宁,08|内蒙古,09|河北,10|山西,11|陕西,12|山东,13|新疆,14|西藏,15|青海,16|甘肃,17|宁夏,18|河南,19|江苏,20|湖北,21|浙江,22|安徽,23|福建,24|江西,25|湖南,26|贵州,27|四川,28|广东,29|云南,30|广西,31|海南,32|香港,33|澳门,34|台湾
     * 处理和处理返回的省级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (TextUtils.isEmpty(response)){
            return false;
        }
        String[] allProvinces = response.split(",");
        if (allProvinces == null || allProvinces.length == 0){
            return false;
        }
        for (String p : allProvinces) {
            String[] array = p.split("\\|");
            Province province = new Province();
            province.setProvinceCode(array[0]);
            province.setProvinceName(array[1]);
            coolWeatherDB.saveProvince(province);
        }
        return true;
    }

    /**
     * 解析和处理服务器返回的某省的市级数据
     * 1901|南京,1902|无锡,1903|镇江,1904|苏州,1905|南通,1906|扬州,1907|盐城,1908|徐州,1909|淮安,1910|连云港,1911|常州,1912|泰州,1913|宿迁
     * @param coolWeatherDB
     * @param response
     * @param provinceId
     * @return
     */
    public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response,int provinceId) {
        if (TextUtils.isEmpty(response)){
            return false;
        }
        String[] allCities = response.split(",");
        if (allCities == null || allCities.length == 0){
            return false;
        }
        for (String c : allCities) {
            String[] array = c.split("\\|");
            City city = new City();
            city.setCityCode(array[0]);
            city.setCityName(array[1]);
            city.setProvinceId(provinceId);
            coolWeatherDB.saveCity(city);
        }
        return true;
    }

    /**
     * 解析和处理服务器返回的某市的县级数据
     * 190401|苏州,190402|常熟,190403|张家港,190404|昆山,190405|吴县东山,190406|吴县,190407|吴江,190408|太仓
     *
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (TextUtils.isEmpty(response)){
            return false;
        }
        String[] allCounties = response.split(",");
        if (allCounties == null || allCounties.length == 0){
            return false;
        }
        for (String c : allCounties) {
            String[] array = c.split("\\|");
            County county = new County();
            county.setCountyCode(array[0]);
            county.setCountyName(array[1]);
            county.setCityId(cityId);
            coolWeatherDB.saveCounty(county);
        }
        return true;
    }

    /**
     * 解析服务器返回的天气信息数据,并将数据保存到本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context,String response){
        if (TextUtils.isEmpty(response)) {
            return;
        }

        try {
            String cityName = "";
            String weatherCode = "";
            String weatherDesc = "";
            String temp1 = "";
            String temp2 = "";
            String publishTime = "";
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("weatherinfo")) {
                JSONObject weatherinfo = jsonObject.getJSONObject("weatherinfo");
                if (weatherinfo.has("city")) {
                    cityName = weatherinfo.getString("city");
                }
                if (weatherinfo.has("cityid")) {
                    weatherCode = weatherinfo.getString("cityid");
                }
                if (weatherinfo.has("weather")) {
                    weatherDesc = weatherinfo.getString("weather");
                }
                if (weatherinfo.has("temp1")){
                    temp1 = weatherinfo.getString("temp1");
                }
                if (weatherinfo.has("temp2")){
                    temp2 = weatherinfo.getString("temp2");
                }
                if (weatherinfo.has("ptime")){
                    publishTime = weatherinfo.getString("ptime");
                }
            }

            saveWeatherInfo(context,cityName,weatherCode,weatherDesc,temp1,temp2,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void saveWeatherInfo(Context context,  String cityName, String weatherCode,String weatherDesc,String temp1, String temp2, String ptime){
        SharedPreferences sp = context.getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("weather_desc", weatherDesc);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("ptime", ptime);
        editor.putString("current_date", new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(new Date()));
        editor.apply();
    }
}
