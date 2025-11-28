package com.example.howsu.screen.login

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LoadingScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

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
            val uid = currentUser.uid

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    // DB에서 닉네임과 가족 ID 확인
                    val name = document.getString("name")
                    val familyId = document.getString("currentFamilyId")

                    // 조건: 이름이 있거나, 가족 ID가 있으면 "기존 유저"로 판단
                    if (document.exists() && (!name.isNullOrBlank() || !familyId.isNullOrBlank())) {
                        authViewModel.updateFcmToken()

                        Log.d("LoadingScreen", "기존 유저입니다. 홈으로 이동")
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        // 신규 유저
                        Log.d("LoadingScreen", "신규 유저입니다. 등록 화면으로 이동")
                        navController.navigate("register_nickname") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LoadingScreen", "DB 조회 실패", e)
                    navController.navigate("auth_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }

        } else {
            // 로그인 안 됨 -> 로그인 화면으로
            navController.navigate("auth_graph") {
                popUpTo(0) { inclusive = true }
            }
        }
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            // 새로운 토큰 가져오기
            val token = task.result

            Log.d("FCM_TOKEN", "내 기기 토큰: $token")
        }
    }
}