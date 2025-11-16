package com.harucoach.harucoachfront.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.harucoach.harucoachfront.R
import kotlinx.coroutines.delay

// (기존 openUrl 함수는 그대로 유지)
fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

// 화면에 필요한 데이터를 표현하기 위한 data class (실제 API 응답에 따라 구조가 달라질 수 있음)
data class DaySummaryData(
    val summaryText: String,
    val emotion: String,
    val emoji: String,
    val keywords: List<String>,
    val recommendationText: String
)

// 임시 Mock 데이터 (실제로는 API 호출 결과로 채워질 것이며, 이전 충돌이 해결되었다는 가정 하에 사용합니다.)
val mockDaySummaryData = DaySummaryData(
    summaryText = "오늘은 평온하지만 살짝 피곤한 하루였어요.",
    emotion = "평온함",
    emoji = "😊",
    keywords = listOf("#산책", "#햇살", "#휴식"),
    recommendationText = "이럴 땐 3분 스트레칭이 좋아요"
)

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar를 사용하기 위해 필요합니다.
@Composable
fun DaySummary(navController: NavController) {
    val context = LocalContext.current

    // 데이터 로딩 상태 등을 관리하는 ViewModel이 있다면 여기서 데이터를 가져올 수 있습니다.
    // 현재는 임시 Mock 데이터를 사용합니다.
    val data = mockDaySummaryData

    // 각 UI 요소의 가시성을 제어할 상태 변수들
    val showRecommendationChat = remember { mutableStateOf(false) }
    val showEmotionReportCard = remember { mutableStateOf(false) }
    val showSummaryChatAndButtons = remember { mutableStateOf(false) }

    // LaunchedEffect를 사용하여 1초 간격으로 UI 요소들을 나타나게 함
    LaunchedEffect(Unit) {
        // 챗봇 추천 메시지 바로 표시
        showRecommendationChat.value = true
        delay(1000L) // 1초 대기

        // 감정 리포트 카드 표시
        showEmotionReportCard.value = true
        delay(1000L) // 1초 대기

        // 챗봇 요약 메시지와 버튼들 동시 표시
        showSummaryChatAndButtons.value = true
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Scaffold의 패딩 적용
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom // 요소를 아래에서부터 위로 쌓습니다.
            ) {
                // (이전에 발생했던 데이터 로딩/파싱 관련 충돌이 해결되고
                // 데이터가 성공적으로 로드되었다는 가정 하에 UI를 구성합니다.)



                // 챗봇 추천 메시지
                if (showRecommendationChat.value) {
                    ChatBubble(
                        text = data.recommendationText,
                        isUser = false, // 챗봇 메시지
                        modifier = Modifier.align(Alignment.Start) // 왼쪽 정렬
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }


                // 감정 리포트 카드 (day_summary 이미지 배경 적용)
                if (showEmotionReportCard.value) {
                    EmotionReportCard(
                        emotion = data.emotion,
                        emoji = data.emoji,
                        keywords = data.keywords,
                        modifier = Modifier.align(Alignment.CenterHorizontally) // 중앙 정렬
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 챗봇 요약 메시지
                if (showSummaryChatAndButtons.value) { // 버튼들과 함께 표시
                    ChatBubble(
                        text = data.summaryText,
                        isUser = false, // 챗봇 메시지
                        modifier = Modifier.align(Alignment.Start) // 왼쪽 정렬
                    )
                    // 마지막 요소이므로 하단에 추가 Spacer는 필요 없을 수 있습니다.
                    // 필요한 경우 추가: Spacer(modifier = Modifier.height(16.dp))
                }
                // "네, 할게요" / "괜찮아요" 버튼들
                if (showSummaryChatAndButtons.value) { // 챗봇 요약 메시지와 함께 표시
                    Column( // 버튼들을 세로로 쌓기 위해 Column 사용
                        modifier = Modifier.fillMaxWidth(), // Column이 전체 너비를 차지하도록 설정
                        horizontalAlignment = Alignment.End // Column의 내용을 오른쪽으로 정렬
                    ) {
                        Button(
                            onClick = {
                                val url = "https://www.youtube.com/"
                                openUrl(context, url)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White), // 배경색을 흰색으로 변경
                            shape = RoundedCornerShape(24.dp), // 둥근 모서리
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)), // 테두리 추가 (1dp 두께, 초록색)
                            modifier = Modifier
                                .wrapContentHeight() // 내용에 따라 높이 조절
                                .padding(horizontal = 4.dp)
                        ) {
                            Text("네, 할게요", color = Color(0xFF4CAF50), fontSize = 16.sp) // 텍스트 색상도 초록색으로 변경
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // 버튼들 사이에 세로 간격 추가

                        Button(
                            onClick = {
                                navController.popBackStack() // 이전 화면으로 돌아가기
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White), // 배경색을 흰색으로 변경
                            shape = RoundedCornerShape(24.dp), // 둥근 모서리
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)), // 테두리 추가 (1dp 두께, 초록색)
                            modifier = Modifier
                                .wrapContentHeight() // 내용에 따라 높이 조절
                                .padding(horizontal = 4.dp)
                        ) {
                            Text("괜찮아요", color = Color(0xFF4CAF50), fontSize = 16.sp) // 텍스트 색상도 초록색으로 변경
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    )
}

// (ChatBubble 컴포저블은 변경 없음)
@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.normal_feelings),
                contentDescription = "Chatbot character",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp
                ))
                .background(if (isUser) Color(0xFF4CAF50) else Color(0xFFC8E6C9))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .weight(1f, fill = false)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// (EmotionReportCard 컴포저블은 변경 없음)
@Composable
fun EmotionReportCard(
    emotion: String,
    emoji: String,
    keywords: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight()
    ) {
        Image(
            painter = painterResource(id = R.drawable.day_summary),
            contentDescription = "Emotion Report Card Background",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = "감정 리포트\n",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "감정: $emotion $emoji",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "키워드: ${keywords.joinToString(" ")}",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }
    }
}