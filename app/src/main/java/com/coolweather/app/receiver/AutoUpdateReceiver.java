package com.coolweather.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coolweather.app.service.AutoUpdateService;

/**
 * Created by wangzhichao on 2017/6/4.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.weather.app.ACTION.ALARM".equals(intent.getAction())){
            Intent service = new Intent(context, AutoUpdateService.class);
            context.startService(service);
        }
    }
}
