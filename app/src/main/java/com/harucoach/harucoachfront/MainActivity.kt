package com.harucoach.harucoachfront

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harucoach.harucoachfront.data.PreferencesManager
import com.harucoach.harucoachfront.ui.screens.HomeScreen
import com.harucoach.harucoachfront.ui.screens.LoginScreen
import com.harucoach.harucoachfront.ui.theme.HaruCoachFrontTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint // 2. Hilt가 의존성을 주입할 진입점임을 선언
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 토큰 있으면 홈 시작
        val prefsManager = PreferencesManager(this)
        val token = prefsManager.readAuthTokenBlocking()
        val startDestination = if (token != null && token.isNotEmpty()) "home" else "login"

        setContent {
            HaruCoachFrontTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") { LoginScreen(navController) }  // 로그인 시작
                    composable("home") { HomeScreen() }  // 성공 화면
                }
            } // end HaruCoachFrontTheme
        }// end setContent
    }
}

