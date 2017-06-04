package com.coolweather.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AutoUpdateService", "启动了:" + SystemClock.currentThreadTimeMillis());

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        // 定时器
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Long elapsedTime = SystemClock.elapsedRealtime();
        Long triggerAtMillis = elapsedTime + 8 * 1000 ;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        i.setAction("com.weather.app.ACTION.ALARM");
        PendingIntent operation = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis,operation);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences sp = getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
        String weatherCode = sp.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this,response);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
