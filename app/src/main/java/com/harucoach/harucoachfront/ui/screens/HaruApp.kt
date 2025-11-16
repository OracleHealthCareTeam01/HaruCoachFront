package com.harucoach.harucoachfront.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.ui.screens.cognitive.CognitiveIntroScreen
import com.harucoach.harucoachfront.ui.screens.cognitive.CognitiveTestScreen
import com.harucoach.harucoachfront.ui.screens.cognitive.CognitiveWaitingScreen

object Routes {
    const val HOME = "home"
    const val COGNITIVE = "cognitive"
    const val COGNITIVE_TEST = "cognitive_test"
    const val COGNITIVE_WAITING = "cognitive_waiting"
    const val DIARY = "diary"
    const val LEARN = "learn"
    const val MY = "my"

    const val DAY_SUMMARY = "day_summary"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaruApp() {
    val nav = rememberNavController()
    val currentRoute = nav.currentRoute()
    
    //타이틀 명지정
    val topTitle = when (currentRoute) {
        Routes.HOME -> "홈"
        Routes.COGNITIVE -> "인지 능력 검사"
        Routes.COGNITIVE_TEST -> "인지 능력 검사"
        Routes.COGNITIVE_WAITING -> "검사 결과 대기"
        Routes.DIARY -> "오늘의 일기"
        Routes.LEARN -> "오늘의 학습"
        Routes.MY -> "내 정보"
        Routes.DAY_SUMMARY -> "하루요약"
        else -> ""
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(topTitle) }) },
        bottomBar = {
            HaruBottomBar(
                currentRoute = currentRoute,
                onSelect = { route ->
                    nav.navigate(route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            //하단 홈 버튼
            composable(Routes.HOME) { HomeScreen(onNavigate = { nav.navigate(it) }) }
            //홈화면 인지능력검사
            composable(Routes.COGNITIVE) { CognitiveIntroScreen(onStart = { nav.navigate(Routes.COGNITIVE_TEST) }) }
            //인지능력 검사 화면
            composable(Routes.COGNITIVE_TEST) { CognitiveTestScreen(nav) }
            // 인지능력 검사
            composable(Routes.COGNITIVE_WAITING) { CognitiveWaitingScreen() }
            //오늘의 일기 
            composable(Routes.DIARY) {
                //SimplePage("오늘의 일기")
                DiaryScreen(nav,onCancel = { nav.popBackStack() })
            }
            //오늘의 학습
            composable(Routes.LEARN) {
                //SimplePage("오늘의 학습")
            }
            //마이페이지
            composable(Routes.MY) {
                ProfileScreen()
            }
            //하루요약
            composable(Routes.DAY_SUMMARY) {
                DaySummary(nav)
            }
        }
    }
}

@Composable
private fun HaruBottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    if (currentRoute != Routes.COGNITIVE_TEST){
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute in listOf(Routes.HOME, Routes.COGNITIVE, Routes.DIARY, Routes.LEARN),
                onClick = { onSelect(Routes.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
                label = { Text("홈") }
            )
            NavigationBarItem(
                selected = currentRoute == Routes.MY,
                onClick = { onSelect(Routes.MY) },
                icon = { Icon(Icons.Default.Person, contentDescription = "마이") },
                label = { Text("마이") }
            )
        }
    }
}

@Composable
private fun NavHostController.currentRoute(): String? {
    val backStackEntry by currentBackStackEntryAsState()
    return backStackEntry?.destination?.route
}
