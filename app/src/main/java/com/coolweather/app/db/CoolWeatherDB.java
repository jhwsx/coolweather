package com.coolweather.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhichao on 2017/6/3.
 */
public class CoolWeatherDB {
    public static final String DB_NAME = "cool_weather";
    public static final int VERSION = 1;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper helper = new CoolWeatherOpenHelper(context, DB_NAME, null, 1);
        db = helper.getWritableDatabase();
    }

    /**
     * 存储Province信息到数据库
     * @param province
     */
    public void saveProvince(Province province){
        ContentValues contentValues = new ContentValues();
        contentValues.put("province_name", province.getProvinceName());
        contentValues.put("pronvince_code", province.getProvinceCode());
        db.insert("Province", null, contentValues);
    }

    /**
     * 从数据库中获取Province信息列表
     * @return
     */
    public List<Province> getProvinces(){
        ArrayList<Province> result = new ArrayList<>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor != null){
            while(cursor.moveToNext()){
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("pronvince_code")));
                result.add(province);
            }
            cursor.close();
        }
        return result;
    }

    /**
     * 存储City信息到数据库
     * @param city
     */
    public void saveCity(City city){
        ContentValues contentValues = new ContentValues();
        contentValues.put("city_name", city.getCityName());
        contentValues.put("city_code", city.getCityCode());
        contentValues.put("province_id", city.getProvinceId());
        db.insert("City", null, contentValues);
    }

    /**
     * 从数据库中获取某省下所有的City信息列表
     * @return
     */
    public List<City> getCities(int provinceId){
        List<City> result = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()){
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                result.add(city);
            }
            cursor.close();
        }
        return result;
    }

    /**
     * 保存County信息到数据库
     * @param county
     */
    public void saveCounty(County county){
        ContentValues contentValues = new ContentValues();
        contentValues.put("county_name", county.getCountyName());
        contentValues.put("county_code", county.getCountyCode());
        contentValues.put("city_id", county.getCityId());
        db.insert("County", null, contentValues);
    }

    /**
     * 从数据库中获取某市下所有County信息列表
     * @return
     */
    public List<County> getCounties(int cityId){
        List<County> result = new ArrayList<>();
        Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor != null) {
            while(cursor.moveToNext()){
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                result.add(county);
            }
            cursor.close();
        }
        return result;
    }
}
