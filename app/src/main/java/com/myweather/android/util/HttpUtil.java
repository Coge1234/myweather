package com.myweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/2/7.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String url_address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url_address).build();
        client.newCall(request).enqueue(callback);
    }
}
