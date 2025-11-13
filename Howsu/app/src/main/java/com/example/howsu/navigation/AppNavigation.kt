package com.example.howsu.navigation // (1. 새 패키지 이름)

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen // (2. JoinScreen 경로 Import)
import com.example.howsu.screen.login.LoginScreen // (2. LoginScreen 경로 Import)
import com.example.howsu.screen.schedule.CreateScheduleScreen
import com.example.howsu.screen.schedule.ScheduleScreen
import com.example.howsu.screen.todo.CreateTodoScreen
import com.example.howsu.screen.todo.TodoScreen

// (TODO: 3. 나중에 ScheduleScreen 등 다른 화면들도 Import)

@Composable
fun AppNavigation() {
    // 4. 내비게이션 컨트롤러 생성
    val navController = rememberNavController()

    // 5. NavHost가 화면을 관리
    NavHost(
        navController = navController,
        startDestination = "login" // ★ 앱 시작 시 보여줄 첫 화면 (원래는 login)
    ) {
        // "login"이라는 경로(주소)를 요청받으면
        composable(route = "login") {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel // ★ ViewModel 전달
            )
        }

        // "join"이라는 경로(주소)를 요청받으면
        composable(route = "join") {
            val authViewModel: AuthViewModel = viewModel()
            JoinScreen(
                navController = navController,
                authViewModel = authViewModel // 필요하면 전달
            )
        }

        // TODO: 나중에 "home" 또는 "schedule" 경로도 여기에 추가
        composable(route = "schedule") {
            ScheduleScreen(navController = navController)
        }

        composable(route = "create_schedule") {
            CreateScheduleScreen(navController = navController)
        }

        composable(route = "todo") {
            TodoScreen(navController = navController)
        }

        composable(route = "create_todo") {
            CreateTodoScreen(navController = navController)
        }

    }
}