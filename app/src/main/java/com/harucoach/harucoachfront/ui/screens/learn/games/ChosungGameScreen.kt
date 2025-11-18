package com.harucoach.harucoachfront.ui.screens.learn.games

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

// 초성 문제 데이터
data class ChosungQuestion(
    val answer: String,           // 정답 (예: "사과")
    val chosung: String,          // 초성 (예: "ㅅㄱ")
    val hint: String,             // 힌트 (예: "빨간색 과일")
    val category: String          // 카테고리 (예: "과일")
)

// 게임 상태
data class ChosungGameState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val questions: List<ChosungQuestion> = emptyList(),
    val isGameOver: Boolean = false,
    val remainingTime: Int = 15,
    val hintUsed: Boolean = false,
    val perfectRun: Boolean = true  // 힌트 없이 완벽하게 풀었는지
)

@Composable
fun ChosungGameScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current

    // 게임 상태
    var gameState by remember { mutableStateOf(ChosungGameState()) }
    var userAnswer by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var showFinalResult by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }  // 🔥 나가기 확인 다이얼로그

    // 음성 인식 상태
    var isListening by remember { mutableStateOf(false) }

    // 🎯 문제 은행
    val questionBank = remember {
        listOf(
            // 과일
            ChosungQuestion("사과", "ㅅㄱ", "🍎 빨간색 과일", "과일"),
            ChosungQuestion("바나나", "ㅂㄴㄴ", "🍌 노란색 긴 과일", "과일"),
            ChosungQuestion("딸기", "ㄸㄱ", "🍓 빨간 작은 과일", "과일"),
            ChosungQuestion("수박", "ㅅㅂ", "🍉 여름 대표 과일", "과일"),
            ChosungQuestion("포도", "ㅍㄷ", "🍇 알갱이가 많은 과일", "과일"),

            // 동물
            ChosungQuestion("강아지", "ㄱㅇㅈ", "🐕 사람의 친구", "동물"),
            ChosungQuestion("고양이", "ㄱㅇㅇ", "🐈 야옹야옹", "동물"),
            ChosungQuestion("토끼", "ㅌㄲ", "🐰 깡충깡충 뛰어요", "동물"),
            ChosungQuestion("호랑이", "ㅎㄹㅇ", "🐯 숲의 왕", "동물"),
            ChosungQuestion("코끼리", "ㅋㄲㄹ", "🐘 코가 긴 동물", "동물"),

            // 직업
            ChosungQuestion("의사", "ㅇㅅ", "👨‍⚕️ 병을 고치는 사람", "직업"),
            ChosungQuestion("선생님", "ㅅㅅㄴ", "👨‍🏫 학교에서 가르치는 사람", "직업"),
            ChosungQuestion("경찰", "ㄱㅊ", "👮 나쁜 사람을 잡아요", "직업"),
            ChosungQuestion("소방관", "ㅅㅂㄱ", "🚒 불을 끄는 사람", "직업"),
            ChosungQuestion("요리사", "ㅇㄹㅅ", "👨‍🍳 맛있는 음식을 만들어요", "직업"),

            // 나라
            ChosungQuestion("한국", "ㅎㄱ", "🇰🇷 우리나라", "나라"),
            ChosungQuestion("미국", "ㅁㄱ", "🇺🇸 자유의 여신상", "나라"),
            ChosungQuestion("일본", "ㅇㅂ", "🇯🇵 초밥의 나라", "나라"),
            ChosungQuestion("중국", "ㅈㄱ", "🇨🇳 만리장성", "나라"),
            ChosungQuestion("프랑스", "ㅍㄹㅅ", "🇫🇷 에펠탑", "나라"),

            // 음식
            ChosungQuestion("김치", "ㄱㅊ", "🥬 한국 대표 반찬", "음식"),
            ChosungQuestion("피자", "ㅍㅈ", "🍕 이탈리아 음식", "음식"),
            ChosungQuestion("치킨", "ㅊㅋ", "🍗 튀긴 닭고기", "음식"),
            ChosungQuestion("라면", "ㄹㅁ", "🍜 빨갛고 매운 면", "음식"),
            ChosungQuestion("햄버거", "ㅎㅂㄱ", "🍔 빵 사이에 고기", "음식")
        )
    }

    // SpeechRecognizer 설정
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                Toast.makeText(context, "말씀하세요!", Toast.LENGTH_SHORT).show()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                Toast.makeText(context, "음성 인식 실패", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    userAnswer = matches[0]
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    // 게임 시작
    fun startGame() {
        gameState = ChosungGameState(
            questions = questionBank.shuffled().take(10),
            remainingTime = 15
        )
        userAnswer = ""
        showHint = false
    }

    // 힌트 보기
    fun showHintClicked() {
        showHint = true
        gameState = gameState.copy(
            hintUsed = true,
            perfectRun = false
        )
    }

    // 정답 확인
    fun checkAnswer() {
        val currentQuestion = gameState.questions[gameState.currentQuestionIndex]
        val isCorrect = userAnswer.trim() == currentQuestion.answer
        val timeTaken = 15 - gameState.remainingTime

        var pointsEarned = 0
        if (isCorrect) {
            pointsEarned = 20

            // 힌트 사용 안 함 보너스
            if (!gameState.hintUsed) {
                pointsEarned += 10
            }

            // 10초 이내 정답 보너스
            if (timeTaken <= 10) {
                pointsEarned += 5
            }

            gameState = gameState.copy(
                score = gameState.score + pointsEarned,
                correctCount = gameState.correctCount + 1
            )

            dialogMessage = buildString {
                append("정답! 🎉\n")
                append("+${pointsEarned}점\n")
                if (!gameState.hintUsed) append("(힌트 미사용 보너스!)\n")
                if (timeTaken <= 10) append("(빠른 답변 보너스!)")
            }
        } else {
            dialogMessage = "틀렸습니다 ❌\n정답: ${currentQuestion.answer}"
        }

        showResultDialog = true
    }

    // 다음 문제
    fun nextQuestion() {
        if (gameState.currentQuestionIndex < 9) {
            gameState = gameState.copy(
                currentQuestionIndex = gameState.currentQuestionIndex + 1,
                remainingTime = 15,
                hintUsed = false
            )
            userAnswer = ""
            showHint = false
        } else {
            // 게임 종료
            var finalScore = gameState.score

            // 완벽 플레이 보너스
            if (gameState.correctCount == 10 && gameState.perfectRun) {
                finalScore += 100
            }

            gameState = gameState.copy(
                score = finalScore,
                isGameOver = true
            )

            gameViewModel.onChosungGameComplete(finalScore, finalScore)
            showFinalResult = true
        }
    }

    // 타이머
    LaunchedEffect(gameState.currentQuestionIndex, gameState.remainingTime) {
        if (!gameState.isGameOver && gameState.questions.isNotEmpty() && gameState.remainingTime > 0) {
            delay(1000)
            gameState = gameState.copy(remainingTime = gameState.remainingTime - 1)

            if (gameState.remainingTime == 0) {
                dialogMessage = "시간 초과! ⏰"
                gameState = gameState.copy(perfectRun = false)
                showResultDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        startGame()
    }

    // --- UI ---
    if (gameState.questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = gameState.questions[gameState.currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 상단 정보
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("문제 ${gameState.currentQuestionIndex + 1}/10", fontSize = 14.sp, color = Color.Gray)
                        Text("점수: ${gameState.score}점", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("남은 시간", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            "${gameState.remainingTime}초",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.remainingTime <= 5) Color.Red else Color(0xFFFF9800)
                        )
                    }
                }

                // 🔥 나가기 버튼
                IconButton(
                    onClick = { showExitDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "나가기",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 카테고리 표시
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFF9800).copy(alpha = 0.2f)
        ) {
            Text(
                text = "🏷️ ${currentQuestion.category}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 초성 표시
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "초성을 보고 맞춰보세요!",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentQuestion.chosung,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00),
                    letterSpacing = 8.sp
                )
            }
        }

        // 힌트 버튼 & 힌트 표시
        if (!showHint) {
            Button(
                onClick = { showHintClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("힌트 보기 (-10점)", fontSize = 16.sp)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        currentQuestion.hint,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 답 입력
        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            label = { Text("정답을 입력하세요") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { checkAnswer() }),
            trailingIcon = {
                IconButton(
                    onClick = { speechRecognizer.startListening(speechRecognizerIntent) }
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "음성 입력",
                        tint = if (isListening) Color.Red else Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // 정답 확인 버튼
        Button(
            onClick = { checkAnswer() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            enabled = userAnswer.isNotBlank()
        ) {
            Text("정답 확인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    // 결과 다이얼로그
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("결과") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showResultDialog = false
                    nextQuestion()
                }) {
                    Text(if (gameState.currentQuestionIndex < 9) "다음 문제" else "결과 보기")
                }
            }
        )
    }

    // 최종 결과
    if (showFinalResult) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("게임 종료! 🎉") },
            text = {
                Column {
                    Text("총 점수: ${gameState.score}점")
                    Text("정답: ${gameState.correctCount}/10")
                    if (gameState.correctCount == 10 && gameState.perfectRun) {
                        Text("완벽 플레이! 🏆 (+100점)", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showFinalResult = false
                    startGame()
                }) {
                    Text("다시 하기")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        navController.navigate("learn") {
                            popUpTo("learn") { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("돌아가기")
                }
            }
        )
    }

    // 🔥 나가기 확인 다이얼로그
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("게임 종료") },
            text = { Text("게임을 종료하시겠습니까?\n현재 점수는 저장되지 않습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        navController.navigate("learn") {
                            popUpTo("learn") { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("나가기")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false }
                ) {
                    Text("계속하기")
                }
            }
        )
    }
}
