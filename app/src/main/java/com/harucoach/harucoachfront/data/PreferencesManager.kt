package com.harucoach.harucoachfront.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class PreferencesManager(private val context: Context) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

        //게임 점수 관련 키 추가
        private val TOTAL_GAME_POINTS = intPreferencesKey("total_game_points")
        private val NUMBERS_GAME_SCORE = intPreferencesKey("numbers_game_score")
        private val MEMORY_GAME_BEST = intPreferencesKey("memory_game_best")
        private val COLOR_GAME_BEST = intPreferencesKey("color_game_best")
        private val CHOSUNG_GAME_BEST = intPreferencesKey("chosung_game_best")
    }

    // 토큰 읽기
    val authTokenFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[AUTH_TOKEN_KEY] } // end authTokenFlow

    // 토큰 저장
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN_KEY] = token
        }
    }// end saveAuthToken

    // 토큰 제거 (로그아웃)
    suspend fun clearAuthToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN_KEY)
        }
    }// end clearAuthToken


    /**
     * 앱 시작 시 한 번만(동기적으로) 토큰을 읽어야 할 때 사용.
     * 주의: runBlocking으로 메인 스레드를 잠시 블록함 — 짧게 읽는 용도로만 사용하세요.
     */
    fun readAuthTokenBlocking(): String? {
        return try {
            runBlocking {
                context.dataStore.data
                    .map { prefs -> prefs[AUTH_TOKEN_KEY] } // null 가능
                    .first()
            }
        } catch (e: Exception) {
            null
        }
    }// end readAuthTokenBlocking

    // ==================== 🔥 게임 점수 관련 ====================

    // 총 누적 포인트 읽기 (Flow)
    val totalGamePointsFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[TOTAL_GAME_POINTS] ?: 0 }

    // 총 누적 포인트 읽기 (동기)
    fun getTotalGamePoints(): Int {
        return try {
            runBlocking {
                context.dataStore.data
                    .map { prefs -> prefs[TOTAL_GAME_POINTS] ?: 0 }
                    .first()
            }
        } catch (e: Exception) {
            0
        }
    }// end getTotalGamePoints

    // 총 누적 포인트 추가
    suspend fun addGamePoints(points: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_GAME_POINTS] ?: 0
            prefs[TOTAL_GAME_POINTS] = current + points
        }
    }// end addGamePoints

    // 숫자 게임 점수 읽기
    val numbersGameScoreFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[NUMBERS_GAME_SCORE] ?: 0 }

    // 숫자 게임 점수 저장
    suspend fun saveNumbersGameScore(score: Int) {
        context.dataStore.edit { prefs ->
            prefs[NUMBERS_GAME_SCORE] = score
        }
    }// end saveNumberGameScore

    // 숫자 게임 점수 1점 추가
    suspend fun incrementNumbersGameScore() {
        context.dataStore.edit { prefs ->
            val current = prefs[NUMBERS_GAME_SCORE] ?: 0
            prefs[NUMBERS_GAME_SCORE] = current + 1
        }
    }// end incrementNumberGameScore

    // 숫자 기억 게임 최고 기록 읽기
    val memoryGameBestFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[MEMORY_GAME_BEST] ?: 0 }

    // 숫자 기억 게임 최고 기록 저장 (더 높은 기록만 저장)
    suspend fun updateMemoryGameBest(digits: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[MEMORY_GAME_BEST] ?: 0
            if (digits > current) {
                prefs[MEMORY_GAME_BEST] = digits
            }
        }
    }// end updateMemoryGameBest

    // 색깔 맞추기 게임 최고 점수 읽기
    val colorGameBestFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[COLOR_GAME_BEST] ?: 0 }

    // 색깔 맞추기 게임 최고 점수 저장
    suspend fun updateColorGameBest(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[COLOR_GAME_BEST] ?: 0
            if (score > current) {
                prefs[COLOR_GAME_BEST] = score
            }
        }
    }// end colorGameBestFlow

    // 🔥 초성 맞추기 게임 최고 점수 읽기
    val chosungGameBestFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[CHOSUNG_GAME_BEST] ?: 0 }

    // 🔥 초성 맞추기 게임 최고 점수 저장
    suspend fun updateChosungGameBest(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[CHOSUNG_GAME_BEST] ?: 0
            if (score > current) {
                prefs[CHOSUNG_GAME_BEST] = score
            }
        }
    }

    //  모든 게임 데이터 초기화 (테스트용)
    suspend fun clearAllGameData() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOTAL_GAME_POINTS)
            prefs.remove(NUMBERS_GAME_SCORE)
            prefs.remove(MEMORY_GAME_BEST)
            prefs.remove(COLOR_GAME_BEST)
        }
    }// end clearAllGameData

}// end PreferencesManager

