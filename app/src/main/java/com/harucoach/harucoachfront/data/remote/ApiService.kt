package com.harucoach.harucoachfront.data.remote



import com.harucoach.harucoachfront.data.models.CognitiveQuestionResponse
import com.harucoach.harucoachfront.data.models.CognitiveResult
import com.harucoach.harucoachfront.data.models.DiaryEntry
import com.harucoach.harucoachfront.data.models.DiaryListResponse
import com.harucoach.harucoachfront.data.models.LoginRequest
import com.harucoach.harucoachfront.data.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService  {
    @POST("/auth/login")  // FastAPI /login 엔드포인트
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String, @Field("password") password: String): LoginResponse




    // TODO 임시 인지 검사 기능 관련 end-point 추가, 변경 요망 (중단 했을때 로직 추가)
    @GET("/cognitive/start")
    suspend fun getCognitiveQuestions(): CognitiveQuestionResponse

    @POST("/cognitive/result")
    suspend fun submitCognitiveAnswers(@Body anwsers: CognitiveQuestionResponse): CognitiveResult



    // 오늘의 일기 관련 예시
    // 기존 ApiService에 추가
    @GET("diaries")  // 전체 목록 엔드포인트
    suspend fun getAllDiaries(): DiaryListResponse

    @POST("diaries")  // 저장
    suspend fun saveDiary(@Body entry: DiaryEntry): Response<Unit>

    @PUT("diaries/{date}")  // 수정 (date로 식별)
    suspend fun updateDiary(@Path("date") date: String, @Body entry: DiaryEntry): Response<Unit>

    @GET("diaries/{date}")  // 단일 가져오기
    suspend fun getDiary(@Path("date") date: String): DiaryEntry?
}