package com.harucoach.harucoachfront.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.harucoach.harucoachfront.ui.componenets.CalendarMonthView
import com.harucoach.harucoachfront.ui.componenets.MoodSelectDialog
import com.harucoach.harucoachfront.viewmodel.DiaryUiState
import com.harucoach.harucoachfront.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DiaryScreen: 한 줄씩 아주 쉬운 설명 주석이 달린 Compose 화면
 *
 * 이 화면은 다음을 보여줍니다:
 *  - 달력(월 단위 스와이프 가능)
 *  - 선택한 날짜(크게)와 감정 칩
 *  - 일기 입력창과 마이크 버튼
 *  - 취소 / 저장 버튼
 *
 * 모든 기능은 주석으로 매우 자세히 설명되어 있습니다.
 */

/* -------------------- 화면 진입부: 함수 선언 -------------------- */
// 이 함수가 바로 '일기 화면' 전체를 그려주는 역할을 합니다.
@OptIn(ExperimentalMaterial3Api::class) // 실험적 API를 사용할 때 붙이는 표시입니다.
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(), // 데이터와 동작을 관리하는 친구(뷰모델)를 받아옵니다.
    onCancel: () -> Unit = {}, // 취소 버튼을 눌렀을 때 호출될 행동(외부에서 정해줄 수 있음)
    onSave: () -> Unit = {} // 저장 버튼을 눌렀을 때 호출될 행동(외부에서 정해줄 수 있음)
) {
    // UI가 보여줄 값들을 ViewModel에서 가져옵니다.
    // collectAsState()는 '실시간으로 값이 바뀌면 UI도 따라 바뀌게' 해주는 도구예요.
    val uiState by viewModel.uiState.collectAsState()            // 화면의 상태 (로딩, 저장중 등)
    val selectedDate by viewModel.selectedDate.collectAsState()  // 사용자가 선택한 날짜
    val text by viewModel.currentText.collectAsState()           // 일기 텍스트 내용
    val mood by viewModel.currentMood.collectAsState()           // 현재 선택된 감정(이모지)
    // end state reads

    // 코루틴 스코프를 가져옵니다. 버튼을 누르면 이 안에서 애니메이션 같은 작업을 할 거예요.
    val coroutineScope = rememberCoroutineScope() // 버튼 클릭 등에서 비동기 작업을 안전하게 실행합니다.
    // end coroutineScope

    // 선택된 날짜를 기준으로 '가운데 달'을 정합니다.
    val centerYearMonth = YearMonth.from(selectedDate) // 예: 2025-11 같은 정보
    // end centerYearMonth

    // 보여줄 달의 범위를 만듭니다. 여기선 -24달 ~ +24달, 즉 49달을 보여줍니다.
    // 리스트의 가운데(index 24)가 현재 달이 되도록 합니다.
    val range = remember { (-24..24).map { centerYearMonth.plusMonths(it.toLong()) } }
    val initialPage = 24 // range에서 중앙 페이지 번호
    // end range setup

    // 페이저(가로로 넘기는 뷰)의 상태를 만듭니다.
    val pagerState = rememberPagerState(initialPage = initialPage)
    // end pagerState

    // 페이저의 현재 페이지가 바뀌면(사용자가 스와이프하면) 이 블록이 실행됩니다.
    LaunchedEffect(pagerState.currentPage) {
        // 현재 보고 있는 달을 계산합니다.
        val ym = range.getOrNull(pagerState.currentPage) ?: centerYearMonth
        // 여기서 'ym'을 이용해 서버에서 그 달의 데이터를 가져오도록 연결할 수 있어요.
    } // end LaunchedEffect(pagerState.currentPage)

    // ViewModel에서 가져온 메모리 저장된 이모지 맵을 읽어옵니다.
    // moodMap의 키는 LocalDate, 값은 그 날짜의 이모지예요.
    val moodMap by viewModel.moodMap.collectAsState()
    // end moodMap

    /* -------------------- 화면 본문: 세로로 쭉 쌓이는 레이아웃 -------------------- */
    Column(
        modifier = Modifier
            .fillMaxSize() // 화면 전체를 채움
            .verticalScroll(rememberScrollState()) // 화면을 위아래로 스크롤할 수 있게 함
            .padding(16.dp) // 화면 가장자리 여백
    ) { // start Column

        /* -------------------- B. 달력 카드 (월간 페이저 포함) -------------------- */
        // Card: 모서리 둥글고 배경색이 있는 상자
        Card(
            modifier = Modifier.fillMaxWidth(), // 가로 전체를 채움
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), // 모서리 둥글게
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FB)), // 배경색: 연하늘
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // 그림자
        ) { // start Card
            Column(modifier = Modifier.padding(12.dp)) { // start Column inside Card

                // 헤더: 왼쪽 이전 버튼, 가운데 현재 년월, 오른쪽 다음 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) { // start Row
                    // 이전 달로 가는 버튼 (왼쪽 화살표)
                    IconButton(onClick = {
                        // 눌렀을 때 현재 페이지가 0보다 크면 한 페이지(한 달) 뒤로 이동
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                // animateScrollToPage는 페이지를 부드럽게 넘깁니다.
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }) { // start IconButton prev
                        // 실제로 화면에 보이는 아이콘
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "이전 달",
                            tint = Color.Gray
                        )
                    } // end IconButton prev

                    Spacer(modifier = Modifier.weight(1f)) // 가운데 정렬을 위한 빈공간

                    // 가운데 텍스트: 현재 페이지의 YearMonth (예: November 2025)
                    val currentYM = range.getOrNull(pagerState.currentPage) ?: centerYearMonth
                    Log.d("currentYM", currentYM.toString())
                    Text(
                        //text = "${currentYM.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentYM.year}",
                        text = "${currentYM.year}년 ${
                            currentYM.format(
                                DateTimeFormatter.ofPattern("MMMM").withLocale(Locale.KOREAN)
                            )
                        }",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111111)
                    )

                    Spacer(modifier = Modifier.weight(1f)) // 가운데 정렬을 위한 빈공간

                    // 다음 달로 가는 버튼 (오른쪽 화살표)
                    IconButton(onClick = {
                        // 눌렀을 때 마지막 페이지보다 작으면 한 페이지 앞으로 이동
                        if (pagerState.currentPage < range.lastIndex) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }) { // start IconButton next
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "다음 달",
                            tint = Color.Gray
                        )
                    } // end IconButton next
                } // end Row (헤더)

                Spacer(modifier = Modifier.height(8.dp)) // 헤더와 달력 사이 간격

                // HorizontalPager: 가로로 넘기는 뷰입니다. 각 페이지는 하나의 달을 표시합니다.
                HorizontalPager(
                    count = range.size, // 페이지 수: 49개
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp) // 높이 최소값을 지정
                ) { pageIndex -> // start HorizontalPager page lambda
                    // 이 블록은 페이지마다 실행됩니다. pageIndex는 페이지 번호(0..48)
                    val ym = range[pageIndex] // 이 페이지가 나타내는 YearMonth
                    // 만약 현재 선택된 날짜가 이 페이지의 달이면 그 날짜를 선택으로 보여주고,
                    // 아니면 그 달의 1일을 선택된 것으로 보여줍니다.
                    val selectedForPage =
                        if (YearMonth.from(selectedDate) == ym) selectedDate else ym.atDay(1)

                    // CalendarMonthView: 한 달치 달력을 그리는 함수(아래에 정의됨)
                    CalendarMonthView(
                        month = ym,
                        selected = selectedForPage,
                        onSelectDate = { date ->
                            // 사용자가 달력의 날짜를 누르면 ViewModel에 선택 날짜를 알려줍니다.
                            viewModel.selectDate(date)
                        },
                        today = LocalDate.now(),
                        moodForDay = { d -> moodMap[d] } // 날짜별로 표시할 이모지를 제공
                    )
                } // end HorizontalPager page lambda

                // 페이지 위치를 표시해 주는 작은 점(인디케이터)
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    activeColor = MaterialTheme.colorScheme.primary
                )
            } // end Column inside Card
        } // end Card (달력 카드)

        Spacer(modifier = Modifier.height(16.dp)) // 달력과 날짜 영역 사이 간격

        /* -------------------- C. 날짜 + 감정칩 영역 -------------------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) { // start Row date+mood
            // 왼쪽: 선택된 날짜를 크게 보여줍니다.
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")), // 예: 2025.11.08
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f) // 가능한 공간을 꽉 채움
            )

            // 오른쪽: 감정(이모지) 선택 칩
            var showMoodDialog by remember { mutableStateOf(false) } // 다이얼로그 보일지 말지 상태
            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) // 모서리를 둥글게
                    .background(Color(0xFFF5F5F5)) // 연한 회색 배경
                    .clickable { showMoodDialog = true } // 누르면 다이얼로그를 켬
                    .padding(horizontal = 14.dp, vertical = 10.dp) // 내부 여백
            ) {
                Text(text = "$mood", color = Color(0xFF222222)) // 현재 감정과 간단한 텍스트
            }

            // 감정 선택 다이얼로그
            if (showMoodDialog) {
                MoodSelectDialog(
                    current = mood,
                    onDismiss = { showMoodDialog = false },
                    onSelect = { m ->
                        // 사용자가 감정을 선택하면 ViewModel에 알려줍니다.
                        viewModel.updateMood(m)
                        showMoodDialog = false
                    }
                )
            }
        } // end Row date+mood

        Spacer(modifier = Modifier.height(12.dp)) // 날짜영역과 입력카드 사이 간격

        /* -------------------- D. 일기 입력 카드 + 마이크 -------------------- */
        Card(
            modifier = Modifier.fillMaxWidth(), // 가로 전체
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), // 모서리 둥글게
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 그림자
        ) { // start Card diary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // 높이 고정
                    .padding(12.dp) // 내부 여백
            ) { // start Box
                // 사용자가 글을 쓰는 칸 (TextField)
                TextField(
                    value = text, // 지금 화면에 보이는 글
                    onValueChange = { viewModel.updateText(it) }, // 글이 바뀌면 ViewModel에 알려줌
                    placeholder = { Text("오늘의 일기를 써보세요") }, // 아무 글도 없을 때 보여주는 회색 안내문
                    modifier = Modifier.fillMaxSize(), // 박스 전체를 채움
                    colors = TextFieldDefaults.colors() // 색상 기본값 사용
                )

                // 오른쪽 아래의 둥근 마이크 버튼 (FloatingActionButton 모양)
                FloatingActionButton(
                    onClick = { /* TODO: 음성 인식 기능 연결 */ }, // 나중에 음성 입력을 연결할 자리
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // 박스의 오른쪽 아래에 붙임
                        .size(56.dp), // 크기
                    containerColor = Color(0xFF2E7D32) // 버튼 배경색(녹색)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "mic",
                        tint = Color.White
                    ) // 마이크 아이콘
                }
            } // end Box
        } // end Card diary

        Spacer(modifier = Modifier.height(16.dp)) // 입력카드와 버튼 사이 여백

        /* -------------------- E. 버튼 영역 (취소 / 저장) -------------------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) { // start Row buttons
            // 왼쪽: 취소 버튼 (테두리형)
            OutlinedButton(onClick = { onCancel() }, modifier = Modifier.weight(1f)) {
                Text("취소")
            }

            Spacer(modifier = Modifier.width(12.dp)) // 버튼 사이 간격

            // TODO 저장 수정 로직 나누어지게되면 버튼 따로 분리 로직 구현
            // 오른쪽: 저장 버튼 (채워진 녹색)
            Button(
                onClick = {
                    viewModel.saveDiary() // 저장 로직을 실행 (ViewModel에서 처리)
                    onSave() // 외부 콜백(예: 화면 닫기) 실행
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("저장", color = Color.White)
            }
        } // end Row buttons

        Spacer(modifier = Modifier.height(8.dp)) // 버튼 밑 상태 표시 여유

        /* -------------------- UI 상태 표시 (저장중, 저장완료, 오류) -------------------- */
        when (uiState) { // 화면 상태에 따라 다른 메시지를 보여줍니다.
            DiaryUiState.Saving -> {
                // 저장하는 중일 때: 진행바(로딩바)를 보여줍니다.
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            DiaryUiState.Saved -> {
                // 저장이 끝났을 때: "저장되었습니다" 메시지를 보여줍니다.
                Text("저장되었습니다", color = MaterialTheme.colorScheme.primary)
            }

            is DiaryUiState.Error -> {
                // 오류가 생겼을 때: 오류 메시지를 빨간색으로 보여줍니다.
                Text(
                    "오류: ${(uiState as DiaryUiState.Error).message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                // 그 밖의 상태(Idle 등)은 아무 것도 안 함
            }
        } // end when uiState

    } // end Column (전체 화면)
} // end DiaryScreen


@Preview
@Composable
fun calendar_preview() {
    DiaryScreen()
}
