package com.harucoach.harucoachfront.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harucoach.harucoachfront.data.models.CognitiveQuestionResponse
import com.harucoach.harucoachfront.data.models.CognitiveResult
import com.harucoach.harucoachfront.data.models.CognitiveUiState
import com.harucoach.harucoachfront.data.models.Point
import com.harucoach.harucoachfront.data.repository.CognitiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CognitiveViewModel @Inject constructor(private val repository: CognitiveRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CognitiveUiState>(CognitiveUiState.Loading)
    val uiState: StateFlow<CognitiveUiState> = _uiState.asStateFlow()

    private var questions: CognitiveQuestionResponse = CognitiveQuestionResponse(sessionId = 0, questionList = emptyList()) // 문제 저장


    /**
     * to /cognitive/start
     */
    fun loadQuestions() {
        // TODO repository 에서 호출해서 데이터 가져오기
        viewModelScope.launch {
            _uiState.value = CognitiveUiState.Loading

            questions = repository.getCognitiveTestQuestions()

            _uiState.value = if (questions.sessionId == -1 || questions.sessionId == 0) {
                CognitiveUiState.Ready(questions)
            } else CognitiveUiState.Error("문제 불러오기 실패")
        }
    }// end loadQuestions

    /**
     * viewModel 이 가지고 있는 questions에 user_answer 변경하는 로직
     */
    fun updateAnswer(questionId: Int, answer: String) {
        // TODO 사용자 질문들 넣기
    }// end updateAnswer

    /**
     * to /cognitive/result
     */
    fun submitAnswers() {
        // TODO 결과값 가져오기  (사용자 답변을 가져오는 방법)
        viewModelScope.launch {
            _uiState.value = CognitiveUiState.Waiting
            val answersList = questions
            val result = repository.submitCognitiveAnswers(answersList)
            _uiState.value = CognitiveUiState.Result(result)
        }
    }// end submitAnswers

}// end CognitiveViewModel