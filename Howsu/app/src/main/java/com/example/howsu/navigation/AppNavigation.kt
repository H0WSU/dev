package com.example.howsu.navigation // (1. 새 패키지 이름)

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.howsu.screen.feed.FeedHomeScreen
import com.example.howsu.screen.feed.FeedViewModel
import com.example.howsu.screen.feed.FeedWriteScreen
import com.example.howsu.data.model.FamilyMember
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

    val member = FamilyMember(
        userId = "user123",
        relationship = "언니",              // 혹은 "엄마", "아빠" 같은 값
        profileImageUrl = "\"C:\\KakaoTalk_20251030_100121311.jpg\"",            // 일단 프로필 없다고 가정, 있으면 URL
        nickName = "이구역의짱"
    )

    val feedViewModel: FeedViewModel = viewModel()

    // 5. NavHost가 화면을 관리
    NavHost(
        navController = navController,
        startDestination = "feed" // ★ 앱 시작 시 보여줄 첫 화면
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

        composable(route = "feed") {
            FeedHomeScreen(navController = navController, viewModel = feedViewModel, member = member)
        }

        composable(route = "create_feed") {
            FeedWriteScreen(
                viewModel = feedViewModel,
                onFinishWrite = { navController.popBackStack() }
            )
        }

        // ★ 피드 수정 화면
        composable("edit_feed/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")?.toLongOrNull()
            val post = feedViewModel.posts.find { it.id == postId }

            if (post != null) {
                FeedWriteScreen(
                    viewModel = feedViewModel,
                    onFinishWrite = { navController.popBackStack() },
                    editPost = post
                )
            }
        }
    }
}