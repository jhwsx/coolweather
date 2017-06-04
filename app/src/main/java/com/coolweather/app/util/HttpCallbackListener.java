package com.coolweather.app.util;

/**
 * Created by wangzhichao on 2017/6/3.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
