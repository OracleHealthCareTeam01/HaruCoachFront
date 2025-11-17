package com.harucoach.harucoachfront.ui.screens.learn.games

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.viewmodel.GameViewModel
import java.util.Locale

// 게임의 현재 상태를 관리하기 위한 데이터 클래스
data class GameState(
    val number1: Int = (10..99).random(), // 2자리 숫자 (10~99)
    val number2: Int = (1..9).random(),   // 1자리 숫자 (1~9)
) {
    // 실제 정답 계산
    val answer: Int
        get() = number1 + number2
}

@Composable
fun NumbersGameScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()  // 🔥 ViewModel 추가!
) {
    // --- 상태 관리 ---
    var gameState by remember { mutableStateOf(GameState()) }
    var userAnswer by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }  // 🔥 정답 여부 추가

    // 🔥 ViewModel에서 현재 점수 가져오기
    val currentScore by gameViewModel.numbersGameScore.collectAsState()

    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // SpeechRecognizer 설정
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                errorMessage = ""
                isListening = true
                Toast.makeText(context, "녹음 시작...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 오류"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                    SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기 사용 중"
                    SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "알 수 없는 오류: $error"
                }
                errorMessage = "오류: $errorMsg"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    userAnswer = matches[0]
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer.setRecognitionListener(listener)

        onDispose {
            speechRecognizer.destroy()
        }
    }

    // --- 함수 정의 ---
    fun generateNewProblem() {
        gameState = GameState()
        userAnswer = ""
    }

    // 🔥 정답 확인 함수 수정
    fun checkAnswer() {
        isCorrect = userAnswer.toIntOrNull() == gameState.answer

        if (isCorrect) {
            // 정답일 때 ViewModel에 알림
            gameViewModel.onNumbersGameCorrect()
            dialogMessage = "정답입니다! 🎉\n+1점 획득!"
        } else {
            dialogMessage = "틀렸습니다. 다시 풀어보세요."
        }

        showResultDialog = true
    }

    // --- UI 레이아웃 ---
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 🔥 1. 현재 점수 표시 (맨 위에 추가)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 점수",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "$currentScore 점",
                        color = Color.White,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // 2. 문제가 나오는 칸
            Text(
                text = "${gameState.number1} + ${gameState.number2} = ?",
                fontSize = 48.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            // 3. 답을 적는 칸 + 마이크 버튼
            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("정답을 입력하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        speechRecognizer.startListening(speechRecognizerIntent)
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "음성으로 답하기")
                    }
                }
            )

            // 4. 정답 확인 버튼
            Button(
                onClick = { checkAnswer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("정답 확인", fontSize = 18.sp)
            }
        }
    }

    // 6. 결과 다이얼로그
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text(if (isCorrect) "정답! 🎉" else "틀렸습니다") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        generateNewProblem()
                    }
                ) {
                    Text("다음 문제 풀기")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        navController.navigate("learn") {
                            popUpTo("learn") { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("그만하기")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NumbersGamePreview() {
    NumbersGameScreen(navController = rememberNavController())
}