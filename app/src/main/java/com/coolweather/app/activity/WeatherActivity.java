package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements View.OnClickListener {

    private Context context;
    private TextView titleText;
    private TextView tvPublishTime;
    private TextView tvPublishDate;
    private TextView tvPublishTemp1;
    private TextView tvPublishTemp2;
    private LinearLayout linearLayoutWeather;
    private TextView tvPublishDesc;
    private TextView tvChangeCity;
    private TextView tvRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        initViews();

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) { // 有县级代号
            tvPublishTime.setText("同步中...");
            linearLayoutWeather.setVisibility(View.INVISIBLE);
            titleText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }
    }

    private void initViews() {
        context = WeatherActivity.this;
        titleText = (TextView) findViewById(R.id.title_text);
        tvPublishTime = (TextView) findViewById(R.id.tv_publish_time);
        tvPublishDate = (TextView) findViewById(R.id.tv_publish_date);
        tvPublishDesc = (TextView) findViewById(R.id.tv_publish_desc);
        tvPublishTemp1 = (TextView) findViewById(R.id.tv_publish_temp1);
        tvPublishTemp2 = (TextView) findViewById(R.id.tv_publish_temp2);
        linearLayoutWeather = (LinearLayout) findViewById(R.id.linearlayout_weather);
        tvChangeCity = (TextView) findViewById(R.id.tv_change_city);
        tvRefresh = (TextView) findViewById(R.id.tv_refresh);
        tvChangeCity.setOnClickListener(this);
        tvRefresh.setOnClickListener(this);
    }

    /**
     * 根据县级代号查询对应的天气代号
     *
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        // 访问服务器获取天气代号
        queryFromServer(address,"countyCode");
    }

    private void queryFromServer(String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                if ("countyCode".equals(type)) {
                    String[] array = response.split("\\|");
                    if (array != null && array.length == 2) {
                        String weatherCode = array[1];
                        SharedPreferences sp = context.getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
                        sp.edit().putString("weather_code", weatherCode).apply();
                        queryWeatherInfo(weatherCode);
                    }
                } else if ("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(context,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }


            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        // 访问服务器获取天气数据
        queryFromServer(address,"weatherCode");
    }

    private void showWeather(){
        SharedPreferences sp = context.getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
        titleText.setText(sp.getString("city_name",""));
        tvPublishTime.setText(sp.getString("ptime","")+"发布");
        tvPublishDate.setText(sp.getString("current_date", ""));
        tvPublishDesc.setText(sp.getString("weather_desc",""));
        tvPublishTemp1.setText(sp.getString("temp1", ""));
        tvPublishTemp2.setText(sp.getString("temp2", ""));
        titleText.setVisibility(View.VISIBLE);
        linearLayoutWeather.setVisibility(View.VISIBLE);

        startService(new Intent(context, AutoUpdateService.class));
    }

    @Override
    public void onClick(View v) {
        if (v == tvChangeCity){
            // 跳转到选择城市页面
            Intent intent = new Intent(context, ChooseAreaActivity.class);
            intent.putExtra("from_weather_activity", true);
            startActivity(intent);
            finish();
        } else if (v == tvRefresh){
            tvPublishTime.setText("同步中...");
            // 获取天气代号
            SharedPreferences sp = context.getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
            String weatherCode = sp.getString("weather_code", "");
            if (! TextUtils.isEmpty(weatherCode)) {
                // 重新查询
                queryWeatherInfo(weatherCode);
            }

        }
    }
}
