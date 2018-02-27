package com.wangan.gpsrecorder.util;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 10394 on 2018-02-06.
 */

public class OKHttpUtils {
    public static String url = "http://47.93.237.6:8080//lidar/getData.jsp";
    static OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    public  static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
