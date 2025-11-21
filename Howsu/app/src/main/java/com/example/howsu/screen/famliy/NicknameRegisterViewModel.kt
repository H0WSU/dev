package com.example.howsu.screen.famliy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NicknameRegisterViewModel : ViewModel() {
    // 화면 데이터
    var nickname by mutableStateOf("")
    var profileImageUrl by mutableStateOf<String?>(null)

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 닉네임을 DB에 저장하는 함수
    fun saveNicknameToFirebase() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // users 컬렉션 -> 내 UID 문서에 -> name 필드 저장
                // merge()를 써야 기존 데이터(가족ID 등)가 있어도 안 지워지고 합쳐짐
                val userData = hashMapOf(
                    "name" to nickname,
                    // 필요하다면 프로필 이미지 URL도 여기서 저장 가능
                    // "profileImageUrl" to profileImageUrl
                )

                db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .await()

                println("닉네임 저장 성공: $nickname")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}