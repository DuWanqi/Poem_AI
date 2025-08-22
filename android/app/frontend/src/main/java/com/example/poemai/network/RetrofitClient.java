package com.example.poemai.network;

import android.content.Context;
import android.util.Log;

import com.example.poemai.MyApplication;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // 修改为模拟器访问本地服务器的地址
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    // 构造函数保持不变，仍然接收 context
    private RetrofitClient(final Context context) { 
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 修改 getInstance 方法，移除 Context 参数
    public static synchronized RetrofitClient getInstance() { 
        if (instance == null) {
            // 通过 MyApplication 获取全局 Context
            final Context applicationContext = MyApplication.getInstance().getAppContext();
            instance = new RetrofitClient(applicationContext);
        }
        return instance;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}