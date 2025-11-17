package com.harucoach.harucoachfront.ui.screens.learn.games

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

// 게임 진행 단계
enum class GamePhase {
    READY,      // 준비 (숫자 표시 전)
    SHOWING,    // 숫자 표시 중
    INPUT,      // 사용자 입력 대기
    RESULT      // 결과 표시
}

// 게임 상태
data class MemoryGameState(
    val currentDigits: Int = 3,           // 현재 자릿수
    val targetNumber: String = "",         // 정답 숫자
    val phase: GamePhase = GamePhase.READY,
    val totalPoints: Int = 0,              // 이번 게임에서 획득한 총 포인트
    val roundPoints: Int = 0               // 현재 라운드 포인트
)

@Composable
fun MemoryGameScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    // ==================== 상태 관리 ====================
    var gameState by remember { mutableStateOf(MemoryGameState()) }
    var userAnswer by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // ViewModel에서 최고 기록 가져오기
    val bestRecord by gameViewModel.memoryGameBest.collectAsState()

    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // ==================== 음성 인식 설정 ====================
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
                    SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "음성 인식 오류"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // 숫자만 추출 (예: "사백이십칠" → "427")
                    userAnswer = matches[0].filter { it.isDigit() }
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

    // ==================== 게임 로직 함수 ====================

    // 랜덤 숫자 생성
    fun generateNumber(digits: Int): String {
        return (1..digits).map { Random.nextInt(0, 10) }.joinToString("")
    }

    // 자릿수에 따른 포인트 계산
    fun calculatePoints(digits: Int): Int {
        return when (digits) {
            3 -> 10
            4 -> 20
            5 -> 50
            6 -> 100
            7 -> 200
            else -> 0
        }
    }

    // 게임 시작
    fun startRound() {
        val number = generateNumber(gameState.currentDigits)
        val points = calculatePoints(gameState.currentDigits)

        gameState = gameState.copy(
            targetNumber = number,
            phase = GamePhase.SHOWING,
            roundPoints = points
        )
        userAnswer = ""
    }

    // 정답 확인
    fun checkAnswer() {
        isCorrect = userAnswer == gameState.targetNumber

        if (isCorrect) {
            // 정답: 다음 라운드로
            val newTotalPoints = gameState.totalPoints + gameState.roundPoints

            dialogMessage = """
                정답입니다! 🎉
                
                ${gameState.currentDigits}자리 성공!
                +${gameState.roundPoints}점 획득!
                
                다음은 ${gameState.currentDigits + 1}자리에 도전하세요!
            """.trimIndent()

            gameState = gameState.copy(
                phase = GamePhase.RESULT,
                totalPoints = newTotalPoints
            )

        } else {
            // 오답: 게임 종료
            val finalDigits = gameState.currentDigits
            val finalPoints = gameState.totalPoints

            // ViewModel에 최고 기록 저장
            gameViewModel.onMemoryGameComplete(
                digits = finalDigits,
                points = finalPoints
            )

            dialogMessage = """
                아쉽습니다!
                
                정답: ${gameState.targetNumber}
                당신의 답: $userAnswer
                
                최종 기록: ${finalDigits}자리
                획득 포인트: ${finalPoints}점
            """.trimIndent()

            gameState = gameState.copy(phase = GamePhase.RESULT)
        }

        showResultDialog = true
    }

    // 다음 라운드
    fun nextRound() {
        gameState = gameState.copy(
            currentDigits = gameState.currentDigits + 1,
            phase = GamePhase.READY
        )
    }

    // 게임 재시작
    fun restartGame() {
        gameState = MemoryGameState()
    }

    // ==================== 자동 진행 로직 ====================

    // READY → SHOWING (자동 시작)
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.READY) {
            delay(500) // 0.5초 대기
            startRound()
        }
    }

    // SHOWING → INPUT (2초 후 자동 전환)
    LaunchedEffect(gameState.phase, gameState.targetNumber) {
        if (gameState.phase == GamePhase.SHOWING) {
            delay(2000) // 2초 표시
            gameState = gameState.copy(phase = GamePhase.INPUT)
        }
    }

    // ==================== UI ====================

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

            // 1. 상단 정보 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF43A047)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "현재 자릿수",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${gameState.currentDigits}자리",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "누적 포인트",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${gameState.totalPoints}점",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (bestRecord > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "최고 기록: ${bestRecord}자리",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. 메인 컨텐츠 영역
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (gameState.phase) {
                        GamePhase.READY -> {
                            Text(
                                text = "준비하세요...",
                                fontSize = 24.sp,
                                color = Color.Gray
                            )
                        }

                        GamePhase.SHOWING -> {
                            // 숫자를 크게 표시
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = scaleIn() + fadeIn()
                            ) {
                                Text(
                                    text = gameState.targetNumber,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF43A047),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        GamePhase.INPUT -> {
                            Text(
                                text = "기억한 숫자를\n입력하세요",
                                fontSize = 24.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        GamePhase.RESULT -> {
                            // 결과는 다이얼로그로 표시
                        }
                    }
                }
            }

            // 3. 입력 필드 (INPUT 단계에서만 표시)
            AnimatedVisibility(
                visible = gameState.phase == GamePhase.INPUT,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it.filter { char -> char.isDigit() } },
                    label = { Text("숫자를 입력하세요") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            speechRecognizer.startListening(speechRecognizerIntent)
                        }) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "음성으로 입력",
                                tint = if (isListening) Color(0xFF43A047) else Color.Gray
                            )
                        }
                    }
                )
            }

            // 4. 정답 확인 버튼 (INPUT 단계에서만 표시)
            AnimatedVisibility(
                visible = gameState.phase == GamePhase.INPUT,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Button(
                    onClick = { checkAnswer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF43A047)
                    ),
                    enabled = userAnswer.length == gameState.currentDigits
                ) {
                    Text(
                        text = if (userAnswer.length == gameState.currentDigits) {
                            "정답 확인"
                        } else {
                            "${gameState.currentDigits}자리 숫자를 입력하세요"
                        },
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    // ==================== 결과 다이얼로그 ====================

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { /* 빈 클릭 방지 */ },
            title = {
                Text(
                    if (isCorrect) "정답! 🎉" else "게임 종료",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                if (isCorrect) {
                    Button(
                        onClick = {
                            showResultDialog = false
                            nextRound()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047)
                        )
                    ) {
                        Text("다음 라운드")
                    }
                } else {
                    Button(
                        onClick = {
                            showResultDialog = false
                            restartGame()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047)
                        )
                    ) {
                        Text("다시 도전")
                    }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("나가기")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryGameScreenPreview() {
    MemoryGameScreen(navController = rememberNavController())
}