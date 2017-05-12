package cn.leeffee.feige.ui.cloud.api;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import cn.leeffee.feige.base.BaseApplication;
import cn.leeffee.feige.manager.cache.CacheManager;
import cn.leeffee.feige.utils.LogUtil;
import cn.leeffee.feige.utils.NetWorkUtil;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by lvhf on 2017/3/25 0025.
 */

public class ApiClient {
    //读超时长，单位：毫秒
    public static final int READ_TIME_OUT = 7676;
    //写超时长，单位：毫秒
    public static final int WRITE_TIME_OUT = 7676;
    //连接时长，单位：毫秒
    public static final int CONNECT_TIME_OUT = 7676;
    public Retrofit retrofit;
    public ApiService mApiService;

    private static SparseArray<ApiClient> sRetrofitManager = new SparseArray<>(HostType.TYPE_COUNT);

    /*************************缓存设置*********************/
/*
   1. noCache 不使用缓存，全部走网络

    2. noStore 不使用缓存，也不存储缓存

    3. onlyIfCached 只使用缓存

    4. maxAge 设置最大失效时间，失效则不使用 需要服务器配合

    5. maxStale 设置最大失效时间，失效则不使用 需要服务器配合 感觉这两个类似 还没怎么弄清楚，清楚的同学欢迎留言

    6. minFresh 设置有效时间，依旧如上

    7. FORCE_NETWORK 只走网络

    8. FORCE_CACHE 只走缓存*/

    /**
     * 设缓存有效期为两天
     */
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;
    /**
     * 查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
     * max-stale 指示客户机可以接收超出超时期间的响应消息。如果指定max-stale消息的值，那么客户机可接收超出超时期指定值之内的响应消息。
     */
    private static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    /**
     * 查询网络的Cache-Control设置，头部Cache-Control设为max-age=0
     * (假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)时则不会使用缓存而请求服务器
     */
    private static final String CACHE_CONTROL_NO_CACHE = "max-age=0";
    public static final String NEED_CACHE = "NEED";
    public static final String NO_NEED_CACHE = "NO_NEED";

    //构造方法私有
    private ApiClient(int hostType) {
        //开启Log
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //增加头部信息
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request build = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.8")
                        .addHeader("Accept-Encoding", "gzip, deflate, sdch")
                        .build();
                return chain.proceed(build);
            }
        };

        /*=======================GET缓存:①无论有无网络都去获取缓存的数据；②先请求网络，失败读缓存=======================*/
        //        File cacheFile = new File(BaseApplication.getAppContext().getCacheDir(), "cache_data");
        //        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        //        OkHttpClient okHttpClient = new OkHttpClient.Builder()
        //                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
        //                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
        //                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
        //               // .addNetworkInterceptor(mRewriteCacheControlInterceptor)//①
        //                .addInterceptor(mRewriteCacheControlInterceptor)//②缓存拦截
        //                .addNetworkInterceptor(mRewriteCacheControlInterceptor)//请求网络拦截
        //                .addInterceptor(headerInterceptor)
        //                .addInterceptor(logInterceptor)
        //                .cache(cache)   //设置缓存
        //                .build();

        /*=======================GET/POST缓存兼容=======================*/
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .addInterceptor(mEnhancedCacheInterceptor)//缓存拦截
                .addInterceptor(headerInterceptor)
                .addInterceptor(logInterceptor)
                //缓存目录在DiskLruCache中设置
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ApiConstants.getHost(hostType))
                .build();
        mApiService = retrofit.create(ApiService.class);
    }


    /**
     * @param hostType HostType中的类型
     */
    public static ApiService getDefault(int hostType) {
        ApiClient retrofitManager = sRetrofitManager.get(hostType);
        if (retrofitManager == null) {
            retrofitManager = new ApiClient(hostType);
            sRetrofitManager.put(hostType, retrofitManager);
        }
        return retrofitManager.mApiService;
    }

    /**
     * 根据主机类型删除
     *
     * @param hostType
     */
    public static void delete(int hostType) {
        sRetrofitManager.delete(hostType);
    }

    /**
     * GET请求根据网络状况获取缓存的策略
     */
    //    @NonNull
    //    public static String getCacheControl() {
    //        return NetWorkUtil.isNetConnected(BaseApplication.getAppContext()) ? CACHE_CONTROL_NO_CACHE : CACHE_CONTROL_CACHE;
    //    }

    /**
     * 云端响应头拦截器，用来配置缓存策略 只能缓存get请求
     * Dangerous interceptor that rewrites the server's cache-control header.
     */
    //    private final Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
    //        @Override
    //        public Response intercept(Chain chain) throws IOException {
    //            Request request = chain.request();
    //            if (!NetWorkUtil.isNetConnected(BaseApplication.getAppContext())) {
    //                request = request.newBuilder()
    //                        .cacheControl(CacheControl.FORCE_CACHE)
    //                        .build();
    //            }
    //            Response originalResponse = chain.proceed(request);
    //            if (NetWorkUtil.isNetConnected(BaseApplication.getAppContext())) {
    //                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
    //                String cacheControl = request.cacheControl().toString();
    //                return originalResponse.newBuilder()
    //                        .header("Cache-Control", cacheControl)
    //                        .removeHeader("Pragma")
    //                        .build();
    //            } else {
    //                return originalResponse.newBuilder()
    //                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC)
    //                        .removeHeader("Pragma")
    //                        .build();
    //            }
    //        }
    //    };
    /**
     * GET/POST同时需要缓存时使用
     */
    private final Interceptor mEnhancedCacheInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String url = request.url().toString();
            RequestBody requestBody = request.body();
            Charset charset = Charset.forName("UTF-8");
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (request.method().equals("POST")) {
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                }
                Buffer buffer = new Buffer();
                try {
                    requestBody.writeTo(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append(buffer.readString(charset));
                buffer.close();
            }
            String key = sb.toString();
            Response response;
            if (NetWorkUtil.isNetConnected(BaseApplication.getAppContext())) {
                String flag = request.header("Is_Need_Cache");
                request = request.newBuilder()
                        .removeHeader("Is_Need_Cache")
                        .removeHeader("Pragma")
                        .build();
                response = chain.proceed(request);
                ResponseBody responseBody = response.body();
                MediaType contentType = responseBody.contentType();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();
                if (contentType != null) {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                }
                String json = buffer.clone().readString(charset);
                LogUtil.e("request method:" + request.method());
                LogUtil.e("存 cache-> key:" + key + "-> json:" + json);
                if (NEED_CACHE.equals(flag)) {
                    CacheManager.getInstance().putCache(key, json);
                }
                return response;
            } else {
                String b = CacheManager.getInstance().getCache(key);
                LogUtil.e("request method:" + request.method());
                LogUtil.e("取 cache-> key:" + key + "-> json:" + b);
                //构建一个新的response响应结果
                response = new Response.Builder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC)
                        .body(ResponseBody.create(MediaType.parse("application/json"), b.getBytes()))
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .build();
                return response;
            }
            // return response;
        }
    };
}
