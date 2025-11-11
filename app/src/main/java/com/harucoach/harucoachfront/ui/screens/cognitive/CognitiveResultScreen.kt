package com.harucoach.harucoachfront.ui.screens.cognitive

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.harucoach.harucoachfront.viewmodel.CognitiveViewModel


@Composable
fun CognitiveResultScreen(
    viewModel: CognitiveViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 2. Grade at the top with dynamic color
        val gradeColor = when ("정상") {
            "정상" -> Color.Green
            "주의" -> Color.Yellow
            "위험" -> Color.Red
            else -> Color.Gray
        }
        Text(
            text = "{닉네임}은 ${"정상"} 등급입니다.",
            style = MaterialTheme.typography.headlineMedium,
            color = gradeColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Recent test results line chart
        Text(text = "최근 인지 능력 테스트 결과와 지난 3개월 인지 능력 점수 변화")
        CustomLineChart(mutableListOf() )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Category scores radar chart
        Text(text = "전반적인 인지 능력 영역별 인지 능력 요약")
        CustomRadarChart(mutableListOf(), mutableListOf() )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Summary text at bottom
        Text(text = "6. 한줄 요약(추후 논의)")
        Text(
            text = "정기적인 인지 능력 테스트를 통해 꾸준히 뇌 건강을 관리하는 것이 중요합니다.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}



@Composable
fun CustomLineChart(points: List<Point>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val xAxisSpace = size.width / (points.size - 1)
        val yScale = size.height / 100f  // Assume 0-100 scale
        val path = Path()

        path.moveTo(0f, size.height - points[0].y * yScale)
        points.forEachIndexed { index, point ->
            val x = index * xAxisSpace
            val y = size.height - point.y * yScale
            path.lineTo(x, y)
            drawCircle(Color.Green, 8f, Offset(x, y))
        }
        drawPath(path, Color.Green, style = Stroke(width = 4f))
    }
}// end CustomLinearChart

@Composable
fun CustomRadarChart(values: List<Double>, labels: List<String>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        val angleStep = 360f / 5  // For 5 categories
        val path = Path()
        values.forEachIndexed { index, value ->
            val angle = index * angleStep
            val x: Float = (center.x + (radius * (value / 100.0) * kotlin.math.cos(Math.toRadians(angle.toDouble()).toFloat()))).toFloat()
            val y: Float = (center.y + (radius * (value / 100.0) * kotlin.math.sin(Math.toRadians(angle.toDouble()).toFloat()))).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, brush = Brush.radialGradient(listOf(Color.Blue.copy(0.5f), Color.Transparent)))
        // Add labels and lines similarly
    }
}// end CustomRadarChart


@Preview
@Composable
fun prev() {
    CognitiveResultScreen()
}