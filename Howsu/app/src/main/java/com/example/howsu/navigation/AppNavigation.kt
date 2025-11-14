package com.example.howsu.navigation // (1. 새 패키지 이름)

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.howsu.screen.home.HomeScreen
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen
import com.example.howsu.screen.login.LoadingScreen
import com.example.howsu.screen.login.LoginScreen
import com.example.howsu.screen.schedule.CreateScheduleScreen
import com.example.howsu.screen.schedule.ScheduleDetailScreen
import com.example.howsu.screen.schedule.ScheduleScreen
import com.example.howsu.screen.todo.CreateTodoScreen
import com.example.howsu.screen.todo.TodoScreen

// (TODO: 다른 화면들도 Import)

@Composable
fun AppNavigation() {
    // 4. 내비게이션 컨트롤러 생성
    val navController = rememberNavController()

    // 5. NavHost가 화면을 관리
    NavHost(
        navController = navController,
        startDestination = "loading" // ★ 앱 시작 시 보여줄 첫 화면
    ) {
        composable(route = "loading") {
            LoadingScreen(navController = navController)
        }

        navigation(startDestination = "login", route = "auth_graph") {

            // "login" Composable 안에 ViewModel 생성
            composable(route = "login") {
                val authViewModel: AuthViewModel = viewModel()
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            // "join" Composable 안에 ViewModel 생성
            composable(route = "join") {
                val authViewModel: AuthViewModel = viewModel()
                JoinScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }

        // home화면 추가
        composable(route = "home") {
            HomeScreen(navController = navController)
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

        composable(
            route = "schedule_detail/{scheduleId}"
        ) { backStackEntry ->
            // URL 경로에서 scheduleId를 꺼냅니다.
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            ScheduleDetailScreen(
                navController = navController,
                scheduleId = scheduleId
            )
        }

        // ★ 3. (신규) 일정 수정 화면
        composable(
            route = "edit_schedule/{scheduleId}"
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")

            CreateScheduleScreen(
                navController = navController,
                scheduleId = scheduleId // ★ 1. scheduleId를 여기에 전달
            )
        }
    }

}