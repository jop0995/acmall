package com.example.user.accessaryshopping;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//레트로핏 쓸때 한군데에 클래스에서만 빌드 하기 위해서 안그러면 긴줄의 정보를 서버에 보내고 받을때 null 값이 뜬다.
public class NetworkClient {

    private static final String BASE_URL = "http://13.209.144.49/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitClient(Context context) {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
