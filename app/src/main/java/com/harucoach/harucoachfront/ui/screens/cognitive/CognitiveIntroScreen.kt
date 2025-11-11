package com.harucoach.harucoachfront.ui.screens.cognitive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harucoach.harucoachfront.ui.theme.HaruGreen
import com.harucoach.harucoachfront.viewmodel.CognitiveViewModel

@Composable
fun CognitiveIntroScreen(
    onStart: () -> Unit = {},
    viewModel: CognitiveViewModel = hiltViewModel()
) {
    // Scaffold의 Top/BottomBar는 HaruApp에서 이미 제공하니 여기서는 본문만
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 큰 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 아래 버튼 공간을 두기 위해
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFEFF5F1)) // 아주 옅은 테두리
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "하루코치와 함께\n인지능력검사를\n시작해볼까요?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 36.sp
                    )
                    Text(
                        text = "안녕하세요! 저는 {마스코트 이름}이에요 😊\n" +
                                "지금부터 {닉네임}님의 생각과 기억을\n살짝 살펴보는 시간을 가져 볼게요.\n\n" +
                                "결과를 바탕으로, {닉네임}님께 꼭 맞는 두뇌 활동을\n추천드릴게요. 시작해볼까요?",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280) // 회색 톤
                    )
                }
                // 빈 공간이 있어도 아래 버튼이 카드 밖으로 밀리지 않도록 Spacer는 필요 없음
            }
        }

        // 하단 시작 버튼
        // TODO viewModel.loadQuestions() 추가
        Button(
            onClick = onStart, // 아직 로직 없으면 비워둬도 OK
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HaruGreen)
        ) {
            Text("인지능력 검사하기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
    }
}
