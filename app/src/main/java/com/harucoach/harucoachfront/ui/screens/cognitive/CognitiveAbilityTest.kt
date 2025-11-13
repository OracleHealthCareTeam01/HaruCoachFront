package com.harucoach.harucoachfront.ui.screens.cognitive

import android.util.Log
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.harucoach.harucoachfront.data.models.AnswerItem
import com.harucoach.harucoachfront.viewmodel.CognitiveViewModel
import com.harucoach.harucoachfront.viewmodel.VoiceRecognitionViewModel
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import kotlin.math.log

@Composable
fun CognitiveTestScreen(
    navController: NavHostController,
    cogViewModel: CognitiveViewModel = hiltViewModel(),
    viewModel: VoiceRecognitionViewModel = viewModel()
) {

    var recordedText by viewModel.recordedText //음성 인식된 값
    val btnState by viewModel.btnState //버튼 상태 1:말하기 2:대기 3:종료
    val remainingTime = remember { mutableIntStateOf(30) } //타이머 시간
    val density = LocalDensity.current
    val fontSizeSp = with(density) { 20.dp.toSp() }
    val fontSizeSp2 = with(density) { 30.dp.toSp() }
    var numBer by remember { mutableIntStateOf(1) } //질문 번호
    var test by remember { mutableStateOf("") } //질문


    val loginResult by cogViewModel.uiState.collectAsState()
    val sessionId = loginResult.sessionId
    val questions  = loginResult.questions

    var showDialog by remember { mutableStateOf(false) }//검사 그만하기 다이얼로그
    var showDialog2 by remember { mutableStateOf(true) }//화면 시작 다이얼로그

    val sttList: MutableList<String> = MutableList(10) { "" };
    val latencyMs: MutableList<Int> = MutableList(10){0};
    //다음 버튼 클릭 이벤트
    // TODO cogViewModel.submitAnswers
    val onNextClicked = {
        if (numBer < 10) {

            sttList[numBer] = recordedText;
            latencyMs[numBer] = remainingTime.intValue;
            numBer++
            remainingTime.intValue = 30
            viewModel.btnState.value = 1
            viewModel.recordedText.value = ""

            val q = questions[numBer-1]
            viewModel.speak(" ${q.text}")
            test = " ${q.text}"

        } else {

            cogViewModel.submit(sttList,latencyMs) // 여기에서 submit() 함수 호출
            navController.navigate("cognitive_waiting") {
                popUpTo("cognitiveTest") { inclusive = true }
            }
        }
    }

    // 타이머
    LaunchedEffect(btnState) {
        if (btnState == 2) {
            while (remainingTime.intValue > 0) {
                delay(1000)
                remainingTime.intValue--
            }
            if (remainingTime.intValue == 0) {
                onNextClicked()
            }
        }
    }

    BackHandler(enabled = true) {}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // 질문
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {

                Text("질문 $numBer / 10", color = Color.Gray)
                Text(
                    text = test,
                    //style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 마이크 버튼
        Button(
            onClick = {
                when (btnState) {
                    1 -> {
                        viewModel.btnState.value = 2
                        viewModel.startListening()
                    }
                    2 -> {
                        viewModel.stopListening()
                        viewModel.btnState.value = 3
                    }
                    3 -> {} // 종료
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = when (btnState) {
                    1 -> Color(0xFF00C853)
                    2 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
            ),
            shape = CircleShape,
            modifier = Modifier.size(150.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (btnState) {
                    1 -> Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
                    2 -> Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
                    3 -> Icon(Icons.Default.StopCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
                }
                Text(
                    when (btnState) {
                        1 -> "말하기"
                        2 -> "대기"
                        else -> "종료"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp2
                )
            }
        }

        // 인식된 결과 표시
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("인식된 음성", color = Color.Black)
                TextField(
                    value = recordedText,
                    onValueChange = { recordedText = it },
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


        if (btnState != 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (btnState != 1) {
                    Button(
                        onClick = {
                            viewModel.btnState.value = 2
                            viewModel.startListening()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier
                            .height(56.dp)
                            .width(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("재녹음", color = Color.White, fontSize = fontSizeSp)
                    }
                } else {
                    Box(
                        Modifier
                            .height(56.dp)
                            .width(140.dp)
                            .alpha(0f)
                    )
                }

                // TODO cogViewModel.updateAnswer()
                Button(
                    onClick = { onNextClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    modifier = Modifier
                        .height(56.dp)
                        .width(140.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("다음", color = Color.White, fontSize = fontSizeSp)
                }
            }
        }
    }

    // 하단 남은시간바
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .height(70.dp)
                .fillMaxWidth()
                .background(Color(0xFF00C853)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "남은시간",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Text("남은 시간: ", color = Color.White, fontSize = fontSizeSp)
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", remainingTime.intValue / 60, remainingTime.intValue % 60),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp
                )
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                modifier = Modifier
                    .height(56.dp)
                    .width(170.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("검사 그만하기", color = Color.White, fontSize = fontSizeSp)
            }
        }//row 끝
    }//하단바

    //검사 그만하기 다이얼로그
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
    //앱 실행시 나오는 다이얼 로그
    if (showDialog2) {
        CustomFullAlertDialog(
            onDismissRequest = {
                showDialog2 = false
                val q = questions[numBer-1]
                viewModel.speak(" ${q.text}")
                test = " ${q.text}"
            }
        )
    }
}
