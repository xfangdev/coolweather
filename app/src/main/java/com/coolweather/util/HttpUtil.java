package com.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    private static OkHttpClient sClient;
    private static Request sRequest;

    public static void sendOKHttpRequest(String address, okhttp3.Callback callback){
        sClient = new OkHttpClient();
        sRequest = new Request.Builder().url(address).build();
        sClient.newCall(sRequest).enqueue(callback);
    }
}
