package com.harucoach.harucoachfront.data.repository

import com.harucoach.harucoachfront.data.models.DiaryEntry
import com.harucoach.harucoachfront.data.models.DiaryListResponse
import com.harucoach.harucoachfront.data.remote.ApiService  // 기존 ApiService import
import com.harucoach.harucoachfront.data.remote.RetrofitInstance
import java.time.LocalDate
import kotlinx.coroutines.delay  // 시뮬레이션 지연
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 일기 관련 사항 API 통신으로 불러오는 역활
 */
@Singleton
class DiaryRepository @Inject constructor(private val apiService: ApiService) {

    // 초기 전체 일기 데이터 가져오기 (백엔드 호출 시뮬레이션)
    suspend fun fetchAllDiaries(): Map<String, DiaryEntry> {
        try {
            // 임시 데이터: 하드코딩된 Map 반환 (백엔드 대신)
            // 실제 백엔드: val response = apiService.getAllDiaries()
            // return response.entries.associateBy { it.date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
            delay(500)  // 네트워크 지연 흉내
            return mapOf(
                LocalDate.now().minusDays(3).toString() to DiaryEntry(LocalDate.now().minusDays(3), "😊", "좋은 날이었다."),
                LocalDate.now().minusDays(7).toString() to DiaryEntry(LocalDate.now().minusDays(7), "😢", "슬픈 기억."),
                LocalDate.now().minusDays(10).toString() to DiaryEntry(LocalDate.now().minusDays(10), "😐", "평범한 하루.")
            )
        } catch (e: Exception) {
            throw Exception("데이터 로드 실패: ${e.message}")
        }
    }

    // 저장 (새 일기)
    suspend fun saveDiary(entry: DiaryEntry) {
        delay(500)  // 시뮬레이션
        // TODO 실제: apiService.saveDiary(entry)
    }

    // 수정 (기존 일기 업데이트)
    suspend fun updateDiary(entry: DiaryEntry) {
        delay(500)  // 시뮬레이션
        // TODO 실제: apiService.updateDiary(entry)
    }

    // 단일 일기 가져오기 (선택 시 사용)
    suspend fun getDiary(date: LocalDate): DiaryEntry? {
        delay(200)  // 시뮬레이션
        // TODO 실제: apiService.getDiary(date.toString())
        return null  // 임시: memoryStore에서 가져오지만, 여기선 ViewModel에서 처리
    }
}