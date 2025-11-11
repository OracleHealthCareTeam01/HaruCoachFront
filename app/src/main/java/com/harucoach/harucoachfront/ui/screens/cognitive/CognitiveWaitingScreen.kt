package com.harucoach.harucoachfront.ui.screens.cognitive

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harucoach.harucoachfront.viewmodel.CognitiveViewModel

/**
 * CognitiveWaitingScreen.kt
 *
 * - 시험(검사)이 끝난 후 결과를 생성하는 중에 보여주는 대기 화면입니다.
 * - 닉네임, 진행 중 로더, 진행 메시지(하단)를 포함합니다.
 *
 * 사용:
 * CognitiveWaitingScreen(nickname = "홍길동", subtitle = "검사결과를 생성하고 있습니다...")
 */

/* -------------------- 화면 컴포저블 선언 -------------------- */
@Composable
fun CognitiveWaitingScreen(
    nickname: String = "{닉네임}님", // 상단에 표시될 닉네임 (기본값)
    subtitle: String = "검사결과를 생성하고 있습니다...", // 하단 상태 텍스트
    modifier: Modifier = Modifier,
    viewModel: CognitiveViewModel = hiltViewModel()
) { // start CognitiveWaitingScreen

//    TODO : LaunchedEffect(Unit) { viewModel.submitAnswers() } 제출 로직

    // 전체 화면을 세로 중앙 정렬로 구성
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) { // start Column

        // 1) 상단 메시지: 큰 감사 문구와 설명
        Text(
            text = "수고하셨어요,", // 한 줄
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            textAlign = TextAlign.Center,
            color = Color(0xFF111111)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // nickname을 보여주는 두번째 줄 (강조)
        Text(
            text = nickname + " 🌿", // 예: "{닉네임}님 🌿"
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = Color(0xFF0F7A49)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // 추가 설명 줄 (작은 회색 텍스트)
        Text(
            text = "쿠모가 결과를 정리하고 있어요.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            textAlign = TextAlign.Center,
            color = Color(0xFF6B6B6B)
        )
        Spacer(modifier = Modifier.height(30.dp))


        // TODO
        //  1. 결과 값이 오면 결과를 볼 수 있는 페이지로 이동되게 버튼으로 변경 ?
        //  2. LaodingSpinnerWithLabel 멈추고 쿠모로 변경???

        // 2) 중앙 로더 영역: 커스텀 회전 애니메이션 + Loading 텍스트
        LoadingSpinnerWithLabel() // 기본 스타일 호출
        Spacer(modifier = Modifier.height(28.dp))

        // 3) 하단 카드형 상태 텍스트 (녹색 버튼처럼 보이게)
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)), // 진한 녹색
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = subtitle,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center
                )
            }
        }

    } // end Column
} // end CognitiveWaitingScreen

/* -------------------- 로더 컴포넌블 (애니메이션 포함) -------------------- */
/**
 * LoadingSpinnerWithLabel:
 * - 중앙에 회전하는 원형 스피너(커스텀)와 "Loading" 텍스트를 표시
 * - 간단한 애니메이션을 사용하여 스피너를 회전시킵니다.
 */
@Composable
private fun LoadingSpinnerWithLabel() { // start LoadingSpinnerWithLabel
    // 애니메이션 값: 무한히 0f -> 360f 로 회전
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 로더 사이즈 (px 단위를 dp로 변환 필요 시 LocalDensity 사용)
    val sizeDp = 96.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) { // start Column
        // 회전 애니메이션을 적용한 Box 안에 여러 개의 작은 원을 배치하여 '로더' 모양을 흉내냄
        Box(
            modifier = Modifier
                .size(sizeDp)
                .rotate(rotation) // 전체 박스를 회전시켜 로더처럼 보이게 함
        ) { // start Box spinner
            // 간단한 표현: 8개의 작은 원을 원형으로 배치 (시계 방향)
            val dotCount = 8
            val dotSize = 12.dp
            val radius = with(LocalDensity.current) { (sizeDp.toPx() * 0.35f) }

            // Canvas 대신 Column+Box로 간단히 구현 (정확한 pixel 위치 계산)
            // 각 점은 Box로 그려지고, 회전 애니메이션이 Box 전체에 적용되어 움직이는 효과를 냄
            for (i in 0 until dotCount) {
                // 각 dot의 회전 각도 (radian)
                val angle = (i.toFloat() / dotCount.toFloat()) * 2f * Math.PI.toFloat()
                // 위치는 draw 시간에 결정되므로 Box 내부의 offset 사용을 위해 absolute positioning 필요.
                // Compose의 Layout 조작이 복잡하므로 간단한 겹치는 작은 원을 사용.
            }

            // 대신, 더 간단하고 안정적인 방법: CircularProgressIndicator를 커스터마이즈하여 사용
            CircularProgressIndicator(
                strokeWidth = 6.dp,
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF616161)
            )
        } // end Box spinner

        Spacer(modifier = Modifier.height(8.dp))

        // 로더 레이블
        Text(
            text = "Loading",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B6B6B))
        )
    } // end Column
} // end LoadingSpinnerWithLabel

/* -------------------- Preview -------------------- */
@Preview
@Composable
fun CognitiveWaitingScreenPreview() {
    CognitiveWaitingScreen(
        nickname = "홍길동님 🌿",
        subtitle = "검사결과를 생성하고 있습니다..."
    )
}