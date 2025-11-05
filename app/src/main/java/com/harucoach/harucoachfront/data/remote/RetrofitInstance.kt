package com.harucoach.harucoachfront.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: ApiService by lazy {  // 지연 초기화로 싱글톤
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  // JSON 변환
            .build()
            .create(ApiService::class.java)
    }
}