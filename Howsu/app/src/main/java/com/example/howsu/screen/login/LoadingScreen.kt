package com.example.howsu.screen.login

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LoadingScreen(navController: NavController) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    LaunchedEffect(key1 = Unit) {
        val currentUser = Firebase.auth.currentUser
        val db = Firebase.firestore

        if (currentUser != null) {
            // 로그인 되어 있음 -> DB에서 유저 정보 확인
            val uid = currentUser.uid

            // 'users' 컬렉션에서 현재 내 UID로 된 문서가 있는지, 닉네임이 있는지 확인
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    // 문서가 존재하고, "nickname" 필드가 비어 있지 않다면 -> 가입 완료된 유저
                    val nickname = document.getString("name") // DB 필드명 확인 필요 (nickName or nickname)

                    if (document.exists() && !nickname.isNullOrBlank()) {
                        // 1. 이미 정보 등록을 마친 유저 -> 홈으로
                        Log.d("LoadingScreen", "기존 유저입니다. 홈으로 이동")
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        // 2. 로그인은 됐지만 닉네임이 없음 -> 닉네임 등록 화면으로
                        Log.d("LoadingScreen", "신규 유저입니다. 등록 화면으로 이동")
                        navController.navigate("register_nickname") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // DB 조회 실패 (인터넷 문제 등) -> 안전하게 로그인 화면으로 보내거나 재시도 유도
                    Log.e("LoadingScreen", "DB 조회 실패", e)
                    navController.navigate("auth_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }

        } else {
            // ★ 로그인 안 되어 있음 -> 로그인 화면으로
            navController.navigate("auth_graph") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}