package com.harucoach.harucoachfront.data.models

// ======= Start =======
data class Question(
    val questionNo: Int,
    val questionId: Int,
    val text: String,
    val category: String?
)

data class StartResponse(
    val sessionId: Long,
    val questions: List<Question>
)

// ======= Submit =======
data class AnswerItem(
    val questionNo: Int,
    val questionId: Int,
    val sttText: String? = null,
    val typedText: String? = null,
    val latencyMs: Int? = null
)

data class SubmitRequest(
    val sessionId: Long,
    val answers: List<AnswerItem>
)

// ======= Result =======
data class ResultResponse(
    val totalScore: Double,
    val perQuestion: Map<Int, Double>,
    val summary: String,
    val grade: String
)
