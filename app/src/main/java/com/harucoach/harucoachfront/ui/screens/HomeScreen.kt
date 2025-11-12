package com.harucoach.harucoachfront.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harucoach.harucoachfront.R
import com.harucoach.harucoachfront.ui.theme.HaruGreen
import com.harucoach.harucoachfront.ui.theme.HaruGreenLight

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    BackHandler(enabled = true) { /* 뒤로가기 버튼을 눌러도 아무 동작도 하지 않습니다. */ }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 상단 인사 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "안녕하세요, {닉네임}님!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "매일 꾸준히 기록하며 건강을 관리하고 계시네요!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(HaruGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👣", fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("오늘의 걸음수", fontSize = 13.sp, color = Color.Gray)
                            Text("2,852", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(HaruGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    1
                    Image(
                        painter = painterResource(id = R.drawable.normal_feelings),
                        contentDescription = "평벙함",
                        modifier = Modifier.size(48.dp) // 이미지 크기 조절
                    )
                }
            }
        }

        // 🔹 여기서 아래 두 함수(LargeFeatureCard, SmallFeatureCard)를 호출함
        LargeFeatureCard(
            title = "인지 능력 검사",
            icon = Icons.Default.Psychology,
            onClick = { onNavigate(Routes.COGNITIVE) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmallFeatureCard(
                title = "오늘의 일기",
                icon = Icons.Default.Edit,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Routes.DIARY) }
            )
            SmallFeatureCard(
                title = "오늘의 학습",
                icon = Icons.Default.Book,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Routes.LEARN) }
            )
        }
    }
}

@Composable
private fun LargeFeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HaruGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SmallFeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = HaruGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}
