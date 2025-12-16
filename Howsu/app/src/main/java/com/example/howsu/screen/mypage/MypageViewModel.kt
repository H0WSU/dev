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

    // --- 가족 정보 상태 ---
    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    // --- 사용자 정보 상태 ---
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    // 내 프로필 이미지 URL 상태
    private val _myProfileUrl = MutableStateFlow<String?>(null)
    val myProfileUrl = _myProfileUrl.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        // ViewModel 초기화 시 데이터 로드
        loadMyInfo()
    }
    fun loadMyInfo() {
        val user = auth.currentUser
        if (user == null) return

        // 1. Firebase Auth에서 이메일 정보 가져오기
        _userEmail.value = user.email ?: "이메일 없음"

        viewModelScope.launch {
            try {
                // 2. Firestore에서 내 정보 (이름, 프로필 URL, 가족 ID) 가져오기
                val userDoc = db.collection("users").document(user.uid).get().await()

                _userName.value = userDoc.getString("name") ?: "사용자"

                val profileUrl = userDoc.getString("profileImageUrl")
                _myProfileUrl.value = profileUrl

                val myFamilyId = userDoc.getString("currentFamilyId")

                // 3. 가족 ID가 있을 경우 가족 이름 가져오기
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