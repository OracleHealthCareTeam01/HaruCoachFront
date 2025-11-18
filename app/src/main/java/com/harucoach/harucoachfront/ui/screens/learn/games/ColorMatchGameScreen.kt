package com.harucoach.harucoachfront.ui.screens.learn.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 색상 데이터 클래스
data class ColorItem(
    val name: String,      // "빨강", "파랑" 등
    val color: Color       // 실제 색상
)

// 문제 타입
enum class QuestionType {
    COLOR,    // 글자의 색깔을 물어봄
    TEXT      // 글자의 내용을 물어봄
}

// 게임 문제 데이터
data class ColorQuestion(
    val textContent: String,     // 글자 내용 ("빨강")
    val textColor: Color,        // 글자 색상 (파란색)
    val questionType: QuestionType,  // 색깔 or 내용
    val correctAnswer: String    // 정답
)

// 게임 상태
data class ColorGameState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val questions: List<ColorQuestion> = emptyList(),
    val isGameOver: Boolean = false,
    val remainingTime: Int = 10,
    val answerTime: Int = 0  // 답변에 걸린 시간
)

@Composable
fun ColorMatchGameScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    // 사용 가능한 색상들
    val colors = remember {
        listOf(
            ColorItem("빨강", Color(0xFFE53935)),
            ColorItem("파랑", Color(0xFF1E88E5)),
            ColorItem("초록", Color(0xFF43A047)),
            ColorItem("노랑", Color(0xFFFDD835))
        )
    }

    // 게임 상태
    var gameState by remember { mutableStateOf(ColorGameState()) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var showFinalResult by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }  // 🔥 나가기 확인 다이얼로그

    val coroutineScope = rememberCoroutineScope()

    // 🔥 10개 문제 생성
    fun generateQuestions(): List<ColorQuestion> {
        return List(10) { index ->
            val textColor = colors.random()
            val textContent = colors.random()
            val questionType = if (index % 2 == 0) QuestionType.COLOR else QuestionType.TEXT

            val correctAnswer = when (questionType) {
                QuestionType.COLOR -> textColor.name  // 글자 색깔이 정답
                QuestionType.TEXT -> textContent.name  // 글자 내용이 정답
            }

            ColorQuestion(
                textContent = textContent.name,
                textColor = textColor.color,
                questionType = questionType,
                correctAnswer = correctAnswer
            )
        }
    }

    // 게임 시작
    fun startGame() {
        gameState = ColorGameState(
            questions = generateQuestions(),
            remainingTime = 10
        )
    }

    // 정답 확인
    fun checkAnswer(selectedAnswer: String) {
        val currentQuestion = gameState.questions[gameState.currentQuestionIndex]
        val isCorrect = selectedAnswer == currentQuestion.correctAnswer
        val timeTaken = 10 - gameState.remainingTime

        var pointsEarned = 0
        if (isCorrect) {
            pointsEarned = 10
            // 5초 이내 정답 시 보너스
            if (timeTaken <= 5) {
                pointsEarned += 5
            }

            gameState = gameState.copy(
                score = gameState.score + pointsEarned,
                correctCount = gameState.correctCount + 1,
                answerTime = timeTaken
            )

            dialogMessage = if (timeTaken <= 5) {
                "정답! 🎉\n+${pointsEarned}점 (빠른 답변 보너스!)"
            } else {
                "정답! ✅\n+${pointsEarned}점"
            }
        } else {
            dialogMessage = "틀렸습니다 ❌\n정답: ${currentQuestion.correctAnswer}"
        }

        showResultDialog = true
    }

    // 다음 문제로
    fun nextQuestion() {
        if (gameState.currentQuestionIndex < 9) {
            gameState = gameState.copy(
                currentQuestionIndex = gameState.currentQuestionIndex + 1,
                remainingTime = 10
            )
        } else {
            // 게임 종료
            var finalScore = gameState.score

            // 완벽한 10문제 보너스
            if (gameState.correctCount == 10) {
                finalScore += 50
            }

            gameState = gameState.copy(
                score = finalScore,
                isGameOver = true
            )

            // ViewModel에 점수 저장
            gameViewModel.onColorGameComplete(finalScore, finalScore)

            showFinalResult = true
        }
    }

    // 게임 시작 (최초 1회)
    LaunchedEffect(Unit) {
        startGame()
    }

    // 타이머
    LaunchedEffect(gameState.currentQuestionIndex, gameState.remainingTime) {
        if (!gameState.isGameOver && gameState.questions.isNotEmpty() && gameState.remainingTime > 0) {
            delay(1000)
            gameState = gameState.copy(remainingTime = gameState.remainingTime - 1)

            // 시간 초과
            if (gameState.remainingTime == 0) {
                dialogMessage = "시간 초과! ⏰"
                showResultDialog = true
            }
        }
    }

    // --- UI ---
    if (gameState.questions.isEmpty()) {
        // 로딩 중
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
            .background(Color(0xFFF5F5F5))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 상단 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        Text(
                            text = "문제 ${gameState.currentQuestionIndex + 1}/10",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "점수: ${gameState.score}점",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "남은 시간",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${gameState.remainingTime}초",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.remainingTime <= 3) Color.Red else Color(0xFF1E88E5)
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

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 문제 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (currentQuestion.questionType) {
                        QuestionType.COLOR -> "이 글자의 색깔은?"
                        QuestionType.TEXT -> "이 글자의 내용은?"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // 색깔이 다른 글자
                Text(
                    text = currentQuestion.textContent,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentQuestion.textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 선택지 버튼들 (2x2 그리드)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.take(2).forEach { colorItem ->
                    ChoiceButton(
                        text = colorItem.name,
                        color = colorItem.color,
                        onClick = { checkAnswer(colorItem.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.drop(2).forEach { colorItem ->
                    ChoiceButton(
                        text = colorItem.name,
                        color = colorItem.color,
                        onClick = { checkAnswer(colorItem.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 4. 정답/오답 다이얼로그
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("결과") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        nextQuestion()
                    }
                ) {
                    Text(if (gameState.currentQuestionIndex < 9) "다음 문제" else "결과 보기")
                }
            }
        )
    }

    // 5. 최종 결과 다이얼로그
    if (showFinalResult) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("게임 종료! 🎉") },
            text = {
                Column {
                    Text("총 점수: ${gameState.score}점")
                    Text("정답 개수: ${gameState.correctCount}/10")
                    if (gameState.correctCount == 10) {
                        Text(
                            "완벽합니다! 🏆 (+50점 보너스)",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalResult = false
                        startGame()  // 다시 시작
                    }
                ) {
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

// 선택지 버튼 컴포넌트
@Composable
private fun ChoiceButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .border(3.dp, color, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
