package com.example.howsu.navigation // (1. 새 패키지 이름)

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen // (2. JoinScreen 경로 Import)
import com.example.howsu.screen.login.LoginScreen // (2. LoginScreen 경로 Import)
// (TODO: 3. 나중에 ScheduleScreen 등 다른 화면들도 Import)

@Composable
fun AppNavigation() {
    // 4. 내비게이션 컨트롤러 생성
    val navController = rememberNavController()

    // 5. NavHost가 화면을 관리합니다.
    NavHost(
        navController = navController,
        startDestination = "login" // ★ 앱 시작 시 보여줄 첫 화면
    ) {
        // "login"이라는 경로(주소)를 요청받으면...
        composable(route = "login") {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel // ★ ViewModel 전달
            )
        }

        // "join"이라는 경로(주소)를 요청받으면...
        composable(route = "join") {
            val authViewModel: AuthViewModel = viewModel()
            JoinScreen(
                navController = navController,
                authViewModel = authViewModel // 필요하면 전달
            )
        }

        // TODO: 나중에 "home" 또는 "schedule" 경로도 여기에 추가
        // composable(route = "home") {
        //     ScheduleScreen(navController = navController)
        // }
    }
}