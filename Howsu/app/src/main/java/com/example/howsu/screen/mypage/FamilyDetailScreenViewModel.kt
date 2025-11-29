package com.example.howsu.screen.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 화면에 보여줄 가족 구성원 정보
data class DisplayFamilyMember(
    val userId: String,
    val nickName: String,         // FamilyMember.nickName
    val relationship: String,     // FamilyMember.relationship
    val profileImageUrl: String?, // FamilyMember.profileImageUrl
)

class FamilyDetailScreenViewModel : ViewModel(){
    // 현재 로그인된 사용자 UID
    private val _currentUserId = MutableStateFlow("")
    val currentUserId = _currentUserId.asStateFlow()

    // 본인 닉네임, 관계 (본인 FamilyMember 정보)
    private val _nickName = MutableStateFlow("")
    val nickName = _nickName.asStateFlow()
    private val _familyRelationship = MutableStateFlow("")
    val familyRelationship = _familyRelationship.asStateFlow()

    // 가족 전체 정보 (FamilyId)
    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    //가족 이름 (FamilyName)
    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    // 본인 프로필 URL
    private val _familyProfileUrl = MutableStateFlow<String?>(null)
    val familyProfileUrl = _familyProfileUrl.asStateFlow()

    // 전체 가족 구성원 목록 (정렬 상태)
    private val _familyMembers = MutableStateFlow<List<DisplayFamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()


    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        // ⭐️ 현재 사용자 UID 초기화
        auth.currentUser?.let {
            _currentUserId.value = it.uid
        }
        loadMyFamilyInfo()
    }

    /*private fun loadMyFamilyInfo(){
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try{
                val uid = user.uid
                // 1. 현재 사용자 정보 로드 (currentFamilyId 획득)
                val userDoc = db.collection("user").document(uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                if(!myFamilyId.isNullOrBlank()){
                    _familyId.value = myFamilyId // familyId 설정

                    // 2. 현재 사용자의 FamilyMember 정보 로드 (본인 카드 정보)
                    val familyMemberQuery = db.collection("familyMembers")
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("familyId", myFamilyId)
                        .limit(1)
                        .get().await()

                    if(familyMemberQuery.documents.isNotEmpty()){
                        val memberDoc = familyMemberQuery.documents.first()
                        _nickName.value = memberDoc.getString("nickName") ?: "닉네임 설정 필요"
                        _familyRelationship.value = memberDoc.getString("relationship") ?: "관계 설정 필요"
                        _familyProfileUrl.value = memberDoc.getString("profileImageUrl")
                    } else {
                        Log.w("FamilyDetailScreenViewModel", "FamilyMember 정보가 없습니다.")
                        // 기본값 설정...
                    }

                    // 3. 전체 가족 구성원 정보 로드 및 정렬 호출
                    loadFamilyMembers(myFamilyId)

                }else{
                    // 가족이 없는 경우 처리
                    _nickName.value = "가족 없음"
                    _familyId.value = "-"
                    _familyRelationship.value = "가족 없음"
                    _familyProfileUrl.value = null
                    _familyMembers.value = emptyList()
                }
            } catch (e: Exception){
                Log.e("FamilyDetailScreenViewModel", "데이터 로드 실패", e)
            }
        }
    }*/
    private fun loadMyFamilyInfo(){
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try{
                val uid = user.uid
                // 1. 현재 사용자 정보 로드 (currentFamilyId 획득)
                val userDoc = db.collection("user").document(uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                if(!myFamilyId.isNullOrBlank()){
                    _familyId.value = myFamilyId // familyId 설정

                    // ⭐️ [추가] 1-1. Family 컬렉션에서 가족 이름 로드
                    loadFamilyName(myFamilyId)

                    // 2. 현재 사용자의 FamilyMember 정보 로드 (본인 카드 정보)
                    // ... (기존 코드와 동일) ...
                    val familyMemberQuery = db.collection("familyMembers")
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("familyId", myFamilyId)
                        .limit(1)
                        .get().await()

                    if(familyMemberQuery.documents.isNotEmpty()){
                        val memberDoc = familyMemberQuery.documents.first()
                        _nickName.value = memberDoc.getString("nickName") ?: "닉네임 설정 필요"
                        _familyRelationship.value = memberDoc.getString("relationship") ?: "관계 설정 필요"
                        _familyProfileUrl.value = memberDoc.getString("profileImageUrl")
                    } else {
                        Log.w("FamilyDetailScreenViewModel", "FamilyMember 정보가 없습니다.")
                    }

                    // 3. 전체 가족 구성원 정보 로드 및 정렬 호출
                    loadFamilyMembers(myFamilyId)

                }else{
                    // ... (가족이 없는 경우 처리 기존 코드와 동일) ...
                }
            } catch (e: Exception){
                Log.e("FamilyDetailScreenViewModel", "데이터 로드 실패", e)
            }
        }
    }

    // ⭐️ [추가] familyId로 familyName을 로드하는 함수
    private suspend fun loadFamilyName(familyId: String) {
        try {
            val familyDoc = db.collection("Family").document(familyId).get().await()
            val name = familyDoc.getString("familyName") // Family 컬렉션에 familyName 필드가 있다고 가정
            _familyName.value = name ?: "우리 가족"
        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 이름 로드 실패", e)
            _familyName.value = "우리 가족"
        }
    }
    private suspend fun loadFamilyMembers(familyId: String) {
        try {
            // familyMembers 컬렉션에서 familyId가 일치하는 모든 문서 조회
            val familyMembersSnapshot = db.collection("familyMembers")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()

            val displayMembers = mutableListOf<DisplayFamilyMember>()

            // User 컬렉션 조회 없이 FamilyMember 정보만 사용
            for (memberDoc in familyMembersSnapshot.documents) {
                val userId = memberDoc.getString("userId")
                val nickName = memberDoc.getString("nickName")
                val relationship = memberDoc.getString("relationship")
                val profileImageUrl = memberDoc.getString("profileImageUrl")

                if (userId != null && nickName != null && relationship != null) {
                    displayMembers.add(
                        DisplayFamilyMember(
                            userId = userId,
                            nickName = nickName,
                            relationship = relationship,
                            profileImageUrl = profileImageUrl
                        )
                    )
                }
            }

            // ⭐️ 현재 사용자를 리스트의 맨 앞으로 이동시켜 정렬
            val sortedMembers = displayMembers.sortedWith(
                compareByDescending { it.userId == _currentUserId.value }
            )

            _familyMembers.value = sortedMembers

        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 구성원 로드 실패", e)
            _familyMembers.value = emptyList()
        }
    }
}