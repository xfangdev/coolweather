package com.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sendOKHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient Client = new OkHttpClient();
        Request Request = new Request.Builder().url(address).build();
        Client.newCall(Request).enqueue(callback);
    }
}
