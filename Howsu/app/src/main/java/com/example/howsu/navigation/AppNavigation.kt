package com.example.howsu.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.howsu.Pet.PetRegisterViewModel // 합쳐진 import
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.screen.family.FamilyInviteScreen // 합쳐진 import
import com.example.howsu.screen.family.FamilyRegisterIntroScreen // 합쳐진 import
import com.example.howsu.screen.famliy.FamilyJoinCompleteScreen // 합쳐진 import
import com.example.howsu.screen.famliy.NicknameRegisterScreen // 합쳐진 import
import com.example.howsu.screen.feed.FeedHomeScreen
import com.example.howsu.screen.feed.FeedViewModel
import com.example.howsu.screen.feed.FeedWriteScreen
import com.example.howsu.screen.home.HomeScreen
import com.example.howsu.screen.home.Pet
import com.example.howsu.screen.home.PetDetailScreen
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.JoinScreen
import com.example.howsu.screen.login.LoadingScreen
import com.example.howsu.screen.login.LoginScreen
import com.example.howsu.screen.mypage.MypageScreen // HEAD 영역의 import
import com.example.howsu.screen.mypage.NotificationScreen // HEAD 영역의 import
import com.example.howsu.screen.pet.PetRegisterCompleteScreen // 합쳐진 import
import com.example.howsu.screen.pet.PetRegisterScreen // 합쳐진 import
import com.example.howsu.screen.schedule.CreateScheduleScreen
import com.example.howsu.screen.schedule.ScheduleDetailScreen
import com.example.howsu.screen.schedule.ScheduleScreen
import com.example.howsu.screen.setting.SettingScreen
import com.example.howsu.screen.todo.CreateTodoScreen
import com.example.howsu.screen.todo.TodoScreen
import java.net.URLEncoder // 합쳐진 import
import java.nio.charset.StandardCharsets // 합쳐진 import

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 임시 데이터
    val member = FamilyMember(
        userId = "user123",
        familyId = "test_family",
        relationship = "언니",
        profileImageUrl = null,
        nickName = "이구역의짱"
    )

    // ★ 상위 레벨에서 ViewModel 생성
    val feedViewModel: FeedViewModel = viewModel()
    val petRegisterViewModel : PetRegisterViewModel = viewModel()

    // 5. NavHost가 화면을 관리
    NavHost(
        navController = navController,
        startDestination = "register_pet" // 시작 경로 유지
    ) {
        composable(route = "loading") {
            LoadingScreen(navController = navController)
        }

        navigation(startDestination = "login", route = "auth_graph") {
            composable(route = "login") {
                val authViewModel: AuthViewModel = viewModel()
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(route = "join") {
                val authViewModel: AuthViewModel = viewModel()
                JoinScreen(navController = navController, authViewModel = authViewModel)
            }
        }

        composable(route = "profile") {
            SettingScreen(navController = navController)
        }

        // --- (닉네임/가족 등록 플로우) ---
        composable(route = "register_nickname") {
            NicknameRegisterScreen(
                navController = navController,
                onNicknameComplete = { nickname, profileUrl ->
                    val route = if (profileUrl != null) {
                        val encodedUrl = URLEncoder.encode(profileUrl, StandardCharsets.UTF_8.toString())
                        "family_register_intro/$nickname?profileUrl=$encodedUrl"
                    } else {
                        "family_register_intro/$nickname"
                    }
                    navController.navigate(route)
                }
            )
        }
        composable(
            route = "family_register_intro/{nickname}?profileUrl={profileUrl}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("profileUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "사용자"
            val finalProfileUrl = backStackEntry.arguments?.getString("profileUrl")
            FamilyRegisterIntroScreen(navController = navController, userNickname = nickname, userProfileUrl = finalProfileUrl)
        }
        composable(
            route = "family_invite_screen/{familyName}/{familyId}",
            arguments = listOf(
                navArgument("familyName") { type = NavType.StringType },
                navArgument("familyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val familyName = backStackEntry.arguments?.getString("familyName") ?: ""
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            FamilyInviteScreen(navController = navController, familyNameInput = familyName, invitedFamilyId = familyId)
        }
        composable(
            route = "family_join_complete/{familyName}?profileUrl={profileUrl}",
            arguments = listOf(
                navArgument("familyName") { type = NavType.StringType },
                navArgument("profileUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val familyName = backStackEntry.arguments?.getString("familyName") ?: "우리"
            val profileUrl = backStackEntry.arguments?.getString("profileUrl")
            FamilyJoinCompleteScreen(navController = navController, familyName = familyName, encodedProfileUrl = profileUrl)
        }

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
            val documentId = backStackEntry.arguments?.getString("documentId")
            CreateTodoScreen(navController = navController, documentId = documentId)
        }

        // 펫 상세
        composable(route = "pet_detail/{petId}") { backStackEntry ->
            val dummyPet = Pet(name = "자몽", age = 7, gender = "여아")
            PetDetailScreen(navController = navController, pet = dummyPet)
        }

        // --- (피드) ---
        composable(route = "feed") {
            FeedHomeScreen(navController = navController, viewModel = feedViewModel, member = member)
        }
        composable(route = "create_feed") {
            FeedWriteScreen(viewModel = feedViewModel, onFinishWrite = { navController.popBackStack() })
        }
        composable("edit_feed/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")?.toLongOrNull()
            val post = feedViewModel.posts.find { it.id == postId }
            if (post != null) {
                FeedWriteScreen(viewModel = feedViewModel, onFinishWrite = { navController.popBackStack() }, editPost = post)
            }
        }

        // --- (마이페이지) ---
        // HEAD 영역에서 가져온 마이페이지 경로
        composable("mypage") {
            MypageScreen(navController = navController)
        }

        // 공지사항 (NotificationScreen) 경로
        composable("notice") {
            NotificationScreen(navController = navController)
        }

        // --- (반려동물 등록 플로우) ---
        // a8bfc36b1b5c620bb088f4fdb705be523f9690d7 영역에서 가져온 경로들

        // 1. 정보 입력 화면
        composable(route = "register_pet") {
            PetRegisterScreen(viewModel = petRegisterViewModel, navController = navController)
        }

        // 2. 등록 완료 화면
        composable("pet_register_complete") {
            val uiState by petRegisterViewModel.uiState.collectAsState()
            val imagePickerLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        petRegisterViewModel.updatePetProfileImage(uri.toString())
                    }
                }

            PetRegisterCompleteScreen(
                uiState = uiState,
                onAddMore = {
                    petRegisterViewModel.resetForNewPet()
                    navController.navigate("register_pet") {
                        popUpTo("pet_register_complete") { inclusive = true }
                    }
                },
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("register_pet") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onPickImage = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }
    } // NavHost 끝
} // AppNavigation 끝