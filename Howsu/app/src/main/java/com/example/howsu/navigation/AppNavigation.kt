package com.example.howsu.navigation // (1. 새 패키지 이름)

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.howsu.Pet.PetRegisterViewModel
import com.example.howsu.screen.feed.FeedHomeScreen
import com.example.howsu.screen.feed.FeedViewModel
import com.example.howsu.screen.feed.FeedWriteScreen
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.screen.home.HomeScreen
import com.example.howsu.screen.home.PetDetailScreen
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen
import com.example.howsu.screen.login.LoadingScreen
import com.example.howsu.screen.login.LoginScreen
import com.example.howsu.screen.schedule.CreateScheduleScreen
import com.example.howsu.screen.schedule.ScheduleDetailScreen
import com.example.howsu.screen.schedule.ScheduleScreen
import com.example.howsu.screen.setting.SettingScreen
import com.example.howsu.screen.todo.CreateTodoScreen
import com.example.howsu.screen.todo.TodoScreen
import com.example.howsu.screen.home.Pet
import com.example.howsu.screen.pet.PetRegisterCompleteScreen
import com.example.howsu.screen.pet.PetRegisterScreen

// (TODO: 다른 화면들도 Import)

@SuppressLint("ViewModelConstructorInComposable")
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
    val petRegisterViewModel : PetRegisterViewModel = viewModel()

    // 5. NavHost가 화면을 관리
    NavHost(
        navController = navController,
        startDestination = "register_pet" // ★ 앱 시작 시 보여줄 첫 화면
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

        // 세팅 화면 추가 (테스트)
        composable(route = "profile") {
            SettingScreen(navController = navController)
        }

        // home화면 추가
        composable(route = "home") {
            HomeScreen(navController = navController)
        }

        // --- (스케줄) ---
        composable(route = "schedule") {
            ScheduleScreen(navController = navController)
        }
        composable(route = "create_schedule") {
            CreateScheduleScreen(navController = navController)
        }
        composable(route = "schedule_detail/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            ScheduleDetailScreen(navController = navController, scheduleId = scheduleId)
        }
        composable(route = "edit_schedule/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            CreateScheduleScreen(navController = navController, scheduleId = scheduleId)
        }

        // --- (투두) ---
        composable(route = "todo") {
            TodoScreen(navController = navController)
        }
        composable(route = "create_todo") {
            CreateTodoScreen(navController = navController)
        }

        composable(route = "edit_todo/{documentId}") { backStackEntry ->
            // 1. documentId를 가져옴
            val documentId = backStackEntry.arguments?.getString("documentId")

            // 2. CreateTodoScreen에 documentId 전달
            CreateTodoScreen(
                navController = navController,
                documentId = documentId
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

        //닉네임 등록 및 반려동물 등록 화면
        composable(route = "register_pet") {
            PetRegisterScreen(viewModel = petRegisterViewModel,navController = navController)
        }

        // 반려동물 등록 완료 화면
        composable("pet_register_complete") {
            val vm = PetRegisterViewModel() // 위와 같은 인스턴스를 공유해야 함
            val uiState by vm.uiState.collectAsState()

            PetRegisterCompleteScreen(
                uiState = uiState,
                onAddMore = {
                    vm.resetForNewPet()
                    navController.navigate("pet_register") {
                        popUpTo("pet_register_complete") { inclusive = true }
                    }
                },
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }

}