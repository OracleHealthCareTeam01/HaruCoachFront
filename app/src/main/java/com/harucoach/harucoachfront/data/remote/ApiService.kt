package com.harucoach.harucoachfront.data.remote



import com.harucoach.harucoachfront.data.models.LoginRequest
import com.harucoach.harucoachfront.data.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService  {
    @POST("/auth/login")  // FastAPI /login 엔드포인트
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String, @Field("password") password: String): LoginResponse
}