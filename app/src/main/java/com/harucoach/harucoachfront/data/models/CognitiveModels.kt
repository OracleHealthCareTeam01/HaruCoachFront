package com.harucoach.harucoachfront.data.models

import java.io.Serializable

// TODO 임시 데이터이니 받을 데이터별로 파라미터 변경 요망


// UI 상태 sealed class (ViewModel에서 사용)
sealed class CognitiveUiState {
    object Loading : CognitiveUiState()
    data class Ready(val questions: CognitiveQuestionResponse) : CognitiveUiState()
    object Waiting : CognitiveUiState()
    data class Result(val result: CognitiveResult) : CognitiveUiState()
    data class Error(val message: String) : CognitiveUiState()
}


/**
 * 인지 검사 질문 파라미터
 */
data class CognitiveQuestionDetail(
    val id: Int,
    val question: String,
    val expected_answer: String,
    var user_anwser: String
) : Serializable


/**
 * 인지 검사 시작할떄 받아야 할 정보
 */
data class CognitiveQuestionResponse (
    val sessionId : Int,
    val questionList : List<CognitiveQuestionDetail>
) : Serializable


/**
 * 인지 검사 결과 값
 */
data class CognitiveResult(
    val grade: String, // "정상", "주의", "위험"
    val recentScores: List<Point>, // 최근 점수 추이 (x: 월, y: 점수)
    val categoryScores: Map<String, Double>, // 카테고리별 점수 e.g., "기억력" to 80.0
    val summary: String // 요약 텍스트
) : Serializable




// Point 클래스 (그래프용, 이전 코드에서 사용)
data class Point(val x: Float, val y: Float)