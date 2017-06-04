package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    private TextView titleText;
    private ListView listView;
    private Context context;

    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();
    private CoolWeatherDB coolWeatherDB;
    private ProgressDialog progressDialog;
    /**
     * 当前选中的级别
     */
    private int currentLevel;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private Province selectedProvince;

    private City selectedCity;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
        if (sp.getBoolean("city_selected",false)) {
            startActivity(new Intent(this, WeatherActivity.class));
            finish();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        context = ChooseAreaActivity.this;
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);

        coolWeatherDB = CoolWeatherDB.getInstance(context);

        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(i).getCountyCode();
                    Intent intent = new Intent(context, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询省级数据,优先从数据库查询,如果数据库没有,再从网络上加载
     */
    private void queryProvinces() {
        // 从数据库中查询省级数据
        provinceList = coolWeatherDB.getProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            titleText.setText("中国");
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询市级数据
     */
    private void queryCities() {
        cityList = coolWeatherDB.getCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            titleText.setText(selectedProvince.getProvinceName());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询县级数据
     */
    private void queryCounties() {
        countyList = coolWeatherDB.getCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            titleText.setText(selectedCity.getCityName());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    private void queryFromServer(String code, final String type) {
        String address;
        if (TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }

                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }
}
