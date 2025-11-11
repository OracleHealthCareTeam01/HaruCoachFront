package com.harucoach.harucoachfront.data.repository

import com.harucoach.harucoachfront.data.models.CognitiveQuestionResponse
import com.harucoach.harucoachfront.data.models.CognitiveResult
import com.harucoach.harucoachfront.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CognitiveRepository @Inject constructor(private val apiService: ApiService) {

    // TODO 임시 여기서 API 통신을 통해 데이터 가져오기 변경 가능

    suspend fun getCognitiveTestQuestions(): CognitiveQuestionResponse {
        return try {
            apiService.getCognitiveQuestions() // API 호출
        } catch (e: Exception) {
            // TODO 만약 못불러오면 sessionId = -1 로 지정, 변경 가능
            CognitiveQuestionResponse(sessionId = -1, questionList = emptyList())
        }
    }

    suspend fun submitCognitiveAnswers(answers: CognitiveQuestionResponse): CognitiveResult {
        return try {
            apiService.submitCognitiveAnswers(answers) // API 호출
        } catch (e: Exception) {
            CognitiveResult("오류", emptyList(), emptyMap(), "제출 실패: ${e.message}") // 임시 에러 결과
        }
    }
}