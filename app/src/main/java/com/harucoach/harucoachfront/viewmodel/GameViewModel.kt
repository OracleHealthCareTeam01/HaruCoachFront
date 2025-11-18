package com.harucoach.harucoachfront.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harucoach.harucoachfront.data.PreferencesManager
import com.harucoach.harucoachfront.data.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * GameViewModel - 게임 점수 및 기록 관리
 *
 * 모든 게임의 점수와 최고 기록을 관리합니다.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application.applicationContext)

    // ==================== UI State ====================

    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    private val _numbersGameScore = MutableStateFlow(0)
    val numbersGameScore: StateFlow<Int> = _numbersGameScore.asStateFlow()

    private val _memoryGameBest = MutableStateFlow(0)
    val memoryGameBest: StateFlow<Int> = _memoryGameBest.asStateFlow()

    private val _colorGameBest = MutableStateFlow(0)
    val colorGameBest: StateFlow<Int> = _colorGameBest.asStateFlow()

    private val _chosungGameBest = MutableStateFlow(0)  // 초성 게임 추가
    val chosungGameBest: StateFlow<Int> = _chosungGameBest.asStateFlow()

    // ==================== 초기화 ====================

    init {
        loadAllGameData()
    }

    // 모든 게임 데이터 로드
    private fun loadAllGameData() {
        viewModelScope.launch {
            // 총 포인트
            prefsManager.totalGamePointsFlow.collect { points ->
                _totalPoints.value = points
            }
        }

        viewModelScope.launch {
            // 숫자 게임 점수
            prefsManager.numbersGameScoreFlow.collect { score ->
                _numbersGameScore.value = score
            }
        }

        viewModelScope.launch {
            // 숫자 기억 게임 최고 기록
            prefsManager.memoryGameBestFlow.collect { best ->
                _memoryGameBest.value = best
            }
        }

        viewModelScope.launch {
            // 색깔 맞추기 게임 최고 점수
            prefsManager.colorGameBestFlow.collect { best ->
                _colorGameBest.value = best
            }
        }

        viewModelScope.launch {
            // 초성 맞추기 게임 최고 점수
            prefsManager.chosungGameBestFlow.collect { best ->
                _chosungGameBest.value = best
            }
        }
    }

    // ==================== 숫자 게임 ====================

    /**
     * 숫자 게임에서 정답을 맞췄을 때 호출
     * - 숫자 게임 점수 +1
     * - 총 누적 포인트 +1
     */
    fun onNumbersGameCorrect() {
        viewModelScope.launch {
            prefsManager.incrementNumbersGameScore()
            prefsManager.addGamePoints(1)
        }
    }

    // ==================== 숫자 기억 게임 ====================

    /**
     * 숫자 기억 게임에서 새로운 기록 달성 시 호출
     * @param digits 맞춘 자릿수
     * @param points 획득할 포인트
     */
    fun onMemoryGameComplete(digits: Int, points: Int) {
        viewModelScope.launch {
            prefsManager.updateMemoryGameBest(digits)
            prefsManager.addGamePoints(points)
        }
    }

    // ==================== 색깔 맞추기 게임 ====================

    /**
     * 색깔 맞추기 게임 완료 시 호출
     * @param score 획득한 점수
     * @param points 누적 포인트에 추가할 값
     */
    fun onColorGameComplete(score: Int, points: Int) {
        viewModelScope.launch {
            prefsManager.updateColorGameBest(score)
            prefsManager.addGamePoints(points)
        }
    }

    // ==================== 초성 맞추기 게임 ====================

    /**
     * 초성 맞추기 게임 완료 시 호출
     * @param score 획득한 점수
     * @param points 누적 포인트에 추가할 값
     */
    fun onChosungGameComplete(score: Int, points: Int) {
        viewModelScope.launch {
            prefsManager.updateChosungGameBest(score)
            prefsManager.addGamePoints(points)
        }
    }

    // ==================== 기타 ====================

    /**
     * 모든 게임 데이터 초기화 (테스트용)
     */
    fun clearAllData() {
        viewModelScope.launch {
            prefsManager.clearAllGameData()
        }
    }
}// end GameViewModel