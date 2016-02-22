package com.name.myassistant.weather.Util;

/**
 * Created by xu on 16-2-3.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
