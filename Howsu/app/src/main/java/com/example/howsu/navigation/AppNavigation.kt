package com.example.howsu.navigation // (1. 새 패키지 이름)

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.howsu.screen.home.HomeScreen
import com.example.howsu.screen.home.PetDetailScreen
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen
import com.example.howsu.screen.login.LoadingScreen
import com.example.howsu.screen.login.LoginScreen
import com.example.howsu.screen.schedule.CreateScheduleScreen
import com.example.howsu.screen.schedule.ScheduleDetailScreen
import com.example.howsu.screen.schedule.ScheduleScreen
import com.example.howsu.screen.todo.CreateTodoScreen
import com.example.howsu.screen.todo.TodoScreen
import com.example.howsu.screen.home.Pet


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

            // "일정 생성" 화면을 재사용
            // TODO: CreateScheduleScreen이 scheduleId를 받아서
            //       ViewModel에서 데이터를 로드하도록 수정해야 함
            CreateScheduleScreen(
                navController = navController
                // scheduleId = scheduleId // <- 나중에 이렇게 전달
            )
        }

        // ★ 펫 상세 정보 화면 추가 (petId를 인자로 받음)
        composable(
            route = "pet_detail/{petId}" // 경로 정의
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")

            // 현재는 PetDetailScreen이 Pet 객체를 직접 받으므로, 임시 Pet 객체를 전달함
            // 실제 앱에서는 petId를 이용해 ViewModel에서 Pet 객체를 가져옴

            // 임시 Pet 객체 생성 (나중에 petId를 이용한 데이터 로드 로직으로 대체)
            val dummyPet = Pet(
                name = "자몽", // 실제로는 ID를 통해 이름 로드
                age = 7,
                gender = "여아"
            )

            // PetDetailScreen 호출
            PetDetailScreen(
                navController = navController,
                pet = dummyPet // TODO: 실제 Pet 객체로 대체 필요
            )
        }
    }
}