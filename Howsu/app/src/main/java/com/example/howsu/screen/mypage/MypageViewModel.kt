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

    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    // ★ [추가] 내 프로필 이미지 URL 상태
    private val _myProfileUrl = MutableStateFlow<String?>(null)
    val myProfileUrl = _myProfileUrl.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        loadMyFamilyInfo()
    }

    private fun loadMyFamilyInfo() {
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try {
                // 1. 내 정보 가져오기
                val userDoc = db.collection("users").document(user.uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                // ★ [추가] 내 프로필 사진 URL 가져오기
                val profileUrl = userDoc.getString("profileImageUrl")
                _myProfileUrl.value = profileUrl

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