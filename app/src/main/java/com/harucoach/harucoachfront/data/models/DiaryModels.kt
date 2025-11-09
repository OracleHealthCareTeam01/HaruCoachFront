package com.harucoach.harucoachfront.data.models

import java.time.LocalDate


/**
 * 일기 임시 데이터
 * TODO 백엔드 측과 확인 후 수정
 */
data class DiaryEntry(
    val date: LocalDate,
    val mood: String,
    val text: String
)

// 백엔드 응답 예시: 전체 일기 목록 (임시 데이터로 사용)
data class DiaryListResponse(
    val entries: List<DiaryEntry>
)