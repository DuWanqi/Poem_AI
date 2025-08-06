
import com.example.poemai.MyApplication; // 导入 MyApplication 类

public class RetrofitClient {
    private static final String BASE_URL = "https://10.0.2.2:8443/"; // Android模拟器访问本地服务器的地址(HTTPS)
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    private RetrofitClient(Context context) { 
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        SSLContext sslContext = null;
        TrustManagerFactory trustManagerFactory = null; // 声明变量
        try {
            sslContext = SSLContext.getInstance("TLS");
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            KeyStore keyStore = loadKeyStore(context); // 自定义方法加载KeyStore，传递 context
            if (keyStore != null) { // 检查 keyStore 是否为 null
                trustManagerFactory.init(keyStore);

                // 日志输出，检查 trustManagerFactory 是否正确初始化
                Log.d("RetrofitClient", "TrustManagerFactory initialized successfully");

                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                // 日志输出，检查 sslContext 是否正确初始化
                Log.d("RetrofitClient", "SSLContext initialized successfully");
            } else {
                Log.e("RetrofitClient", "KeyStore is null, SSLContext will not be initialized");
            }
        } catch (Exception e) {
            // 使用 Log.e() 替换 printStackTrace()
            Log.e("RetrofitClient", "Error initializing SSLContext", e);
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        if (sslContext != null && trustManagerFactory != null) { // 空值检查
            client = client.newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
                    .build();
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() { 
        if (instance == null) {
            // 通过 MyApplication 获取全局 Context
            Context applicationContext = MyApplication.getInstance().getAppContext();
            instance = new RetrofitClient(applicationContext);
        }
        return instance;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }

    private KeyStore loadKeyStore(Context context) { 
        // 实现加载KeyStore的逻辑
        try {
            // 从raw资源中读取证书文件
            InputStream inputStream = context.getResources().openRawResource(R.raw.certificate);
            
            if (inputStream == null) {
                Log.e("RetrofitClient", "Certificate file not found");
                return null;
            } else {
                // 日志输出，确认 inputStream 不为 null
                Log.d("RetrofitClient", "Certificate file found");
            }

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