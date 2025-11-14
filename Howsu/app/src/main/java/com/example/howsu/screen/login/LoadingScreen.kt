package com.example.howsu.screen.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * 앱 시작 시 사용자의 로그인 상태를 확인하고 적절한 화면으로 분기하는 스크린
 */
@Composable
fun LoadingScreen(navController: NavController) {

    // 화면 중앙에 로딩 스피너 표시
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    // 이 Composable이 화면에 보일 때 '단 한 번' 실행
    LaunchedEffect(key1 = Unit) {
        // Firebase.auth.currentUser는 현재 캐시된(로그인된) 사용자를 즉시 반환
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            // ★ 이미 로그인됨
            // 메인 앱 화면 (원래라면 home으로)
            navController.navigate("schedule") {
                // LoadingScreen을 백스택에서 제거 (뒤로 가기 눌렀을 때 로딩 화면이 안 나오게)
                // 0은 NavHost의 가장 루트를 의미
                popUpTo(0) { inclusive = true }
            }
        } else {
            // ★ 로그인되지 않음
            // 로그인/회원가입 그래프("auth_graph")로 이동
            navController.navigate("auth_graph") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}