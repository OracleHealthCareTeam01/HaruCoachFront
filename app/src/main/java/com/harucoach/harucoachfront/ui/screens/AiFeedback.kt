package com.harucoach.harucoachfront.ui.screens

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader // ImageLoader를 import 합니다.
import coil.compose.AsyncImage // AsyncImage를 import 합니다.
import coil.decode.ImageDecoderDecoder // GIF 디코더를 import 합니다.

import com.harucoach.harucoachfront.R

/**
 * BottomSheet 내부에 표시될 AI 피드백 화면의 콘텐츠입니다.
 */

@Composable
fun InfiniteAnimation(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 화면 꽉 채우기
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),  // 좌우 꽉 채움
            //color = Color(207, 208, 212)// <<-- 이렇게 수정!
            color = Color.Transparent
        ) {
            AiFeedback(onDismissRequest)
        }

    }
}

@Composable
fun AiFeedback(onDismissRequest: () -> Unit) {

// 1. 'infiniteRepeatable' 애니메이션을 관리하는 '무한 전환(InfiniteTransition)' 객체를 생성하고 기억합니다.
    // 이 객체는 하나 이상의 자식 애니메이션(예: animateFloat)을 무한히 반복시킬 수 있습니다.
    val infiniteTransition = rememberInfiniteTransition(label = "HeartSizeAnimation") // label을 추가하면 디버깅 시 유용합니다.

    // 2. 'infiniteTransition'을 사용하여 Float 타입의 값을 무한히 변화시키는 애니메이션을 정의합니다.
    // 이 애니메이션은 아래 'animationSpec'에 따라 'initialValue'와 'targetValue' 사이를 계속 오갑니다.
    val heartSize by infiniteTransition.animateFloat(
        // 애니메이션 시작 값 (처음 크기)
        initialValue = 250.0f,
        // 애니메이션 목표 값 (최대로 커질 크기)
        targetValue = 300.0f,
        // 3. 애니메이션의 동작 방식과 세부 설정을 정의합니다.
        animationSpec = infiniteRepeatable(
            // 'tween'은 시작과 끝 값이 있는 기본적인 애니메이션을 만듭니다.
            animation = tween(
                durationMillis = 800, // 한 사이클(100.0f -> 250.0f)이 진행되는 시간 (0.8초)
                delayMillis = 100,    // 애니메이션이 시작되기 전의 딜레이 시간 (0.1초)
                easing = FastOutLinearInEasing // 애니메이션 속도 곡선: 처음엔 빨랐다가 선형 속도로 변경됩니다.
            ),
            // 'repeatMode'는 애니메이션이 반복될 때의 방식을 결정합니다.
            // 'RepeatMode.Reverse'는 정방향(100->250)으로 재생된 후, 역방향(250->100)으로 재생되어 부드럽게 커졌다 작아지는 효과를 줍니다.
            // 'RepeatMode.Restart'는 정방향 재생 후 다시 처음부터 정방향으로 재생됩니다 (크기가 뿅하고 다시 작아짐).
            repeatMode = RepeatMode.Reverse
        ),
        label = "쿠무tSizeValue" // 이 애니메이션 값에 대한 레이블
    )
    Box(
        // contentAlignment를 Center로 설정하면 Box 내부의 모든 자식들이 정중앙에 배치됩니다.
        contentAlignment = Alignment.Center,
        // Box가 화면 전체를 차지하도록 하여, 중앙 정렬의 기준 영역을 화면 전체로 만듭니다.
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 전체를 Column으로 감싸서 수직으로 배치합니다.
        Column(
            modifier = Modifier.fillMaxWidth(), // 가로를 꽉 채워야 오른쪽 정렬이 의미가 있습니다.
            horizontalAlignment = Alignment.CenterHorizontally // 자식들을 기본적으로 수평 중앙 정렬
        ) {
            // 첫 번째 요소: 말풍선 애니메이션 (기존 Row)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(heartSize.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.speech_bubble),
                        contentDescription = "분석 중 말풍선",
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "쿠무가 {닉네임}의\n하루를 요약중이에요\n",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // 2. GIF 디코더를 포함하는 ImageLoader 생성
            val imageLoader = ImageLoader.Builder(LocalContext.current)
                .components {
                    add(ImageDecoderDecoder.Factory())
                }
                .build()

            // 3. 두 번째 요소: 쿠무 캐릭터 GIF (말풍선 아래, 오른쪽에 배치)
            AsyncImage(
                model = R.drawable.kumu,
                contentDescription = "쿠무 캐릭터 애니메이션",
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(120.dp) // 고정된 크기
                    .align(Alignment.End) // <<-- Column 내에서 오른쪽 끝으로 정렬!
                    .padding(end = 16.dp) // 화면 오른쪽 끝에서 약간의 여백 주기
            )
        }
    }
}