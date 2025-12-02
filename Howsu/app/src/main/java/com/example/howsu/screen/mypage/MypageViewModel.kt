package com.example.howsu.screen.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MypageViewModel : ViewModel() {

    // --- 가족 정보 ---
    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    // --- ⭐ 추가된 사용자 정보 상태 ⭐ ---
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()
    // ---

    // 내 프로필 이미지 URL 상태
    private val _myProfileUrl = MutableStateFlow<String?>(null)
    val myProfileUrl = _myProfileUrl.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        loadMyFamilyInfo() // 함수 이름은 그대로 유지하되, 내부에서 사용자 정보도 로드
    }

    private fun loadMyFamilyInfo() {
        val user = auth.currentUser
        if (user == null) return

        // ⭐ Auth에서 이메일 즉시 가져오기 ⭐
        _userEmail.value = user.email ?: "이메일 없음"

        viewModelScope.launch {
            try {
                // 1. 내 정보 가져오기 (Firestore)
                val userDoc = db.collection("users").document(user.uid).get().await()

                // ⭐ 사용자 이름 가져오기 ⭐
                _userName.value = userDoc.getString("name") ?: "사용자"

                // 내 프로필 사진 URL 가져오기
                val profileUrl = userDoc.getString("profileImageUrl")
                _myProfileUrl.value = profileUrl

                // 2. 가족 정보 가져오기
                val myFamilyId = userDoc.getString("currentFamilyId")

                if (!myFamilyId.isNullOrBlank()) {
                    val familyDoc = db.collection("families").document(myFamilyId).get().await()
                    if (familyDoc.exists()) {
                        _familyName.value = familyDoc.getString("familyName") ?: "우리 가족"
                        _familyId.value = myFamilyId
                    }
                } else {
                    _familyName.value = "가족 없음"
                    _familyId.value = "-"
                }
            } catch (e: Exception) {
                Log.e("MypageViewModel", "데이터 로드 실패", e)
            }
        }
    }
}