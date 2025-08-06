package com.example.poemai.network;

import android.content.Context;
import android.util.Log;

import com.example.poemai.MyApplication;
import com.example.poemai.R;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class RetrofitClient {
    private static final String BASE_URL = "https://10.0.2.2:8443/"; // Android模拟器访问本地服务器的地址(HTTPS)
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    // 构造函数保持不变，仍然接收 context
    private RetrofitClient(final Context context) { 

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

//        SSLContext sslContext = null;
//        TrustManagerFactory trustManagerFactory = null; // 声明变量
//        try {
//            sslContext = SSLContext.getInstance("TLS");
//            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//
//            KeyStore keyStore = loadKeyStore(context); // 自定义方法加载KeyStore，传递 context
//            if (keyStore != null) { // 检查 keyStore 是否为 null
//                trustManagerFactory.init(keyStore);
//
//                // 日志输出，检查 trustManagerFactory 是否正确初始化
//                Log.d("RetrofitClient", "TrustManagerFactory initialized successfully");
//
//                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
//
//                // 日志输出，检查 sslContext 是否正确初始化
//                Log.d("RetrofitClient", "SSLContext initialized successfully");
//            } else {
//                Log.e("RetrofitClient", "KeyStore is null, SSLContext will not be initialized");
//            }
//        } catch (Exception e) {
//            // 使用 Log.e() 替换 printStackTrace()
//            Log.e("RetrofitClient", "Error initializing SSLContext", e);
//        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

//        if (sslContext != null && trustManagerFactory != null) { // 空值检查
//            client = client.newBuilder()
//                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
//                    .build();
//        }

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

    private KeyStore loadKeyStore(final Context context) { 
        // 实现加载KeyStore的逻辑
        try {
            // 从raw资源中读取证书文件
            // 注意：openRawResource在找不到资源时会抛出异常，而不是返回null
            InputStream inputStream = context.getResources().openRawResource(R.raw.certificate);
            
            // 创建KeyStore并加载证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, new char[0]); // 提供一个空字符数组
            return keyStore;
        } catch (Exception e) {
            Log.e("RetrofitClient", "Failed to load KeyStore", e);
            return null;
        }
    }
}