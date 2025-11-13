package com.harucoach.harucoachfront.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harucoach.harucoachfront.data.models.AnswerItem
import com.harucoach.harucoachfront.data.models.Question
import com.harucoach.harucoachfront.data.models.ResultResponse
import com.harucoach.harucoachfront.data.repository.CognitiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CognitiveUiState(
    val loading: Boolean = false,
    val sessionId: Long? = null,
    val questions: List<Question> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val result: ResultResponse? = null,
    val error: String? = null
)

@HiltViewModel
class CognitiveViewModel @Inject constructor(
    private val repo: CognitiveRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CognitiveUiState())
    val uiState: StateFlow<CognitiveUiState> = _uiState

    init {
        // 화면 진입 시 자동으로 10문항 로드
        startTest(userId = 2, count = 10)
    }


    fun startTest(userId: Int = 1, count: Int = 10, category: String? = null) {
        _uiState.value = _uiState.value.copy(loading = true, error = null, result = null)
        viewModelScope.launch {
            try {
                val res = repo.startTest(userId, count, category)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    sessionId = res.sessionId,
                    questions = res.questions,
                    answers = emptyMap(),

                    )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false, error = "시작 실패: ${e.message}"
                )
            }
        }
    }

    fun updateAnswer(questionNo: Int, text: String) {
        val newMap = _uiState.value.answers.toMutableMap()
        newMap[questionNo] = text
        _uiState.value = _uiState.value.copy(answers = newMap)
    }

    fun submit(sttList: List<String>, latencyMs: List<Int>) {
        val state = _uiState.value
        val sid = state.sessionId ?: return
        val payload = state.questions.map { q ->
            AnswerItem(
                questionNo = q.questionNo,
                questionId = q.questionId,
                sttText = sttList[q.questionNo - 1],                    // STT 결과는 여기로
                typedText = state.answers[q.questionNo] ?: "",
                latencyMs = latencyMs[q.questionNo - 1]
            )
        }
        Log.d("테스트",payload.toString())

        _uiState.value = state.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val res = repo.submitAnswers(sid, payload)
                _uiState.value = _uiState.value.copy(loading = false, result = res)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false, error = "제출 실패: ${e.message}"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = CognitiveUiState()
    }
}
