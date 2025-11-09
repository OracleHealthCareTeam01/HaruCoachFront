package com.harucoach.harucoachfront.ui.componenets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MoodSelectDialog:
 * - 현재 감정(current)을 보여주고,
 * - 사용자가 고르면 onSelect 감정이 호출됩니다.
 * - 닫기 버튼을 누르면 onDismiss가 호출됩니다.
 */
@Composable
fun MoodSelectDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    // start MoodSelectDialog
    // 감정 목록을 만듭니다.
    val moods = listOf(
        "🙂 행복함",
        "🙂 보통",
        "😢 우울함",
        "😠 화남",
        "😌 차분함"
    )
    // AlertDialog는 가운데 뜨는 작은 창입니다.
    AlertDialog(
        onDismissRequest = onDismiss, // 바깥을 눌러도 닫힐 수 있게 함
        confirmButton = { // 닫기 버튼: 누르면 onDismiss 호출
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
        title = { Text("오늘의 기분") }, // 다이얼로그 제목
        text = {
            Column {
                // 감정들을 가로로 3개씩 묶어서 보여줍니다.
                moods.chunked(1).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { m ->
                            Text(
                                text = m,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable { onSelect(m) } // 누르면 onSelect 호출
                                    .padding(10.dp)
                            )
                        }
                    }
                }// end moods
            }// end Column
        }// end text
    )// end AlertDialog
} // end MoodSelectDialog


@Preview
@Composable
fun previewMoodSelectModal() {
    MoodSelectDialog(current = "🙂 행복함", onDismiss = {}, onSelect = {})
}