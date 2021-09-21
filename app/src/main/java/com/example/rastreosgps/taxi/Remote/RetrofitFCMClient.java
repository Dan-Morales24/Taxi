package com.example.rastreosgps.taxi.Remote;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitFCMClient {

    private static Retrofit instance;

    public static Retrofit getInstance(){

        return instance == null ? new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/") //don´t forget
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : instance;

    }

}
