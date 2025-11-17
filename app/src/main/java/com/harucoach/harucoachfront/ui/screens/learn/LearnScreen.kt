package com.harucoach.harucoachfront.ui.screens.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.viewmodel.GameViewModel

/**
 * LearnScreen - 오늘의 학습 메인 화면
 *
 * 여러 인지능력 향상 게임들을 선택할 수 있는 화면입니다.
 * - 숫자 게임 (기존)
 * - 숫자 기억 게임 (새로 추가)
 * - 색깔 맞추기 게임 (새로 추가)
 */
@Composable
fun LearnScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()  // 🔥 ViewModel 추가!
) {
    // 🔥 ViewModel에서 데이터 가져오기
    val totalPoints by gameViewModel.totalPoints.collectAsState()
    val numbersGameScore by gameViewModel.numbersGameScore.collectAsState()
    val memoryGameBest by gameViewModel.memoryGameBest.collectAsState()
    val colorGameBest by gameViewModel.colorGameBest.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 1. 누적 포인트 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFB74D),  // 주황
                                Color(0xFFFF9800)   // 진한 주황
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "트로피",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(
                            text = "총 누적 포인트",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "$totalPoints 점",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. 게임 안내 텍스트
        Text(
            text = "🎮 게임을 선택해주세요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        // 3. 게임 카드 목록
        GameCard(
            title = "숫자 게임",
            description = "기존에 있던 숫자 게임입니다.",
            emoji = "🔢",
            bestScore = "현재 점수: $numbersGameScore 점",  // 🔥 실시간 점수 표시
            gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)),
            onClick = {
                navController.navigate("numbers_game")
            }
        )

        GameCard(
            title = "숫자 기억하기",
            description = "화면에 표시되는 숫자를 기억하세요!",
            emoji = "🧠",
            bestScore = if (memoryGameBest > 0) "최고 기록: ${memoryGameBest}자리" else "최고 기록: -",  // 🔥 실시간 기록
            gradientColors = listOf(Color(0xFF66BB6A), Color(0xFF43A047)),
            onClick = {
                navController.navigate("memory_game")
            }
        )

        GameCard(
            title = "색깔 맞추기",
            description = "색깔과 글자를 빠르게 구분하세요!",
            emoji = "🎨",
            bestScore = if (colorGameBest > 0) "최고 기록: ${colorGameBest}점" else "최고 기록: -",  // 🔥 실시간 점수
            gradientColors = listOf(Color(0xFFEC407A), Color(0xFFE91E63)),
            onClick = {
                navController.navigate("color_game")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * GameCard - 개별 게임을 나타내는 카드 컴포넌트
 */
@Composable
private fun GameCard(
    title: String,
    description: String,
    emoji: String,
    bestScore: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 이모지 아이콘 (그라데이션 배경)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Brush.verticalGradient(gradientColors),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 오른쪽: 게임 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bestScore,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF999999)
                )
            }

            // 화살표 아이콘
            Text(
                text = "▶",
                fontSize = 20.sp,
                color = Color(0xFFBBBBBB)
            )
        }
    }
}

// ==================== Preview ====================
@Preview(showBackground = true)
@Composable
private fun LearnScreenPreview() {
    MaterialTheme {
        Surface {
            LearnScreen(
                navController = rememberNavController()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            GameCard(
                title = "숫자 기억하기",
                description = "화면에 표시되는 숫자를 기억하세요!",
                emoji = "🧠",
                bestScore = "최고 기록: 5자리",
                gradientColors = listOf(Color(0xFF66BB6A), Color(0xFF43A047)),
                onClick = {}
            )
        }
    }
}