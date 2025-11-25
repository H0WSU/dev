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

    // 화면에 보여줄 상태
    private val _familyName = MutableStateFlow("") // 로딩 중일 땐 빈 문자열
    val familyName = _familyName.asStateFlow()

    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    // Firebase 인스턴스
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        loadMyFamilyInfo()
    }

    private fun loadMyFamilyInfo() {
        val user = auth.currentUser
        // 로그인이 안 되어 있으면 중단
        if (user == null) {
            Log.e("MypageViewModel", "로그인된 유저가 없습니다")
            return
        }

        viewModelScope.launch {
            try {
                // [1단계] 'users' 컬렉션에서 내 UID로 문서 조회 -> 'currentFamilyId' 가져오기
                val userDoc = db.collection("users").document(user.uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                if (!myFamilyId.isNullOrBlank()) {
                    // [2단계] 'families' 컬렉션에서 familyId로 문서 조회 -> 'familyName' 가져오기
                    val familyDoc = db.collection("families").document(myFamilyId).get().await()

                    if (familyDoc.exists()) {
                        val myFamilyName = familyDoc.getString("familyName") ?: "우리 가족"

                        // [3단계] 상태 업데이트 (UI에 반영됨)
                        _familyName.value = myFamilyName
                        _familyId.value = myFamilyId

                        Log.d("MypageViewModel", "가족 정보 로드 성공: $myFamilyName ($myFamilyId)")
                    }
                } else {
                    Log.d("MypageViewModel", "가입된 가족이 없습니다.")
                    _familyName.value = "가족 없음"
                    _familyId.value = "-"
                }

            } catch (e: Exception) {
                Log.e("MypageViewModel", "데이터 불러오기 실패", e)
                _familyName.value = "불러오기 실패"
            }
        }
    }
}