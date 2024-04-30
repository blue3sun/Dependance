package com.lanjing.translater.Utils;

import com.lanjing.translater.bean.Translate;
import com.lanjing.translater.bean.TranslateParams;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RetrofitService {
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @POST("translate")
    Observable<Translate> translateByPost(@Body TranslateParams translateParams);

    @Headers("Content-Type:application/x-www-form-urlencoded")
    @GET("translate")
    Observable<Translate> translateByGet(@Query("q")String q,@Query("from") String from,
                                         @Query("to")String to,@Query("appid") String appid,
                                         @Query("salt")String salt,@Query("sign") String sign);
}
