package com.harucoach.harucoachfront.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import java.util.Locale


@Composable
fun CognitiveTestScreen(navController: NavHostController) {
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

    // 현재 Compose 컨텍스트에서 Context 객체를 가져옴
    val context = LocalContext.current
    // 뒤로가기 버튼 비활성화
    // Compose 상태 변수들 정의
    // `remember`와 `mutableStateOf`를 사용하여 상태가 변경될 때 UI가 자동으로 업데이트되도록 함
    var recordedText by remember { mutableStateOf("") } // 녹음된 텍스트를 저장

    var errorMessage by remember { mutableStateOf("") } // 오류 메시지를 저장
    var isListening by remember { mutableStateOf(false) } // 음성 인식기 작동 여부

    // SpeechRecognizer 인스턴스 생성 및 기억
    // 컴포저블이 리컴포즈되어도 동일한 인스턴스를 유지
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    // 음성 인식 인텐트 설정 및 기억
    // 음성 인식 서비스에 전달할 추가 정보들을 정의
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 음성 인식 서비스를 호출하는 패키지 이름을 지정
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            // 자유 형식 음성 인식을 위한 언어 모델 설정
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // 기기의 기본 언어로 음성 인식 설정 (한국어 등)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            // 부분 인식 결과를 수신할지 여부 설정 (실시간 텍스트 업데이트에 사용)
            //putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // 음성 입력이 완료되었다고 판단하기 위한 최대 무음 시간 (3초)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
            // 음성 입력이 아마도 완료되었을 수 있다고 판단하기 위한 최대 무음 시간 (3초)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
            // 음성 인식기가 최소한 유지되어야 하는 시간 (10초)
            // 이 시간 동안 음성이 없으면 타임아웃 오류 발생 가능
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);
            // 언어 선호도만 반환할지 여부 (여기서는 true로 설정되어 있지만, 일반적으로 음성 인식을 위해서는 false)
            // 이 옵션이 true이면 실제 음성 인식은 수행되지 않고 언어 설정만 반환될 수 있음. 주의 필요.
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
        }
    }

    // TextToSpeech 인스턴스
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // DisposableEffect를 사용하여 SpeechRecognizer 및 TextToSpeech의 생명주기를 관리
    DisposableEffect(Unit) {
        // SpeechRecognizer 리스너 설정
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                errorMessage = "" // 오류 메시지 초기화
                isListening = true // 녹음 중 상태로 변경
                Toast.makeText(context, "녹음 시작...", Toast.LENGTH_SHORT).show() // 녹음 시작 토스트 메시지
            }
            override fun onBeginningOfSpeech() { /*...*/ }
            override fun onRmsChanged(rmsdB: Float) { /*...*/ }
            override fun onBufferReceived(buffer: ByteArray?) { /*...*/ }
            override fun onEndOfSpeech() { /*...*/ }
            override fun onError(error: Int) {
                isListening = false // 오류 발생 시 녹음 중 상태 해제
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 오류"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                    SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기 사용 중"
                    SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "알 수 없는 오류: $error"
                }
                errorMessage = "오류: $errorMsg" // 오류 메시지 업데이트
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() // 오류 메시지 토스트
            }
            //완료 리턴 결과 값
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {

                    recordedText = matches[0]
                    Log.d("recordedText 테스트0", recordedText)
                    btnState = 3
                }
            }
            //부분 리턴값
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {

                    Log.d("recordedText 테스트1", recordedText)
                    recordedText = matches[0] // 첫 번째 부분 인식 결과를 recordedText에 표시
                    Log.d("recordedText 테스트2", recordedText)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) { /*...*/ }
        }
        speechRecognizer.setRecognitionListener(listener)

        // TextToSpeech 초기화
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        // 컴포저블이 화면에서 제거될 때 호출되는 클린업 람다
        onDispose {
            speechRecognizer.destroy() // SpeechRecognizer 리소스 해제
            tts?.stop()
            tts?.shutdown()
        }
    }


    BackHandler(enabled = true) { /* 뒤로가기 버튼을 눌러도 아무 동작도 하지 않습니다. */ }

    //사용설명서 다이얼로그 화면
    var showDialog2 by remember { mutableStateOf(true) }
    if (showDialog2) {
        CustomFullAlertDialog(
            onDismissRequest = {
                // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 누를 때
                showDialog2 = false
                tts?.speak("올해가 몇년도 인가요?", TextToSpeech.QUEUE_FLUSH, null, "dialogDismiss")
            }
        )
    }



    // "다음" 버튼 로직
    val onNextClicked = {
        if (numBer != 10){
            btnState = 1
            numBer += 1
            remainingTime.intValue = 30
            // 여기에 다음 질문으로 넘어가는 로직 추가
            time = 1
            recordedText ="";
            tts?.speak("올해가 몇년도 인가요?", TextToSpeech.QUEUE_FLUSH, null, "dialogDismiss")

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
                        time = 2;

                        // 여기에 녹음 시작 등의 로직 추가
                        recordedText = "" // 녹음 시작 시 안내 메시지 표시
                        errorMessage = "" // 오류 메시지 초기화
                        speechRecognizer.startListening(speechRecognizerIntent) // 음성 인식 시작

                    }
                    2 -> { // 현재 상태가 '대기'이면
                        // 다음 상태로 변경 (예: 종료)
                        btnState = 3
                        //recognizedText.value = "음성인식 완료"

                        // 여기에 녹음 종료 로직 추가
                        speechRecognizer.stopListening()
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

            // 하단 버튼들
            if (btnState != 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (btnState != 1) {
                        Button(
                            onClick = {
                                recordedText = "" // 녹음 시작 시 안내 메시지 표시
                                errorMessage = "" // 오류 메시지 초기화
                                speechRecognizer.startListening(speechRecognizerIntent) // 음성 인식 시작
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
