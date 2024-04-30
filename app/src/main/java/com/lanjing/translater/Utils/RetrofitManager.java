package com.lanjing.translater.Utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private volatile static Retrofit mRetrofit;

    public static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            synchronized (RetrofitManager.class) {
                if (mRetrofit == null) {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
//                            Request request = chain.request().newBuilder()
//                                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                                    .build();
//                            Response response = chain.proceed(request);
                            Response response = chain.proceed(chain.request());
                            return response;
                        }
                    }).build();
                    mRetrofit = new Retrofit.Builder()
//                            .client(okHttpClient)
                            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build();
                }
            }
        }
        return mRetrofit;
    }
}
