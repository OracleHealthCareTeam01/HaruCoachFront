package com.harucoach.harucoachfront.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CognitiveTestScreen(navController: NavHostController) {
    // 뒤로가기 버튼 비활성화
    BackHandler(enabled = true) { /* 뒤로가기 버튼을 눌러도 아무 동작도 하지 않습니다. */ }

    //사용설명서 다이얼로그 화면
    var showDialog2 by remember { mutableStateOf(true) }
    if (showDialog2) {
        CustomFullAlertDialog(
            onDismissRequest = {
                // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 누를 때
                showDialog2 = false
            }
        )
    }
    //검사 그만하기 다이얼로그
    var showDialog by remember { mutableStateOf(false) }

    //검사시간
    val remainingTime = remember { mutableIntStateOf(30) }
    //음성으로 입력받아 저장할 공간
    val recognizedText = remember { mutableStateOf("") }
    // 버튼 구분 코드 1 = 말하기, 2 = 대기, 3 = 종료
    var btnState by remember { mutableIntStateOf(1) }

    var time by remember { mutableIntStateOf(1) }
    var numBer by remember { mutableIntStateOf(1) }
    val density = LocalDensity.current
    val fontSizeSp = with(density) { 20.dp.toSp() } // 👈 dp → sp 변환
    val fontSizeSp2 = with(density) { 30.dp.toSp() } // 👈 dp → sp 변환


    // "다음" 버튼 로직
    val onNextClicked = {
        if (numBer != 10){
            btnState = 1
            numBer += 1
            remainingTime.intValue = 30
            // 여기에 다음 질문으로 넘어가는 로직 추가
            time = 1
        }
        else{
            //홈화면 이동
            navController.navigate("home") {
                // 백 스택에서 cognitiveTest 화면을 제거하여 뒤로 가기 버튼을 눌렀을 때 다시 돌아오지 않도록 합니다.
                popUpTo("cognitiveTest") {
                    inclusive = true
                }
            }
        }
    }

    // 타이머 로직
    LaunchedEffect(time) {
        if (time == 2) {
            while (remainingTime.intValue > 0) {
                delay(1000)
                remainingTime.intValue--
            }
            if (remainingTime.intValue == 0) {
                onNextClicked()
            }
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                ,
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                // 1. Column을 추가하고 여기에 패딩을 적용합니다.
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp) // 좌우 패딩 16dp 적용
                        .fillMaxSize(), // Column이 Card 영역 전체를 차지하도록 설정
                    verticalArrangement = Arrangement.Center // (선택사항) 내용을 수직 중앙에 배치
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // 질문 번호
                    Row {

                        Text(
                            text = "질문 ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = numBer.toString(),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = " / 10",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    // 질문 문장
                    Text(
                        text = "올해가 몇년도 인가요?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,

                        )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // 2. 상태에 따라 버튼의 onClick 로직을 분리
            val onClickAction = {
                when (btnState) {
                    1 -> { // 현재 상태가 '말하기'이면
                        // 다음 상태로 변경 (예: 대기)
                        btnState = 2
                        // 여기에 녹음 시작 등의 로직 추가
                        time = 2;
                    }
                    2 -> { // 현재 상태가 '대기'이면
                        // 다음 상태로 변경 (예: 종료)
                        btnState = 3
                        // 여기에 녹음 종료 로직 추가
                        recognizedText.value = "음성인식 완료"
                    }
                    3 -> { // 현재 상태가 '종료'이면
                        //아무동작 안하기
                        btnState = 3
                        // 여기에 초기화 로직 추가
                    }
                }
            }

            // `btnState`에 따라 버튼 색상을 동적으로 변경합니다.
            val buttonColor = {
                when (btnState)
                {
                    1 -> Color(0xFF00C853) // 초록색
                    2 -> Color(0xFFFFC107) // 노란색
                    else -> Color(0xFFF44336) // 빨간색
                }
            }

            // 마이크 버튼
            Button(
                onClick = { onClickAction() }, // onClickAction() 함수 호출
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor()), // 이제 buttonColor는 Color 값입니다.
                shape = CircleShape,
                modifier = Modifier.size(150.dp)

            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (btnState) {
                        1 -> { // 말하기 상태
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "말하기",
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "말하기",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = fontSizeSp2
                            )
                        }
                        2 -> { // 대기 상태
                            Icon(
                                imageVector = Icons.Default.MoreHoriz, // ... 아이콘
                                contentDescription = "대기",
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "대기",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = fontSizeSp2
                            )
                        }
                        3 -> { // 종료 상태 (예시)
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "종료",
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "종료",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = fontSizeSp2
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(10.dp))
            // 음성 인식 결과
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp) // 좌우 패딩 16dp 적용
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "인식된 음성",
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = recognizedText.value,
                        onValueChange = { recognizedText.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = false,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        singleLine = false,
                        placeholder={
                            Text("음성 인식이 여기에 표시됩니다.")
                        })

                }
            }

            // 하단 버튼들
            if (btnState != 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (btnState != 1) {
                        Button(
                            onClick = {

                                btnState = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            modifier = Modifier
                                .height(56.dp) // 버튼 높이
                                .width(140.dp), // 버튼 너비
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "재녹음",
                                color = Color.White,
                                fontSize = fontSizeSp

                            )
                        }
                    }else {
                        // 버튼 공간 유지, 투명하게 만들기
                        Box(
                            modifier = Modifier
                                .height(56.dp) // 버튼 높이
                                .width(140.dp) // 버튼 너비
                                .alpha(0f) // 👈 완전히 투명하지만 공간 유지
                        )
                    }
                    Button(
                        onClick = { onNextClicked() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier
                            .height(56.dp) // 버튼 높이
                            .width(140.dp), // 버튼 너비
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "다음",
                            color = Color.White,
                            fontSize = fontSizeSp
                        )
                    }
                }
            }


        }
        Column(
            modifier = Modifier
                .fillMaxSize()
              ,
        ){
            Spacer(modifier = Modifier.weight(1f)) // This pushes content to the top
            Row(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF00C853)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "말하기",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        "남은 시간: ",
                        color = Color.White,
                        fontSize = fontSizeSp
                    )

                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", remainingTime.intValue / 60, remainingTime.intValue % 60),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSizeSp
                    )
                }
                Button(
                    onClick = {
                        showDialog = true
                        /* 검사 종료 로직 */
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                    modifier = Modifier
                        .height(56.dp) // 버튼 높이
                        .width(170.dp), // 버튼 너비
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "검사 그만하기",
                        color = Color.White,
                        fontSize = fontSizeSp
                    )
                }
                if (showDialog) {
                    CustomAlertDialog(
                        onDismissRequest = {
                            // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 누를 때
                        },
                        onContinueClick = {
                            // '계속하기' 버튼 클릭 시
                            showDialog = false
                            // 여기에 검사 계속 로직 추가
                        },
                        onStopClick = {
                            // '검사 그만하기' 버튼 클릭 시
                            // 여기에 검사 종료 로직 추가
                            //home.kt로 이동
                            showDialog = false
                            navController.navigate("home") {
                                // 백 스택에서 cognitiveTest 화면을 제거하여 뒤로 가기 버튼을 눌렀을 때 다시 돌아오지 않도록 합니다.
                                popUpTo("cognitiveTest") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }

}
